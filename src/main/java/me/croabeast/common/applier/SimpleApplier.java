package me.croabeast.common.applier;

import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * A simple implementation of {@link ObjectApplier} that applies transformations sequentially.
 * <p>
 * {@code SimpleApplier} stores an object of type {@code T} and allows modifications via
 * chained {@link UnaryOperator} calls. The final result can be retrieved using {@link #result()}.
 * </p>
 *
 * @param <T> the type of the object being transformed.
 */
class SimpleApplier<T> implements ObjectApplier<T> {

    private T object;

    /**
     * Constructs a new {@code SimpleApplier} with the specified object.
     *
     * @param object the object to transform (must not be {@code null}).
     */
    SimpleApplier(T object) {
        this.object = Objects.requireNonNull(object);
    }

    /**
     * Applies the specified transformation operator with the given priority.
     * <p>
     * In this implementation, the priority parameter is ignored and the operator is simply applied.
     * </p>
     *
     * @param priority the priority at which to apply the operator.
     * @param operator the transformation to apply.
     * @return this {@code SimpleApplier} instance.
     */
    @NotNull
    public SimpleApplier<T> apply(Priority priority, UnaryOperator<T> operator) {
        return apply(operator);
    }

    /**
     * Applies the specified transformation operator to the stored object.
     *
     * @param operator the transformation to apply.
     * @return this {@code SimpleApplier} instance.
     */
    @NotNull
    public SimpleApplier<T> apply(UnaryOperator<T> operator) {
        object = Objects.requireNonNull(operator).apply(object);
        return this;
    }

    /**
     * Returns the transformed object.
     *
     * @return the final result.
     */
    @Override
    public T result() {
        return object;
    }

    /**
     * Returns a string representation of the transformed object.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return object.toString();
    }
}
