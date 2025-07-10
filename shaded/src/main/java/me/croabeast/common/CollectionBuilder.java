package me.croabeast.common;

import me.croabeast.common.util.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * A versatile builder and wrapper for collections, providing a fluent API for filtering,
 * mapping, sorting, and transforming collections.
 * <p>
 * {@code CollectionBuilder} encapsulates a mutable list of elements and allows chaining operations
 * such as filtering, adding, removing, mapping, and more. This utility simplifies working with collections
 * in a functional style, making code more readable and expressive.
 * </p>
 * <p>
 * Example usage:
 * <pre><code>
 * List&lt;Integer&gt; numbers = Arrays.asList(1, 2, 3, 4, 5);
 * List&lt;Integer&gt; evenNumbers = CollectionBuilder.of(numbers)
 *         .filter(n -&gt; n % 2 == 0)
 *         .toList();
 * // evenNumbers now contains [2, 4]
 *
 * String joined = CollectionBuilder.of("a", "b", "c")
 *         .apply(s -&gt; s.toUpperCase())
 *         .toList()
 *         .toString();
 * // joined is "[A, B, C]"
 * </code></pre>
 * </p>
 *
 * @param <T> the type of elements in this collection builder
 */
public final class CollectionBuilder<T> implements Iterable<T>, Copyable<CollectionBuilder<T>> {

    private final List<T> collection;

    /**
     * Private constructor that initializes the builder with the given collection.
     *
     * @param collection the collection to wrap; a copy is made to ensure mutability.
     */
    private CollectionBuilder(Collection<T> collection) {
        this.collection = new ArrayList<>(collection);
    }

    /**
     * Returns an iterator over the elements in this builder.
     *
     * @return an {@link Iterator} for the collection
     */
    @NotNull
    public Iterator<T> iterator() {
        return collection.iterator();
    }

    /**
     * Filters the elements of the collection based on the provided predicate.
     * <p>
     * Elements that do not satisfy the predicate are removed.
     * </p>
     *
     * @param predicate the condition to apply for filtering
     * @return this builder instance for method chaining
     */
    public CollectionBuilder<T> filter(Predicate<T> predicate) {
        Objects.requireNonNull(predicate);
        collection.removeIf(predicate.negate());
        return this;
    }

    /**
     * Adds a single element to the collection.
     *
     * @param element the element to add
     * @return this builder instance for method chaining
     */
    public CollectionBuilder<T> add(T element) {
        collection.add(element);
        return this;
    }

    /**
     * Removes a single element from the collection.
     *
     * @param element the element to remove
     * @return this builder instance for method chaining
     */
    public CollectionBuilder<T> remove(T element) {
        collection.remove(element);
        return this;
    }

    /**
     * Adds all elements from the provided collection to this builder.
     *
     * @param elements the collection of elements to add
     * @return this builder instance for method chaining
     */
    public CollectionBuilder<T> addAll(Collection<? extends T> elements) {
        collection.addAll(elements);
        return this;
    }

    /**
     * Adds all provided elements to this builder.
     *
     * @param elements an array of elements to add
     * @return this builder instance for method chaining
     */
    @SafeVarargs
    public final CollectionBuilder<T> addAll(T... elements) {
        return addAll(ArrayUtils.toList(elements));
    }

    /**
     * Adds all elements from the provided iterable to this builder.
     *
     * @param elements the iterable of elements to add
     * @return this builder instance for method chaining
     */
    public CollectionBuilder<T> addAll(Iterable<? extends T> elements) {
        List<T> list = new ArrayList<>();
        elements.forEach(list::add);
        return addAll(list);
    }

    /**
     * Adds all elements from the provided iterator to this builder.
     *
     * @param elements the iterator of elements to add
     * @return this builder instance for method chaining
     */
    public CollectionBuilder<T> addAll(Iterator<? extends T> elements) {
        List<T> list = new ArrayList<>();
        while (elements.hasNext())
            list.add(elements.next());
        return addAll(list);
    }

    /**
     * Adds all elements from the provided enumeration to this builder.
     *
     * @param elements the enumeration of elements to add
     * @return this builder instance for method chaining
     */
    public CollectionBuilder<T> addAll(Enumeration<? extends T> elements) {
        return addAll(Collections.list(elements));
    }

    /**
     * Removes all elements in the specified collection from this builder.
     *
     * @param elements the collection of elements to remove
     * @return this builder instance for method chaining
     */
    public CollectionBuilder<T> removeAll(Collection<? extends T> elements) {
        collection.removeAll(elements);
        return this;
    }

    /**
     * Sorts the elements in this builder using the provided comparator.
     *
     * @param comparator the comparator to determine the order of the elements
     * @return this builder instance for method chaining
     */
    public CollectionBuilder<T> sort(Comparator<? super T> comparator) {
        collection.sort(Objects.requireNonNull(comparator));
        return this;
    }

    /**
     * Applies a transformation function to each element in this builder.
     * <p>
     * Each element is replaced with the result of applying the operator.
     * </p>
     *
     * @param operator the transformation function
     * @return this builder instance for method chaining
     */
    public CollectionBuilder<T> apply(UnaryOperator<T> operator) {
        collection.replaceAll(Objects.requireNonNull(operator));
        return this;
    }

    /**
     * Maps each element in this builder to a new value using the provided function.
     *
     * @param function the mapping function
     * @param <U>      the type of the mapped elements
     * @return a new {@link CollectionBuilder} containing the mapped elements
     */
    public <U> CollectionBuilder<U> map(Function<T, U> function) {
        Objects.requireNonNull(function);
        List<U> list = new ArrayList<>();
        collection.forEach(t -> list.add(function.apply(t)));
        return new CollectionBuilder<>(list);
    }

    /**
     * Creates a copy of this {@code CollectionBuilder}.
     *
     * @return a new builder with the same elements
     */
    @NotNull
    public CollectionBuilder<T> copy() {
        return new CollectionBuilder<>(collection);
    }

    /**
     * Finds and returns the first element matching the provided predicate.
     * <p>
     * If no element matches, returns the specified default value.
     * </p>
     *
     * @param predicate the condition to test
     * @param def       the default value to return if no match is found
     * @return the first matching element, or the default value if none is found
     */
    public T findFirst(Predicate<T> predicate, T def) {
        Objects.requireNonNull(predicate);
        for (T object : collection)
            if (predicate.test(object)) return object;
        return def;
    }

    /**
     * Finds and returns the first element matching the provided predicate.
     * <p>
     * If no element matches, returns {@code null}.
     * </p>
     *
     * @param predicate the condition to test
     * @return the first matching element, or {@code null} if none is found
     */
    @Nullable
    public T findFirst(Predicate<T> predicate) {
        return findFirst(predicate, null);
    }

    /**
     * Collects the elements of this builder into a new collection provided by the supplier.
     *
     * @param supplier a supplier to create a new collection
     * @param <C>      the type of the collection to return
     * @return a new collection containing all elements in this builder
     */
    public <C extends Collection<T>> C collect(Supplier<C> supplier) {
        Objects.requireNonNull(supplier);
        C collection = Objects.requireNonNull(supplier.get());
        collection.addAll(this.collection);
        return collection;
    }

    /**
     * Returns the number of elements that satisfy a given predicate.
     *
     * @param predicate the condition to test
     * @return the count of elements that match the predicate
     */
    public int sizeByFilter(Predicate<T> predicate) {
        return filter(predicate).size();
    }

    /**
     * Returns a new list containing all elements in this builder.
     *
     * @return a {@link List} of elements
     */
    public List<T> toList() {
        return new ArrayList<>(collection);
    }

    /**
     * Returns a new set containing all unique elements in this builder.
     *
     * @return a {@link Set} of elements
     */
    public Set<T> toSet() {
        return new HashSet<>(collection);
    }

    /**
     * Returns a new queue containing all elements in this builder.
     *
     * @return a {@link Queue} of elements
     */
    public Queue<T> toQueue() {
        return new LinkedList<>(collection);
    }

    /**
     * Returns an {@link Enumeration} over the elements in this builder.
     *
     * @return an enumeration of elements
     */
    public Enumeration<T> toEnumeration() {
        return Collections.enumeration(collection);
    }

    /**
     * Returns the number of elements currently held in this builder.
     *
     * @return the size of the collection
     */
    public int size() {
        return collection.size();
    }

    /**
     * Returns a string representation of the underlying collection.
     *
     * @return a string representing the collection
     */
    @Override
    public String toString() {
        return collection.toString();
    }

    /**
     * Creates a new {@code CollectionBuilder} from the provided collection.
     *
     * @param collection the source collection; if {@code null}, an empty collection is used
     * @param <T>        the type of elements in the collection
     * @return a new {@code CollectionBuilder} instance
     */
    public static <T> CollectionBuilder<T> of(Collection<T> collection) {
        return new CollectionBuilder<>(collection == null ? new ArrayList<>() : collection);
    }

    /**
     * Creates a new {@code CollectionBuilder} from the entries of the provided map.
     *
     * @param map the source map whose entries are to be used
     * @param <K> the type of the keys in the map
     * @param <V> the type of the values in the map
     * @return a new {@code CollectionBuilder} containing the map's entries
     */
    public static <K, V> CollectionBuilder<Map.Entry<K, V>> of(Map<K, V> map) {
        return of(map.entrySet());
    }

    /**
     * Creates a new {@code CollectionBuilder} from the elements provided by an iterator.
     *
     * @param iterator the source iterator of elements
     * @param <T>      the type of elements
     * @return a new {@code CollectionBuilder} containing the elements from the iterator
     */
    public static <T> CollectionBuilder<T> of(Iterator<T> iterator) {
        Objects.requireNonNull(iterator);
        List<T> collection = new ArrayList<>();
        while (iterator.hasNext())
            collection.add(iterator.next());
        return of(collection);
    }

    /**
     * Creates a new {@code CollectionBuilder} from the elements provided by an iterable.
     *
     * @param iterable the source iterable of elements
     * @param <T>      the type of elements
     * @return a new {@code CollectionBuilder} containing the elements from the iterable
     */
    public static <T> CollectionBuilder<T> of(Iterable<T> iterable) {
        Objects.requireNonNull(iterable);
        List<T> collection = new ArrayList<>();
        iterable.forEach(collection::add);
        return of(collection);
    }

    /**
     * Creates a new {@code CollectionBuilder} from the elements provided by an enumeration.
     *
     * @param enumeration the source enumeration of elements
     * @param <T>         the type of elements
     * @return a new {@code CollectionBuilder} containing the elements from the enumeration
     */
    public static <T> CollectionBuilder<T> of(Enumeration<T> enumeration) {
        Objects.requireNonNull(enumeration);
        List<T> collection = new ArrayList<>();
        while (enumeration.hasMoreElements())
            collection.add(enumeration.nextElement());
        return of(collection);
    }

    /**
     * Creates a new {@code CollectionBuilder} from an array of elements.
     *
     * @param elements the array of elements to include
     * @param <T>      the type of elements
     * @return a new {@code CollectionBuilder} containing the given elements
     */
    @SafeVarargs
    public static <T> CollectionBuilder<T> of(T... elements) {
        return of(ArrayUtils.toList(elements));
    }

    /**
     * Creates a copy of the given {@code CollectionBuilder}.
     *
     * @param builder the builder to copy
     * @param <T>     the type of elements
     * @return a new {@code CollectionBuilder} with the same elements as the provided builder
     */
    public static <T> CollectionBuilder<T> of(CollectionBuilder<T> builder) {
        return new CollectionBuilder<>(builder.collection);
    }
}
