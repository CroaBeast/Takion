package me.croabeast.common.applier;

import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

/**
 * A simplified implementation of {@link StringApplier} that applies transformations sequentially.
 * <p>
 * {@code StringSimpleApplier} wraps a string and allows it to be modified by chaining multiple
 * {@link UnaryOperator} transformations. It is a simple, non-prioritized applier.
 * </p>
 */
class StringSimpleApplier extends SimpleApplier<String> implements StringApplier {

    /**
     * Constructs a new {@code StringSimpleApplier} for the given string.
     *
     * @param object the string to transform (must not be {@code null}).
     */
    StringSimpleApplier(String object) {
        super(object);
    }

    /**
     * Applies the given transformation operator at the specified priority.
     *
     * @param priority the priority for the transformation.
     * @param operator the transformation to apply.
     * @return this {@code StringSimpleApplier} instance for chaining.
     */
    @NotNull
    public StringSimpleApplier apply(Priority priority, UnaryOperator<String> operator) {
        return apply(operator);
    }

    /**
     * Applies the given transformation operator at the default priority (NORMAL).
     *
     * @param operator the transformation to apply.
     * @return this {@code StringSimpleApplier} instance for chaining.
     */
    @NotNull
    public StringSimpleApplier apply(UnaryOperator<String> operator) {
        super.apply(operator);
        return this;
    }

    /**
     * Returns the final transformed string.
     *
     * @return the resulting string.
     */
    @Override
    public String toString() {
        return result();
    }
}
