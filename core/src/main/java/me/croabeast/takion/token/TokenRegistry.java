package me.croabeast.takion.token;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Registry of tokens of one kind.
 *
 * <p>Replaces the three managers that carried the same CRUD surface with different getter names.
 *
 * @param <K> token type held by this registry
 * @since 2.0.0
 */
public interface TokenRegistry<K extends Token<?>> {

    /**
     * Registers a token, keeping the existing one when the id is taken.
     *
     * @param token token to register
     * @return {@code true} when it was stored
     */
    boolean load(@Nullable K token);

    /**
     * Removes the token with the given id.
     *
     * @param id token id
     * @return {@code true} when one was removed
     */
    boolean remove(@Nullable String id);

    /**
     * Replaces the token registered under an id.
     *
     * @param id    token id
     * @param token replacement
     * @return {@code true} when it was replaced
     */
    boolean edit(@Nullable String id, @Nullable K token);

    /**
     * Returns the token with the given id.
     *
     * @param id token id
     * @return the token, or {@code null}
     */
    @Nullable
    K get(@Nullable String id);

    /**
     * Returns every registered token.
     *
     * @return registered tokens
     */
    @NotNull
    Collection<K> getAll();

    /**
     * Returns whether an id is registered.
     *
     * @param id token id
     * @return {@code true} when present
     */
    default boolean has(@Nullable String id) {
        return get(id) != null;
    }

    /**
     * Removes every registered token.
     */
    void clear();
}
