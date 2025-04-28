package me.croabeast.takion.format;

import me.croabeast.common.util.ArrayUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A {@link Format} specialization that applies formatting based on both
 * an input string and an auxiliary argument of type {@code T}, optionally
 * in the context of one {@link Player} instance.
 * <p>
 * BiFormat is useful when your formatting logic depends not only on the text
 * itself but also on some external parameter (for example, a numeric threshold,
 * a lookup key, or any other object), and optionally on player-specific context.
 * </p>
 *
 * @param <T> the type of the auxiliary argument supplied to the format
 * @param <R> the result type produced by this format when applied to the input
 * @see Format
 * @see TextFormat
 */
public interface BiFormat<T, R> extends Format<R> {

    /**
     * Applies this format to the given input string, using the provided auxiliary
     * argument and optional player context.
     * <p>
     * Implementations must define how the {@code argument} and {@code player}
     * influence the transformation of {@code string} into an instance of {@code R}.
     * </p>
     * </p>
     *
     * @param player   the player providing context (might be {@code null})
     * @param argument an auxiliary value used in formatting (might be {@code null})
     * @param string   the input text to format
     * @return the formatted result of type {@code R}
     */
    @NotNull
    R accept(Player player, T argument, String string);

    /**
     * Convenience overload of {@link #accept(Player, T, String)} without player context.
     * <p>
     * Delegates to {@link #accept(Player, T, String)} with a {@code null}
     * player object.
     * </p>
     *
     * @param argument an auxiliary value used in formatting (might be {@code null})
     * @param string   the input text to format
     * @return the formatted result of type {@code R}
     */
    @NotNull
    default R accept(T argument, String string) {
        return accept(null, argument, string);
    }

    /**
     * Convenience overload of {@link #accept(Player, T, String)} without auxiliary argument.
     * <p>
     * Delegates to {@link #accept(Player, T, String)} with a {@code null}
     * argument.
     * </p>
     *
     * @param player a player providing context (might be {@code null})
     * @param string  the input text to format
     * @return the formatted result of type {@code R}
     */
    @NotNull
    default R accept(Player player, String string) {
        return accept(player, null, string);
    }
}
