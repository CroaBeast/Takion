package me.croabeast.takion.message;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.croabeast.common.CollectionBuilder;
import me.croabeast.common.Copyable;
import me.croabeast.common.PlayerFormatter;
import me.croabeast.common.applier.StringApplier;
import me.croabeast.common.util.ArrayUtils;
import me.croabeast.common.util.ReplaceUtils;
import me.croabeast.takion.TakionLib;
import me.croabeast.takion.channel.Channel;
import me.croabeast.takion.format.ContextualFormat;
import me.croabeast.takion.placeholder.Placeholder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * A versatile message sender that formats and dispatches messages to players or external services.
 * <p>
 * {@code MessageSender} is designed for use in Minecraft plugins to process messages before sending.
 * It supports dynamic message formatting, placeholder replacement, function-based text modifications,
 * and conditional logging. Messages are formatted based on a chain of text appliers, and can be sent
 * to one or more target players or even via webhook if no target is provided.
 * </p>
 * <p>
 * Key features:
 * <ul>
 *   <li>Manage a collection of {@link Placeholder} objects that are dynamically replaced in messages.</li>
 *   <li>Support for custom {@link PlayerFormatter} functions to alter messages based on player context.</li>
 *   <li>Flexible target setting: messages can be sent to individual players, collections of players,
 *       or broadcast as system messages.</li>
 *   <li>Flag filtering using {@link Channel.Flag} to control which message types are sent.</li>
 *   <li>Automatic logging of messages if enabled, with an error prefix for unsent messages.</li>
 * </ul>
 * </p>
 * <p>
 * Example usage:
 * <pre><code>
 * TakionLib lib = TakionLib.fromPlugin(plugin);
 *
 * MessageSender sender = new MessageSender(lib, player);
 * sender.addPlaceholder("{player}", player.getName())
 *       .addFunctions(s -&gt; s.toUpperCase())
 *       .setFlags(Channel.Flag.CHAT)
 *       .setLogger(true)
 *       .setErrorPrefix("&c[ERROR]&7 ");
 *
 * // Send a message to the player
 * sender.send("Hello, {player}! Welcome to the server.");
 * </code></pre>
 * </p>
 *
 * @see Placeholder
 * @see PlayerFormatter
 * @see Channel.Flag
 * @see TakionLib
 */
@Accessors(chain = true)
@Setter
public class MessageSender implements Copyable<MessageSender> {

    /**
     * The TakionLib instance providing configuration, channels, placeholders, and logging utilities.
     */
    private final TakionLib lib;

    /**
     * The set of target players to which messages will be sent.
     */
    private final Set<Player> targets = new HashSet<>();

    /**
     * The player used as a context for parsing and formatting messages.
     */
    private Player parser;

    /**
     * A list of placeholders to be applied in the message.
     */
    private final List<Placeholder<?>> placeholders;

    /**
     * A set of player formatter functions that modify the message based on the player context.
     */
    private final Set<PlayerFormatter> functions;

    /**
     * A set of flags indicating which types of messages are allowed to be sent.
     */
    private final Set<Channel.Flag> flags = new HashSet<>();

    /**
     * Flag indicating whether the message should also be logged.
     */
    @Getter
    private boolean logger = true;

    /**
     * Flag indicating whether placeholder replacement should be case sensitive.
     */
    @Getter
    private boolean sensitive = true;

    /**
     * A prefix to be prepended to error messages when a message is not sent.
     */
    @Getter
    private String errorPrefix = "&c[NOT_SENT]&7 ";

    /**
     * Constructs a new {@code MessageSender} with the specified library instance, target players, and parser.
     *
     * @param lib     the TakionLib instance (must not be {@code null})
     * @param targets the collection of target players; may be {@code null} or empty
     * @param parser  the player used as the formatting context; may be {@code null}
     */
    public MessageSender(TakionLib lib, Collection<? extends Player> targets, Player parser) {
        this.lib = lib;
        placeholders = new ArrayList<>();
        functions = new HashSet<>();
        setTargets(targets);
        this.parser = parser;
    }

    /**
     * Constructs a new {@code MessageSender} with the specified library instance and target players.
     *
     * @param lib     the TakionLib instance
     * @param targets the collection of target players
     */
    public MessageSender(TakionLib lib, Collection<? extends Player> targets) {
        this(lib, targets, null);
    }

    /**
     * Constructs a new {@code MessageSender} with the specified library instance and a single player.
     *
     * @param lib    the TakionLib instance
     * @param player the player to target (if {@code null}, no target is set)
     */
    public MessageSender(TakionLib lib, Player player) {
        this(lib, player != null ? ArrayUtils.toList(player) : null, player);
    }

    /**
     * Constructs a new {@code MessageSender} with the specified library instance and no target players.
     *
     * @param lib the TakionLib instance
     */
    public MessageSender(TakionLib lib) {
        this(lib, null, null);
    }

    /**
     * Copy constructor to create a new {@code MessageSender} from an existing instance.
     *
     * @param sender the {@code MessageSender} to copy
     */
    public MessageSender(MessageSender sender) {
        this(sender.lib, sender.targets, sender.parser);
        this.flags.addAll(sender.flags);
        this.placeholders.addAll(sender.placeholders);
        this.functions.addAll(sender.functions);
        this.errorPrefix = sender.getErrorPrefix();
        this.logger = sender.isLogger();
        this.sensitive = sender.isSensitive();
    }

    /**
     * Sets the target players for message sending.
     * <p>
     * If the provided collection is {@code null} or empty, the targets are not modified.
     * </p>
     *
     * @param targets the collection of players to set as targets
     * @return this {@code MessageSender} instance for chaining
     */
    public MessageSender setTargets(Collection<? extends Player> targets) {
        if (targets == null || targets.isEmpty())
            return this;
        this.targets.clear();
        this.targets.addAll(targets);
        this.targets.removeIf(Objects::isNull);
        return this;
    }

    /**
     * Sets the target players from an array of {@link CommandSender} objects.
     * <p>
     * Only those that are instances of {@link Player} are retained.
     * </p>
     *
     * @param senders an array of command senders
     * @return this {@code MessageSender} instance for chaining
     */
    public MessageSender setTargets(CommandSender... senders) {
        return setTargets(CollectionBuilder
                .of(senders)
                .map(s -> s instanceof Player ? (Player) s : null)
                .filter(Objects::nonNull).toSet());
    }

    /**
     * Sets the target players from an array of {@link Player} objects.
     *
     * @param targets an array of players
     * @return this {@code MessageSender} instance for chaining
     */
    public MessageSender setTargets(Player... targets) {
        return setTargets(ArrayUtils.toList(targets));
    }

    /**
     * Adds one or more {@link PlayerFormatter} functions to modify messages.
     *
     * @param functions an array of player formatters
     * @return this {@code MessageSender} instance for chaining
     */
    public MessageSender addFunctions(PlayerFormatter... functions) {
        this.functions.addAll(ArrayUtils.toList(functions));
        return this;
    }

    /**
     * Adds one or more {@link UnaryOperator} functions to modify messages.
     * <p>
     * Each operator is wrapped as a {@link PlayerFormatter} that ignores the player parameter.
     * </p>
     *
     * @param operators an array of unary operators to modify messages
     * @return this {@code MessageSender} instance for chaining
     */
    @SafeVarargs
    public final MessageSender addFunctions(UnaryOperator<String>... operators) {
        try {
            ArrayUtils.toList(operators).forEach(u -> {
                if (u != null)
                    functions.add((p, s) -> u.apply(s));
            });
        } catch (Exception ignored) {}
        return this;
    }

    /**
     * Sets the flags that determine which types of messages are allowed to be sent.
     *
     * @param flags an array of {@link Channel.Flag} values
     * @return this {@code MessageSender} instance for chaining
     */
    public MessageSender setFlags(Channel.Flag... flags) {
        this.flags.clear();
        this.flags.addAll(ArrayUtils.toList(flags));
        return this;
    }

    /**
     * Adds a {@link Placeholder} to be applied in the message.
     *
     * @param placeholder the placeholder to add (its sensitive flag is updated to match this sender)
     * @param <T>         the type of the placeholder value
     * @return this {@code MessageSender} instance for chaining
     */
    public <T> MessageSender addPlaceholder(Placeholder<T> placeholder) {
        placeholders.add(placeholder.setSensitive(isSensitive()));
        return this;
    }

    /**
     * Adds a placeholder with a constant value.
     *
     * @param key   the placeholder key
     * @param value the constant value for the placeholder
     * @param <T>   the type of the placeholder value
     * @return this {@code MessageSender} instance for chaining
     */
    public <T> MessageSender addPlaceholder(String key, T value) {
        return addPlaceholder(new Placeholder<>(key, value).setSensitive(isSensitive()));
    }

    /**
     * Adds multiple placeholders from a collection.
     *
     * @param placeholders a collection of placeholders to add
     * @return this {@code MessageSender} instance for chaining
     */
    public MessageSender addPlaceholders(Collection<? extends Placeholder<?>> placeholders) {
        Objects.requireNonNull(placeholders).forEach(this::addPlaceholder);
        return this;
    }

    /**
     * Adds multiple placeholders from an array.
     *
     * @param placeholders an array of placeholders to add
     * @return this {@code MessageSender} instance for chaining
     */
    public MessageSender addPlaceholders(Placeholder<?>... placeholders) {
        return addPlaceholders(ArrayUtils.toList(placeholders));
    }

    /**
     * Adds placeholders using parallel arrays of keys and values.
     * <p>
     * Both arrays must have the same length and be applicable for replacement.
     * </p>
     *
     * @param keys   an array of placeholder keys
     * @param values an array of placeholder values
     * @param <T>    the type of the placeholder values
     * @return this {@code MessageSender} instance for chaining
     * @throws NullPointerException if keys and values are not applicable for replacement
     */
    @SafeVarargs
    public final <T> MessageSender addPlaceholders(String[] keys, T... values) {
        if (ReplaceUtils.isApplicable(keys, values)) {
            for (int i = 0; i < keys.length; i++)
                try {
                    addPlaceholder(keys[i], values[i]);
                } catch (Exception ignored) {}
            return this;
        }
        throw new NullPointerException("Keys/Values are not applicable for replacements.");
    }

    /**
     * Creates a copy of this {@code MessageSender}.
     *
     * @return a new {@code MessageSender} instance with the same configuration
     */
    @NotNull
    public MessageSender copy() {
        return new MessageSender(this);
    }

    /**
     * Sends a list of messages to the target players.
     * <p>
     * Each message is processed through formatting (placeholder replacement, function application, etc.)
     * and then sent individually. If no target players are defined, messages are logged or sent via webhook if allowed.
     * </p>
     *
     * @param strings a list of raw message strings
     * @return {@code true} if at least one message was sent successfully; {@code false} otherwise
     */
    public boolean send(List<String> strings) {
        if (strings == null) return false;

        final List<Message> messages = new ArrayList<>();
        for (String s : strings)
            if (s != null) messages.add(new Message(s));
        if (messages.isEmpty()) return false;

        if (targets.isEmpty()) {
            boolean atLeastOneIsSent = false;
            for (Message message : messages) {
                if (message.flag == Channel.Flag.WEBHOOK && message.isAllowed())
                    atLeastOneIsSent = message.send(null);
                message.log(atLeastOneIsSent);
            }
            return atLeastOneIsSent;
        }

        boolean atLeastOneIsSent = false;
        for (Message message : messages) {
            if (message.isBlank() && message.isAllowed()) {
                message.log(true);
                targets.forEach(message::send);
                continue;
            }

            ContextualFormat<Boolean> format = lib.getFormatManager().get("BLANK_SPACES");
            if (format.accept(targets, message.message) && !message.isAllowed())
                continue;

            boolean wasSent = false, wasLogged = false;
            for (Player target : targets) {
                boolean isSent = message.send(target);
                if (isSent && !wasSent)
                    wasSent = true;
                if (wasLogged) continue;
                message.log(isSent);
                wasLogged = true;
            }

            if (!atLeastOneIsSent) atLeastOneIsSent = wasSent;
        }

        return atLeastOneIsSent;
    }

    /**
     * Sends an array of messages to the target players.
     *
     * @param strings an array of raw message strings
     * @return {@code true} if at least one message was sent successfully; {@code false} otherwise
     */
    public boolean send(String... strings) {
        return send(ArrayUtils.toList(strings));
    }

    /**
     * Inner class representing an individual message to be processed and sent.
     * <p>
     * A {@code Message} encapsulates a raw message string, determines the appropriate channel based on the message,
     * applies formatting, logs the message if required, and sends the message to a player.
     * </p>
     */
    private class Message {

        /**
         * The communication channel through which this message will be sent.
         */
        private final Channel channel;

        /**
         * The flag associated with the channel, representing the type of message (e.g., CHAT, ACTION_BAR).
         */
        private final Channel.Flag flag;

        /**
         * The raw message string.
         */
        private final String message;

        /**
         * Constructs a new {@code Message} with the provided raw message.
         * <p>
         * The channel is determined by the {@link TakionLib}'s channel manager.
         * </p>
         *
         * @param message the raw message to be processed
         */
        private Message(String message) {
            this.message = message;
            channel = lib.getChannelManager().identify(message);
            flag = channel.getFlag();
        }

        /**
         * Checks if the message string is blank.
         *
         * @return {@code true} if the message is blank; {@code false} otherwise
         */
        boolean isBlank() {
            return StringUtils.isBlank(message);
        }

        /**
         * Processes and formats the raw message by applying prefix replacements,
         * player format functions, and placeholder substitutions.
         *
         * @return the fully formatted message as a {@link String}
         */
        String formatMessage() {
            StringApplier applier = StringApplier.simplified(message)
                    .apply(s -> lib.replacePrefixKey(s, false));
            functions.forEach(f -> applier.apply(s -> f.apply(parser, s)));
            placeholders.forEach(p -> applier.apply(s -> p.replace(parser, s)));
            return applier.toString();
        }

        /**
         * Determines whether the message is allowed to be sent based on the channel flag and the sender's flags.
         *
         * @return {@code true} if sending is allowed; {@code false} otherwise
         */
        boolean isAllowed() {
            return flags.isEmpty() || flags.contains(flag);
        }

        /**
         * Logs the message using the {@link TakionLib} logger.
         * <p>
         * The message is formatted, prefixed with an error prefix if not sent, and then logged.
         * </p>
         *
         * @param sent {@code true} if the message was sent successfully; {@code false} otherwise
         */
        void log(boolean sent) {
            if (!isLogger()) return;
            String s = lib.getPlaceholderManager().replace(parser, formatMessage());
            String error = getErrorPrefix();
            lib.getLogger().log((sent || StringUtils.isBlank(error) ? "" : error) + s);
        }

        /**
         * Sends the formatted message to a single player.
         *
         * @param target the target player
         * @return {@code true} if the message was sent successfully; {@code false} otherwise
         */
        boolean send(Player target) {
            return isAllowed() && channel.send(target, parser, formatMessage());
        }
    }
}
