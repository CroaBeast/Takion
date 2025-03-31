package me.croabeast.file;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

/**
 * Represents a specialized {@link Mappable} for mapping integer keys to collections of
 * {@link ConfigurationSection} objects.
 * <p>
 * {@code SectionMappable} provides a framework for organizing configuration sections by numeric keys,
 * along with utility methods to convert the stored values into more specific data structures such as sets
 * or lists. It is designed to be implemented by a concrete class to ensure full functionality, such as
 * correctly handling storage and retrieval of configuration sections.
 * </p>
 * <p>
 * <strong>Note:</strong> While the base {@code SectionMappable} interface must be implemented for full
 * functionality, its nested sub-interfaces {@code SectionMappable.Set} and {@code SectionMappable.List} do not
 * require separate implementations. They can be directly instantiated using the provided static factory methods
 * {@link #asSet(Map)} and {@link #asList(Map)}, which return ready-to-use instances.
 * </p>
 *
 * @param <C> the type of collection that holds {@link ConfigurationSection} elements
 * @param <S> the type of the implementing {@code SectionMappable} instance
 */
public interface SectionMappable<C extends Collection<ConfigurationSection>, S extends SectionMappable<C, S>> extends
        Mappable<ConfigurationSection, C, S> {

    /**
     * Creates a {@code SectionMappable.Set} instance from the given map.
     *
     * @param map a map with integer keys and sets of {@link ConfigurationSection} as values
     * @return a new instance of {@code SectionMappable.Set} populated with the given map
     */
    static SectionMappable.Set asSet(Map<Integer, java.util.Set<ConfigurationSection>> map) {
        return new MapUtils.AbstractSection.SetImpl(map);
    }

    /**
     * Creates an empty {@code SectionMappable.Set} instance.
     *
     * @return a new empty instance of {@code SectionMappable.Set}
     */
    static SectionMappable.Set asSet() {
        return new MapUtils.AbstractSection.SetImpl();
    }

    /**
     * Creates a {@code SectionMappable.List} instance from the given map.
     *
     * @param map a map with integer keys and lists of {@link ConfigurationSection} as values
     * @return a new instance of {@code SectionMappable.List} populated with the given map
     */
    static SectionMappable.List asList(Map<Integer, java.util.List<ConfigurationSection>> map) {
        return new MapUtils.AbstractSection.ListImpl(map);
    }

    /**
     * Creates an empty {@code SectionMappable.List} instance.
     *
     * @return a new empty instance of {@code SectionMappable.List}
     */
    static SectionMappable.List asList() {
        return new MapUtils.AbstractSection.ListImpl();
    }

    /**
     * Represents a {@code SectionMappable} implementation backed by a {@link java.util.Set}.
     * <p>
     * This sub-interface extends both {@link SectionMappable} and {@link BaseSet} to provide default methods
     * for converting and transforming configuration sections into sets. No additional implementation is required
     * beyond instantiating it via the static methods.
     * </p>
     */
    interface Set extends
            SectionMappable<java.util.Set<ConfigurationSection>, Set>, BaseSet<ConfigurationSection, Set>
    {
        /**
         * Transforms the contained {@link ConfigurationSection} elements into a {@link UnitMappable.Set}
         * by applying the provided function.
         *
         * @param function the function to transform each configuration section into a {@link ConfigurableUnit}
         * @param <U>      the type of the resulting {@link ConfigurableUnit}
         * @return a {@link UnitMappable.Set} containing the transformed elements.
         */
        @NotNull
        <U extends ConfigurableUnit> UnitMappable.Set<U> toUnits(Function<ConfigurationSection, U> function);

        /**
         * Converts this set-based mappable into a list-based mappable.
         *
         * @return a {@link List} representation of this mappable.
         */
        @NotNull
        List toList();
    }

    /**
     * Represents a {@code SectionMappable} implementation backed by a {@link java.util.List}.
     * <p>
     * This sub-interface extends both {@link SectionMappable} and {@link BaseList} to provide default methods
     * for converting and transforming configuration sections into lists. No additional implementation is required
     * beyond instantiating it via the static methods.
     * </p>
     */
    interface List extends
            SectionMappable<java.util.List<ConfigurationSection>, List>, BaseList<ConfigurationSection, List>
    {
        /**
         * Transforms the contained {@link ConfigurationSection} elements into a {@link UnitMappable.List}
         * by applying the provided function.
         *
         * @param function the function to transform each configuration section into a {@link ConfigurableUnit}
         * @param <U>      the type of the resulting {@link ConfigurableUnit}
         * @return a {@link UnitMappable.List} containing the transformed elements.
         */
        @NotNull
        <U extends ConfigurableUnit> UnitMappable.List<U> toUnits(Function<ConfigurationSection, U> function);

        /**
         * Converts this list-based mappable into a set-based mappable.
         *
         * @return a {@link Set} representation of this mappable.
         */
        @NotNull
        Set toSet();
    }
}
