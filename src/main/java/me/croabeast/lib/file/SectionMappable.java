package me.croabeast.lib.file;

import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a mapping of configuration sections, organized by integer keys.
 *
 * <p> This interface extends the Map interface to provide methods to manipulate
 * and convert these mappings into other forms, particularly into mappings of
 * configurable units.
 */
public interface SectionMappable extends Mappable<ConfigurationSection> {

    SectionMappable filter(Predicate<ConfigurationSection> predicate);

    SectionMappable order(Comparator<Integer> comparator);

    default SectionMappable order(boolean ascendant) {
        return order(ascendant ? Comparator.naturalOrder() : Comparator.reverseOrder());
    }

    /**
     * Converts this mapping of configuration sections to a mapping of configurable units.
     *
     * <p> This is useful when you need to transform configuration sections into more specific
     * unit objects that implement the ConfigurableUnit interface.
     *
     * @param function The function to convert a ConfigurationSection to a ConfigurableUnit.
     * @param <U>      The type of the ConfigurableUnit.
     * @return A UnitMappable instance containing the converted units.
     * @throws NullPointerException if the function is null.
     */
    default <U extends ConfigurableUnit> UnitMappable<U> toUnits(Function<ConfigurationSection, U> function) {
        Objects.requireNonNull(function);

        Map<Integer, Set<U>> map = new LinkedHashMap<>();
        forEach((key, value) -> {
            Set<U> set = new LinkedHashSet<>();
            value.forEach(c -> set.add(function.apply(c)));

            map.put(key, set);
        });

        return UnitMappable.of(map);
    }

    /**
     * Creates a SectionMappable instance from the provided map.
     * This method is used to wrap a standard map into a SectionMappable interface.
     *
     * @param map The map to create the SectionMappable from.
     * @return A SectionMappable instance.
     */
    static SectionMappable of(Map<Integer, Set<ConfigurationSection>> map) {
        return new MapUtils.SectionMapImpl(map);
    }

    /**
     * Creates an empty SectionMappable instance.
     * This is useful when you need a SectionMappable without any initial data.
     *
     * @return An empty SectionMappable instance.
     */
    static SectionMappable empty() {
        return of(new LinkedHashMap<>());
    }
}
