package me.croabeast.file;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * YAMLFile is a utility class for managing YAML configuration files in Bukkit plugins.
 *
 * <p> It provides methods for loading, saving, and updating configuration files, as well
 * as handling resource paths and logging.
 */
@Accessors(chain = true)
public class YAMLFile {

    private final FileLoader loader;

    /**
     * The name of the YAML file.
     */
    @Getter
    private String name = "file-" + UUID.randomUUID().hashCode();
    /**
     * The folder in which the YAML file is located.
     */
    @Nullable @Getter
    private String folder;

    /**
     * The file location path.
     */
    @Getter
    private final String location;
    /**
     * The YAML file.
     */
    @NotNull @Getter
    private final File file;

    private String path;
    private FileConfiguration configuration;

    private YAMLUpdater updater;

    /**
     * Action for logging messages.
     */
    @Setter
    private Consumer<String> loggerAction;
    /**
     * Flag indicating whether the YAML file is updatable.
     */
    @Setter @Getter
    private boolean updatable = true;

    /**
     * Message displayed when loading the YAML file fails.
     */
    @Setter
    private String loadErrorMessage;
    /**
     * Message displayed when loading the YAML file succeeds.
     */
    @Setter
    private String loadSuccessMessage;
    /**
     * Message displayed when saving the YAML file fails.
     */
    @Setter
    private String saveErrorMessage;
    /**
     * Message displayed when saving the YAML file succeeds.
     */
    @Setter
    private String saveSuccessMessage;
    /**
     * Message displayed when updating the YAML file fails.
     */
    @Setter
    private String updateErrorMessage;
    /**
     * Message displayed when updating the YAML file succeeds.
     */
    @Setter
    private String updateSuccessMessage;

    /**
     * Constructs a YAMLFile with the specified loader, folder, and name.
     *
     * @param loader the object loader
     * @param folder the folder name (nullable)
     * @param name the file name
     *
     * @throws IOException if an I/O error occurs
     */
    public YAMLFile(Object loader, @Nullable String folder, String name) throws IOException {
        this.loader = new FileLoader(loader);

        if (StringUtils.isNotBlank(name))
            this.name = name;

        File dataFolder = this.loader.getDataFolder();
        String location = name + ".yml";

        if (StringUtils.isNotBlank(folder)) {
            this.folder = folder;

            File file = new File(dataFolder, folder);
            if (!file.exists()) file.mkdirs();

            location = folder + File.separator + location;
        }

        this.location = location;
        file = new File(dataFolder, location);

        try {
            setResourcePath(location);
        } catch (Exception ignored) {}

        try {
            this.updater = YAMLUpdater.of(loader, path, file);
        } catch (Exception ignored) {}

        loggerAction = System.out::println;

        loadErrorMessage = "File couldn't be loaded.";
        loadSuccessMessage = "&cFile " + getLocation() + " missing... &7Generating!";

        String msg = "&7The &e" + getLocation() + "&7 file ";

        loadErrorMessage = msg + "has been&a saved&7.";
        loadSuccessMessage = msg + "&ccouldn't be saved&7.";

        updateErrorMessage = msg + "has been&a updated&7.";
        updateSuccessMessage = msg + "&ccouldn't be updated&7.";
    }

    /**
     * Constructs a YAMLFile with the specified loader and name.
     *
     * @param loader the object loader
     * @param name the file name
     * @throws IOException if an I/O error occurs
     */
    public YAMLFile(Object loader, String name) throws IOException {
        this(loader, null, name);
    }

    private void loadUpdaterToData(boolean debug) {
        try {
            this.updater = YAMLUpdater.of(loader.loader, path, getFile());
        } catch (Exception e) {
            if (debug) e.printStackTrace();
        }
    }

    /**
     * Sets the resource path for this YAMLFile.
     *
     * @param path the resource path
     * @param debug if true, enables debug logging
     * @return this YAMLFile instance
     */
    public YAMLFile setResourcePath(String path, boolean debug) {
        if (StringUtils.isBlank(path))
            throw new NullPointerException();

        this.path = path.replace('\\', '/');

        loadUpdaterToData(debug);
        return this;
    }

    /**
     * Sets the resource path for this YAMLFile.
     *
     * @param path the resource path
     * @return this YAMLFile instance
     */
    public YAMLFile setResourcePath(String path) {
        return setResourcePath(path, false);
    }

    /**
     * Gets the input stream of the resource file.
     * @return the input stream of the resource
     */
    public InputStream getResource() {
        return loader.getResource(path);
    }

    /**
     * Reloads the YAML configuration from the file.
     * @return the reloaded FileConfiguration
     */
    @SneakyThrows
    public FileConfiguration reload() {
        YamlConfiguration c = new YamlConfiguration();
        try {
            c.load(getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return configuration = c;
    }

    private void log(String line, boolean debug) {
        if (debug && loggerAction != null)
            loggerAction.accept(line);
    }

    private void log(String line, Exception e, boolean debug) {
        if (!debug) return;

        if (loggerAction != null) loggerAction.accept(line);
        e.printStackTrace();
    }

    /**
     * Saves the default configuration if the file does not exist.
     *
     * @param debug if true, enables debug logging
     * @return true if the defaults were saved, false otherwise
     */
    public boolean saveDefaults(boolean debug) {
        if (getFile().exists()) return true;

        try {
            ResourceUtils.saveResource(getResource(), loader.getDataFolder(), getLocation());
        } catch (Exception e) {
            log(loadErrorMessage, e, debug);
            return false;
        }

        log(loadSuccessMessage, debug);
        reload();
        return true;
    }

    /**
     * Saves the default configuration if the file does not exist.
     * @return true if the defaults were saved, false otherwise
     */
    public boolean saveDefaults() {
        return saveDefaults(false);
    }

    /**
     * Gets the YAML configuration.
     *
     * @return the FileConfiguration
     */
    @NotNull
    public FileConfiguration getConfiguration() {
        return configuration == null ? reload() : configuration;
    }

    /**
     * Saves the YAML configuration to the file.
     *
     * @param debug if true, enables debug logging
     * @return true if the configuration was saved successfully, false otherwise
     */
    public boolean save(boolean debug) {
        try {
            getConfiguration().save(getFile());

            log(saveSuccessMessage, debug);
            return true;
        }
        catch (Exception e) {
            log(saveErrorMessage, e, debug);
            return false;
        }
    }

    /**
     * Saves the YAML configuration to the file.
     * @return true if the configuration was saved successfully, false otherwise
     */
    public boolean save() {
        return save(false);
    }

    /**
     * Updates the YAML configuration.
     *
     * @param debug if true, enables debug logging
     * @return true if the configuration was updated successfully, false otherwise
     */
    public boolean update(boolean debug) {
        if (!isUpdatable()) return false;

        try {
            if (updater == null) loadUpdaterToData(debug);
            updater.update();

            log(updateSuccessMessage, debug);
            return true;
        }
        catch (Exception e) {
            log(updateErrorMessage, e, debug);
            return false;
        }
    }

    /**
     * Updates the YAML configuration.
     * @return true if the configuration was updated successfully, false otherwise
     */
    public boolean update() {
        return update(false);
    }

    /**
     * Returns a string representation of the YAMLFile.
     * @return a string representation of the YAMLFile
     */
    @Override
    public String toString() {
        return "YAMLFile{folder='" + getFolder() + "', name='" + getName() + "'}";
    }

    /**
     * Computes a hash code for this YAMLFile.
     * @return a hash code value for this YAMLFile
     */
    @Override
    public int hashCode() {
        return Objects.hash(getFolder(), getName());
    }

    /**
     * Compares this YAMLFile to another YAMLFile based on folder and name.
     *
     * @param folder the folder name to compare
     * @param name the file name to compare
     *
     * @return true if the folder and name are equal, false otherwise
     */
    public boolean equals(String folder, String name) {
        return Objects.equals(this.getFolder(), folder) && Objects.equals(this.getName(), name);
    }

    /**
     * Compares this YAMLFile to another object.
     *
     * @param o the object to compare
     * @return true if the object is a YAMLFile with the same folder and name, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;

        if (this == o) return true;
        if (getClass() != o.getClass()) return false;

        YAMLFile f = (YAMLFile) o;
        return equals(f.getFolder(), f.getName());
    }
}
