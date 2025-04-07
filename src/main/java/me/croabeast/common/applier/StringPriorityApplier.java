package me.croabeast.common.applier;

import org.jetbrains.annotations.NotNull;
import java.util.function.UnaryOperator;

/**
 * A prioritized implementation of {@link StringApplier} that allows transformation operators
 * to be applied based on their assigned priority.
 * <p>
 * {@code StringPriorityApplier} extends {@link PriorityApplier} and implements {@link StringApplier},
 * enabling complex, ordered transformations on strings.
 * </p>
 */
class StringPriorityApplier extends PriorityApplier<String> implements StringApplier {

    /**
     * Constructs a new {@code StringPriorityApplier} for the given string.
     *
     * @param object the string to transform (must not be {@code null}).
     */
    StringPriorityApplier(String object) {
        super(object);
    }

    /**
     * Applies the given transformation operator at the specified priority.
     *
     * @param priority the priority for the transformation.
     * @param operator the transformation to apply.
     * @return this {@code StringPriorityApplier} instance for chaining.
     */
    @NotNull
    public StringPriorityApplier apply(Priority priority, UnaryOperator<String> operator) {
        super.apply(priority, operator);
        return this;
    }

    /**
     * Applies the given transformation operator at the default priority (NORMAL).
     *
     * @param operator the transformation to apply.
     * @return this {@code StringPriorityApplier} instance for chaining.
     */
    @NotNull
    public StringPriorityApplier apply(UnaryOperator<String> operator) {
        return apply(null, operator);
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
