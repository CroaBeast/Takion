package me.croabeast.file;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A concrete implementation of {@link Mappable} backed by a {@link HashMap}.
 * <p>
 * {@code HashMappable} associates integer keys with collections of elements of type {@code T}.
 * It extends {@code HashMap<Integer, C>} and implements the {@code Mappable} interface, providing
 * utility methods for ordering and copying the mapping. This implementation is intended as a base
 * class; certain methods (such as {@link #getStoredValues()}) must be overridden in subclasses or
 * specific instances to provide concrete functionality.
 * </p>
 *
 * @param <T> the type of elements stored in the collections.
 * @param <C> the type of collection that holds the elements.
 * @see Mappable
 */
public class HashMappable<T, C extends Collection<T>> extends HashMap<Integer, C> implements Mappable<T, C, HashMappable<T, C>> {

    /**
     * Constructs an empty {@code HashMappable}.
     */
    public HashMappable() {}

    /**
     * Constructs a new {@code HashMappable} with the mappings copied from the provided map.
     *
     * @param map the map whose entries are to be placed in this mappable.
     */
    public HashMappable(Map<Integer, ? extends C> map) {
        super(map);
    }

    /**
     * Orders the entries of this {@code HashMappable} using the provided comparator.
     * <p>
     * This method creates a new ordered map (a {@link TreeMap}) based on the given comparator,
     * copies all entries into it, and then returns a new {@code HashMappable} instance
     * containing the ordered entries.
     * </p>
     *
     * @param comparator the comparator used for ordering the keys.
     * @return a new {@code HashMappable} instance with entries ordered according to the comparator.
     */
    @NotNull
    public HashMappable<T, C> order(Comparator<Integer> comparator) {
        TreeMap<Integer, C> map = new TreeMap<>(comparator);
        map.putAll(this);
        return new HashMappable<>(map);
    }

    /**
     * Creates a shallow copy of this {@code HashMappable}.
     * <p>
     * The returned copy contains the same keys and values as this instance.
     * </p>
     *
     * @return a new {@code HashMappable} instance with the same data.
     */
    @NotNull
    public HashMappable<T, C> copy() {
        return new HashMappable<>(this);
    }

    /**
     * Retrieves all stored values combined into a single collection.
     * <p>
     * <b>Note:</b> This method is not implemented in {@code HashMappable} and throws an
     * {@link UnsupportedOperationException}. It must be overridden in a subclass or a concrete
     * instance to provide the desired behavior.
     * </p>
     *
     * @return a collection containing all values from the mapping.
     * @throws UnsupportedOperationException if this method is not overridden.
     */
    @NotNull
    public C getStoredValues() {
        throw new UnsupportedOperationException("getStoredValues");
    }

    /**
     * Returns this instance.
     *
     * @return this {@code HashMappable} instance.
     */
    @NotNull
    public HashMappable<T, C> instance() {
        return this;
    }
}
