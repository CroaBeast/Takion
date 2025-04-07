package me.croabeast.common;

import org.jetbrains.annotations.NotNull;

/**
 * A generic interface that defines a method for creating a copy of an object.
 * <p>
 * Classes that implement {@code Copyable} provide their own implementation of the {@link #copy()} method,
 * returning a new instance that is a copy of the original object. This can be useful for creating
 * deep or shallow copies of objects as needed.
 * </p>
 *
 * @param <T> the type of the object to be copied.
 */
public interface Copyable<T> {

    /**
     * Creates and returns a copy of this object.
     *
     * @return a new instance that is a copy of this object.
     */
    @NotNull
    T copy();
}
