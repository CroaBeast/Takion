package me.croabeast.file;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Represents a mappable collection of {@link ConfigurableUnit} elements.
 * It extends {@link Mappable} and provides additional factory methods for
 * handling sets and lists of configurable units.
 *
 * @param <U> The type of configurable unit.
 * @param <C> The type of collection that holds the configurable units.
 */
public interface UnitMappable<U extends ConfigurableUnit, C extends Collection<U>> extends Mappable<U, C, UnitMappable<U, C>> {

    /**
     * Returns an instance of this {@link UnitMappable}.
     * This method ensures type safety by returning the correct subtype.
     *
     * @return This instance of {@link UnitMappable}.
     */
    @NotNull
    default UnitMappable<U, C> instance() {
        return this;
    }

    /**
     * Creates a copy of this {@link UnitMappable} with the same content.
     *
     * @return A new {@link UnitMappable} with copied data.
     */
    @NotNull
    default UnitMappable<U, C> copy() {
        return of(getSupplier(), this);
    }

    /**
     * Creates a new {@link UnitMappable} using the provided supplier and map.
     *
     * @param supplier The supplier that provides a new collection instance.
     * @param map      The map whose contents will be copied.
     * @param <U>      The type of configurable unit.
     * @param <C>      The type of collection that holds the configurable units.
     * @return A new {@link UnitMappable} instance.
     */
    static <U extends ConfigurableUnit, C extends Collection<U>> UnitMappable<U, C> of(Supplier<C> supplier, Map<Integer, C> map) {
        UnitMappable<U, C> before = new MapUtils.UnitImpl<>(supplier);
        before.putAll(map);
        return before;
    }

    /**
     * Creates a new empty {@link UnitMappable} using the provided supplier.
     *
     * @param supplier The supplier that provides a new collection instance.
     * @param <U>      The type of configurable unit.
     * @param <C>      The type of collection that holds the configurable units.
     * @return A new empty {@link UnitMappable} instance.
     */
    static <U extends ConfigurableUnit, C extends Collection<U>> UnitMappable<U, C> of(Supplier<C> supplier) {
        return new MapUtils.UnitImpl<>(supplier);
    }

    /**
     * Creates a new {@link UnitMappable.Set} from a given map.
     *
     * @param map The map whose contents will be copied.
     * @param <U> The type of configurable unit.
     * @return A new {@link UnitMappable.Set} with copied data.
     */
    static <U extends ConfigurableUnit> UnitMappable.Set<U> asSet(Map<Integer, java.util.Set<U>> map) {
        UnitMappable.Set<U> set = new MapUtils.UnitImpl.Set<>();
        set.putAll(map);
        return set;
    }

    /**
     * Creates an empty {@link UnitMappable.Set}.
     *
     * @param <U> The type of configurable unit.
     * @return A new empty {@link UnitMappable.Set}.
     */
    static <U extends ConfigurableUnit> UnitMappable.Set<U> asSet() {
        return new MapUtils.UnitImpl.Set<>();
    }

    /**
     * Creates a new {@link UnitMappable.List} from a given map.
     *
     * @param map The map whose contents will be copied.
     * @param <U> The type of configurable unit.
     * @return A new {@link UnitMappable.List} with copied data.
     */
    static <U extends ConfigurableUnit> UnitMappable.List<U> asList(Map<Integer, java.util.List<U>> map) {
        UnitMappable.List<U> list = new MapUtils.UnitImpl.List<>();
        list.putAll(map);
        return list;
    }

    /**
     * Creates an empty {@link UnitMappable.List}.
     *
     * @param <U> The type of configurable unit.
     * @return A new empty {@link UnitMappable.List}.
     */
    static <U extends ConfigurableUnit> UnitMappable.List<U> asList() {
        return new MapUtils.UnitImpl.List<>();
    }

    /**
     * Represents a {@link UnitMappable} implementation backed by a {@link java.util.Set}.
     *
     * @param <U> The type of configurable unit.
     */
    interface Set<U extends ConfigurableUnit> extends UnitMappable<U, java.util.Set<U>> {

        /**
         * Returns an instance of this {@link UnitMappable.Set}.
         *
         * @return This instance of {@link UnitMappable.Set}.
         */
        @NotNull
        default UnitMappable.Set<U> instance() {
            return this;
        }

        /**
         * Creates a copy of this {@link UnitMappable.Set}.
         *
         * @return A new {@link UnitMappable.Set} with copied data.
         */
        @NotNull
        default UnitMappable.Set<U> copy() {
            return asSet(this);
        }
    }

    /**
     * Represents a {@link UnitMappable} implementation backed by a {@link java.util.List}.
     *
     * @param <U> The type of configurable unit.
     */
    interface List<U extends ConfigurableUnit> extends UnitMappable<U, java.util.List<U>> {

        /**
         * Returns an instance of this {@link UnitMappable.List}.
         *
         * @return This instance of {@link UnitMappable.List}.
         */
        @NotNull
        default UnitMappable.List<U> instance() {
            return this;
        }

        /**
         * Creates a copy of this {@link UnitMappable.List}.
         *
         * @return A new {@link UnitMappable.List} with copied data.
         */
        @NotNull
        default UnitMappable.List<U> copy() {
            return asList(this);
        }
    }
}
