package me.croabeast.lib.builder;

import org.jetbrains.annotations.NotNull;

/**
 * A foundational interface for builder patterns, ensuring a self-referential type.
 *
 * @param <B> the specific builder type extending this interface
 */
public interface Builder<B extends Builder<B>> {

    /**
     * Provides an instance of the builder, ensuring fluent-style method chaining.
     *
     * @return the builder instance
     */
    @NotNull
    B instance();
}
