package me.croabeast.file;

import me.croabeast.lib.CollectionBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

/**
 * Utility class containing helper implementations for the {@code Mappable} interface.
 * <p>
 * {@code MapUtils} provides abstract base classes and concrete implementations for mapping
 * integer keys to collections of elements. These implementations support advanced operations
 * like filtering, ordering, and transforming stored values.
 * </p>
 * <p>
 * There are two primary groups of implementations:
 * </p>
 * <ul>
 *   <li>
 *     <b>Section-based mappables:</b> for mapping {@link ConfigurationSection} instances,
 *     represented by {@link AbstractSection} and its nested classes {@code SetImpl} and {@code ListImpl}.
 *   </li>
 *   <li>
 *     <b>Unit-based mappables:</b> for mapping configurable units (instances of {@link ConfigurableUnit}),
 *     represented by {@link AbstractUnit} and its nested classes {@code SetImpl} and {@code ListImpl}.
 *   </li>
 * </ul>
 */
final class MapUtils {

    /**
     * An abstract implementation of {@link Mappable} that uses a {@link LinkedHashMap}
     * to store mappings from integer keys to collections of elements.
     *
     * @param <T> The type of elements stored in the collections.
     * @param <C> The type of collection that holds the elements.
     * @param <M> The type of the implementing {@code Mappable} instance.
     */
    static abstract class AbstractMappable<T, C extends Collection<T>, M extends Mappable<T, C, M>> extends LinkedHashMap<Integer, C> implements Mappable<T, C, M> {

        /**
         * Creates an empty {@code AbstractMappable}.
         */
        AbstractMappable() {}

        /**
         * Creates a new {@code AbstractMappable} instance and populates it with the given map.
         *
         * @param map the initial map data; if {@code null} or empty, the mappable will be empty.
         */
        AbstractMappable(Map<Integer, ? extends C> map) {
            if (map != null && !map.isEmpty()) putAll(map);
        }
    }

    /**
     * An abstract implementation of {@link SectionMappable} for {@link ConfigurationSection} elements.
     * <p>
     * This class extends {@link AbstractMappable} to provide a mappable structure where each key maps
     * to a collection of {@link ConfigurationSection} objects.
     * </p>
     *
     * @param <C> The type of collection containing {@link ConfigurationSection} elements.
     * @param <S> The type of the implementing {@code SectionMappable} instance.
     */
    static abstract class AbstractSection<C extends Collection<ConfigurationSection>, S extends SectionMappable<C, S>> extends
            AbstractMappable<ConfigurationSection, C, S> implements SectionMappable<C, S> {

        /**
         * Creates an empty {@code AbstractSection}.
         */
        AbstractSection() {}

        /**
         * Creates a new {@code AbstractSection} instance populated with the provided map.
         *
         * @param map the initial map data.
         */
        AbstractSection(Map<Integer, ? extends C> map) {
            super(map);
        }

        /**
         * A concrete implementation of {@link SectionMappable} backed by a {@link java.util.Set}.
         */
        static final class SetImpl extends AbstractSection<java.util.Set<ConfigurationSection>, Set> implements SectionMappable.Set {

            /**
             * Creates an empty {@code SetImpl} instance.
             */
            SetImpl() {}

            /**
             * Creates a {@code SetImpl} instance populated with the provided map.
             *
             * @param map the initial map data mapping integers to sets of {@link ConfigurationSection}.
             */
            SetImpl(Map<Integer, java.util.Set<ConfigurationSection>> map) {
                super(map);
            }

            /**
             * Transforms the contained {@link ConfigurationSection} elements into a {@link UnitMappable.Set}
             * using the provided function.
             *
             * @param function the transformation function mapping a {@link ConfigurationSection} to a {@link ConfigurableUnit}.
             * @param <U>      the type of resulting {@link ConfigurableUnit}.
             * @return a {@link UnitMappable.Set} containing the transformed elements.
             */
            @NotNull
            public <U extends ConfigurableUnit> UnitMappable.Set<U> toUnits(Function<ConfigurationSection, U> function) {
                Objects.requireNonNull(function);
                Map<Integer, java.util.Set<U>> map = new LinkedHashMap<>();
                forEach((k, v) -> map.put(k, CollectionBuilder.of(v).map(function).toSet()));
                return new AbstractUnit.SetImpl<>(map);
            }

            /**
             * Converts this set-based mappable into a list-based mappable.
             *
             * @return a {@link SectionMappable.List} with equivalent contents.
             */
            @NotNull
            public List toList() {
                ListImpl list = new ListImpl();
                forEach((k, v) -> list.put(k, CollectionBuilder.of(v).toList()));
                return new ListImpl(list);
            }

            /**
             * Orders the entries of this mappable based on the given comparator.
             *
             * @param comparator the comparator for ordering keys.
             * @return a new {@code SetImpl} instance with entries ordered as specified.
             */
            @NotNull
            public Set order(Comparator<Integer> comparator) {
                TreeMap<Integer, java.util.Set<ConfigurationSection>> map = new TreeMap<>(comparator);
                map.putAll(this);
                return new SetImpl(map);
            }

            /**
             * Creates a copy of this set-based mappable.
             *
             * @return a new {@code SetImpl} instance with the same mappings.
             */
            @NotNull
            public Set copy() {
                return new SetImpl(this);
            }

            /**
             * Returns this instance.
             *
             * @return this {@code SectionMappable.Set} instance.
             */
            @NotNull
            public Set instance() {
                return this;
            }
        }

        /**
         * A concrete implementation of {@link SectionMappable} backed by a {@link java.util.List}.
         */
        static final class ListImpl extends AbstractSection<java.util.List<ConfigurationSection>, List> implements SectionMappable.List {

            /**
             * Creates an empty {@code ListImpl} instance.
             */
            ListImpl() {}

            /**
             * Creates a {@code ListImpl} instance populated with the provided map.
             *
             * @param map the initial map data mapping integers to lists of {@link ConfigurationSection}.
             */
            ListImpl(Map<Integer, java.util.List<ConfigurationSection>> map) {
                super(map);
            }

            /**
             * Transforms the contained {@link ConfigurationSection} elements into a {@link UnitMappable.List}
             * using the provided function.
             *
             * @param function the transformation function mapping a {@link ConfigurationSection} to a {@link ConfigurableUnit}.
             * @param <U>      the type of resulting {@link ConfigurableUnit}.
             * @return a {@link UnitMappable.List} containing the transformed elements.
             */
            @NotNull
            public <U extends ConfigurableUnit> UnitMappable.List<U> toUnits(Function<ConfigurationSection, U> function) {
                Objects.requireNonNull(function);
                Map<Integer, java.util.List<U>> map = new LinkedHashMap<>();
                forEach((k, v) -> map.put(k, CollectionBuilder.of(v).map(function).toList()));
                return new AbstractUnit.ListImpl<>(map);
            }

            /**
             * Converts this list-based mappable into a set-based mappable.
             *
             * @return a {@link SectionMappable.Set} with equivalent contents.
             */
            @NotNull
            public Set toSet() {
                SetImpl list = new SetImpl();
                forEach((k, v) -> list.put(k, CollectionBuilder.of(v).toSet()));
                return new SetImpl(list);
            }

            /**
             * Orders the entries of this mappable based on the given comparator.
             *
             * @param comparator the comparator for ordering keys.
             * @return a new {@code ListImpl} instance with entries ordered as specified.
             */
            @NotNull
            public List order(Comparator<Integer> comparator) {
                TreeMap<Integer, java.util.List<ConfigurationSection>> map = new TreeMap<>(comparator);
                map.putAll(this);
                return new ListImpl(map);
            }

            /**
             * Creates a copy of this list-based mappable.
             *
             * @return a new {@code ListImpl} instance with the same mappings.
             */
            @NotNull
            public List copy() {
                return new ListImpl(this);
            }

            /**
             * Returns this instance.
             *
             * @return this {@code SectionMappable.List} instance.
             */
            @NotNull
            public List instance() {
                return this;
            }
        }
    }

    /**
     * An abstract implementation of {@link UnitMappable} for configurable units.
     * <p>
     * {@code AbstractUnit} extends {@link AbstractMappable} to map integer keys to collections of configurable units,
     * and implements {@link UnitMappable} to provide additional unit-specific functionality.
     * </p>
     *
     * @param <U>  The type of configurable unit.
     * @param <C>  The type of collection that holds the configurable units.
     * @param <UM> The type of the implementing {@code UnitMappable} instance.
     */
    static abstract class AbstractUnit<U extends ConfigurableUnit, C extends Collection<U>, UM extends UnitMappable<U, C, UM>> extends
            AbstractMappable<U, C, UM> implements UnitMappable<U, C, UM> {

        /**
         * Creates an empty {@code AbstractUnit}.
         */
        AbstractUnit() {}

        /**
         * Creates a new {@code AbstractUnit} instance populated with the provided map.
         *
         * @param map the initial map data.
         */
        AbstractUnit(Map<Integer, ? extends C> map) {
            super(map);
        }

        /**
         * A concrete implementation of {@link UnitMappable} backed by a {@link java.util.Set}.
         *
         * @param <U> The type of configurable unit.
         */
        static final class SetImpl<U extends ConfigurableUnit> extends AbstractUnit<U, java.util.Set<U>, Set<U>> implements UnitMappable.Set<U> {

            /**
             * Creates an empty {@code SetImpl} instance.
             */
            SetImpl() {}

            /**
             * Creates a {@code SetImpl} instance populated with the provided map.
             *
             * @param map the initial map data mapping integers to sets of configurable units.
             */
            SetImpl(Map<Integer, java.util.Set<U>> map) {
                super(map);
            }

            /**
             * Converts this unit mappable into a list-based unit mappable.
             *
             * @return a {@link UnitMappable.List} containing the same data.
             */
            @NotNull
            public List<U> toList() {
                ListImpl<U> list = new ListImpl<>();
                forEach((k, v) -> list.put(k, CollectionBuilder.of(v).toList()));
                return new ListImpl<>(list);
            }

            /**
             * Orders the entries of this unit mappable based on the given comparator.
             *
             * @param comparator the comparator for ordering keys.
             * @return a new {@code SetImpl} instance with ordered entries.
             */
            @NotNull
            public Set<U> order(Comparator<Integer> comparator) {
                TreeMap<Integer, java.util.Set<U>> map = new TreeMap<>(comparator);
                map.putAll(this);
                return new SetImpl<>(map);
            }

            /**
             * Creates a copy of this unit mappable.
             *
             * @return a new {@code SetImpl} instance with the same data.
             */
            @NotNull
            public Set<U> copy() {
                return new SetImpl<>(this);
            }

            /**
             * Returns this instance.
             *
             * @return this {@code UnitMappable.Set} instance.
             */
            @NotNull
            public Set<U> instance() {
                return this;
            }
        }

        /**
         * A concrete implementation of {@link UnitMappable} backed by a {@link java.util.List}.
         *
         * @param <U> The type of configurable unit.
         */
        static final class ListImpl<U extends ConfigurableUnit> extends AbstractUnit<U, java.util.List<U>, List<U>> implements UnitMappable.List<U> {

            /**
             * Creates an empty {@code ListImpl} instance.
             */
            ListImpl() {}

            /**
             * Creates a {@code ListImpl} instance populated with the provided map.
             *
             * @param map the initial map data mapping integers to lists of configurable units.
             */
            ListImpl(Map<Integer, java.util.List<U>> map) {
                super(map);
            }

            /**
             * Converts this unit mappable into a set-based unit mappable.
             *
             * @return a {@link UnitMappable.Set} containing the same data.
             */
            @NotNull
            public Set<U> toSet() {
                SetImpl<U> list = new SetImpl<>();
                forEach((k, v) -> list.put(k, CollectionBuilder.of(v).toSet()));
                return new SetImpl<>(list);
            }

            /**
             * Orders the entries of this unit mappable based on the given comparator.
             *
             * @param comparator the comparator for ordering keys.
             * @return a new {@code ListImpl} instance with ordered entries.
             */
            @NotNull
            public List<U> order(Comparator<Integer> comparator) {
                TreeMap<Integer, java.util.List<U>> map = new TreeMap<>(comparator);
                map.putAll(this);
                return new ListImpl<>(map);
            }

            /**
             * Creates a copy of this unit mappable.
             *
             * @return a new {@code ListImpl} instance with the same data.
             */
            @NotNull
            public List<U> copy() {
                return new ListImpl<U>(this);
            }

            /**
             * Returns this instance.
             *
             * @return this {@code UnitMappable.List} instance.
             */
            @NotNull
            public List<U> instance() {
                return this;
            }
        }
    }
}
