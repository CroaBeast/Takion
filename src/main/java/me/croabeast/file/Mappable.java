package me.croabeast.file;

import me.croabeast.lib.builder.Builder;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a specialized mapping structure that associates integer keys with collections of elements.
 * This interface extends {@link Map} and provides additional functionality for filtering, ordering,
 * and retrieving stored values efficiently.
 *
 * @param <T> The type of elements stored in the collections.
 * @param <C> The type of collection that holds the elements.
 * @param <M> The type of the implementing {@code Mappable} instance.
 */
@SuppressWarnings("unchecked")
public interface Mappable<T, C extends Collection<T>, M extends Mappable<T, C, M>> extends Map<Integer, C>, Builder<M> {

    /**
     * Provides a supplier that generates new instances of the collection type {@code C}.
     *
     * @return A {@link Supplier} capable of creating new collections of type {@code C}.
     */
    @NotNull
    Supplier<C> getSupplier();

    /**
     * Filters the stored elements based on the given predicate, modifying the current instance.
     *
     * @param predicate The condition used to filter elements.
     * @return The modified instance of {@code M}, with non-matching elements removed.
     */
    default M filter(Predicate<T> predicate) {
        values().forEach(c -> c.removeIf(predicate.negate()));
        return instance();
    }

    /**
     * Orders the keys in the map based on the given comparator.
     *
     * @param comparator The comparator used to determine the ordering of keys.
     * @return A reference to the modified instance with reordered keys.
     */
    default M order(Comparator<Integer> comparator) {
        Map<Integer, C> orderedMap = new TreeMap<>(comparator);
        orderedMap.putAll(this);

        clear();
        putAll(orderedMap);
        return instance();
    }

    /**
     * Orders the keys in ascending or descending order.
     *
     * @param ascendant If {@code true}, orders in ascending order; otherwise, orders in descending order.
     * @return A reference to the modified instance with reordered keys.
     */
    default M order(boolean ascendant) {
        return order(ascendant ? Comparator.naturalOrder() : Comparator.reverseOrder());
    }

    /**
     * Retrieves all values across all collections and merges them into a new collection.
     *
     * @param supplier The supplier used to create a new collection instance.
     * @param <X>      The type of the resulting collection.
     * @return A collection containing all stored values.
     */
    @NotNull
    default <X extends Collection<T>> X getAllValues(Supplier<X> supplier) {
        Objects.requireNonNull(supplier);
        X collection = supplier.get();
        values().forEach(collection::addAll);
        return collection;
    }

    /**
     * Retrieves all stored values in a new collection of type {@code C}.
     *
     * @return A collection containing all values stored in the map.
     */
    @NotNull
    default C getAllValues() {
        return getAllValues(getSupplier());
    }

    /**
     * Returns the current instance, cast to its implementing type {@code M}.
     *
     * @return The instance of {@code M}.
     */
    @NotNull
    default M instance() {
        return (M) this;
    }

    /**
     * Creates a shallow copy of the current instance.
     *
     * @return A new instance of {@code M} with the same data.
     */
    @NotNull
    default M copy() {
        return (M) of(getSupplier(), this);
    }

    /**
     * Creates a new {@code Mappable} instance with a given collection supplier and initial data.
     *
     * @param supplier The supplier responsible for creating collections of type {@code C}.
     * @param map      The initial map data to populate the instance.
     * @param <T>      The type of elements stored in the collections.
     * @param <C>      The type of collection that holds the elements.
     * @param <M>      The type of the implementing {@code Mappable} instance.
     * @return A new instance of {@code Mappable}.
     */
    static <T, C extends Collection<T>, M extends Mappable<T, C, M>> Mappable<T, C, M> of(Supplier<C> supplier, Map<Integer, C> map) {
        Mappable<T, C, M> instance = new MapUtils.MappableImpl<>(supplier);
        instance.putAll(map);
        return instance;
    }

    /**
     * Creates a new {@code Mappable} instance with a given collection supplier.
     *
     * @param supplier The supplier responsible for creating collections of type {@code C}.
     * @param <T>      The type of elements stored in the collections.
     * @param <C>      The type of collection that holds the elements.
     * @param <M>      The type of the implementing {@code Mappable} instance.
     * @return A new instance of {@code Mappable}.
     */
    static <T, C extends Collection<T>, M extends Mappable<T, C, M>> Mappable<T, C, M> of(Supplier<C> supplier) {
        return new MapUtils.MappableImpl<>(supplier);
    }

    /**
     * Creates a {@code Mappable.Set} instance from an existing map.
     *
     * @param map The map containing integer keys and set values.
     * @param <T> The type of elements in the set.
     * @return A new {@code Set} instance.
     */
    static <T> Set<T> asSet(Map<Integer, java.util.Set<T>> map) {
        Set<T> set = new MapUtils.MappableImpl.Set<>();
        set.putAll(map);
        return set;
    }

    /**
     * Creates an empty {@code Mappable.Set} instance.
     *
     * @param <T> The type of elements in the set.
     * @return A new empty {@code Set} instance.
     */
    static <T> Set<T> asSet() {
        return new MapUtils.MappableImpl.Set<>();
    }

    /**
     * Creates a {@code Mappable.List} instance from an existing map.
     *
     * @param map The map containing integer keys and list values.
     * @param <T> The type of elements in the list.
     * @return A new {@code List} instance.
     */
    static <T> List<T> asList(Map<Integer, java.util.List<T>> map) {
        List<T> list = new MapUtils.MappableImpl.List<>();
        list.putAll(map);
        return list;
    }

    /**
     * Creates an empty {@code Mappable.List} instance.
     *
     * @param <T> The type of elements in the list.
     * @return A new empty {@code List} instance.
     */
    static <T> List<T> asList() {
        return new MapUtils.MappableImpl.List<>();
    }

    /**
     * Represents a {@code Mappable} implementation specialized for {@link java.util.Set} collections.
     *
     * @param <T> The type of elements stored in the set.
     */
    interface Set<T> extends Mappable<T, java.util.Set<T>, Set<T>> {

        /**
         * Returns the current instance.
         *
         * @return The current instance of {@code Set<T>}.
         */
        @NotNull
        default Set<T> instance() {
            return this;
        }

        /**
         * Creates a copy of this instance.
         *
         * @return A new {@code Set<T>} with the same data.
         */
        @NotNull
        default Set<T> copy() {
            return asSet(this);
        }
    }

    /**
     * Represents a {@code Mappable} implementation specialized for {@link java.util.List} collections.
     *
     * @param <T> The type of elements stored in the list.
     */
    interface List<T> extends Mappable<T, java.util.List<T>, List<T>> {

        /**
         * Returns the current instance.
         *
         * @return The current instance of {@code List<T>}.
         */
        @NotNull
        default List<T> instance() {
            return this;
        }

        /**
         * Creates a copy of this instance.
         *
         * @return A new {@code List<T>} with the same data.
         */
        @NotNull
        default List<T> copy() {
            return asList(this);
        }
    }
}
