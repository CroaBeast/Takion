package me.croabeast.takion.tag;

import me.croabeast.takion.token.TokenRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Registry of the tags known to a library instance.
 */
public interface TagManager extends TokenRegistry<Tag> {

    /**
     * Gets a tag by id.
     *
     * @param id tag id
     * @return registered tag, or null
     */
    @Nullable
    default Tag getTag(@Nullable String id) {
        return get(id);
    }

    /**
     * Gets all registered tags.
     *
     * @return registered tags
     */
    @NotNull
    default Collection<Tag> getTags() {
        return getAll();
    }

    /**
     * Checks whether a tag id is registered.
     *
     * @param id tag id
     * @return true when registered
     */
    default boolean hasTag(@Nullable String id) {
        return has(id);
    }
}
