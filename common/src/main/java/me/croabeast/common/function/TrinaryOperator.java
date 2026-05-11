package me.croabeast.common.function;

/**
 * Represents an operator that accepts three operands of the same type and returns a result of that type.
 * <p>
 * This is a specialization of {@link TriFunction} where all three input arguments and the result are of the same type.
 * It is often used for combining three values together.
 * </p>
 *
 * @param <T> the type of the operands and the result of the operator
 */
@FunctionalInterface
public interface TrinaryOperator<T> extends TriFunction<T, T, T, T> {
    // No additional methods required; inherits apply() and andThen() from TriFunction.
}
