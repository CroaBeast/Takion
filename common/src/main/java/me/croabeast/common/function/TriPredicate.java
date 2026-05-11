package me.croabeast.common.function;

import java.util.Objects;

/**
 * Represents a predicate (boolean-valued function) of three arguments.
 * <p>
 * This is a three-arity specialization of {@link java.util.function.Predicate} that takes three inputs and
 * returns a boolean result. It also provides methods for composing predicates via logical AND, OR, and negation.
 * </p>
 *
 * @param <T> the type of the first argument to the predicate
 * @param <U> the type of the second argument to the predicate
 * @param <V> the type of the third argument to the predicate
 */
@FunctionalInterface
public interface TriPredicate<T, U, V> {

    /**
     * Evaluates this predicate on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @param v the third input argument
     * @return {@code true} if the input arguments match the predicate, otherwise {@code false}
     */
    boolean test(T t, U u, V v);

    /**
     * Returns a composed predicate that represents a short-circuiting logical AND of this predicate and another.
     * <p>
     * When evaluating the composed predicate, if this predicate is {@code false}, then the {@code other} predicate
     * is not evaluated.
     * </p>
     *
     * @param other a predicate that will be logically-ANDed with this predicate
     * @return a composed predicate that represents the short-circuiting logical AND of this predicate and the {@code other} predicate
     * @throws NullPointerException if {@code other} is null
     */
    default TriPredicate<T, U, V> and(TriPredicate<? super T, ? super U, ? super V> other) {
        Objects.requireNonNull(other);
        return (t, u, v) -> test(t, u, v) && other.test(t, u, v);
    }

    /**
     * Returns a predicate that represents the logical negation of this predicate.
     *
     * @return a predicate that represents the logical negation of this predicate
     */
    default TriPredicate<T, U, V> negate() {
        return (t, u, v) -> !test(t, u, v);
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical OR of this predicate and another.
     * <p>
     * When evaluating the composed predicate, if this predicate is {@code true}, then the {@code other} predicate
     * is not evaluated.
     * </p>
     *
     * @param other a predicate that will be logically-ORed with this predicate
     * @return a composed predicate that represents the short-circuiting logical OR of this predicate and the {@code other} predicate
     * @throws NullPointerException if {@code other} is null
     */
    default TriPredicate<T, U, V> or(TriPredicate<? super T, ? super U, ? super V> other) {
        Objects.requireNonNull(other);
        return (t, u, v) -> test(t, u, v) || other.test(t, u, v);
    }
}
