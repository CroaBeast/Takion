package me.croabeast.lib.file;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Base interface representing a map of integer keys to sets of values.
 * This interface extends the Map interface and is used as a base for more specific
 * mappings, such as SectionMappable and UnitMappable.
 *
 * @param <T> The type of the values in the sets.
 */
public interface Mappable<T> extends Map<Integer, Set<T>> {

    Mappable<T> filter(Predicate<T> predicate);

    Mappable<T> order(Comparator<Integer> comparator);

    default Mappable<T> order(boolean ascendant) {
        return order(ascendant ? Comparator.naturalOrder() : Comparator.reverseOrder());
    }

    default <C extends Collection<T>> C values(Supplier<C> supplier) {
        Objects.requireNonNull(supplier);
        C collection = supplier.get();

        values().forEach(collection::addAll);
        return collection;
    }

    static <T> Mappable<T> of(Map<Integer, Set<T>> map) {
        return new MapUtils.BaseMapImpl<>(map);
    }

    static <T> Mappable<T> empty() {
        return of(new HashMap<>());
    }
}
