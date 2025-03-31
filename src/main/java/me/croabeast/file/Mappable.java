package me.croabeast.file;

import me.croabeast.lib.builder.Builder;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a specialized mapping structure that associates integer keys with collections of elements.
 * <p>
 * {@code Mappable} extends {@link Map} and {@link Builder} to provide a fluent interface for performing operations
 * such as filtering, ordering, and merging values across grouped collections.
 * </p>
 * <p>
 * <b>Important:</b> Concrete implementations of {@code Mappable} (as well as its sub-interfaces {@code BaseSet}
 * and {@code BaseList}) must be provided to ensure full functionality. The default methods offered in these interfaces,
 * such as {@link #getStoredValues(Supplier)} and {@link #copy()}, rely on a proper implementation of the underlying storage.
 * Without an implementation, methods like {@link #getStoredValues()} (the no-argument version) will not function as expected.
 * </p>
 * <p>
 * For example, the {@code BaseSet} and {@code BaseList} sub-interfaces provide default implementations to convert the stored
 * values into a {@link Set} or {@link List} respectively, but they require that you implement a concrete class that extends
 * {@code Mappable} to fully support these operations.
 * </p>
 * <p>
 * Example usage:
 * <pre><code>
 * // Create a concrete implementation of Mappable (for instance, HashMappable)
 * Mappable&lt;String, List&lt;String&gt;, ?&gt; mappable = new HashMappable&lt;&gt;();
 * mappable.put(1, Arrays.asList("One", "Uno"));
 * mappable.put(2, Arrays.asList("Two", "Dos"));
 *
 * // Use the default methods to merge all stored values into a List
 * List&lt;String&gt; allValues = mappable.getStoredValues(ArrayList::new);
 * System.out.println(allValues); // Outputs: [One, Uno, Two, Dos]
 * </code></pre>
 * </p>
 *
 * @param <T> The type of elements stored in the collections.
 * @param <C> The type of collection that holds the elements.
 * @param <B> The type of the implementing {@code Mappable} instance.
 * @see Builder
 */
public interface Mappable<T, C extends Collection<T>, B extends Mappable<T, C, B>>
        extends Map<Integer, C>, Iterable<Map.Entry<Integer, C>>, Builder<B> {

    /**
     * Filters the stored elements based on the given predicate, modifying the current instance.
     *
     * @param predicate The condition used to filter elements.
     * @return The modified instance of {@code B}, with non-matching elements removed.
     */
    @NotNull
    default B filter(Predicate<T> predicate) {
        values().forEach(c -> c.removeIf(predicate.negate()));
        return instance();
    }

    /**
     * Orders the keys in the mapping based on the given comparator.
     *
     * @param comparator The comparator used to order the keys.
     * @return A new instance of {@code B} with the keys ordered accordingly.
     */
    @NotNull
    B order(Comparator<Integer> comparator);

    /**
     * Orders the keys in ascending or descending order.
     *
     * @param ascendant If {@code true}, orders in ascending order; otherwise, orders in descending order.
     * @return A new instance of {@code B} with the keys ordered as specified.
     */
    @NotNull
    default B order(boolean ascendant) {
        return order(ascendant ? Comparator.naturalOrder() : Comparator.reverseOrder());
    }

    /**
     * Creates a shallow copy of the current instance.
     *
     * @return A new instance of {@code B} with the same data.
     */
    @NotNull
    B copy();

    /**
     * Retrieves all stored values merged into a single collection, using the provided supplier to create the collection.
     *
     * @param supplier The supplier used to create a new collection instance.
     * @param <X>      The type of the resulting collection.
     * @return A collection containing all stored values across all keys.
     */
    @NotNull
    default <X extends Collection<T>> X getStoredValues(Supplier<X> supplier) {
        Objects.requireNonNull(supplier);
        X collection = supplier.get();
        values().forEach(collection::addAll);
        return collection;
    }

    /**
     * Retrieves all stored values merged into a single collection.
     * <p>
     * Note that this method must be implemented by a concrete class for full functionality.
     * </p>
     *
     * @return A collection containing all stored values.
     */
    @NotNull
    C getStoredValues();

    /**
     * Returns an iterator over the map's entries.
     *
     * @return an iterator over {@link Map.Entry} objects containing the integer keys and corresponding collections.
     */
    @NotNull
    default Iterator<Entry<Integer, C>> iterator() {
        return entrySet().iterator();
    }

    /**
     * A sub-interface of {@code Mappable} specialized for {@link java.util.Set} collections.
     * <p>
     * Implementations of {@code BaseSet} provide default behavior to retrieve stored values as a {@link Set}.
     * </p>
     *
     * @param <T> The type of elements stored in the set.
     */
    interface BaseSet<T, B extends BaseSet<T, B>> extends Mappable<T, Set<T>, B> {

        /**
         * Retrieves all stored values merged into a {@link Set}.
         *
         * @return A set containing all stored values.
         */
        @NotNull
        default Set<T> getStoredValues() {
            return getStoredValues(HashSet::new);
        }
    }

    /**
     * A sub-interface of {@code Mappable} specialized for {@link java.util.List} collections.
     * <p>
     * Implementations of {@code BaseList} provide default behavior to retrieve stored values as a {@link List}.
     * </p>
     *
     * @param <T> The type of elements stored in the list.
     */
    interface BaseList<T, B extends BaseList<T, B>> extends Mappable<T, List<T>, B> {

        /**
         * Retrieves all stored values merged into a {@link List}.
         *
         * @return A list containing all stored values.
         */
        @NotNull
        default List<T> getStoredValues() {
            return getStoredValues(ArrayList::new);
        }
    }
}
