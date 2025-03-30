package me.croabeast.file;

import me.croabeast.lib.CollectionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Represents a mappable collection of {@link ConfigurableUnit} elements.
 * <p>
 * {@code UnitMappable} extends {@link Mappable} to provide additional factory methods
 * for creating instances backed by different collection types (such as {@link java.util.Set} or {@link java.util.List}).
 * This interface allows for conversion of the internal mappings into concrete collection types and
 * provides utility methods to transform the contained {@link ConfigurableUnit} objects.
 * </p>
 *
 * @param <U> The type of configurable unit.
 * @param <C> The type of collection that holds the configurable units.
 */
public interface UnitMappable<U extends ConfigurableUnit, C extends Collection<U>> extends Mappable<U, C, UnitMappable<U, C>> {

    /**
     * Returns this instance of {@code UnitMappable}.
     * <p>
     * This method ensures type safety by returning the current instance cast to the correct subtype.
     * </p>
     *
     * @return This instance of {@code UnitMappable}.
     */
    @NotNull
    default UnitMappable<U, C> instance() {
        return this;
    }

    /**
     * Creates a copy of this {@code UnitMappable} with the same contents.
     *
     * @return A new {@code UnitMappable} instance with copied data.
     */
    @NotNull
    default UnitMappable<U, C> copy() {
        return of(getSupplier(), this);
    }

    /**
     * Converts this UnitMappable into a {@link java.util.Set} of configurable units.
     * <p>
     * For each entry in the mappable, the contained collection is transformed into a {@code Set}
     * using {@link CollectionBuilder#toSet()}.
     * </p>
     *
     * @return a {@link UnitMappable.Set} representing the same mappings as a {@code Map<Integer, Set<U>>}.
     */
    @NotNull
    default Set<U> toSet() {
        Map<Integer, java.util.Set<U>> map = new LinkedHashMap<>();
        forEach((k, v) -> map.put(k, CollectionBuilder.of(v).toSet()));
        return asSet(map);
    }

    /**
     * Converts this UnitMappable into a {@link java.util.List} of configurable units.
     * <p>
     * For each entry in the mappable, the contained collection is transformed into a {@code List}
     * using {@link CollectionBuilder#toList()}.
     * </p>
     *
     * @return a {@link UnitMappable.List} representing the same mappings as a {@code Map<Integer, List<U>>}.
     */
    @NotNull
    default List<U> toList() {
        Map<Integer, java.util.List<U>> map = new LinkedHashMap<>();
        forEach((k, v) -> map.put(k, CollectionBuilder.of(v).toList()));
        return asList(map);
    }

    /**
     * Creates a new {@code UnitMappable} instance with the provided supplier and map.
     *
     * @param supplier The supplier that provides a new collection instance.
     * @param map      The map whose contents will be copied.
     * @param <U>      The type of configurable unit.
     * @param <C>      The type of collection that holds the configurable units.
     * @return A new {@code UnitMappable} instance populated with the provided map.
     */
    static <U extends ConfigurableUnit, C extends Collection<U>> UnitMappable<U, C> of(Supplier<C> supplier, Map<Integer, C> map) {
        UnitMappable<U, C> before = new MapUtils.UnitImpl<>(supplier);
        before.putAll(map);
        return before;
    }

    /**
     * Creates a new empty {@code UnitMappable} instance using the provided collection supplier.
     *
     * @param supplier The supplier that provides a new collection instance.
     * @param <U>      The type of configurable unit.
     * @param <C>      The type of collection that holds the configurable units.
     * @return A new empty {@code UnitMappable} instance.
     */
    static <U extends ConfigurableUnit, C extends Collection<U>> UnitMappable<U, C> of(Supplier<C> supplier) {
        return new MapUtils.UnitImpl<>(supplier);
    }

    /**
     * Creates a new {@code UnitMappable.Set} instance from a given map.
     *
     * @param map The map whose contents will be copied.
     * @param <U> The type of configurable unit.
     * @return A new {@code UnitMappable.Set} with copied data.
     */
    static <U extends ConfigurableUnit> UnitMappable.Set<U> asSet(Map<Integer, java.util.Set<U>> map) {
        UnitMappable.Set<U> set = new MapUtils.UnitImpl.Set<>();
        set.putAll(map);
        return set;
    }

    /**
     * Creates an empty {@code UnitMappable.Set} instance.
     *
     * @param <U> The type of configurable unit.
     * @return A new empty {@code UnitMappable.Set}.
     */
    static <U extends ConfigurableUnit> UnitMappable.Set<U> asSet() {
        return new MapUtils.UnitImpl.Set<>();
    }

    /**
     * Creates a new {@code UnitMappable.List} instance from a given map.
     *
     * @param map The map whose contents will be copied.
     * @param <U> The type of configurable unit.
     * @return A new {@code UnitMappable.List} with copied data.
     */
    static <U extends ConfigurableUnit> UnitMappable.List<U> asList(Map<Integer, java.util.List<U>> map) {
        UnitMappable.List<U> list = new MapUtils.UnitImpl.List<>();
        list.putAll(map);
        return list;
    }

    /**
     * Creates an empty {@code UnitMappable.List} instance.
     *
     * @param <U> The type of configurable unit.
     * @return A new empty {@code UnitMappable.List}.
     */
    static <U extends ConfigurableUnit> UnitMappable.List<U> asList() {
        return new MapUtils.UnitImpl.List<>();
    }

    /**
     * Represents a {@code UnitMappable} implementation backed by a {@link java.util.Set}.
     *
     * @param <U> The type of configurable unit.
     */
    interface Set<U extends ConfigurableUnit> extends UnitMappable<U, java.util.Set<U>> {

        /**
         * Returns this instance of {@code UnitMappable.Set}.
         *
         * @return this instance for fluent chaining.
         */
        @NotNull
        default UnitMappable.Set<U> instance() {
            return this;
        }

        /**
         * Creates a copy of this {@code UnitMappable.Set}, preserving all its mappings.
         *
         * @return a new {@code UnitMappable.Set} instance with the same contents.
         */
        @NotNull
        default UnitMappable.Set<U> copy() {
            return asSet(this);
        }

        /**
         * Convenience method to return this instance as a set.
         *
         * @return this instance.
         */
        @NotNull
        default UnitMappable.Set<U> toSet() {
            return instance();
        }
    }

    /**
     * Represents a {@code UnitMappable} implementation backed by a {@link java.util.List}.
     *
     * @param <U> The type of configurable unit.
     */
    interface List<U extends ConfigurableUnit> extends UnitMappable<U, java.util.List<U>> {

        /**
         * Returns this instance of {@code UnitMappable.List}.
         *
         * @return this instance for fluent chaining.
         */
        @NotNull
        default UnitMappable.List<U> instance() {
            return this;
        }

        /**
         * Creates a copy of this {@code UnitMappable.List}, preserving all its mappings.
         *
         * @return a new {@code UnitMappable.List} instance with the same contents.
         */
        @NotNull
        default UnitMappable.List<U> copy() {
            return asList(this);
        }

        /**
         * Convenience method to return this instance as a list.
         *
         * @return this instance.
         */
        @NotNull
        default UnitMappable.List<U> toList() {
            return instance();
        }
    }
}
