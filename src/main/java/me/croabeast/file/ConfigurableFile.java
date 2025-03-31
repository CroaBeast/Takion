package me.croabeast.file;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Represents a YAML file that supports configuration management by implementing the {@link Configurable} interface.
 * <p>
 * {@code ConfigurableFile} extends {@link YAMLFile} to provide a concrete implementation for interacting with YAML
 * configuration files. It enables loading, saving, and updating configuration settings, and exposes the underlying
 * {@link org.bukkit.configuration.file.FileConfiguration} for further manipulation.
 * </p>
 * <p>
 * This class is intended for use in Bukkit plugins where configuration files need to be managed dynamically.
 * It offers constructors that accept a loader object, an optional folder name, and a file name, and it throws an
 * {@link IOException} if an I/O error occurs during initialization.
 * </p>
 * <p>
 * Example usage:
 * <pre><code>
 * try {
 *     ConfigurableFile configFile = new ConfigurableFile(plugin, "config", "settings");
 *     // Set resource path if needed
 *     configFile.setResourcePath("config/settings.yml");
 *
 *     // Modify the configuration
 *     configFile.set("key", "value");
 *
 *     // Save changes
 *     configFile.save();
 * } catch (IOException e) {
 *     e.printStackTrace();
 * }
 * </code></pre>
 * </p>
 *
 * @see YAMLFile
 * @see Configurable
 */
public class ConfigurableFile extends YAMLFile implements Configurable {

    /**
     * Constructs a new {@code ConfigurableFile} with the specified loader, folder, and name.
     * <p>
     * The file is created in the given folder within the plugin's data directory, and the YAML configuration is loaded.
     * </p>
     *
     * @param loader The loader object used to retrieve the data folder and resources.
     * @param folder The folder where the file is located (may be {@code null}).
     * @param name   The name of the YAML file (without extension).
     * @param <T>    The type of the loader object.
     * @throws IOException if an I/O error occurs during file initialization.
     */
    public <T> ConfigurableFile(T loader, @Nullable String folder, String name) throws IOException {
        super(loader, folder, name);
    }

    /**
     * Constructs a new {@code ConfigurableFile} with the specified loader and name.
     * <p>
     * The file is created in the plugin's data folder, and the YAML configuration is loaded.
     * </p>
     *
     * @param loader The loader object used to retrieve the data folder and resources.
     * @param name   The name of the YAML file (without extension).
     * @param <T>    The type of the loader object.
     * @throws IOException if an I/O error occurs during file initialization.
     */
    public <T> ConfigurableFile(T loader, String name) throws IOException {
        super(loader, name);
    }

    /**
     * Sets the resource path for this configuration file.
     * <p>
     * The resource path is used to locate the default resource in the JAR to be saved to the file system.
     * </p>
     *
     * @param resourcePath the resource path to set.
     * @return this {@code ConfigurableFile} instance for method chaining.
     * @throws NullPointerException if {@code resourcePath} is blank.
     */
    @Override
    public ConfigurableFile setResourcePath(String resourcePath) throws NullPointerException {
        super.setResourcePath(resourcePath);
        return this;
    }

    /**
     * Sets whether this configuration file should be updatable.
     * <p>
     * When set to {@code true}, the file can be updated via the {@link #update()} method.
     * </p>
     *
     * @param updatable {@code true} if the file should be updatable; {@code false} otherwise.
     * @return this {@code ConfigurableFile} instance for method chaining.
     */
    @Override
    public ConfigurableFile setUpdatable(boolean updatable) {
        super.setUpdatable(updatable);
        return this;
    }
}
