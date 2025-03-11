package me.croabeast.lib;

import me.croabeast.lib.util.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class CollectionBuilder<T> implements Iterable<T> {

    private final List<T> collection;

    CollectionBuilder(Collection<T> collection) {
        this.collection = new ArrayList<>(collection);
    }

    @NotNull
    public Iterator<T> iterator() {
        return collection.iterator();
    }

    public CollectionBuilder<T> filter(Predicate<T> predicate) {
        Objects.requireNonNull(predicate);

        collection.removeIf(predicate.negate());
        return this;
    }

    public CollectionBuilder<T> add(T element) {
        collection.add(element);
        return this;
    }

    public CollectionBuilder<T> remove(T element) {
        collection.remove(element);
        return this;
    }

    public CollectionBuilder<T> addAll(Collection<? extends T> elements) {
        collection.addAll(elements);
        return this;
    }

    @SafeVarargs
    public final CollectionBuilder<T> addAll(T... elements) {
        return addAll(ArrayUtils.toList(elements));
    }

    public CollectionBuilder<T> addAll(Iterable<? extends T> elements) {
        List<T> list = new ArrayList<>();
        elements.forEach(list::add);
        return addAll(list);
    }

    public CollectionBuilder<T> addAll(Iterator<? extends T> elements) {
        List<T> list = new ArrayList<>();
        while (elements.hasNext())
            list.add(elements.next());
        return addAll(list);
    }

    public CollectionBuilder<T> addAll(Enumeration<? extends T> elements) {
        return addAll(Collections.list(elements));
    }

    public CollectionBuilder<T> removeAll(Collection<? extends T> elements) {
        collection.removeAll(elements);
        return this;
    }

    public CollectionBuilder<T> sort(Comparator<? super T> comparator) {
        collection.sort(Objects.requireNonNull(comparator));
        return this;
    }

    public CollectionBuilder<T> apply(UnaryOperator<T> operator) {
        collection.replaceAll(Objects.requireNonNull(operator));
        return this;
    }

    public <U> CollectionBuilder<U> map(Function<T, U> function) {
        Objects.requireNonNull(function);

        List<U> list = new ArrayList<>();
        collection.forEach(t -> list.add(function.apply(t)));

        return new CollectionBuilder<>(list);
    }

    public CollectionBuilder<T> copy() {
        return new CollectionBuilder<>(collection);
    }

    public T findFirst(Predicate<T> predicate, T def) {
        Objects.requireNonNull(predicate);

        for (T object : collection)
            if (predicate.test(object)) return object;

        return def;
    }

    @Nullable
    public T findFirst(Predicate<T> predicate) {
        return findFirst(predicate, null);
    }

    public <C extends Collection<T>> C collect(Supplier<C> supplier) {
        Objects.requireNonNull(supplier);

        C collection = supplier.get();
        collection.addAll(this.collection);

        return collection;
    }

    public int sizeByFilter(Predicate<T> predicate) {
        return filter(predicate).size();
    }

    public List<T> toList() {
        return new ArrayList<>(collection);
    }

    public Set<T> toSet() {
        return new HashSet<>(collection);
    }

    public Queue<T> toQueue() {
        return new LinkedList<>(collection);
    }

    public Enumeration<T> toEnumeration() {
        return Collections.enumeration(collection);
    }

    public int size() {
        return collection.size();
    }

    @Override
    public String toString() {
        return collection.toString();
    }

    public static <T> CollectionBuilder<T> of(Collection<T> collection) {
        return new CollectionBuilder<>(collection == null ? new ArrayList<>() : collection);
    }

    public static <K, V> CollectionBuilder<Map.Entry<K, V>> of(Map<K, V> map) {
        return of(map.entrySet());
    }

    public static <T> CollectionBuilder<T> of(Iterator<T> iterator) {
        Objects.requireNonNull(iterator);

        List<T> collection = new ArrayList<>();
        while (iterator.hasNext())
            collection.add(iterator.next());

        return of(collection);
    }

    public static <T> CollectionBuilder<T> of(Iterable<T> iterable) {
        Objects.requireNonNull(iterable);

        List<T> collection = new ArrayList<>();
        iterable.forEach(collection::add);

        return of(collection);
    }

    public static <T> CollectionBuilder<T> of(Enumeration<T> enumeration) {
        Objects.requireNonNull(enumeration);

        List<T> collection = new ArrayList<>();
        while (enumeration.hasMoreElements())
            collection.add(enumeration.nextElement());

        return of(collection);
    }

    @SafeVarargs
    public static <T> CollectionBuilder<T> of(T... elements) {
        return of(ArrayUtils.toList(elements));
    }

    public static <T> CollectionBuilder<T> of(CollectionBuilder<T> builder) {
        return new CollectionBuilder<>(builder.collection);
    }
}
