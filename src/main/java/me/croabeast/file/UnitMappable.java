package me.croabeast.file;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

/**
 * Represents a mappable collection of configurable units.
 * <p>
 * {@code UnitMappable} extends {@link Mappable} to provide a specialized mapping where integer keys
 * are associated with collections of {@link ConfigurableUnit} objects. It adds factory methods for
 * creating new instances with specific backing collection types and supports operations such as merging
 * values from multiple keys.
 * </p>
 * <p>
 * <b>Important:</b> For full functionality, you must implement the base {@code UnitMappable} interface.
 * However, its nested sub-interfaces {@code UnitMappable.Set} and {@code UnitMappable.List} are designed
 * to be directly instantiated using the provided static factory methods {@link #asSet(Map)} and {@link #asList(Map)},
 * which return ready-to-use implementations.
 * </p>
 *
 * @param <U>  The type of configurable unit stored.
 * @param <C>  The type of collection that holds the configurable units.
 * @param <UM> The type of the implementing {@code UnitMappable} instance.
 * @see Mappable
 */
public interface UnitMappable<U extends ConfigurableUnit, C extends Collection<U>, UM extends UnitMappable<U, C, UM>> extends Mappable<U, C, UM> {

    /**
     * Creates a new {@code UnitMappable.Set} instance from the given map.
     *
     * @param map the map containing integer keys mapping to sets of {@link ConfigurableUnit} objects.
     * @param <U> the type of configurable unit.
     * @return a new {@code UnitMappable.Set} instance populated with the provided map.
     */
    static <U extends ConfigurableUnit> UnitMappable.Set<U> asSet(Map<Integer, java.util.Set<U>> map) {
        return new MapUtils.AbstractUnit.SetImpl<>(map);
    }

    /**
     * Creates an empty {@code UnitMappable.Set} instance.
     *
     * @param <U> the type of configurable unit.
     * @return a new empty {@code UnitMappable.Set} instance.
     */
    static <U extends ConfigurableUnit> UnitMappable.Set<U> asSet() {
        return new MapUtils.AbstractUnit.SetImpl<>();
    }

    /**
     * Creates a new {@code UnitMappable.List} instance from the given map.
     *
     * @param map the map containing integer keys mapping to lists of {@link ConfigurableUnit} objects.
     * @param <U> the type of configurable unit.
     * @return a new {@code UnitMappable.List} instance populated with the provided map.
     */
    static <U extends ConfigurableUnit> UnitMappable.List<U> asList(Map<Integer, java.util.List<U>> map) {
        return new MapUtils.AbstractUnit.ListImpl<>(map);
    }

    /**
     * Creates an empty {@code UnitMappable.List} instance.
     *
     * @param <U> the type of configurable unit.
     * @return a new empty {@code UnitMappable.List} instance.
     */
    static <U extends ConfigurableUnit> UnitMappable.List<U> asList() {
        return new MapUtils.AbstractUnit.ListImpl<>();
    }

    /**
     * A sub-interface of {@code UnitMappable} specialized for set-backed collections.
     * <p>
     * This interface extends both {@code UnitMappable} and {@code BaseSet}, providing default
     * methods for retrieving the stored values as a {@link java.util.Set}. It is designed to be
     * directly instantiated via the static factory methods.
     * </p>
     *
     * @param <U> the type of configurable unit stored in the set.
     */
    interface Set<U extends ConfigurableUnit> extends UnitMappable<U, java.util.Set<U>, Set<U>>, BaseSet<U, UnitMappable.Set<U>> {

        /**
         * Converts this set-based mappable into a list of configurable units.
         *
         * @return a {@link List} containing all the stored configurable units.
         */
        @NotNull
        List<U> toList();
    }

    /**
     * A sub-interface of {@code UnitMappable} specialized for list-backed collections.
     * <p>
     * This interface extends both {@code UnitMappable} and {@code BaseList}, providing default
     * methods for retrieving the stored values as a {@link java.util.List}. It is designed to be
     * directly instantiated via the static factory methods.
     * </p>
     *
     * @param <U> the type of configurable unit stored in the list.
     */
    interface List<U extends ConfigurableUnit> extends UnitMappable<U, java.util.List<U>, List<U>>, BaseList<U, UnitMappable.List<U>> {

        /**
         * Converts this list-based mappable into a set of configurable units.
         *
         * @return a {@link Set} containing all the stored configurable units.
         */
        @NotNull
        Set<U> toSet();
    }
}
