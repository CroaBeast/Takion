package me.croabeast.common.applier;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Represents a generic applier that allows successive transformations on an object.
 * <p>
 * Implementations of {@code ObjectApplier} enable chaining of {@link UnaryOperator} transformations
 * based on an optional priority. After applying a series of operators, the final result can be obtained
 * via the {@link #result()} method.
 * </p>
 *
 * @param <T> the type of the object being transformed.
 */
public interface Applier<T> {

    /**
     * Applies the given transformation operator with the specified priority.
     *
     * @param priority the priority at which to apply the operator.
     * @param operator the transformation to apply.
     * @return this {@code ObjectApplier} instance for chaining.
     * @throws NullPointerException if {@code operator} is {@code null}.
     */
    @NotNull
    Applier<T> apply(Priority priority, UnaryOperator<T> operator);

    /**
     * Applies the given transformation operator at the default priority (NORMAL).
     *
     * @param operator the transformation to apply.
     * @return this {@code ObjectApplier} instance for chaining.
     * @throws NullPointerException if {@code operator} is {@code null}.
     */
    @NotNull
    Applier<T> apply(UnaryOperator<T> operator);

    /**
     * Returns the final result after applying all the transformations.
     *
     * @return the transformed object.
     */
    T result();

    /**
     * Creates a simplified {@code ObjectApplier} for the provided object.
     *
     * @param object the object to transform.
     * @param <T>    the type of the object.
     * @return a new instance of a simplified {@code ObjectApplier}.
     */
    static <T> Applier<T> simplified(T object) {
        return new SimpleApplier<>(object);
    }

    /**
     * Creates a simplified {@code ObjectApplier} by extracting the result from an existing applier.
     *
     * @param applier the existing applier.
     * @param <T>     the type of the object.
     * @return a new instance of a simplified {@code ObjectApplier} initialized with the result.
     */
    static <T> Applier<T> simplified(Applier<T> applier) {
        return simplified(Objects.requireNonNull(applier).result());
    }

    /**
     * Creates a prioritized {@code ObjectApplier} for the provided object.
     *
     * @param object the object to transform.
     * @param <T>    the type of the object.
     * @return a new instance of a prioritized {@code ObjectApplier}.
     */
    static <T> Applier<T> prioritized(T object) {
        return new PriorityApplier<>(object);
    }

    /**
     * Creates a prioritized {@code ObjectApplier} by extracting the result from an existing applier.
     *
     * @param applier the existing applier.
     * @param <T>     the type of the object.
     * @return a new instance of a prioritized {@code ObjectApplier} initialized with the result.
     */
    static <T> Applier<T> prioritized(Applier<T> applier) {
        return prioritized(Objects.requireNonNull(applier).result());
    }

    /**
     * Represents the priority levels for transformation operators.
     */
    enum Priority {
        LOWEST,
        LOW,
        NORMAL,
        HIGH,
        HIGHEST
    }
}
