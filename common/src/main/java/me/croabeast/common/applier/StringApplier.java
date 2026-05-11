package me.croabeast.common.applier;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * A specialization of {@link Applier} for handling String transformations.
 * <p>
 * The {@code StringApplier} interface provides a fluent API to apply successive string operations,
 * such as formatting, replacement, or any other transformation. It extends {@link Applier} with methods
 * that are specific to strings.
 * </p>
 */
public interface StringApplier extends Applier<String> {

    /**
     * Applies a transformation to the string with the specified priority.
     *
     * @param priority the priority at which to apply the operator.
     * @param operator the transformation to apply.
     * @return this {@code StringApplier} instance for chaining.
     * @throws NullPointerException if {@code operator} is {@code null}.
     */
    @NotNull
    StringApplier apply(Priority priority, UnaryOperator<String> operator);

    /**
     * Applies a transformation to the string at the default priority (NORMAL).
     *
     * @param operator the transformation to apply.
     * @return this {@code StringApplier} instance for chaining.
     * @throws NullPointerException if {@code operator} is {@code null}.
     */
    @NotNull
    StringApplier apply(UnaryOperator<String> operator);

    /**
     * Returns the resulting string after all transformations have been applied.
     *
     * @return the transformed string.
     */
    @Override
    String toString();

    /**
     * Creates a simplified {@code StringApplier} for the provided string.
     *
     * @param string the string to transform.
     * @return a new instance of a simplified {@code StringApplier}.
     */
    static StringApplier simplified(String string) {
        return new StringSimpleApplier(string);
    }

    /**
     * Creates a simplified {@code StringApplier} by extracting the result from an existing {@code StringApplier}.
     *
     * @param applier the existing {@code StringApplier}.
     * @return a new instance of a simplified {@code StringApplier} initialized with the result.
     */
    static StringApplier simplified(StringApplier applier) {
        return simplified(Objects.requireNonNull(applier).toString());
    }

    /**
     * Creates a prioritized {@code StringApplier} for the provided string.
     *
     * @param string the string to transform.
     * @return a new instance of a prioritized {@code StringApplier}.
     */
    static StringApplier prioritized(String string) {
        return new StringPriorityApplier(string);
    }

    /**
     * Creates a prioritized {@code StringApplier} by extracting the result from an existing {@code StringApplier}.
     *
     * @param applier the existing {@code StringApplier}.
     * @return a new instance of a prioritized {@code StringApplier} initialized with the result.
     */
    static StringApplier prioritized(StringApplier applier) {
        return prioritized(Objects.requireNonNull(applier).toString());
    }
}
