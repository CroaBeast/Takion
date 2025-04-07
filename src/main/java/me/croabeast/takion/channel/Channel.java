package me.croabeast.takion.channel;

import me.croabeast.takion.message.MessageSender;
import me.croabeast.common.Regex;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Represents a communication channel for sending messages to players.
 * <p>
 * A {@code Channel} defines how messages are formatted, parsed, and delivered, including support for
 * custom prefixes, patterns, and case sensitivity settings. It supports multiple types of message delivery,
 * such as chat, action bar, titles, boss bars, JSON, or webhook-based messages.
 * </p>
 * <p>
 * Key functionalities include:
 * <ul>
 *   <li>Managing channel prefixes: Each channel can have one or more prefixes, where the first prefix
 *       is considered the primary prefix.</li>
 *   <li>Message formatting: The channel can define a regex pattern to match messages and then format
 *       them based on a target and parser context.</li>
 *   <li>Message delivery: Channels provide methods to send formatted messages to one or more players.</li>
 *   <li>Flag specification: Each channel is associated with a {@link Flag} which indicates the type of message
 *       (e.g. CHAT, ACTION_BAR, etc.).</li>
 * </ul>
 * </p>
 * <p>
 * Example usage:
 * <pre><code>
 * // Assuming an implementation of Channel exists, e.g., ChatChannel
 * Channel channel = new ChatChannel("global");
 * channel.addPrefix("[Global]");
 * String formatted = channel.formatString(targetPlayer, parserPlayer, "Hello, world!");
 * channel.send(targetPlayers, parserPlayer, formatted);</code></pre></p>
 *
 * @see MessageSender
 */
public interface Channel {

    /**
     * Retrieves the unique name of the channel.
     *
     * @return the channel name as a {@link String}
     */
    @NotNull
    String getName();

    /**
     * Retrieves the list of prefixes associated with this channel.
     * <p>
     * The first element of this list is considered the primary prefix.
     * </p>
     *
     * @return a {@link List} of prefix strings
     */
    @NotNull
    List<String> getPrefixes();

    /**
     * Retrieves the primary prefix of the channel.
     * <p>
     * This is a convenience method that returns the first prefix in the list.
     * </p>
     *
     * @return the primary prefix as a {@link String}
     */
    @NotNull
    default String getPrefix() {
        return getPrefixes().get(0);
    }

    /**
     * Replaces the current prefixes with a new collection of prefixes.
     *
     * @param prefixes a collection of new prefix strings (must not be {@code null})
     */
    default void setPrefixes(@NotNull Collection<String> prefixes) {
        getPrefixes().clear();
        getPrefixes().addAll(prefixes);
    }

    /**
     * Adds a new prefix to the channel.
     *
     * @param prefix the prefix string to add (must not be {@code null})
     */
    default void addPrefix(@NotNull String prefix) {
        getPrefixes().add(prefix);
    }

    /**
     * Removes a prefix from the channel.
     *
     * @param prefix the prefix string to remove (must not be {@code null})
     */
    default void removePrefix(@NotNull String prefix) {
        getPrefixes().remove(prefix);
    }

    /**
     * Indicates whether the channel's matching and formatting operations are case-sensitive.
     *
     * @return {@code true} if case-sensitive; {@code false} otherwise
     */
    boolean isCaseSensitive();

    /**
     * Sets whether the channel's operations should be case-sensitive.
     *
     * @param sensitive {@code true} for case-sensitive behavior; {@code false} for case-insensitive
     */
    void setCaseSensitive(boolean sensitive);

    /**
     * Retrieves the regex pattern used by this channel for matching messages.
     *
     * @return the regex pattern as a {@link String}, or {@code null} if not set
     */
    @Nullable @Regex
    String getPattern();

    /**
     * Sets the regex pattern to be used for matching messages in this channel.
     *
     * @param pattern the regex pattern as a {@link String} (can be {@code null})
     */
    void setPattern(@Nullable @Regex String pattern);

    /**
     * Creates a {@link Matcher} for the given input string using this channel's pattern.
     *
     * @param string the input string to match against the channel's pattern
     * @return a {@link Matcher} instance for the provided string
     */
    @NotNull
    Matcher matcher(String string);

    /**
     * Formats a message string according to the channel's formatting rules.
     * <p>
     * The method may apply prefixes, modify cases, or perform other transformations to produce the final message.
     * </p>
     *
     * @param target the player who will receive the message
     * @param parser the player context used for formatting (may affect colorization, etc.)
     * @param string the raw message to format
     * @return the formatted message as a {@link String}
     */
    String formatString(Player target, Player parser, String string);

    /**
     * Retrieves the flag that represents the type of messages sent through this channel.
     *
     * @return the channel's {@link Flag}
     */
    Flag getFlag();

    /**
     * Sends a formatted message to a collection of target players.
     * <p>
     * The message is processed according to the channel's formatting rules before delivery.
     * </p>
     *
     * @param targets the collection of players to send the message to (if {@code null}, the channel may use a default)
     * @param parser  the player context for formatting the message
     * @param message the message to send
     * @return {@code true} if the message was sent successfully; {@code false} otherwise
     */
    boolean send(Collection<? extends Player> targets, Player parser, String message);

    /**
     * Sends a formatted message to a collection of target players using no parser context.
     *
     * @param targets the collection of players to send the message to
     * @param input   the message to send
     * @return {@code true} if the message was sent successfully; {@code false} otherwise
     */
    default boolean send(Collection<? extends Player> targets, String input) {
        return send(targets, null, input);
    }

    /**
     * Sends a formatted message to a single player, optionally using a separate parser context.
     *
     * @param target the target player to send the message to; if {@code null}, the parser is used as the target
     * @param parser the player context for formatting the message
     * @param input  the message to send
     * @return {@code true} if the message was sent successfully; {@code false} otherwise
     */
    default boolean send(Player target, Player parser, String input) {
        Set<Player> targets = new HashSet<>();
        targets.add(target != null ? target : parser);
        return send(targets, parser, input);
    }

    /**
     * Sends a formatted message to a single player, using that player as both target and parser.
     *
     * @param player the player to send the message to
     * @param input  the message to send
     * @return {@code true} if the message was sent successfully; {@code false} otherwise
     */
    default boolean send(Player player, String input) {
        return send(player, player, input);
    }

    /**
     * Sends a formatted message using a default target (usually a broadcast or system message).
     *
     * @param input the message to send
     * @return {@code true} if the message was sent successfully; {@code false} otherwise
     */
    default boolean send(String input) {
        return send((Collection<? extends Player>) null, input);
    }

    /**
     * Enumeration of possible channel types.
     * <p>
     * The flag defines how a message should be delivered.
     * </p>
     */
    enum Flag {
        /**
         * A standard chat message.
         */
        CHAT,
        /**
         * A message displayed in the player's action bar.
         */
        ACTION_BAR,
        /**
         * A large title displayed in the center of the screen.
         */
        TITLE,
        /**
         * A message displayed on a boss bar.
         */
        BOSSBAR,
        /**
         * A message formatted in JSON.
         */
        JSON,
        /**
         * A message sent via a webhook.
         */
        WEBHOOK
    }
}
