package me.croabeast.common.builder;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents a builder that holds a single value of type {@code T} and allows modifications.
 *
 * @param <T> the type of value stored by the builder
 * @param <B> the specific builder type extending this interface
 */
public interface Builder<T, B extends Builder<T, B>> extends BaseBuilder<B> {

    /**
     * Retrieves the stored value.
     *
     * @return the current value
     */
    T getValue();

    /**
     * Modifies the stored value using the provided consumer function.
     *
     * @param consumer the function to modify the value
     *
     * @return the builder instance for fluent method calls
     * @throws NullPointerException if the consumer is null
     */
    default B modify(Consumer<T> consumer) {
        Objects.requireNonNull(consumer).accept(getValue());
        return instance();
    }
}
