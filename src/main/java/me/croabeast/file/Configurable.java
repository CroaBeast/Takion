package me.croabeast.file;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Represents an object that can manage and manipulate a {@link FileConfiguration}.
 * This interface provides utility methods to retrieve, modify, and navigate through configuration data.
 */
@FunctionalInterface
public interface Configurable {

    /**
     * Gets the primary configuration associated with this configurable instance.
     *
     * @return The {@link FileConfiguration} instance.
     */
    @NotNull
    FileConfiguration getConfiguration();

    /**
     * Retrieves a value from the configuration and attempts to cast it to the specified type.
     *
     * @param path  The path to the configuration value.
     * @param clazz The expected class type.
     * @param <T>   The type of the value.
     * @return The retrieved value, or {@code null} if casting fails.
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
     * Retrieves a value from the configuration, returning a default if the value does not exist.
     *
     * @param path The path to the configuration value.
     * @param def  The default value to return if the key does not exist.
     * @param <T>  The type of the value.
     * @return The retrieved value or the default value if not found.
     */
    @SuppressWarnings("unchecked")
    default <T> T get(String path, T def) {
        return (T) getConfiguration().get(path, def);
    }

    /**
     * Sets a value in the configuration.
     *
     * @param path  The path to the configuration key.
     * @param value The value to set.
     * @param <T>   The type of the value.
     */
    default <T> void set(String path, T value) {
        getConfiguration().set(path, value);
    }

    /**
     * Checks if the configuration contains a specific key.
     *
     * @param path          The path to check.
     * @param ignoresDefault Whether to ignore default values.
     * @return {@code true} if the path exists, otherwise {@code false}.
     */
    default boolean contains(String path, boolean ignoresDefault) {
        return getConfiguration().contains(path, ignoresDefault);
    }

    /**
     * Checks if the configuration contains a specific key, considering default values.
     *
     * @param path The path to check.
     * @return {@code true} if the path exists, otherwise {@code false}.
     */
    default boolean contains(String path) {
        return getConfiguration().contains(path, true);
    }

    /**
     * Retrieves a configuration section from the given path.
     *
     * @param path The path of the section.
     * @return The {@link ConfigurationSection} or {@code null} if not found.
     */
    @Nullable
    default ConfigurationSection getSection(String path) {
        return StringUtils.isBlank(path) ? getConfiguration() : getConfiguration().getConfigurationSection(path);
    }

    /**
     * Retrieves a list of string values from the specified path.
     *
     * @param path The path of the list.
     * @return A list of strings.
     */
    default List<String> toStringList(String path) {
        return toStringList(getConfiguration(), path);
    }

    /**
     * Retrieves all keys within a specified section.
     *
     * @param path The section path.
     * @param deep Whether to include subkeys recursively.
     * @return A list of keys.
     */
    @NotNull
    default List<String> getKeys(String path, boolean deep) {
        ConfigurationSection section = getSection(path);
        return section != null ? new ArrayList<>(section.getKeys(deep)) : new ArrayList<>();
    }

    /**
     * Retrieves all top-level keys within a specified section.
     *
     * @param path The section path.
     * @return A list of keys.
     */
    @NotNull
    default List<String> getKeys(String path) {
        return getKeys(path, false);
    }

    /**
     * Retrieves all subsections within a specified section.
     *
     * @param path The section path.
     * @param deep Whether to retrieve subsections recursively.
     * @return A map containing subsection names and their corresponding {@link ConfigurationSection} objects.
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
     * Retrieves all top-level subsections within a specified section.
     *
     * @param path The section path.
     * @return A map containing subsection names and their corresponding {@link ConfigurationSection} objects.
     */
    @NotNull
    default Map<String, ConfigurationSection> getSections(String path) {
        return getSections(path, false);
    }

    /**
     * Converts a section into a {@link SectionMappable.Set}.
     *
     * @param path The section path.
     * @return A mapped section.
     */
    @NotNull
    default SectionMappable.Set asSectionMap(String path) {
        return toSectionMap(getConfiguration(), path);
    }

    /**
     * Converts a section into a {@link UnitMappable.Set} using a transformation function.
     *
     * @param path     The section path.
     * @param function The function to transform each section into a unit.
     * @param <U>      The unit type.
     * @return A mapped unit set.
     */
    @NotNull
    default <U extends ConfigurableUnit> UnitMappable.Set<U> asUnitMap(String path, Function<ConfigurationSection, U> function) {
        return asSectionMap(path).toUnits(function);
    }

    /**
     * Converts a configuration section's value into a list of strings.
     *
     * @param section The configuration section.
     * @param path    The path within the section.
     * @param def     The default list if the path does not exist.
     * @return A list of strings.
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
     * @param section The configuration section.
     * @param path    The path within the section.
     * @return A list of strings.
     */
    static List<String> toStringList(ConfigurationSection section, String path) {
        return toStringList(section, path, new ArrayList<>());
    }

    /**
     * Converts a configuration section into a {@link SectionMappable.Set}.
     *
     * @param section The configuration section.
     * @param path    The path within the section.
     * @return A mapped section set.
     */
    @NotNull
    static SectionMappable.Set toSectionMap(@Nullable ConfigurationSection section, @Nullable String path) {
        if (StringUtils.isNotBlank(path) && section != null)
            section = section.getConfigurationSection(path);

        if (section == null)
            return SectionMappable.asSet();

        return SectionMappable.asSet();
    }

    /**
     * Creates a new {@code Configurable} instance from a given {@link FileConfiguration}.
     *
     * @param section The configuration.
     * @return A new {@code Configurable} instance.
     */
    @NotNull
    static Configurable of(FileConfiguration section) {
        return () -> Objects.requireNonNull(section);
    }
}
