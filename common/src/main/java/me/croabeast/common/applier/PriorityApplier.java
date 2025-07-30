package me.croabeast.common.applier;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * An implementation of {@link Applier} that supports prioritizing transformation operators.
 * <p>
 * {@code PriorityApplier} maintains a map of transformation operators grouped by their assigned priority.
 * When {@link #result()} is called, it applies the operators in order of descending priority (from highest to lowest)
 * to the initial object.
 * </p>
 *
 * @param <T> the type of the object being transformed.
 */
class PriorityApplier<T> implements Applier<T> {

    private final Map<Priority, Set<UnaryOperator<T>>> os = new TreeMap<>(Comparator.reverseOrder());
    private final T object;

    /**
     * Constructs a new {@code PriorityApplier} with the specified object.
     *
     * @param object the object to transform (must not be {@code null}).
     */
    PriorityApplier(T object) {
        this.object = Objects.requireNonNull(object);
    }

    /**
     * Applies the given transformation operator at the specified priority.
     *
     * @param priority the priority to assign; if {@code null}, defaults to {@link Priority#NORMAL}.
     * @param operator the transformation to apply.
     * @return this {@code PriorityApplier} instance for chaining.
     * @throws NullPointerException if {@code operator} is {@code null}.
     */
    @NotNull
    public PriorityApplier<T> apply(Priority priority, UnaryOperator<T> operator) {
        priority = priority == null ? Priority.NORMAL : priority;
        Objects.requireNonNull(operator);

        Set<UnaryOperator<T>> set = os.getOrDefault(priority, new LinkedHashSet<>());
        set.add(operator);

        os.put(priority, set);
        return this;
    }

    /**
     * Applies the given transformation operator at the default priority (NORMAL).
     *
     * @param operator the transformation to apply.
     * @return this {@code PriorityApplier} instance for chaining.
     */
    @NotNull
    public PriorityApplier<T> apply(UnaryOperator<T> operator) {
        return apply(null, operator);
    }

    /**
     * Applies all registered transformation operators in order of descending priority and returns the result.
     *
     * @return the transformed object.
     */
    @Override
    public T result() {
        SimpleApplier<T> applier = new SimpleApplier<>(object);
        os.forEach((p, o) -> o.forEach(applier::apply));
        return applier.result();
    }

    /**
     * Returns a string representation of the transformed object.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return result().toString();
    }
}
