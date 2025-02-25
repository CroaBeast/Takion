package me.croabeast.lib.file;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * The Configurable interface provides methods for accessing and manipulating
 * configuration data.
 *
 * <p> This interface is typically used for handling configurations stored in
 * YAML files within a Bukkit plugin.
 */
@FunctionalInterface
public interface Configurable {

    /**
     * Retrieves the underlying file configuration.
     *
     * @return the file configuration instance
     */
    @NotNull
    FileConfiguration getConfiguration();

    /**
     * Retrieves a value from the configuration at the specified path and attempts to cast it to the given type.
     *
     * @param <T>   the type of the value to retrieve
     * @param path  the path to the value in the configuration
     * @param clazz the class of the type to cast the value to
     * @return the value at the specified path, or null if not found or if the type cast fails
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
     * Retrieves a value from the configuration at the specified path or returns a default
     * value if the path does not exist.
     *
     * @param <T>  the type of the value to retrieve
     * @param path the path to the value in the configuration
     * @param def  the default value to return if the path does not exist
     *
     * @return the value at the specified path, or the default value if not found or cannot
     * be cast to the specified type
     */
    @SuppressWarnings("unchecked")
    default <T> T get(String path, T def) {
        return (T) getConfiguration().get(path, def);
    }

    /**
     * Sets a value in the configuration at the specified path.
     *
     * @param <T>   the type of the value to set
     * @param path  the path to set the value in the configuration
     * @param value the value to set
     */
    default <T> void set(String path, T value) {
        getConfiguration().set(path, value);
    }

    default boolean contains(String path, boolean ignoresDefault) {
        return getConfiguration().contains(path, ignoresDefault);
    }

    default boolean contains(String path) {
        return getConfiguration().contains(path, true);
    }

    /**
     * Retrieves a configuration section at the specified path.
     *
     * @param path the path to the configuration section
     * @return the configuration section at the specified path, or null if not found
     */
    @Nullable
    default ConfigurationSection getSection(String path) {
        return StringUtils.isBlank(path) ? getConfiguration() : getConfiguration().getConfigurationSection(path);
    }

    /**
     * Converts a list at the specified path in the configuration to a list of strings.
     *
     * @param path the path to the list in the configuration
     * @return a list of strings from the specified path, or an empty list if not found
     */
    default List<String> toStringList(String path) {
        return toStringList(getConfiguration(), path);
    }

    /**
     * Retrieves the keys under the specified path in the configuration.
     *
     * @param path the path to the keys in the configuration
     * @param deep whether to include keys from nested sections
     *
     * @return a list of keys under the specified path
     */
    @NotNull
    default List<String> getKeys(String path, boolean deep) {
        ConfigurationSection section = getSection(path);
        return section != null ? new ArrayList<>(section.getKeys(deep)) : new ArrayList<>();
    }

    /**
     * Retrieves the keys under the specified path in the configuration, excluding nested keys.
     *
     * @param path the path to the keys in the configuration
     * @return a list of keys under the specified path
     */
    @NotNull
    default List<String> getKeys(String path) {
        return getKeys(path, false);
    }

    /**
     * Retrieves a map of configuration sections under the specified path.
     *
     * @param path the path to the configuration sections
     * @param deep whether to include nested sections
     *
     * @return a map of configuration sections under the specified path
     */
    @NotNull
    default Map<String, ConfigurationSection> getSections(String path, boolean deep) {
        Map<String, ConfigurationSection> map = new LinkedHashMap<>();

        ConfigurationSection section = getSection(path);
        if (section != null)
            for (String key : section.getKeys(deep)) {
                ConfigurationSection c = section.getConfigurationSection(key);
                if (c != null) map.put(key, c);
            }

        return map;
    }

    /**
     * Retrieves a map of configuration sections under the specified path, excluding nested sections.
     *
     * @param path the path to the configuration sections
     * @return a map of configuration sections under the specified path
     */
    @NotNull
    default Map<String, ConfigurationSection> getSections(String path) {
        return getSections(path, false);
    }

    /**
     * Creates a SectionMappable from the configuration sections under the specified path.
     *
     * @param path the path to the configuration sections
     * @return a SectionMappable containing the configuration sections grouped by priority
     */
    @NotNull
    default SectionMappable asSectionMap(String path) {
        return toSectionMap(getConfiguration(), path);
    }

    /**
     * Creates a UnitMappable from the configuration units under the specified path
     * using the provided function.
     *
     * @param path     the path to the configuration units
     * @param function the function to convert a ConfigurationSection to a ConfigurableUnit
     * @param <U>      the type of the ConfigurableUnit
     *
     * @return a UnitMappable containing the configuration units grouped by priority
     */
    @NotNull
    default <U extends ConfigurableUnit> UnitMappable<U> asUnitMap(String path, Function<ConfigurationSection, U> function) {
        return asSectionMap(path).toUnits(function);
    }

    /**
     * Converts a list at the specified path in the configuration section to a list of strings.
     *
     * @param section the configuration section
     * @param path    the path to the list in the configuration
     * @param def     the default list to return if the path does not exist
     *
     * @return a list of strings from the specified path, or the default list if not found
     */
    static List<String> toStringList(ConfigurationSection section, String path, List<String> def) {
        if (section == null) return def;

        if (!section.isList(path)) {
            final Object temp = section.get(path);
            return temp != null ?
                    Collections.singletonList(temp.toString()) : def;
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
     * Converts a list at the specified path in the configuration section to a list of strings.
     *
     * @param section the configuration section
     * @param path    the path to the list in the configuration
     *
     * @return a list of strings from the specified path, or an empty list if not found
     */
    static List<String> toStringList(ConfigurationSection section, String path) {
        return toStringList(section, path, new ArrayList<>());
    }

    /**
     * Creates a SectionMappable from the configuration sections under the specified
     * path and sorts them by priority.
     *
     * @param section the main configuration section
     * @param path    the path to the configuration units
     *
     * @return a SectionMappable containing the configuration units grouped by priority
     */
    @NotNull
    static SectionMappable toSectionMap(@Nullable ConfigurationSection section, @Nullable String path) {
        if (StringUtils.isNotBlank(path) && section != null)
            section = section.getConfigurationSection(path);

        if (section == null)
            return SectionMappable.empty();

        Set<String> sectionKeys = section.getKeys(false);
        if (sectionKeys.isEmpty())
            return SectionMappable.empty();

        Comparator<Integer> sort = Comparator.reverseOrder();
        Map<Integer, Set<ConfigurationSection>> map = new TreeMap<>(sort);

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

        return SectionMappable.of(map);
    }

    /**
     * Creates a Configurable instance from a FileConfiguration.
     *
     * @param section the file configuration section
     * @return a new Configurable instance
     */
    @NotNull
    static Configurable of(FileConfiguration section) {
        return () -> Objects.requireNonNull(section);
    }
}