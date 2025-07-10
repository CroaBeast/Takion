package me.croabeast.takion.format;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A specialization of {@link Format} for formats that operate solely on the input string,
 * without regard to player context.
 * <p>
 * {@code TextFormat} implementations define a single abstract {@code accept(String)} method
 * that transforms or interprets the provided text and returns a result of type {@code T}.
 * The context‑aware {@code accept} overloads that take a {@link Player} simply delegate to
 * the string‑only variant by default.
 * </p>
 *
 * @param <T> the result type produced by this format when applied to input text
 * @see Format
 */
public interface TextFormat<T> extends Format<T> {

    /**
     * Applies this format to the given text and returns the transformed result.
     * <p>
     * Implementations must override this method to perform the desired formatting,
     * parsing, or transformation logic on the raw input string.
     * </p>
     *
     * @param string the input text to format or process (never {@code null})
     * @return the result of applying this format to {@code string}
     */
    @NotNull
    T accept(String string);

    /**
     * Applies this format to the given text for a single player context.
     * <p>
     * The default implementation ignores the {@code player} argument and delegates
     * directly to {@link #accept(String)}. Override only if your format requires
     * player-specific handling when a single {@link Player} is provided.
     * </p>
     *
     * @param player the player context
     * @param string the input text to format or process
     * @return the result of applying this format to {@code string}
     */
    @NotNull
    default T accept(Player player, String string) {
        return accept(string);
    }
}
