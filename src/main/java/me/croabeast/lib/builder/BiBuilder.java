package me.croabeast.lib.builder;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Represents a builder that holds two values and supports modification operations.
 *
 * @param <T> the type of the first value
 * @param <U> the type of the second value
 * @param <B> the specific builder type extending this interface
 */
public interface BiBuilder<T, U, B extends BiBuilder<T, U, B>> extends Builder<B> {

    /**
     * Retrieves the first stored value.
     *
     * @return the first value
     */
    T getFirst();

    /**
     * Retrieves the second stored value.
     *
     * @return the second value
     */
    U getSecond();

    /**
     * Modifies both stored values using the provided bi-consumer function.
     *
     * @param consumer the function to modify the values
     *
     * @return the builder instance for fluent method calls
     * @throws NullPointerException if the consumer is null
     */
    default B modify(BiConsumer<T, U> consumer) {
        Objects.requireNonNull(consumer).accept(getFirst(), getSecond());
        return instance();
    }
}
