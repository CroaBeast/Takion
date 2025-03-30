package me.croabeast.lib.builder;

import java.util.Objects;

/**
 * A builder interface for managing and modifying three related values in a fluent, chainable manner.
 * <p>
 * The {@code TriBuilder} interface extends {@link Builder} and provides access to three distinct elements:
 * the first, second, and third values. It supports modifications through a {@link TriConsumer},
 * allowing you to perform operations on all three elements at once.
 * </p>
 *
 * @param <T> the type of the first element
 * @param <U> the type of the second element
 * @param <V> the type of the third element
 * @param <B> the concrete builder type for fluent chaining
 * @see Builder
 */
public interface TriBuilder<T, U, V, B extends TriBuilder<T, U, V, B>> extends Builder<B> {

    /**
     * Retrieves the first element.
     *
     * @return the first element of type {@code T}
     */
    T getFirst();

    /**
     * Retrieves the second element.
     *
     * @return the second element of type {@code U}
     */
    U getSecond();

    /**
     * Retrieves the third element.
     *
     * @return the third element of type {@code V}
     */
    V getThird();

    /**
     * Applies the provided {@link TriConsumer} to the three elements.
     * <p>
     * This method allows you to perform an action on all three values simultaneously.
     * The consumer must not be {@code null}. After execution, it returns the builder instance
     * for fluent method chaining.
     * </p>
     *
     * @param consumer a {@code TriConsumer} that processes the first, second, and third elements
     * @return the current builder instance for further modifications
     * @throws NullPointerException if the consumer is {@code null}
     */
    default B modify(TriConsumer<T, U, V> consumer) {
        Objects.requireNonNull(consumer).accept(getFirst(), getSecond(), getThird());
        return instance();
    }

    /**
     * A functional interface representing an operation that accepts three input arguments and returns no result.
     * <p>
     * This is similar to {@link java.util.function.Consumer} but accepts three parameters.
     * It is intended for operations that operate on three elements simultaneously.
     * </p>
     *
     * @param <T> the type of the first argument to the operation
     * @param <U> the type of the second argument to the operation
     * @param <V> the type of the third argument to the operation
     */
    @FunctionalInterface
    interface TriConsumer<T, U, V> {

        /**
         * Performs this operation on the given arguments.
         *
         * @param t the first input argument
         * @param u the second input argument
         * @param v the third input argument
         */
        void accept(T t, U u, V v);

        /**
         * Returns a composed {@code TriConsumer} that performs, in sequence, this operation followed by the {@code after} operation.
         * <p>
         * If performing either operation throws an exception, it is relayed to the caller.
         * </p>
         *
         * @param after the operation to perform after this operation
         * @return a composed {@code TriConsumer} that performs in sequence this operation followed by the {@code after} operation
         * @throws NullPointerException if {@code after} is {@code null}
         */
        default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after) {
            Objects.requireNonNull(after);
            return (t, u, v) -> {
                accept(t, u, v);
                after.accept(t, u, v);
            };
        }
    }
}
