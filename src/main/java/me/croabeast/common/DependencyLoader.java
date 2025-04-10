package me.croabeast.common;

import lombok.Setter;
import lombok.experimental.UtilityClass;
import me.croabeast.common.reflect.Reflector;
import me.croabeast.file.YAMLFile;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * The {@code DependencyLoader} class provides functionality to dynamically download and load external
 * JAR dependencies at runtime for Bukkit/Spigot/Paper plugins.
 * <p>
 * It supports downloading files from remote repositories (such as Maven or a custom repository) and
 * loading them into the plugin’s classpath using low-level reflection (via {@link Unsafe} and a custom
 * {@link Reflector}) to modify the internal structures of the class loader.
 * </p>
 * <p>
 * There are two main modes for storing the downloaded libraries:
 * <ul>
 *   <li><b>Complex structure</b>: Uses a Maven-like directory layout (e.g.,
 *       {@code /libraries/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar}).</li>
 *   <li><b>Flat structure</b>: Places the JAR files directly inside the libraries folder
 *       (e.g., {@code /libraries/gson-2.8.9.jar}).</li>
 * </ul>
 * The behavior is controlled by the {@code complexStructure} flag.
 * </p>
 * <p>
 * Two preconfigured instances are provided:
 * <ul>
 *   <li>{@code BUKKIT_LOADER} uses Bukkit's world container with a subfolder named "libraries" and has its structure
 *       setting locked.</li>
 *   <li>You may create custom loaders using the factory methods {@link #fromFolder(File, String)}
 *       or {@link #fromFolder(File)}.</li>
 * </ul>
 * </p>
 * <p>
 * Example usage:
 * <pre><code>
 * // Initialize the dependency loader (typically in your plugin's onLoad):
 * DependencyLoader loader = DependencyLoader.BUKKIT_LOADER;
 *
 * // Load a dependency from a custom repository URL:
 * loader.load("com.example", "my-library", "1.0.0", DependencyLoader.MAVEN_REPO_URLS[0]);
 *
 * // Optionally, load dependencies from a YAML configuration file:
 * loader.loadFromYAML(new File(getDataFolder(), "libraries.yml"));
 * </code></pre>
 * </p>
 */
public class DependencyLoader {

    /**
     * A preconfigured {@code DependencyLoader} that uses Bukkit's world container and a folder named "libraries".
     * <p>
     * Note: The structure of this loader is fixed, so its {@code complexStructure} flag cannot be changed.
     * </p>
     */
    public static final DependencyLoader BUKKIT_LOADER =
            new DependencyLoader(Bukkit.getWorldContainer(), "libraries") {
                @Override
                public void setComplexStructure(boolean complex) {
                    throw new IllegalStateException("Structure can't be changed.");
                }
            };

    /**
     * An array of default Maven repository URLs.
     */
    public static final String[] MAVEN_REPO_URLS = {
            "https://repo1.maven.org/maven2/",
            "https://repo.maven.apache.org/maven2/"
    };

    /**
     * The folder where external dependency JAR files are stored.
     */
    private final File librariesFolder;

    /**
     * Flag to determine the folder structure to use when saving dependencies.
     * <p>
     * If {@code true}, a complex (Maven-like) directory structure is used. If {@code false}, a flat structure
     * is used (all JARs placed directly in the base libraries folder).
     * </p>
     */
    @Setter
    private boolean complexStructure = true;

    /**
     * Constructs a new {@code DependencyLoader} with the specified base folder and subfolder.
     *
     * @param folder  the base folder where dependencies will be stored
     * @param newName the name of the subfolder to use; if blank, the base folder is used directly
     */
    private DependencyLoader(File folder, String newName) {
        this.librariesFolder = StringUtils.isBlank(newName) ? folder : new File(folder, newName);
    }

    // Internal logging enum for different log levels.
    private enum Log {
        GOOD(Level.INFO),
        BAD(Level.WARNING),
        ERROR(Level.SEVERE);

        private final Level level;

        Log(Level level) {
            this.level = level;
        }
    }

    /**
     * Logs a message to Bukkit's logger with the specified log level.
     *
     * @param log     the log level (GOOD, BAD, or ERROR)
     * @param message the message to log
     */
    private void log(Log log, String message) {
        Bukkit.getLogger().log(log.level, "[DependencyLoader] " + message);
    }

    /**
     * Downloads a file from the specified URL and saves it to the given destination file.
     *
     * @param urlString the URL of the file to download
     * @param destiny   the destination file where the downloaded file will be saved
     * @throws IOException if an I/O error occurs during download
     */
    private void downloadFile(String urlString, File destiny) throws IOException {
        URL url = new URL(urlString);

        // First, perform a HEAD request to ensure the URL is reachable.
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                log(Log.ERROR, "URL not reachable: " + urlString);
        }
        catch (Exception e) {
            log(Log.ERROR, "URL not reachable: " + urlString);
            e.printStackTrace();
        }

        // Download the file using a buffered stream.
        try (
                FileOutputStream out = new FileOutputStream(destiny);
                InputStream in = url.openStream()
        ) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }

    /**
     * Downloads and loads a dependency JAR file into the plugin’s classpath.
     * <p>
     * This method constructs the URL for the dependency using its group, artifact, and version.
     * It downloads the JAR if it does not already exist in the libraries folder (using either a complex or flat
     * directory structure based on the {@code complexStructure} flag), and then loads it into the runtime classpath
     * by using low-level reflection.
     * </p>
     *
     * @param group    the dependency group ID
     * @param artifact the dependency artifact ID
     * @param version  the version of the dependency (can include variable version tags)
     * @param repoUrl  the base URL of the repository from which to download the dependency
     */
    public final void load(String group, String artifact, String version, String repoUrl) {
        try {
            // Build path using File.separator for cross-platform compatibility
            String path = group.replace('.', File.separatorChar) +
                    File.separatorChar + artifact +
                    File.separatorChar + version;
            String jarName = artifact + "-" + version + ".jar";

            // Determine the target file location based on the structure flag.
            File jarFile = new File(librariesFolder,
                    (complexStructure ? (path + File.separatorChar) : "") + jarName);
            if (!jarFile.exists()) {
                log(Log.GOOD, "Downloading: " + jarName);
                jarFile.getParentFile().mkdirs();

                // Construct the full URL ensuring correct use of separators
                StringBuilder builder = new StringBuilder(repoUrl);

                if (!repoUrl.endsWith("/")) builder.append('/');
                builder.append(path.replace(File.separatorChar, '/'))
                        .append('/')
                        .append(jarName);

                downloadFile(builder.toString(), jarFile);

                if (jarFile.length() == 0) {
                    log(Log.ERROR, "Download failed or file is empty: " + jarName);
                    return;
                }
            }

            // Load the JAR into the classpath using low-level reflection.
            Utils.load0(jarFile);
            log(Log.GOOD, "Loaded: " + jarName);
        } catch (Exception e) {
            log(Log.ERROR, "Error loading dependency: " + artifact + " v" + version);
            e.printStackTrace();
        }
    }

    /**
     * Loads dependencies from the given {@link FileConfiguration}.
     * <p>
     * The configuration must contain a list of dependency maps under the key {@code dependencies}. Each dependency
     * map should define the keys {@code group}, {@code artifact}, {@code version}, and optionally {@code repo} (if not,
     * the first Maven repository URL is used).
     * </p>
     *
     * @param c the {@code FileConfiguration} containing the dependency definitions
     */
    private void loadFromConfiguration(FileConfiguration c) {
        List<Map<?, ?>> dependencies = c.getMapList("dependencies");

        for (Map<?, ?> map : dependencies) {
            String group = (String) map.get("group");
            String artifact = (String) map.get("artifact");
            String version = (String) map.get("version");

            if (group == null || artifact == null || version == null) {
                log(Log.BAD, "Invalid dependency: " + map);
                continue;
            }

            String repo = (String) map.get("repo");
            if (repo == null) repo = MAVEN_REPO_URLS[0];

            load(group, artifact, version, repo);
        }
    }

    /**
     * Loads dependencies specified in a YAML file.
     * <p>
     * The YAML file must have an extension of {@code .yml} and include the dependency configuration.
     * </p>
     *
     * @param file the YAML file containing dependency definitions
     */
    public void loadFromYAML(File file) {
        if (!file.getAbsolutePath().endsWith(".yml")) {
            log(Log.BAD, file + " isn't a valid .yml file.");
            return;
        }

        if (!file.exists()) {
            log(Log.BAD, file + " doesn't exist.");
            return;
        }

        loadFromConfiguration(YamlConfiguration.loadConfiguration(file));
    }

    /**
     * Loads dependencies specified by the given {@link YAMLFile}.
     *
     * @param file a {@code YAMLFile} instance containing dependency definitions
     */
    public void loadFromYAML(YAMLFile file) {
        loadFromConfiguration(file.getConfiguration());
    }

    /**
     * Creates a new {@code DependencyLoader} instance for the specified folder and subfolder.
     *
     * @param librariesFolder the base folder containing dependency libraries
     * @param folderName      the subfolder name to use (may be {@code null})
     * @return a new {@code DependencyLoader} instance
     */
    public static DependencyLoader fromFolder(File librariesFolder, String folderName) {
        return new DependencyLoader(librariesFolder, folderName);
    }

    /**
     * Creates a new {@code DependencyLoader} instance for the specified folder without a subfolder.
     *
     * @param librariesFolder the base folder containing dependency libraries
     * @return a new {@code DependencyLoader} instance
     */
    public static DependencyLoader fromFolder(File librariesFolder) {
        return fromFolder(librariesFolder, null);
    }

    /**
     * A private utility class that provides low-level methods to load JAR files into the plugin's classpath.
     * <p>
     * The {@code Utils} class uses {@link Unsafe} to access and modify the internal structures of the URLClassLoader,
     * effectively adding the provided JAR file to the runtime classpath.
     * </p>
     */
    @UtilityClass
    private static class Utils {

        /**
         * The {@link Unsafe} instance used to perform low-level memory operations.
         */
        private final Unsafe THE_UNSAFE = Reflector.of(Unsafe.class).get("theUnsafe");

        /**
         * Recursively searches for a declared field with the given name in the class hierarchy.
         *
         * @param clazz the class to search in
         * @param field the name of the field to find
         * @return the {@link Field} if found; {@code null} otherwise
         */
        Field findField(Class<?> clazz, String field) {
            try {
                return clazz.getDeclaredField(field);
            } catch (Exception e) {
                Class<?> s = clazz.getSuperclass();
                return s == null ? null : findField(s, field);
            }
        }

        /**
         * Loads the specified JAR file into the plugin’s classpath.
         * <p>
         * This method uses {@link Unsafe} and reflection to access internal fields of the current class loader,
         * namely the "ucp" field and its collections, to add the URL of the JAR file dynamically.
         * </p>
         *
         * @param file the JAR file to load
         * @throws Exception if the JAR cannot be loaded into the classpath
         */
        @SuppressWarnings("unchecked")
        void load0(File file) throws Exception {
            final ClassLoader mainLoader = DependencyLoader.class.getClassLoader();

            Field field = findField(mainLoader.getClass(), "ucp");
            if (field == null)
                throw new IllegalStateException("Couldn't find URLClassLoader field 'ucp'");

            long offset = THE_UNSAFE.objectFieldOffset(field);
            Object ucp = THE_UNSAFE.getObject(mainLoader, offset);

            field = ucp.getClass().getDeclaredField("path");
            offset = THE_UNSAFE.objectFieldOffset(field);
            Collection<URL> paths = (Collection<URL>) THE_UNSAFE.getObject(ucp, offset);

            try {
                field = ucp.getClass().getDeclaredField("unopenedUrls");
            } catch (NoSuchFieldException e) {
                field = ucp.getClass().getDeclaredField("urls");
            }

            offset = THE_UNSAFE.objectFieldOffset(field);
            Collection<URL> urls = (Collection<URL>) THE_UNSAFE.getObject(ucp, offset);

            URL url = file.toURI().toURL();
            if (paths.contains(url)) return;

            paths.add(url);
            urls.add(url);
        }
    }
}
