package me.croabeast.file;

import me.croabeast.lib.CollectionBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a {@link Mappable} implementation that works with {@link ConfigurationSection}.
 * <p>
 * A SectionMappable is designed to manage and transform collections of {@link ConfigurationSection} objects,
 * typically used to organize configuration data. It provides methods to convert its underlying map into
 * a {@code Set} or {@code List} representation and offers utilities to transform the stored sections into
 * configurable units via the {@link UnitMappable} interface.
 * </p>
 *
 * @param <C> The type of collection that holds {@link ConfigurationSection} elements.
 */
public interface SectionMappable<C extends Collection<ConfigurationSection>> extends Mappable<ConfigurationSection, C, SectionMappable<C>> {

    /**
     * Returns this instance of {@link SectionMappable}.
     *
     * @return the current instance.
     */
    @NotNull
    default SectionMappable<C> instance() {
        return this;
    }

    /**
     * Creates a copy of the current {@link SectionMappable}, preserving all its entries.
     *
     * @return a new {@link SectionMappable} instance with the same contents.
     */
    @NotNull
    default SectionMappable<C> copy() {
        return of(getSupplier(), this);
    }

    /**
     * Converts this SectionMappable into a Set-based representation.
     * <p>
     * Each entry in this mappable is converted so that its collection is transformed into a {@link Set}
     * of {@link ConfigurationSection} elements.
     * </p>
     *
     * @return a {@link SectionMappable.Set} representing the same mappings as a map of {@code Map<Integer, Set<ConfigurationSection>>}.
     */
    @NotNull
    default Set toSet() {
        Map<Integer, java.util.Set<ConfigurationSection>> map = new LinkedHashMap<>();
        forEach((k, v) -> map.put(k, CollectionBuilder.of(v).toSet()));
        return asSet(map);
    }

    /**
     * Converts this SectionMappable into a List-based representation.
     * <p>
     * Each entry in this mappable is converted so that its collection is transformed into a {@link List}
     * of {@link ConfigurationSection} elements.
     * </p>
     *
     * @return a {@link SectionMappable.List} representing the same mappings as a map of {@code Map<Integer, List<ConfigurationSection>>}.
     */
    @NotNull
    default List toList() {
        Map<Integer, java.util.List<ConfigurationSection>> map = new LinkedHashMap<>();
        forEach((k, v) -> map.put(k, CollectionBuilder.of(v).toList()));
        return asList(map);
    }

    /**
     * Creates a new {@link SectionMappable} instance with the provided collection supplier and map.
     *
     * @param supplier the supplier for the collection type.
     * @param map      the initial mapping of indices to collections.
     * @param <C>      the type of collection containing {@link ConfigurationSection} elements.
     * @return a new {@link SectionMappable} instance populated with the provided map.
     */
    static <C extends Collection<ConfigurationSection>> SectionMappable<C> of(Supplier<C> supplier, Map<Integer, C> map) {
        SectionMappable<C> before = new MapUtils.SectionImpl<>(supplier);
        before.putAll(map);
        return before;
    }

    /**
     * Creates a new empty {@link SectionMappable} instance using the provided collection supplier.
     *
     * @param supplier the supplier for the collection type.
     * @param <C>      the type of collection containing {@link ConfigurationSection} elements.
     * @return a new empty {@link SectionMappable} instance.
     */
    static <C extends Collection<ConfigurationSection>> SectionMappable<C> of(Supplier<C> supplier) {
        return new MapUtils.SectionImpl<>(supplier);
    }

    /**
     * Converts a map of indexed sets of {@link ConfigurationSection} into a {@link SectionMappable.Set} instance.
     *
     * @param map the input map with integer keys mapping to sets of {@link ConfigurationSection} objects.
     * @return a {@link SectionMappable.Set} representing the provided map.
     */
    static Set asSet(Map<Integer, java.util.Set<ConfigurationSection>> map) {
        Set set = new MapUtils.SectionImpl.Set();
        set.putAll(map);
        return set;
    }

    /**
     * Creates an empty {@link SectionMappable.Set} instance.
     *
     * @return a new empty {@link SectionMappable.Set}.
     */
    static Set asSet() {
        return new MapUtils.SectionImpl.Set();
    }

    /**
     * Converts a map of indexed lists of {@link ConfigurationSection} into a {@link SectionMappable.List} instance.
     *
     * @param map the input map with integer keys mapping to lists of {@link ConfigurationSection} objects.
     * @return a {@link SectionMappable.List} representing the provided map.
     */
    static List asList(Map<Integer, java.util.List<ConfigurationSection>> map) {
        List set = new MapUtils.SectionImpl.List();
        set.putAll(map);
        return set;
    }

    /**
     * Creates an empty {@link SectionMappable.List} instance.
     *
     * @return a new empty {@link SectionMappable.List}.
     */
    static List asList() {
        return new MapUtils.SectionImpl.List();
    }

    /**
     * Represents a {@link SectionMappable} implementation backed by a {@link java.util.Set}.
     * <p>
     * This sub-interface provides additional utility methods specific to set-based mappings.
     * It allows copying the current set mapping and transforming the stored {@link ConfigurationSection}
     * objects into configurable units via {@link UnitMappable}.
     * </p>
     */
    interface Set extends SectionMappable<java.util.Set<ConfigurationSection>> {

        /**
         * Returns this instance of {@link SectionMappable.Set}.
         *
         * @return the current instance.
         */
        @NotNull
        default SectionMappable.Set instance() {
            return this;
        }

        /**
         * Creates a copy of the current {@link SectionMappable.Set}, preserving all its mappings.
         *
         * @return a new {@link SectionMappable.Set} instance with identical contents.
         */
        @NotNull
        default SectionMappable.Set copy() {
            return asSet(this);
        }

        /**
         * Transforms the contained {@link ConfigurationSection} elements into a {@link UnitMappable.Set}
         * by applying the provided function.
         *
         * @param function the transformation function mapping a {@link ConfigurationSection} to a {@link ConfigurableUnit}
         * @param <U>      the type of the resulting {@link ConfigurableUnit}
         * @return a {@link UnitMappable.Set} containing the transformed elements
         */
        default <U extends ConfigurableUnit> UnitMappable.Set<U> toUnits(Function<ConfigurationSection, U> function) {
            Objects.requireNonNull(function);
            Map<Integer, java.util.Set<U>> map = new HashMap<>();
            forEach((k, v) -> map.put(k, CollectionBuilder.of(v).map(function).toSet()));
            return UnitMappable.asSet(map);
        }

        /**
         * Convenience method to return this instance as a set.
         *
         * @return this instance.
         */
        @NotNull
        default SectionMappable.Set toSet() {
            return instance();
        }
    }

    /**
     * Represents a {@link SectionMappable} implementation backed by a {@link java.util.List}.
     * <p>
     * This sub-interface provides additional utility methods specific to list-based mappings.
     * It allows copying the current list mapping and transforming the stored {@link ConfigurationSection}
     * objects into configurable units via {@link UnitMappable}.
     * </p>
     */
    interface List extends SectionMappable<java.util.List<ConfigurationSection>> {

        /**
         * Returns this instance of {@link SectionMappable.List}.
         *
         * @return the current instance.
         */
        @NotNull
        default SectionMappable.List instance() {
            return this;
        }

        /**
         * Creates a copy of the current {@link SectionMappable.List}, preserving all its mappings.
         *
         * @return a new {@link SectionMappable.List} instance with identical contents.
         */
        @NotNull
        default SectionMappable.List copy() {
            return asList(this);
        }

        /**
         * Transforms the contained {@link ConfigurationSection} elements into a {@link UnitMappable.List}
         * by applying the provided function.
         *
         * @param function the transformation function mapping a {@link ConfigurationSection} to a {@link ConfigurableUnit}
         * @param <U>      the type of the resulting {@link ConfigurableUnit}
         * @return a {@link UnitMappable.List} containing the transformed elements
         */
        default <U extends ConfigurableUnit> UnitMappable.List<U> toUnits(Function<ConfigurationSection, U> function) {
            Objects.requireNonNull(function);
            Map<Integer, java.util.List<U>> map = new HashMap<>();
            forEach((k, v) -> map.put(k, CollectionBuilder.of(v).map(function).toList()));
            return UnitMappable.asList(map);
        }

        /**
         * Convenience method to return this instance as a list.
         *
         * @return this instance.
         */
        @NotNull
        default SectionMappable.List toList() {
            return instance();
        }
    }
}
