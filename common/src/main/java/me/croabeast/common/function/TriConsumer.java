package me.croabeast.common.function;

import java.util.Objects;

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
public interface TriConsumer<T, U, V> {

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