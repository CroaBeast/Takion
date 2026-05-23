package me.croabeast.common.applier;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * An implementation of {@link Applier} that supports prioritizing transformation operators.
 * <p>
 * {@code PriorityApplier} maintains a map of transformation operators grouped by their assigned priority.
 * When {@link #result()} is called, it applies the operators in order of descending priority
 * (from highest to lowest) to the initial object.
 * </p>
 *
 * @param <T> the type of the object being transformed
 */
class PriorityApplier<T> implements Applier<T> {

    private final Map<Priority, Set<UnaryOperator<T>>> os = new TreeMap<>(Comparator.reverseOrder());
    private final T object;

    /**
     * Constructs a new {@code PriorityApplier} with the specified object.
     *
     * @param object the object to transform (must not be {@code null})
     */
    PriorityApplier(T object) {
        this.object = Objects.requireNonNull(object);
    }

    /**
     * Applies the given transformation operator at the specified priority.
     *
     * @param p        the priority to assign; if {@code null}, defaults to {@link Priority#NORMAL}
     * @param operator the transformation to apply
     * @return this {@code PriorityApplier} instance for chaining
     * @throws NullPointerException if {@code operator} is {@code null}
     */
    @NotNull
    public PriorityApplier<T> apply(Priority p, UnaryOperator<T> operator) {
        Objects.requireNonNull(operator);
        os.computeIfAbsent(p == null ? Priority.NORMAL : p, s -> new LinkedHashSet<>()).add(operator);
        return this;
    }

    /**
     * Applies the given transformation operator at the default priority ({@link Priority#NORMAL}).
     *
     * @param operator the transformation to apply
     * @return this {@code PriorityApplier} instance for chaining
     */
    @NotNull
    public PriorityApplier<T> apply(UnaryOperator<T> operator) {
        return apply(null, operator);
    }

    /**
     * Applies all registered transformation operators in order of descending priority and returns the result.
     *
     * @return the transformed object
     */
    @Override
    public T result() {
        T result = object;
        for (Set<UnaryOperator<T>> operators : os.values())
            for (UnaryOperator<T> operator : operators)
                result = operator.apply(result);
        return result;
    }

    /**
     * Returns a string representation of the transformed object after all operators are applied.
     *
     * @return the string representation of the result
     */
    @Override
    public String toString() {
        return result().toString();
    }
}
