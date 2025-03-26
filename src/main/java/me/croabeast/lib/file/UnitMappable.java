package me.croabeast.lib.file;

import java.util.*;
import java.util.function.Predicate;

/**
 * Represents a mapping of configurable units, organized by integer keys.
 *
 * <p> This interface extends the Map interface to provide additional functionality
 * specifically for handling ConfigurableUnit objects.
 *
 * @param <U> The type of ConfigurableUnit.
 */
public interface UnitMappable<U extends ConfigurableUnit> extends Mappable<U> {

    UnitMappable<U> filter(Predicate<U> predicate);

    UnitMappable<U> order(Comparator<Integer> comparator);

    default UnitMappable<U> order(boolean ascendant) {
        return order(ascendant ? Comparator.naturalOrder() : Comparator.reverseOrder());
    }

    default UnitMappable<U> copy() {
        return of(this);
    }

    /**
     * Creates a UnitMappable instance from the provided map.
     *
     * <p> This method is used to wrap a standard map into a UnitMappable interface,
     * allowing for more specialized handling of configurable units.
     *
     * @param map The map to create the UnitMappable from.
     * @param <U> The type of ConfigurableUnit.
     * @return A UnitMappable instance.
     */
    static <U extends ConfigurableUnit> UnitMappable<U> of(Map<Integer, Set<U>> map) {
        return new MapUtils.UnitMapImpl<>(map);
    }

    /**
     * Creates an empty UnitMappable instance.
     * This method is useful when you need a UnitMappable without any initial data.
     *
     * @param <U> The type of ConfigurableUnit.
     * @return An empty UnitMappable instance.
     */
    static <U extends ConfigurableUnit> UnitMappable<U> empty() {
        return of(new LinkedHashMap<>());
    }
}
