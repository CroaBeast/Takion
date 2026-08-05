package me.croabeast.takion.action;

import me.croabeast.takion.token.TokenRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Registry of the actions known to a library instance.
 */
public interface ActionManager extends TokenRegistry<Action> {

    @Nullable
    default Action getAction(@Nullable String id) {
        return get(id);
    }

    @NotNull
    default Collection<Action> getActions() {
        return getAll();
    }

    default boolean hasAction(@Nullable String id) {
        return has(id);
    }
}
