package me.croabeast.common.map;

import me.croabeast.common.CollectionBuilder;
import me.croabeast.common.util.ReplaceUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A fluent builder for creating and transforming {@link LinkedHashMap} instances.
 * <p>
 * The {@code MapBuilder} class wraps an internal {@link LinkedHashMap} and provides
 * methods for adding, removing, filtering, and mapping entries in a fluent style.
 * It also supports importing from existing maps or collections of map entries, and
 * producing immutable copies via {@link #build()}.
 * </p>
 *
 * @param <K> the type of keys in the map
 * @param <V> the type of values in the map
 * @author CroaBeast
 * @see LinkedHashMap
 * @see CollectionBuilder
 */
public class MapBuilder<K, V> implements Iterable<Map.Entry<K, V>> {

    private final Map<K, V> map = new LinkedHashMap<>();

    /**
     * Creates an empty {@code MapBuilder}.
     */
    public MapBuilder() {}

    /**
     * Creates a {@code MapBuilder} initialized with the entries of the given map.
     *
     * @param map an existing map whose entries will be copied; may be {@code null}
     */
    public MapBuilder(Map<? extends K, ? extends V> map) {
        if (map != null) this.map.putAll(map);
    }

    /**
     * Creates a {@code MapBuilder} initialized with the entries from the given collection.
     *
     * @param collection a collection of map entries to copy; may be {@code null}
     */
    public MapBuilder(Collection<Map.Entry<? extends K, ? extends V>> collection) {
        if (collection != null)
            collection.forEach(e -> this.map.put(e.getKey(), e.getValue()));
    }

    /**
     * Associates the specified value with the specified key in this builder.
     *
     * @param key   the key to add
     * @param value the value to associate
     * @return this builder (for chaining)
     */
    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    /**
     * Adds the given entry to this builder.
     *
     * @param entry the entry whose key and value to add
     * @return this builder (for chaining)
     */
    public MapBuilder<K, V> put(Map.Entry<? extends K, ? extends V> entry) {
        return put(entry.getKey(), entry.getValue());
    }

    /**
     * Associates the specified value with the specified key if the key is not already present.
     *
     * @param key   the key to add
     * @param value the value to associate if absent
     * @return this builder (for chaining)
     */
    public MapBuilder<K, V> putIfAbsent(K key, V value) {
        map.putIfAbsent(key, value);
        return this;
    }

    /**
     * Adds the given entry only if its key is not already present.
     *
     * @param entry the entry whose key and value to add if absent
     * @return this builder (for chaining)
     */
    public MapBuilder<K, V> putIfAbsent(Map.Entry<? extends K, ? extends V> entry) {
        return putIfAbsent(entry.getKey(), entry.getValue());
    }

    /**
     * Copies all entries from the given map into this builder.
     *
     * @param map the map whose entries to add (must not be {@code null})
     * @return this builder (for chaining)
     */
    public MapBuilder<K, V> putAll(Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map, "map");
        map.forEach(this::put);
        return this;
    }

    /**
     * Removes the mapping for the specified key if present.
     *
     * @param key the key to remove
     * @return this builder (for chaining)
     */
    public MapBuilder<K, V> remove(K key) {
        map.remove(key);
        return this;
    }

    /**
     * Removes the entry for the specified key only if it is currently mapped to the given value.
     *
     * @param key   the key whose mapping to remove
     * @param value the value expected to be associated with the key
     * @return this builder (for chaining)
     */
    public MapBuilder<K, V> remove(K key, V value) {
        map.remove(key, value);
        return this;
    }

    /**
     * Removes up to {@code counting} entries matching the given key.
     * If {@code counting} is negative, all matching entries are removed.
     *
     * @param key      the key to remove
     * @param counting the maximum number of entries to remove, or negative for unlimited
     * @return this builder (for chaining)
     */
    public MapBuilder<K, V> removeAllByKey(K key, int counting) {
        boolean finite = counting > -1;
        Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<K, V> e = it.next();
            if (Objects.equals(e.getKey(), key)) {
                it.remove();
                if (finite && --counting < 0) break;
            }
        }
        return this;
    }

    /**
     * Removes up to {@code counting} entries matching the given value.
     * If {@code counting} is negative, all matching entries are removed.
     *
     * @param value    the value to remove
     * @param counting the maximum number of entries to remove, or negative for unlimited
     * @return this builder (for chaining)
     */
    public MapBuilder<K, V> removeAllByValue(V value, int counting) {
        boolean finite = counting > -1;
        Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<K, V> e = it.next();
            if (Objects.equals(e.getValue(), value)) {
                it.remove();
                if (finite && --counting < 0) break;
            }
        }
        return this;
    }

    /**
     * Retains only those entries whose keys match the given predicate.
     *
     * @param predicate a predicate to test keys
     * @return this builder (for chaining)
     */
    public MapBuilder<K, V> filterByKey(Predicate<K> predicate) {
        Objects.requireNonNull(predicate);
        map.keySet().removeIf(predicate.negate());
        return this;
    }

    /**
     * Retains only those entries whose values match the given predicate.
     *
     * @param predicate a predicate to test values
     * @return this builder (for chaining)
     */
    public MapBuilder<K, V> filterByValue(Predicate<V> predicate) {
        Objects.requireNonNull(predicate);
        map.values().removeIf(predicate.negate());
        return this;
    }

    /**
     * Retains only those entries for which the given bi-predicate returns {@code true}.
     *
     * @param predicate a bi-predicate to test key and value pairs
     * @return this builder (for chaining)
     */
    public MapBuilder<K, V> filter(BiPredicate<K, V> predicate) {
        Objects.requireNonNull(predicate);
        map.entrySet().removeIf(e -> predicate.negate().test(e.getKey(), e.getValue()));
        return this;
    }

    /**
     * Transforms all keys using the given function, keeping values unchanged.
     *
     * @param function a function to apply to each key
     * @param <A>      the new key type
     * @return a new {@code MapBuilder} with transformed keys
     */
    public <A> MapBuilder<A, V> applyByKey(Function<K, A> function) {
        Objects.requireNonNull(function);
        Map<A, V> entries = new LinkedHashMap<>();

        for (Map.Entry<K, V> entry : map.entrySet())
            entries.put(function.apply(entry.getKey()), entry.getValue());

        return new MapBuilder<>(entries);
    }

    /**
     * Transforms all values using the given function, keeping keys unchanged.
     *
     * @param function a function to apply to each value
     * @param <B>      the new value type
     * @return a new {@code MapBuilder} with transformed values
     */
    public <B> MapBuilder<K, B> applyByValue(Function<V, B> function) {
        Objects.requireNonNull(function);
        Map<K, B> entries = new LinkedHashMap<>();

        for (Map.Entry<K, V> entry : map.entrySet())
            entries.put(entry.getKey(), function.apply(entry.getValue()));

        return new MapBuilder<>(entries);
    }

    /**
     * Transforms both keys and values using the given functions.
     *
     * @param keyFunction   a function to apply to each key
     * @param valueFunction a function to apply to each value
     * @param <A>           the new key type
     * @param <B>           the new value type
     * @return a new {@code MapBuilder} with transformed entries
     */
    public <A, B> MapBuilder<A, B> map(Function<K, A> keyFunction, Function<V, B> valueFunction) {
        Objects.requireNonNull(keyFunction);
        Objects.requireNonNull(valueFunction);

        Map<A, B> entries = new LinkedHashMap<>();
        for (final Map.Entry<K, V> entry : map.entrySet())
            entries.put(
                    keyFunction.apply(entry.getKey()),
                    valueFunction.apply(entry.getValue())
            );

        return new MapBuilder<>(entries);
    }

    /**
     * Checks if the specified key is present.
     *
     * @param key the key to check
     * @return {@code true} if present, {@code false} otherwise
     */
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    /**
     * Checks if the specified value is present.
     *
     * @param value the value to check
     * @return {@code true} if present, {@code false} otherwise
     */
    public boolean containsValue(V value) {
        return map.containsValue(value);
    }

    /**
     * Returns the value to which the specified key is mapped, or the default if none.
     *
     * @param key the key
     * @param def the default value to return if key is absent
     * @return the mapped or default value
     */
    public V get(K key, V def) {
        return map.getOrDefault(key, def);
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if none.
     *
     * @param key the key
     * @return the mapped value, or {@code null}
     */
    public V get(K key) {
        return map.get(key);
    }

    /**
     * Removes all mappings.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Returns the number of entries.
     *
     * @return the map size
     */
    public int size() {
        return map.size();
    }

    /**
     * Finds the first key associated with the given value, or returns the default if none.
     *
     * @param value the value to search for
     * @param def   the default key if not found
     * @return the found key or {@code def}
     */
    public K fromValue(V value, K def) {
        for (Map.Entry<K, V> e : map.entrySet())
            if (Objects.equals(e.getValue(), value))
                return e.getKey();
        return def;
    }

    /**
     * Finds the first key associated with the given value, or {@code null} if none.
     *
     * @param value the value to search for
     * @return the found key or {@code null}
     */
    @Nullable
    public K fromValue(V value) {
        return fromValue(value, null);
    }

    /**
     * Finds the first key matching the given predicate, or returns the default if none.
     *
     * @param predicate a predicate to test keys
     * @param def       the default key if not found
     * @return the found key or {@code def}
     */
    public K findFirstKey(Predicate<K> predicate, K def) {
        Objects.requireNonNull(predicate);

        for (K k : map.keySet())
            if (predicate.test(k)) return k;
        return def;
    }

    /**
     * Finds the first key matching the given predicate, or {@code null} if none.
     *
     * @param predicate a predicate to test keys
     * @return the found key or {@code null}
     */
    @Nullable
    public K findFirstKey(Predicate<K> predicate) {
        return findFirstKey(predicate, null);
    }

    /**
     * Finds the first value matching the given predicate, or returns the default if none.
     *
     * @param predicate a predicate to test values
     * @param def       the default value if not found
     * @return the found value or {@code def}
     */
    public V findFirstValue(Predicate<V> predicate, V def) {
        Objects.requireNonNull(predicate);

        for (V v : map.values())
            if (predicate.test(v)) return v;
        return def;
    }

    /**
     * Finds the first value matching the given predicate, or {@code null} if none.
     *
     * @param predicate a predicate to test values
     * @return the found value or {@code null}
     */
    @Nullable
    public V findFirstValue(Predicate<V> predicate) {
        return findFirstValue(predicate, null);
    }

    /**
     * Returns a {@link List} of all keys, preserving insertion order.
     *
     * @return a list of keys
     */
    public List<K> keys() {
        return CollectionBuilder.of(map.keySet()).toList();
    }

    /**
     * Returns a {@link List} of all values, preserving insertion order.
     *
     * @return a list of values
     */
    public List<V> values() {
        return CollectionBuilder.of(map.values()).toList();
    }

    /**
     * Returns a {@link List} of all entries, preserving insertion order.
     *
     * @return a list of map entries
     */
    public List<Map.Entry<K, V>> entries() {
        return CollectionBuilder.of(map.entrySet()).toList();
    }

    /**
     * Returns an iterator over the entries in this builder.
     *
     * @return an iterator of map entries
     */
    @NotNull
    public Iterator<Map.Entry<K, V>> iterator() {
        return entries().iterator();
    }

    /**
     * Performs the given action for each entry in this builder.
     *
     * @param consumer a bi-consumer accepting key and value
     */
    public void forEach(BiConsumer<K, V> consumer) {
        map.forEach(consumer);
    }

    /**
     * Builds and returns a new {@link LinkedHashMap} containing the current entries.
     *
     * @return an immutable copy of the internal map
     */
    public Map<K, V> build() {
        return new LinkedHashMap<>(map);
    }

    /**
     * Checks whether this builder contains no entries.
     *
     * @return {@code true} if empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns a string representation of the internal map.
     *
     * @return a string representation of the internal map
     */
    @Override
    public String toString() {
        return map.toString();
    }

    /**
     * Creates a singleton map containing the given key-value pair.
     *
     * @param key   the single key
     * @param value the single value
     * @param <K>   the key type
     * @param <V>   the value type
     * @return a map with exactly one entry
     */
    public static <K, V> Map<K, V> singleton(K key, V value) {
        return new MapBuilder<K, V>().put(key, value).build();
    }

    /**
     * Creates a {@link Map} by pairing elements from two collections: one of keys and one of values.
     * <p>
     * Each element in the {@code keys} collection is associated with the element at the same index
     * in the {@code values} collection. The iteration order of the returned map follows the iteration
     * order of the {@code keys} collection.
     * </p>
     *
     * @param <K>    the type of the map keys
     * @param <V>    the type of the map values
     * @param keys   the collection of keys (must not be {@code null})
     * @param values the collection of values (must not be {@code null}), must have the same size as {@code keys}
     * @return a new {@code LinkedHashMap} containing each key mapped to its corresponding value
     * @throws NullPointerException      if either {@code keys} or {@code values} is {@code null}
     * @throws IndexOutOfBoundsException if {@code keys.size() != values.size()}
     */
    public static <K, V> Map<K, V> mapOf(Collection<K> keys, Collection<V> values) {
        Objects.requireNonNull(keys, "keys collection must not be null");
        Objects.requireNonNull(values, "values collection must not be null");

        if (keys.size() != values.size())
            throw new IndexOutOfBoundsException(
                    "Keys and values must have the same length: "
                    + keys.size() + " != " + values.size()
            );

        List<K> resultKeys = new ArrayList<>(keys);
        List<V> resultValues = new ArrayList<>(values);

        Map<K, V> builder = new LinkedHashMap<>();
        for (int i = 0; i < keys.size(); i++)
            builder.put(resultKeys.get(i), resultValues.get(i));

        return builder;
    }

    /**
     * Creates a {@link Map} by pairing elements from two arrays: one of keys and one of values.
     * <p>
     * Each element in the {@code keys} array is associated with the element at the same index
     * in the {@code values} array. The iteration order of the returned map follows the order
     * of the {@code keys} array.
     * </p>
     *
     * @param <K>    the type of the map keys
     * @param <V>    the type of the map values
     * @param keys   the array of keys (must not be {@code null})
     * @param values the array of values (must not be {@code null}), must have the same length as {@code keys}
     * @return a new {@code LinkedHashMap} containing each key mapped to its corresponding value
     * @throws NullPointerException      if either {@code keys} or {@code values} is {@code null}
     * @throws IndexOutOfBoundsException if {@code keys.length != values.length}
     * @see ReplaceUtils#isApplicable(Object[], Object[])
     */
    @SafeVarargs
    public static <K, V> Map<K, V> mapOf(K[] keys, V... values) {
        Objects.requireNonNull(keys, "keys array must not be null");
        Objects.requireNonNull(values, "values array must not be null");

        if (!ReplaceUtils.isApplicable(keys, values))
            throw new IndexOutOfBoundsException(
                    "Keys and values must have the same length: "
                    + keys.length + " != " + values.length
            );

        Map<K, V> map = new LinkedHashMap<>();
        for (int i = 0; i < keys.length; i++) map.put(keys[i], values[i]);
        return map;
    }
}
