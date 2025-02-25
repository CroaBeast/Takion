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
public interface UnitMappable<U extends ConfigurableUnit> extends Map<Integer, Set<U>> {

    default UnitMappable<U> order(boolean ascendant) {
        final Comparator<Integer> comparator = ascendant ?
                Comparator.naturalOrder() :
                Comparator.reverseOrder();

        Map<Integer, Set<U>> units = new TreeMap<>(comparator);
        units.putAll(this);

        return UnitMappable.of(units);
    }

    default UnitMappable<U> filter(Predicate<U> predicate) {
        UnitMappable<U> units = UnitMappable.empty();

        forEach(((integer, set) -> {
            final Set<U> results = new HashSet<>();
            for (U unit : set)
                if (predicate.test(unit)) results.add(unit);

            units.put(integer, results);
        }));

        return units;
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
        return ConfigMapUtils.unit(map);
    }
}
