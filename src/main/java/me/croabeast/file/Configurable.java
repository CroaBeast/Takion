package me.croabeast.file;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Represents an object that can manage and manipulate a {@link FileConfiguration}.
 * <p>
 * The {@code Configurable} interface provides a set of utility methods for retrieving,
 * modifying, and navigating configuration data stored in a YAML file.
 * It simplifies access to configuration values, subsections, and lists,
 * and offers methods to convert configuration sections into mappable data structures.
 * </p>
 * <p>
 * <strong>Note:</strong> Concrete implementations of this interface should supply the actual
 * {@link FileConfiguration} instance by implementing {@link #getConfiguration()}.
 * </p>
 * <p>
 * Example usage:
 * <pre><code>
 * // Create a configurable instance (e.g., via a lambda expression)
 * Configurable config = () -&gt; YamlConfiguration.loadConfiguration(new File("config.yml"));
 *
 * // Retrieve a string value with a default
 * String value = config.get("some.path", "default");
 *
 * // Check if a path exists
 * boolean exists = config.contains("some.path");
 *
 * // Retrieve a list of strings from the configuration section
 * List&lt;String&gt; list = Configurable.toStringList(config.getConfiguration(), "list.path");
 *
 * // Retrieve subsections as a map
 * Map&lt;String, ConfigurationSection&gt; sections = config.getSections("section.path", true);
 *
 * // Convert a configuration section to a mappable set of sections
 * SectionMappable.Set mapped = config.asSectionMap("section.path");
 * </code></pre>
 * </p>
 *
 * @see FileConfiguration
 * @see YamlConfiguration
 */
@FunctionalInterface
public interface Configurable {

    /**
     * Gets the primary configuration associated with this configurable instance.
     *
     * @return the {@link FileConfiguration} instance (never {@code null}).
     */
    @NotNull
    FileConfiguration getConfiguration();

    /**
     * Retrieves a value from the configuration at the specified path and attempts to cast it to the given type.
     *
     * @param path  the path to the configuration value.
     * @param clazz the expected class type of the value.
     * @param <T>   the type of the value.
     * @return the value at the specified path, or {@code null} if casting fails.
     */
    @Nullable
    default <T> T get(String path, Class<T> clazz) {
        try {
            return clazz.cast(getConfiguration().get(path));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Retrieves a value from the configuration at the specified path, returning a default if the key does not exist.
     *
     * @param path the path to the configuration value.
     * @param def  the default value to return if the key does not exist.
     * @param <T>  the type of the value.
     * @return the retrieved value or the default value if not found.
     */
    @SuppressWarnings("unchecked")
    default <T> T get(String path, T def) {
        return (T) getConfiguration().get(path, def);
    }

    /**
     * Sets a value in the configuration at the specified path.
     *
     * @param path  the path to the configuration key.
     * @param value the value to set.
     * @param <T>   the type of the value.
     */
    default <T> void set(String path, T value) {
        getConfiguration().set(path, value);
    }

    /**
     * Checks if the configuration contains a specific key.
     *
     * @param path           the path to check.
     * @param ignoresDefault whether to ignore default values.
     * @return {@code true} if the path exists, otherwise {@code false}.
     */
    default boolean contains(String path, boolean ignoresDefault) {
        return getConfiguration().contains(path, ignoresDefault);
    }

    /**
     * Checks if the configuration contains a specific key, considering default values.
     *
     * @param path the path to check.
     * @return {@code true} if the path exists, otherwise {@code false}.
     */
    default boolean contains(String path) {
        return getConfiguration().contains(path, true);
    }

    /**
     * Retrieves a configuration section from the specified path.
     *
     * @param path the path to the configuration section.
     * @return the {@link ConfigurationSection} at the specified path, or {@code null} if not found.
     */
    @Nullable
    default ConfigurationSection getSection(String path) {
        return StringUtils.isBlank(path) ? getConfiguration() : getConfiguration().getConfigurationSection(path);
    }

    /**
     * Retrieves a list of strings from the configuration at the specified path.
     *
     * @param path the path to the list.
     * @return a list of strings from the configuration, or an empty list if the path does not exist.
     */
    default List<String> toStringList(String path) {
        return toStringList(getConfiguration(), path);
    }

    /**
     * Retrieves all keys within a specified configuration section.
     *
     * @param path the section path.
     * @param deep whether to include keys recursively from nested sections.
     * @return a list of keys under the specified section.
     */
    @NotNull
    default List<String> getKeys(String path, boolean deep) {
        ConfigurationSection section = getSection(path);
        return section != null ? new ArrayList<>(section.getKeys(deep)) : new ArrayList<>();
    }

    /**
     * Retrieves all top-level keys within a specified configuration section.
     *
     * @param path the section path.
     * @return a list of keys under the specified section.
     */
    @NotNull
    default List<String> getKeys(String path) {
        return getKeys(path, false);
    }

    /**
     * Retrieves all subsections within a specified configuration section.
     *
     * @param path the section path.
     * @param deep whether to include nested subsections recursively.
     * @return a map of subsection names to their corresponding {@link ConfigurationSection} objects.
     */
    @NotNull
    default Map<String, ConfigurationSection> getSections(String path, boolean deep) {
        Map<String, ConfigurationSection> map = new LinkedHashMap<>();
        ConfigurationSection section = getSection(path);
        if (section != null) {
            for (String key : section.getKeys(deep)) {
                ConfigurationSection c = section.getConfigurationSection(key);
                if (c != null) map.put(key, c);
            }
        }
        return map;
    }

    /**
     * Retrieves all top-level subsections within a specified configuration section.
     *
     * @param path the section path.
     * @return a map of subsection names to their corresponding {@link ConfigurationSection} objects.
     */
    @NotNull
    default Map<String, ConfigurationSection> getSections(String path) {
        return getSections(path, false);
    }

    /**
     * Converts a configuration section into a {@link SectionMappable.Set}.
     *
     * @param path the path to the configuration section.
     * @return a {@link SectionMappable.Set} representing the configuration section.
     */
    @NotNull
    default SectionMappable.Set asSectionMap(String path) {
        return toSectionMap(getConfiguration(), path);
    }

    /**
     * Converts a configuration section into a {@link UnitMappable.Set} by applying a transformation function.
     *
     * @param path     the path to the configuration section.
     * @param function the function that transforms a {@link ConfigurationSection} into a {@link ConfigurableUnit}.
     * @param <U>      the type of the configurable unit.
     * @return a {@link UnitMappable.Set} containing the transformed units.
     */
    @NotNull
    default <U extends ConfigurableUnit> UnitMappable.Set<U> asUnitMap(String path, Function<ConfigurationSection, U> function) {
        return asSectionMap(path).toUnits(function);
    }

    /**
     * Converts a configuration section's value into a list of strings.
     *
     * @param section the configuration section.
     * @param path    the path within the section.
     * @param def     the default list to return if the key does not exist.
     * @return a list of strings representing the configuration value, or the default list if not found.
     */
    static List<String> toStringList(ConfigurationSection section, String path, List<String> def) {
        if (section == null) return def;
        if (!section.isList(path)) {
            final Object temp = section.get(path);
            return temp != null ? Collections.singletonList(temp.toString()) : def;
        }
        List<?> raw = section.getList(path, new ArrayList<>());
        if (!raw.isEmpty()) {
            List<String> list = new ArrayList<>();
            raw.forEach(o -> list.add(o.toString()));
            return list;
        }
        return def;
    }

    /**
     * Converts a configuration section's value into a list of strings.
     *
     * @param section the configuration section.
     * @param path    the path within the section.
     * @return a list of strings representing the configuration value, or an empty list if not found.
     */
    static List<String> toStringList(ConfigurationSection section, String path) {
        return toStringList(section, path, new ArrayList<>());
    }

    /**
     * Converts a configuration section into a {@link SectionMappable.Set} instance.
     * <p>
     * This method navigates to the desired configuration section, extracts its keys, and groups the subsections
     * based on their priority and permission values. The result is a mappable set that is ordered descendingly.
     * </p>
     *
     * @param section the main configuration section.
     * @param path    the path within the configuration.
     * @return a {@link SectionMappable.Set} representing the mapped configuration sections.
     */
    @NotNull
    static SectionMappable.Set toSectionMap(@Nullable ConfigurationSection section, @Nullable String path) {
        if (StringUtils.isNotBlank(path) && section != null)
            section = section.getConfigurationSection(path);
        if (section == null)
            return SectionMappable.asSet();
        Set<String> sectionKeys = section.getKeys(false);
        if (sectionKeys.isEmpty())
            return SectionMappable.asSet();
        Map<Integer, Set<ConfigurationSection>> map = new HashMap<>();
        for (String key : sectionKeys) {
            ConfigurationSection id = section.getConfigurationSection(key);
            if (id == null) continue;
            String perm = id.getString("permission", "DEFAULT");
            int def = perm.matches("(?i)default") ? 0 : 1;
            int priority = id.getInt("priority", def);
            Set<ConfigurationSection> m = map.getOrDefault(priority, new LinkedHashSet<>());
            m.add(id);
            map.put(priority, m);
        }
        return SectionMappable.asSet(map).order(false);
    }

    /**
     * Creates a new {@code Configurable} instance from a given {@link FileConfiguration}.
     * <p>
     * The returned {@code Configurable} is a functional instance whose {@link #getConfiguration()}
     * method returns the specified {@code FileConfiguration}. This method is useful for integrating with
     * configuration management systems.
     * </p>
     *
     * @param section the file configuration to wrap.
     * @return a new {@code Configurable} instance.
     */
    @NotNull
    static Configurable of(FileConfiguration section) {
        return () -> Objects.requireNonNull(section);
    }
}
