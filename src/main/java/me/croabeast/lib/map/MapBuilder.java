package me.croabeast.lib.map;

import me.croabeast.lib.CollectionBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A utility class that provides methods for creating and manipulating maps.
 *
 * <p> It uses a {@link LinkedHashMap} as the underlying data structure and supports
 * various operations such as adding, removing, filtering, applying, and mapping
 * keys and values.
 *
 * @param <K> the type of the keys in the map
 * @param <V> the type of the values in the map
 *
 * @author CroaBeast
 * @version 1.4
 */
public class MapBuilder<K, V> implements Iterable<Entry<K, V>> {

    private final Map<K, V> map = new LinkedHashMap<>();

    /**
     * Creates an empty map builder.
     */
    public MapBuilder() {}

    /**
     * Creates a map builder with the given map as the initial content.
     *
     * @param map the map to copy from, or null if none
     */
    public MapBuilder(Map<? extends K, ? extends V> map) {
        if (map != null) this.map.putAll(map);
    }

    /**
     * Creates a map builder with the given collection of entries as the initial content.
     *
     * @param collection the collection of entries to copy from, or null if none
     */
    public MapBuilder(Collection<Entry<? extends K, ? extends V>> collection) {
        if (collection != null)
            collection.forEach(e -> map.put(e.getKey(), e.getValue()));
    }

    /**
     * Puts a key-value pair into the map builder.
     *
     * @param key the key to put
     * @param value the value to put
     *
     * @return this map builder
     */
    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    /**
     * Puts an entry into the map builder.
     *
     * @param entry the entry to put
     *
     * @return this map builder
     * @throws NullPointerException if the entry is null
     */
    public MapBuilder<K, V> put(Entry<? extends K, ? extends V> entry) {
        return put(entry.getKey(), entry.getValue());
    }

    /**
     * Puts a map entry into the map builder.
     *
     * @param entry the map entry to put
     *
     * @return this map builder
     * @throws NullPointerException if the entry is null
     */
    public MapBuilder<K, V> put(Map.Entry<? extends K, ? extends V> entry) {
        return put(Entry.of(entry));
    }

    public MapBuilder<K, V> putIfAbsent(K key, V value) {
        map.putIfAbsent(key, value);
        return this;
    }

    public MapBuilder<K, V> putIfAbsent(Entry<? extends K, ? extends V> entry) {
        return putIfAbsent(entry.getKey(), entry.getValue());
    }

    public MapBuilder<K, V> putIfAbsent(Map.Entry<? extends K, ? extends V> entry) {
        return putIfAbsent(Entry.of(entry));
    }

    /**
     * Puts all the key-value pairs from the given map into the map builder.
     *
     * @param map the map to copy from
     * @return this map builder
     * @throws NullPointerException if the map is null
     */
    public MapBuilder<K, V> putAll(Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map).forEach(this::put);
        return this;
    }

    /**
     * Removes a key and its associated value from the map builder.
     *
     * @param key the key to remove
     * @return this map builder
     */
    public MapBuilder<K, V> remove(K key) {
        map.remove(key);
        return this;
    }

    /**
     * Removes a key and its associated value from the map builder if the value
     * matches the given value.
     *
     * @param key the key to remove
     * @param value the value to match
     *
     * @return this map builder
     */
    public MapBuilder<K, V> remove(K key, V value) {
        map.remove(key, value);
        return this;
    }

    /**
     * Removes all the occurrences of a key and its associated value from the
     * map builder.
     *
     * @param key the key to remove
     * @param counting the maximum number of occurrences to remove, or -1 for
     *                unlimited
     *
     * @return this map builder
     */
    public MapBuilder<K, V> removeAllByKey(K key, int counting) {
        boolean finite = counting > -1;

        for (Map.Entry<K, V> e : map.entrySet()) {
            K k = e.getKey();

            if (!Objects.equals(k, key)) continue;
            map.remove(k);

            if (finite && counting-- < 0) break;
        }

        return this;
    }

    /**
     * Removes all the occurrences of a value and its associated key from the
     * map builder.
     *
     * @param value the value to remove
     * @param counting the maximum number of occurrences to remove, or -1 for
     *                unlimited
     *
     * @return this map builder
     */
    public MapBuilder<K, V> removeAllByValue(V value, int counting) {
        boolean finite = counting > -1;

        for (Map.Entry<K, V> e : map.entrySet()) {
            if (!Objects.equals(e.getValue(), value))
                continue;

            map.remove(e.getKey());
            if (finite && counting-- < 0) break;
        }

        return this;
    }

    /**
     * Filters the key-value pairs in the map builder by applying a predicate
     * to the keys.
     *
     * <p> Only the pairs that satisfy the predicate are kept in the map builder.
     *
     * @param predicate the predicate to test the keys
     *
     * @return this map builder
     * @throws NullPointerException if the predicate is null
     */
    public MapBuilder<K, V> filterByKey(Predicate<K> predicate) {
        Objects.requireNonNull(predicate);

        map.keySet().removeIf(k -> predicate.negate().test(k));
        return this;
    }

    /**
     * Filters the key-value pairs in the map builder by applying a predicate
     * to the values.
     *
     * <p> Only the pairs that satisfy the predicate are kept in the map builder.
     *
     * @param predicate the predicate to test the values
     *
     * @return this map builder
     * @throws NullPointerException if the predicate is null
     */
    public MapBuilder<K, V> filterByValue(Predicate<V> predicate) {
        Objects.requireNonNull(predicate);

        map.values().removeIf(v -> predicate.negate().test(v));
        return this;
    }

    public MapBuilder<K, V> filter(BiPredicate<K, V> predicate) {
        Objects.requireNonNull(predicate);

        map.entrySet().removeIf(e -> predicate.negate().test(e.getKey(), e.getValue()));
        return this;
    }

    /**
     * Applies a function to the keys in the map builder and returns a new map
     * builder with the transformed keys. The values are unchanged.
     *
     * @param <A> the type of the transformed keys
     * @param function the function to apply to the keys
     *
     * @return a new map builder with the transformed keys
     * @throws NullPointerException if the function is null
     */
    public <A> MapBuilder<A, V> applyByKey(Function<K, A> function) {
        Objects.requireNonNull(function);
        List<Entry<A, V>> entries = new LinkedList<>();

        for (Map.Entry<K, V> entry : map.entrySet()) {
            A key = function.apply(entry.getKey());
            V value = entry.getValue();

            entries.add(Entry.of(key, value));
        }

        MapBuilder<A, V> builder = new MapBuilder<>();
        entries.forEach(builder::put);

        return builder;
    }

    /**
     * Applies a function to the values in the map builder and returns a new map
     * builder with the transformed values. The keys are unchanged.
     *
     * @param <B> the type of the transformed values
     * @param function the function to apply to the values
     *
     * @return a new map builder with the transformed values
     * @throws NullPointerException if the function is null
     */
    public <B> MapBuilder<K, B> applyByValue(Function<V, B> function) {
        Objects.requireNonNull(function);
        List<Entry<K, B>> entries = new LinkedList<>();

        for (Map.Entry<K, V> entry : map.entrySet()) {
            K key = entry.getKey();
            B value = function.apply(entry.getValue());

            entries.add(Entry.of(key, value));
        }

        MapBuilder<K, B> builder = new MapBuilder<>();
        entries.forEach(builder::put);

        return builder;
    }

    /**
     * Applies two functions to the keys and values in the map builder and returns
     * a new map builder with the transformed pairs.
     *
     * @param <A> the type of the transformed keys
     * @param <B> the type of the transformed values
     *
     * @param keyFunction the function to apply to the keys
     * @param valueFunction the function to apply to the values
     *
     * @return a new map builder with the transformed pairs
     * @throws NullPointerException if either function is null
     */
    public <A, B> MapBuilder<A, B> map(Function<K, A> keyFunction, Function<V, B> valueFunction) {
        Objects.requireNonNull(valueFunction);
        Objects.requireNonNull(keyFunction);

        List<Entry<A, B>> entries = new LinkedList<>();

        for (Map.Entry<K, V> entry : map.entrySet())
            entries.add(Entry.of(
                    keyFunction.apply(entry.getKey()),
                    valueFunction.apply(entry.getValue())
            ));

        MapBuilder<A, B> builder = new MapBuilder<>();
        entries.forEach(builder::put);

        return builder;
    }

    /**
     * Checks if the map builder contains a given key.
     *
     * @param key the key to check
     * @return true if the map builder contains the key, false otherwise
     */
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    /**
     * Checks if the map builder contains a given value.
     *
     * @param value the value to check
     * @return true if the map builder contains the value, false otherwise
     */
    public boolean containsValue(V value) {
        return map.containsValue(value);
    }

    /**
     * Gets the value associated with a given key from the map builder, or a default
     * value if the key is not found.
     *
     * @param key the key to look up
     * @param def the default value to return if the key is not found
     *
     * @return the value associated with the key, or the default value
     */
    public V get(K key, V def) {
        return map.getOrDefault(key, def);
    }

    /**
     * Gets the value associated with a given key from the map builder, or null
     * if the key is not found.
     *
     * @param key the key to look up
     * @return the value associated with the key, or null
     */
    public V get(K key) {
        return map.get(key);
    }

    /**
     * Clears the map builder of all the key-value pairs.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Returns the size of the map builder, i.e. the number of key-value pairs.
     *
     * @return the size of the map builder
     */
    public int size() {
        return map.size();
    }

    public K fromValue(V value, K def) {
        for (Map.Entry<K, V> e : map.entrySet())
            if (Objects.equals(e.getValue(), value))
                return e.getKey();

        return def;
    }

    @Nullable
    public K fromValue(V value) {
        return fromValue(value, null);
    }

    public K findFirstKey(Predicate<K> predicate, K def) {
        Objects.requireNonNull(predicate);

        for (K object : map.keySet())
            if (predicate.test(object)) return object;

        return def;
    }

    @Nullable
    public K findFirstKey(Predicate<K> predicate) {
        return findFirstKey(predicate, null);
    }

    public V findFirstValue(Predicate<V> predicate, V def) {
        Objects.requireNonNull(predicate);

        for (V object : map.values())
            if (predicate.test(object)) return object;

        return def;
    }

    @Nullable
    public V findFirstValue(Predicate<V> predicate) {
        return findFirstValue(predicate, null);
    }

    /**
     * Returns a list of the keys in the map builder.
     *
     * @return a list of the keys in the map builder
     */
    public List<K> keys() {
        return CollectionBuilder.of(map.keySet()).toList();
    }

    /**
     * Returns a list of the values in the map builder.
     *
     * @return a list of the values in the map builder
     */
    public List<V> values() {
        return CollectionBuilder.of(map.values()).toList();
    }

    /**
     * Returns a list of the entries in the map builder.
     *
     * @return a list of the entries in the map builder
     */
    public List<Entry<K, V>> entries() {
        return CollectionBuilder.of(map.entrySet()).map(Entry::of).toList();
    }

    @NotNull
    public Iterator<Entry<K, V>> iterator() {
        return entries().iterator();
    }

    public void forEach(BiConsumer<K, V> consumer) {
        map.forEach(consumer);
    }

    /**
     * Returns a map that contains the same key-value pairs as the map builder.
     *
     * @return a map that contains the same key-value pairs as the map builder
     */
    public Map<K, V> build() {
        return new LinkedHashMap<>(map);
    }

    /**
     * Checks if the map builder is empty, i.e. has no key-value pairs.
     *
     * @return true if the map builder is empty, false otherwise
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns a string representation of the map builder.
     *
     * @return a string representation of the map builder
     */
    public String toString() {
        return map.toString();
    }

    public static <K, V> Map<K, V> singleton(K key, V value) {
        return new MapBuilder<K, V>().put(key, value).build();
    }
}
