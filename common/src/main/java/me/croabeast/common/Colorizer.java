package me.croabeast.common;

import org.bukkit.entity.Player;

/**
 * A functional interface for applying color and formatting transformations to strings.
 * <p>
 * A {@code Colorizer} receives an optional target {@link Player}, an optional parser {@link Player},
 * and a raw string, and returns a colorized (or otherwise transformed) version of that string.
 * The target and parser contexts allow implementations to apply player-specific formatting,
 * such as permission-based color codes or PlaceholderAPI replacements.
 * </p>
 */
@FunctionalInterface
public interface Colorizer {

    /**
     * Applies color and formatting transformations to the given string.
     *
     * @param target the player who will receive the colorized text (may be {@code null})
     * @param parser the player context used as the source for formatting (may be {@code null})
     * @param string the raw string to colorize
     * @return the colorized string
     */
    String colorize(Player target, Player parser, String string);
}
