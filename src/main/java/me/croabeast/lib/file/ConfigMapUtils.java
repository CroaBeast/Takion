package me.croabeast.lib.file;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Utility class providing helper methods and inner classes to handle mappings
 * of configuration sections and configurable units.
 *
 * <p> This class supports the creation and manipulation of maps that store sets of
 * configuration sections or units, organized by integer keys.
 */
@UtilityClass
final class ConfigMapUtils {

    /**
     * Base interface representing a map of integer keys to sets of values.
     * This interface extends the Map interface and is used as a base for more specific
     * mappings, such as SectionMappable and UnitMappable.
     *
     * @param <S> The type of the values in the sets.
     */
    interface BaseMappable<S> extends Map<Integer, Set<S>> {}

    /**
     * Implementation of the BaseMappable interface for generic values.
     * This class wraps a standard map and provides additional methods to handle
     * sets of values.
     *
     * @param <U> The type of the values in the sets.
     */
    private static class BaseMapImpl<U> implements BaseMappable<U> {

        final Map<Integer, Set<U>> map;

        BaseMapImpl(Map<Integer, Set<U>> map) {
            this.map = map;
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return map.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }

        @Override
        public Set<U> get(Object key) {
            return map.get(key);
        }

        @Nullable
        @Override
        public Set<U> put(Integer key, Set<U> value) {
            return map.put(key, value);
        }

        @Override
        public Set<U> remove(Object key) {
            return map.remove(key);
        }

        @Override
        public void putAll(@NotNull Map<? extends Integer, ? extends Set<U>> m) {
            map.putAll(m);
        }

        @Override
        public void clear() {
            map.clear();
        }

        @NotNull
        @Override
        public Set<Integer> keySet() {
            return map.keySet();
        }

        @NotNull
        @Override
        public Collection<Set<U>> values() {
            return map.values();
        }

        @NotNull
        @Override
        public Set<Entry<Integer, Set<U>>> entrySet() {
            return map.entrySet();
        }

        @Override
        public Set<U> getOrDefault(Object key, Set<U> defaultValue) {
            return map.getOrDefault(key, defaultValue);
        }

        @Override
        public void forEach(BiConsumer<? super Integer, ? super Set<U>> action) {
            map.forEach(action);
        }

        @Override
        public void replaceAll(BiFunction<? super Integer, ? super Set<U>, ? extends Set<U>> function) {
            map.replaceAll(function);
        }

        @Override
        public Set<U> putIfAbsent(Integer key, Set<U> value) {
            return map.putIfAbsent(key, value);
        }

        @Override
        public boolean remove(Object key, Object value) {
            return map.remove(key, value);
        }

        @Override
        public boolean replace(Integer key, Set<U> oldValue, Set<U> newValue) {
            return map.replace(key, oldValue, newValue);
        }

        @Override
        public Set<U> replace(Integer key, Set<U> value) {
            return map.replace(key, value);
        }

        @Override
        public Set<U> computeIfAbsent(Integer key, @NotNull Function<? super Integer, ? extends Set<U>> mappingFunction) {
            return map.computeIfAbsent(key, mappingFunction);
        }

        @Override
        public Set<U> computeIfPresent(Integer key, @NotNull BiFunction<? super Integer, ? super Set<U>, ? extends Set<U>> remappingFunction) {
            return map.computeIfPresent(key, remappingFunction);
        }

        @Override
        public Set<U> compute(Integer key, @NotNull BiFunction<? super Integer, ? super Set<U>, ? extends Set<U>> remappingFunction) {
            return map.compute(key, remappingFunction);
        }

        @Override
        public Set<U> merge(Integer key, @NotNull Set<U> value, @NotNull BiFunction<? super Set<U>, ? super Set<U>, ? extends Set<U>> remappingFunction) {
            return map.merge(key, value, remappingFunction);
        }
    }

    /**
     * Implementation of the BaseMappable interface specifically for ConfigurationSection values.
     * This class wraps a standard map and provides additional methods to handle sets of configuration sections.
     */
    private static class SectionMapImpl extends BaseMapImpl<ConfigurationSection> implements SectionMappable {
        SectionMapImpl(Map<Integer, Set<ConfigurationSection>> map) {
            super(map);
        }
    }

    /**
     * Implementation of the BaseMappable interface specifically for ConfigurableUnit values.
     * This class wraps a standard map and provides additional methods to handle sets of configurable units.
     *
     * @param <U> The type of the ConfigurableUnit.
     */
    private static class UnitMapImpl<U extends ConfigurableUnit> extends BaseMapImpl<U> implements UnitMappable<U> {
        UnitMapImpl(Map<Integer, Set<U>> map) {
            super(map);
        }
    }

    /**
     * Creates a SectionMappable instance from the provided map.
     * This method is used to wrap a standard map into a SectionMappable interface.
     *
     * @param map The map to create the SectionMappable from.
     * @return A SectionMappable instance.
     */
    SectionMappable simple(Map<Integer, Set<ConfigurationSection>> map) {
        return new SectionMapImpl(map);
    }

    /**
     * Creates a UnitMappable instance from the provided map.
     * This method is used to wrap a standard map into a UnitMappable interface,
     * allowing for more specialized handling of configurable units.
     *
     * @param map The map to create the UnitMappable from.
     * @param <U> The type of the ConfigurableUnit.
     * @return A UnitMappable instance.
     */
    <U extends ConfigurableUnit> UnitMappable<U> unit(Map<Integer, Set<U>> map) {
        return new UnitMapImpl<>(map);
    }
}
