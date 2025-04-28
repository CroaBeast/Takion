package me.croabeast.takion.format;

import me.croabeast.common.util.ArrayUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Extends {@link Format} to support transformations that depend on a collection of players
 * rather than a single player or none.
 * <p>
 * {@code ContextualFormat} introduces the core method {@link #accept(Collection, String)} which
 * receives the full player context for formats that need to broadcast or aggregate across
 * multiple players at once.
 * </p>
 *
 * @param <T> the type of the result produced when this format is applied
 */
public interface ContextualFormat<T> extends Format<T> {

    /**
     * Transform the given text using this format in the context of multiple players.
     * <p>
     * Implementations must override this method to perform the desired logic
     * based on the raw input string and the full player collection.
     * </p>
     *
     * @param players the player contexts (may be {@code null} or empty)
     * @param string  the text to transform (never {@code null})
     * @return the result of applying this format across the given players
     */
    @NotNull
    T accept(Collection<? extends Player> players, String string);

    /**
     * Convenience overload for a single player context.
     * <p>
     * Delegates to {@link #accept(Collection, String)} by wrapping
     * the given {@link Player} into a singleton collection.
     * Override only if single-player handling must differ.
     * </p>
     *
     * @param player the player context
     * @param string the text to transform
     * @return the result of applying this format
     */
    @NotNull
    default T accept(Player player, String string) {
        return accept(ArrayUtils.toList(player), string);
    }
}
