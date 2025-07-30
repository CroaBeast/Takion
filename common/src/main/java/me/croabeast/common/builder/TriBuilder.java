package me.croabeast.common.builder;

import me.croabeast.common.function.TriConsumer;

import java.util.Objects;

/**
 * A builder interface for managing and modifying three related values in a fluent, chainable manner.
 * <p>
 * The {@code TriBuilder} interface extends {@link me.croabeast.common.builder.BaseBuilder} and provides access to three distinct elements:
 * the first, second, and third values. It supports modifications through a {@link TriConsumer},
 * allowing you to perform operations on all three elements at once.
 * </p>
 *
 * @param <T> the type of the first element
 * @param <U> the type of the second element
 * @param <V> the type of the third element
 * @param <B> the concrete builder type for fluent chaining
 * @see me.croabeast.common.builder.BaseBuilder
 */
public interface TriBuilder<T, U, V, B extends TriBuilder<T, U, V, B>> extends BaseBuilder<B> {

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
}
