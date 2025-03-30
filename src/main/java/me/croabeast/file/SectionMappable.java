package me.croabeast.file;

import me.croabeast.lib.CollectionBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a {@link Mappable} implementation that works with {@link ConfigurationSection}.
 * It allows managing and transforming collections of {@link ConfigurationSection} instances.
 *
 * @param <C> The type of collection containing {@link ConfigurationSection} elements.
 */
public interface SectionMappable<C extends Collection<ConfigurationSection>> extends Mappable<ConfigurationSection, C, SectionMappable<C>> {

    /**
     * Returns this instance of {@link SectionMappable}.
     *
     * @return The current instance.
     */
    @NotNull
    default SectionMappable<C> instance() {
        return this;
    }

    /**
     * Creates a copy of the current {@link SectionMappable}, preserving all elements.
     *
     * @return A new {@link SectionMappable} instance with the same contents.
     */
    @NotNull
    default SectionMappable<C> copy() {
        return of(getSupplier(), this);
    }

    /**
     * Creates a new {@link SectionMappable} instance with the provided supplier and map.
     *
     * @param supplier The supplier for the collection type.
     * @param map      The initial mapping of indices to collections.
     * @param <C>      The type of collection containing {@link ConfigurationSection} elements.
     * @return A new {@link SectionMappable} instance populated with the provided map.
     */
    static <C extends Collection<ConfigurationSection>> SectionMappable<C> of(Supplier<C> supplier, Map<Integer, C> map) {
        SectionMappable<C> before = new MapUtils.SectionImpl<>(supplier);
        before.putAll(map);
        return before;
    }

    /**
     * Creates a new empty {@link SectionMappable} instance using the provided collection supplier.
     *
     * @param supplier The supplier for the collection type.
     * @param <C>      The type of collection containing {@link ConfigurationSection} elements.
     * @return A new empty {@link SectionMappable} instance.
     */
    static <C extends Collection<ConfigurationSection>> SectionMappable<C> of(Supplier<C> supplier) {
        return new MapUtils.SectionImpl<>(supplier);
    }

    /**
     * Converts a map of indexed {@link ConfigurationSection} sets into a {@link SectionMappable.Set} instance.
     *
     * @param map The input map containing indexed sets of {@link ConfigurationSection}.
     * @return A {@link SectionMappable.Set} containing all mapped values.
     */
    static Set asSet(Map<Integer, java.util.Set<ConfigurationSection>> map) {
        Set set = new MapUtils.SectionImpl.Set();
        set.putAll(map);
        return set;
    }

    /**
     * Creates an empty {@link SectionMappable.Set} instance.
     *
     * @return A new empty {@link SectionMappable.Set}.
     */
    static Set asSet() {
        return new MapUtils.SectionImpl.Set();
    }

    /**
     * Converts a map of indexed {@link ConfigurationSection} lists into a {@link SectionMappable.List} instance.
     *
     * @param map The input map containing indexed lists of {@link ConfigurationSection}.
     * @return A {@link SectionMappable.List} containing all mapped values.
     */
    static List asList(Map<Integer, java.util.List<ConfigurationSection>> map) {
        List set = new MapUtils.SectionImpl.List();
        set.putAll(map);
        return set;
    }

    /**
     * Creates an empty {@link SectionMappable.List} instance.
     *
     * @return A new empty {@link SectionMappable.List}.
     */
    static List asList() {
        return new MapUtils.SectionImpl.List();
    }

    /**
     * Represents a {@link SectionMappable} implementation backed by a {@link java.util.Set}.
     * Provides additional utilities to transform the contained {@link ConfigurationSection} elements into configurable units.
     */
    interface Set extends SectionMappable<java.util.Set<ConfigurationSection>> {

        /**
         * Returns this instance of {@link SectionMappable.Set}.
         *
         * @return The current instance.
         */
        @NotNull
        default SectionMappable.Set instance() {
            return this;
        }

        /**
         * Creates a copy of the current {@link SectionMappable.Set}, preserving all elements.
         *
         * @return A new {@link SectionMappable.Set} instance with the same contents.
         */
        @NotNull
        default SectionMappable.Set copy() {
            return asSet(this);
        }

        /**
         * Transforms the current {@link ConfigurationSection} elements into a {@link UnitMappable.Set}
         * using the provided function.
         *
         * @param function The transformation function mapping {@link ConfigurationSection} to a {@link ConfigurableUnit}.
         * @param <U>      The type of resulting {@link ConfigurableUnit}.
         * @return A {@link UnitMappable.Set} containing transformed elements.
         */
        default <U extends ConfigurableUnit> UnitMappable.Set<U> toUnits(Function<ConfigurationSection, U> function) {
            Objects.requireNonNull(function);
            Map<Integer, java.util.Set<U>> map = new HashMap<>();
            forEach((k, v) -> map.put(k, CollectionBuilder.of(v).map(function).toSet()));
            return UnitMappable.asSet(map);
        }
    }

    /**
     * Represents a {@link SectionMappable} implementation backed by a {@link java.util.List}.
     * Provides additional utilities to transform the contained {@link ConfigurationSection} elements into configurable units.
     */
    interface List extends SectionMappable<java.util.List<ConfigurationSection>> {

        /**
         * Returns this instance of {@link SectionMappable.List}.
         *
         * @return The current instance.
         */
        @NotNull
        default SectionMappable.List instance() {
            return this;
        }

        /**
         * Creates a copy of the current {@link SectionMappable.List}, preserving all elements.
         *
         * @return A new {@link SectionMappable.List} instance with the same contents.
         */
        @NotNull
        default SectionMappable.List copy() {
            return asList(this);
        }

        /**
         * Transforms the current {@link ConfigurationSection} elements into a {@link UnitMappable.List}
         * using the provided function.
         *
         * @param function The transformation function mapping {@link ConfigurationSection} to a {@link ConfigurableUnit}.
         * @param <U>      The type of resulting {@link ConfigurableUnit}.
         * @return A {@link UnitMappable.List} containing transformed elements.
         */
        default <U extends ConfigurableUnit> UnitMappable.List<U> toUnits(Function<ConfigurationSection, U> function) {
            Objects.requireNonNull(function);
            Map<Integer, java.util.List<U>> map = new HashMap<>();
            forEach((k, v) -> map.put(k, CollectionBuilder.of(v).map(function).toList()));
            return UnitMappable.asList(map);
        }
    }
}
