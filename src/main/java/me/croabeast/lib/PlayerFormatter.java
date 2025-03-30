package me.croabeast.lib;

import org.bukkit.entity.Player;

import java.util.function.BiFunction;

/**
 * Represents a functional interface for formatting messages based on a {@link Player}.
 * <p>
 * This interface extends {@link BiFunction}, allowing it to take a {@code Player} and a {@code String}
 * as input and return a formatted string. It can be used to personalize messages with player-specific
 * details, such as names, ranks, or statistics.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * PlayerFormatter formatter = (player, message) ->
 *     message.replace("{player}", player.getName());
 *
 * String result = formatter.apply(somePlayer, "Hello, {player}!");
 * // Output: "Hello, Steve!" </pre>
 *
 * @see BiFunction
 */
public interface PlayerFormatter extends BiFunction<Player, String, String> {}
