package me.croabeast.lib.file;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility class providing helper methods and inner classes to handle mappings
 * of configuration sections and configurable units.
 *
 * <p> This class supports the creation and manipulation of maps that store sets of
 * configuration sections or units, organized by integer keys.
 */
@UtilityClass
final class MapUtils {

    static class BaseMapImpl<T> implements Mappable<T> {

        final Map<Integer, Set<T>> map = new TreeMap<>();

        BaseMapImpl(Map<Integer, Set<T>> map) {
            putAll(map);
        }

        @Override
        public Mappable<T> filter(Predicate<T> predicate) {
            map.values().forEach(c -> c.removeIf(predicate.negate()));
            return this;
        }

        @Override
        public Mappable<T> order(Comparator<Integer> comparator) {
            Map<Integer, Set<T>> units = new TreeMap<>(comparator);
            units.putAll(this);

            clear();
            putAll(units);
            return this;
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
        public Set<T> get(Object key) {
            return map.get(key);
        }

        @Nullable
        @Override
        public Set<T> put(Integer key, Set<T> value) {
            return map.put(key, value);
        }

        @Override
        public Set<T> remove(Object key) {
            return map.remove(key);
        }

        @Override
        public void putAll(@NotNull Map<? extends Integer, ? extends Set<T>> m) {
            map.putAll(m);
        }

        @Override
        public void clear() {
            map.clear();
        }

        @NotNull
        public Set<Integer> keySet() {
            return map.keySet();
        }

        @NotNull
        public Collection<Set<T>> values() {
            return map.values();
        }

        @NotNull
        public Set<Entry<Integer, Set<T>>> entrySet() {
            return map.entrySet();
        }

        @Override
        public Set<T> getOrDefault(Object key, Set<T> defaultValue) {
            return map.getOrDefault(key, defaultValue);
        }

        @Override
        public void forEach(BiConsumer<? super Integer, ? super Set<T>> action) {
            map.forEach(action);
        }

        @Override
        public void replaceAll(BiFunction<? super Integer, ? super Set<T>, ? extends Set<T>> function) {
            map.replaceAll(function);
        }

        @Override
        public Set<T> putIfAbsent(Integer key, Set<T> value) {
            return map.putIfAbsent(key, value);
        }

        @Override
        public boolean remove(Object key, Object value) {
            return map.remove(key, value);
        }

        @Override
        public boolean replace(Integer key, Set<T> oldValue, Set<T> newValue) {
            return map.replace(key, oldValue, newValue);
        }

        @Override
        public Set<T> replace(Integer key, Set<T> value) {
            return map.replace(key, value);
        }

        @Override
        public Set<T> computeIfAbsent(Integer key, @NotNull Function<? super Integer, ? extends Set<T>> mappingFunction) {
            return map.computeIfAbsent(key, mappingFunction);
        }

        @Override
        public Set<T> computeIfPresent(Integer key, @NotNull BiFunction<? super Integer, ? super Set<T>, ? extends Set<T>> remappingFunction) {
            return map.computeIfPresent(key, remappingFunction);
        }

        @Override
        public Set<T> compute(Integer key, @NotNull BiFunction<? super Integer, ? super Set<T>, ? extends Set<T>> remappingFunction) {
            return map.compute(key, remappingFunction);
        }

        @Override
        public Set<T> merge(Integer key, @NotNull Set<T> value, @NotNull BiFunction<? super Set<T>, ? super Set<T>, ? extends Set<T>> remappingFunction) {
            return map.merge(key, value, remappingFunction);
        }
    }

    static class SectionMapImpl extends BaseMapImpl<ConfigurationSection> implements SectionMappable {

        SectionMapImpl(Map<Integer, Set<ConfigurationSection>> map) {
            super(map);
        }

        @Override
        public SectionMappable filter(Predicate<ConfigurationSection> predicate) {
            map.values().forEach(c -> c.removeIf(predicate.negate()));
            return this;
        }

        @Override
        public SectionMappable order(Comparator<Integer> comparator) {
            Map<Integer, Set<ConfigurationSection>> units = new TreeMap<>(comparator);
            units.putAll(this);

            clear();
            putAll(units);
            return this;
        }
    }

    static class UnitMapImpl<U extends ConfigurableUnit> extends BaseMapImpl<U> implements UnitMappable<U> {
        UnitMapImpl(Map<Integer, Set<U>> map) {
            super(map);
        }

        @Override
        public UnitMappable<U> filter(Predicate<U> predicate) {
            map.values().forEach(c -> c.removeIf(predicate.negate()));
            return this;
        }

        @Override
        public UnitMappable<U> order(Comparator<Integer> comparator) {
            Map<Integer, Set<U>> units = new TreeMap<>(comparator);
            units.putAll(this);

            clear();
            putAll(units);
            return this;
        }

    }
}
