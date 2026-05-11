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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
 * It supports downloading files from remote repositories (such as Maven or a custom repository) and loading them into the
 * plugin’s classpath by using low-level reflection (via {@link Unsafe} and {@link Reflector}) to modify the internal
 * structures of the class loader.
 * </p>
 * <p>
 * There are two main modes for storing the downloaded libraries:
 * <ul>
 *   <li><b>Complex structure</b>: Uses a Maven-like directory layout (e.g., <code>/libraries/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar</code>).</li>
 *   <li><b>Flat structure</b>: Places the JAR files directly inside the libraries folder (e.g., <code>/libraries/gson-2.8.9.jar</code>).</li>
 * </ul>
 * The mode is determined by the {@link #complexStructure} flag.
 * </p>
 * <p>
 * Preconfigured instances:
 * <ul>
 *   <li>{@code BUKKIT_LOADER} uses Bukkit's world container with a subfolder named "libraries" and locks its structure setting.</li>
 *   <li>Custom loaders can be created via {@link #fromFolder(File, String)} or {@link #fromFolder(File)}.</li>
 * </ul>
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * // In your plugin's onLoad or onEnable method:
 * DependencyLoader loader = DependencyLoader.BUKKIT_LOADER;
 * loader.load("com.example", "my-library", "${version}", DependencyLoader.MAVEN_REPO_URLS[0], false);
 *
 * // Alternatively, load dependencies from a YAML configuration file:
 * loader.loadFromYAML(new File(getDataFolder(), "dependencies.yml"));
 * }</pre>
 * </p>
 *
 * Warning: This class doesn't work if you're using paper-plugin.yml to load the plugin.
 * It requires the plugin to be loaded via the standard Bukkit/Spigot/Paper loading mechanism.
 *
 * @see YAMLFile
 * @see YamlConfiguration
 */
public class DependencyLoader {

    /**
     * A preconfigured {@code DependencyLoader} that uses Bukkit's world container and a subfolder named "libraries".
     * <p>
     * Note: The folder structure for this loader is fixed; its {@link #complexStructure} flag cannot be changed.
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
     * The base folder where external dependency JAR files are stored.
     */
    private final File librariesFolder;

    /**
     * Flag that determines which folder structure is used when saving dependencies.
     * <p>
     * If {@code true}, a complex (Maven-like) directory structure is used; if {@code false}, a flat structure is applied.
     * </p>
     */
    @Setter
    private boolean complexStructure = true;

    /**
     * Constructs a new {@code DependencyLoader} with the specified base folder and subfolder name.
     *
     * @param folder  the base folder where dependencies will be stored.
     * @param newName the name of the subfolder to use; if blank, the base folder is used directly.
     */
    private DependencyLoader(File folder, String newName) {
        this.librariesFolder = StringUtils.isBlank(newName) ? folder : new File(folder, newName);
    }

    /**
     * Logs a message using Bukkit's logger at the specified log level.
     *
     * @param log     the {@code Log} enum value representing the log level.
     * @param message the message to log.
     */
    private void log(Log log, String message) {
        Bukkit.getLogger().log(log.level, "[DependencyLoader] " + message);
    }

    /**
     * Attempts to download a file from the specified URL and save it to the destination file.
     *
     * @param urlString the URL of the file to download.
     * @param destiny   the destination file where the downloaded file will be saved.
     * @return {@code true} if the download succeeds; {@code false} otherwise.
     * @throws IOException if an I/O error occurs during download.
     */
    private boolean downloadFile(String urlString, File destiny) throws IOException {
        URL url = new URL(urlString);

        // Perform a HEAD request to check if the URL is reachable.
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                log(Log.ERROR, "URL not reachable: " + urlString);
                return false;
            }
        } catch (Exception e) {
            log(Log.ERROR, "URL not reachable: " + urlString);
            e.printStackTrace();
            return false;
        }

        // Download the file using a buffered stream.
        try (FileOutputStream out = new FileOutputStream(destiny);
             InputStream in = url.openStream()) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            return true;
        }
    }

    /**
     * Downloads and loads a dependency JAR file into the plugin's runtime classpath.
     * <p>
     * The method constructs the dependency path and file name from the group, artifact, and version.
     * It downloads the JAR if it does not exist or if {@code replace} is {@code true}, and then loads
     * it dynamically via reflection.
     * </p>
     *
     * @param group    the dependency group ID.
     * @param artifact the dependency artifact ID.
     * @param version  the dependency version (this can be parameterized via release tags).
     * @param repoUrl  the base URL of the repository to download the dependency from; if {@code null}, the first URL from {@link #MAVEN_REPO_URLS} is used.
     * @param replace  if {@code true}, the dependency will be re-downloaded even if it exists.
     * @return {@code true} if the dependency was successfully loaded; {@code false} otherwise.
     */
    public final boolean load(String group, String artifact, String version, String repoUrl, boolean replace) {
        repoUrl = repoUrl != null ? repoUrl : MAVEN_REPO_URLS[0];

        try {
            // Build the dependency path using file separators for cross-platform compatibility.
            String path = group.replace('.', File.separatorChar) + File.separatorChar
                    + artifact + File.separatorChar + version;
            String jarName = artifact + "-" + version + ".jar";

            // Determine the target file location based on the complexStructure flag.
            File jarFile = new File(librariesFolder,
                    (complexStructure ? (path + File.separatorChar) : "") + jarName);
            if (!jarFile.exists() || replace) {
                if (jarFile.exists()) jarFile.delete();

                log(Log.GOOD, "Downloading: " + jarName);
                jarFile.getParentFile().mkdirs();

                StringBuilder builder = new StringBuilder(repoUrl);
                if (!repoUrl.endsWith("/")) builder.append('/');

                builder.append(path.replace(File.separatorChar, '/'))
                        .append('/')
                        .append(jarName);

                if (!downloadFile(builder.toString(), jarFile))
                    return false;

                if (jarFile.length() == 0) {
                    log(Log.ERROR, "Download failed or file is empty: " + jarName);
                    return false;
                }
            }

            // Load the JAR into the classpath using low-level reflection.
            Utils.load0(jarFile);
            log(Log.GOOD, "Loaded: " + jarName);
            return true;
        } catch (Exception e) {
            log(Log.ERROR, "Error loading dependency: " + artifact + " v" + version);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Overloaded variant of {@link DependencyLoader#load(String, String, String, String, boolean) the main load method} that uses the default repository URL.
     *
     * @param group    the dependency group ID.
     * @param artifact the dependency artifact ID.
     * @param version  the dependency version.
     * @return {@code true} if the dependency was successfully loaded; {@code false} otherwise.
     */
    public final boolean load(String group, String artifact, String version, boolean replace) {
        return load(group, artifact, version, null, replace);
    }

    /**
     * Overloaded variant of {@link DependencyLoader#load(String, String, String, String, boolean) the main load method} that does not replace existing files and uses the default repository URL.
     *
     * @param group    the dependency group ID.
     * @param artifact the dependency artifact ID.
     * @param version  the dependency version.
     * @return {@code true} if the dependency was successfully loaded; {@code false} otherwise.
     */
    public final boolean load(String group, String artifact, String version, String repoUrl) {
        return load(group, artifact, version, repoUrl, false);
    }

    /**
     * Overloaded variant of {@link DependencyLoader#load(String, String, String, String, boolean) the main load method} that does not replace existing files and uses the default repository URL.
     *
     * @param group    the dependency group ID.
     * @param artifact the dependency artifact ID.
     * @param version  the dependency version.
     * @return {@code true} if the dependency was successfully loaded; {@code false} otherwise.
     */
    public final boolean load(String group, String artifact, String version) {
        return load(group, artifact, version, false);
    }

    /**
     * Loads dependencies from a given {@link FileConfiguration}.
     * <p>
     * The configuration should contain a list of dependency maps under the key {@code dependencies}.
     * </p>
     *
     * @param c the {@code FileConfiguration} containing dependency definitions.
     * @return {@code true} if at least one dependency was loaded successfully; {@code false} otherwise.
     */
    public boolean loadFromConfiguration(FileConfiguration c) {
        List<Map<?, ?>> dependencies = c.getMapList("dependencies");
        boolean loadAtLeastOne = false;

        for (Map<?, ?> map : dependencies) {
            String group = (String) map.get("group");
            String artifact = (String) map.get("artifact");
            String version = (String) map.get("version");

            if (group == null || artifact == null || version == null) {
                log(Log.BAD, "Invalid dependency: " + map);
                continue;
            }

            Boolean replace = (Boolean) map.get("replace");
            loadAtLeastOne = load(
                    group, artifact, version, (String) map.get("repo"),
                    replace != null && replace
            );
        }

        return loadAtLeastOne;
    }

    /**
     * Loads dependencies specified in a YAML file.
     * <p>
     * The YAML file must have a <code>.yml</code> extension and contain dependency definitions.
     * </p>
     *
     * @param file the YAML file containing dependency definitions.
     * @return {@code true} if dependencies were loaded successfully; {@code false} otherwise.
     */
    public boolean loadFromFile(File file) {
        if (!file.getAbsolutePath().endsWith(".yml")) {
            log(Log.BAD, file + " isn't a valid .yml file.");
            return false;
        }

        if (!file.exists()) {
            log(Log.BAD, file + " doesn't exist.");
            return false;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        return loadFromConfiguration(config);
    }

    /**
     * Loads dependencies specified in a {@link YAMLFile} instance.
     *
     * @param file a {@code YAMLFile} containing dependency definitions.
     * @return {@code true} if dependencies were loaded successfully; {@code false} otherwise.
     */
    public boolean loadFromYAML(YAMLFile file) {
        return loadFromConfiguration(file.getConfiguration());
    }

    /**
     * Creates a new {@code DependencyLoader} instance for the specified folder and subfolder.
     *
     * @param librariesFolder the base folder containing dependency libraries.
     * @param folderName      the subfolder name to use (maybe {@code null}).
     * @return a new {@code DependencyLoader} instance.
     */
    public static DependencyLoader fromFolder(File librariesFolder, String folderName) {
        return new DependencyLoader(librariesFolder, folderName);
    }

    /**
     * Creates a new {@code DependencyLoader} instance for the specified folder without a subfolder.
     *
     * @param librariesFolder the base folder containing dependency libraries.
     * @return a new {@code DependencyLoader} instance.
     */
    public static DependencyLoader fromFolder(File librariesFolder) {
        return fromFolder(librariesFolder, null);
    }

    /**
     * Internal logging levels used by {@code DependencyLoader}.
     */
    private enum Log {
        /**
         * Represents a successful or positive log message.
         */
        GOOD(Level.INFO),
        /**
         * Represents a warning or negative log message.
         */
        BAD(Level.WARNING),
        /**
         * Represents an error log message.
         */
        ERROR(Level.SEVERE);

        private final Level level;

        Log(Level level) {
            this.level = level;
        }
    }

    /**
     * A private utility class that provides low-level methods to load JAR files into the plugin's classpath.
     * <p>
     * The {@code Utils} class uses {@link Unsafe} and reflection to access internal fields of the current class loader,
     * such as the "ucp" field and its URL collections, to add the provided JAR file dynamically.
     * </p>
     */
    @UtilityClass
    private static class Utils {

        /**
         * An instance of {@link Unsafe} obtained via reflection, used for low-level memory operations.
         */
        private final Unsafe THE_UNSAFE = Reflector.of(Unsafe.class).get("theUnsafe");

        /**
         * Recursively searches for a declared field with the given name in the class hierarchy.
         *
         * @param clazz the class to search in.
         * @param field the name of the field to find.
         * @return the {@link Field} if found; {@code null} otherwise.
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
         * specifically the "ucp" field and its collections, to add the URL of the JAR file dynamically.
         * </p>
         *
         * @param file the JAR file to load.
         * @throws Exception if the JAR cannot be loaded into the classpath.
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
