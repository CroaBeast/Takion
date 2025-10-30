package me.croabeast.takion;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.croabeast.common.PlayerFormatter;
import me.croabeast.common.Regex;
import me.croabeast.common.applier.StringApplier;
import me.croabeast.common.util.Exceptions;
import me.croabeast.prismatic.PrismaticAPI;
import me.croabeast.scheduler.GlobalScheduler;
import me.croabeast.takion.channel.ChannelManager;
import me.croabeast.takion.character.CharacterManager;
import me.croabeast.takion.format.FormatManager;
import me.croabeast.takion.format.PlainFormat;
import me.croabeast.takion.format.StringFormat;
import me.croabeast.takion.logger.TakionLogger;
import me.croabeast.takion.message.MessageSender;
import me.croabeast.takion.message.TitleManager;
import me.croabeast.takion.placeholder.PlaceholderManager;
import me.croabeast.takion.rule.GameRuleManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * The central class of the Takion library that provides a comprehensive suite of utilities
 * for managing messaging, logging, placeholder replacement, and other plugin functionalities.
 * <p>
 * TakionLib integrates various components including:
 * <ul>
 *   <li><b>Logging:</b> Managed via {@link TakionLogger} for both server and plugin-specific logs.</li>
 *   <li><b>Channel Management:</b> Handled by a {@link ChannelManager} to define and identify communication channels.</li>
 *   <li><b>Title Management:</b> Configured through a {@link TitleManager} implementation to display titles.</li>
 *   <li><b>Placeholder Management:</b> Managed by a {@link PlaceholderManager} to dynamically replace tokens in messages.</li>
 *   <li><b>Character Management:</b> Provided by a {@link CharacterManager} for text alignment and formatting.</li>
 *   <li><b>Messaging:</b> Facilitated by {@link MessageSender} for sending formatted messages to players.</li>
 *   <li><b>Text Processing:</b> Utilizes {@link PrismaticAPI}, {@link StringApplier}, and related utilities for colorization and string modifications.</li>
 * </ul>
 * </p>
 * <p>
 * Example usage:
 * <pre><code>
 * // Initialize TakionLib in your plugin's onEnable method:
 * TakionLib lib = new TakionLib(this);
 *
 * // Send a message to a player:
 * lib.getLoadedSender().addPlaceholder("{player}", player.getName())
 *     .send("Hello, {player}! Welcome to our server.");
 *
 * // Format a title and send it:
 * lib.getTitleManager().builder("Welcome", "Enjoy your stay!").send(player);
 * </code></pre>
 * </p>
 *
 * @see TakionLogger
 * @see ChannelManager
 * @see TitleManager
 * @see PlaceholderManager
 * @see CharacterManager
 * @see MessageSender
 * @see PrismaticAPI
 */
@Getter @Setter
public class TakionLib {

    private static final TakionLib NO_PLUGIN = new TakionLib(null);
    private static TakionLib instance = NO_PLUGIN;

    /**
     * The plugin instance associated with this TakionLib.*/
    private final Plugin plugin;

    /**
     * The global scheduler used for scheduling tasks across the server.
     *
     * <p> This scheduler is used for managing asynchronous tasks and periodic operations
     * that need to run independently of the plugin's lifecycle.
     *
     * <p> Example usage:
     * <pre><code>
     * // Schedule a task to run after 5 seconds:
     * TakionLib.getLib().getScheduler().runTaskLater(() -> {
     *     // Your task code here
     * }, 100L); // 100 ticks = 5 seconds
     * </code></pre>
     *
     * Note: This field can be {@code null} if the plugin is not set or if the scheduler is not available.
     * It is recommended to check for null before using the scheduler.
     * @see GlobalScheduler
     */
    private GlobalScheduler scheduler = null;

    /**
     * The logger for server-level logs (configured to not use plugin logger).
     */
    private TakionLogger serverLogger;

    /**
     * The logger for plugin-specific logs.
     */
    private TakionLogger logger;

    /**
     * Manages communication channels.
     */
    @NotNull
    private final ChannelManager channelManager;

    /**
     * Manages title messages.
     */
    @NotNull
    private final TitleManager titleManager;

    /**
     * Manages dynamic placeholder replacement.
     */
    @NotNull
    private final PlaceholderManager placeholderManager;

    /**
     * Manages character formatting and alignment.
     */
    @NotNull
    private final CharacterManager characterManager;

    @NotNull
    private final FormatManager formatManager;

    @NotNull
    private final GameRuleManager gameRuleManager;

    /**
     * The language prefix displayed in messages.
     */
    private String langPrefix;

    /**
     * The key used to denote where the language prefix should be replaced.
     */
    private String langPrefixKey = "<P>";

    /**
     * The delimiter used to separate lines in formatted messages.
     * <p>
     * This is stored as a regex pattern (e.g., Pattern.quote("&lt;n&gt;")).
     * </p>
     */
    @Regex
    private String lineSeparator = Pattern.quote("<n>");

    /**
     * The prefix used for center alignment in messages.
     */
    private String centerPrefix = "[C]";

    /**
     * A preloaded instance of {@link MessageSender} used as a template.
     */
    @Getter(AccessLevel.NONE)
    private MessageSender loadedSender = new MessageSender(this);

    /**
     * Constructs a new {@code TakionLib} instance and initializes all components.
     *
     * @param plugin the plugin instance associated with this library (must not be {@code null})
     */
    public TakionLib(Plugin plugin) {
        this.plugin = plugin;

        this.serverLogger = new TakionLogger(this, false);
        this.logger = new TakionLogger(this);

        if (plugin != null)
            this.scheduler = GlobalScheduler.getScheduler(plugin);

        titleManager = new TitleManager() {
            @Setter @Getter
            private int fadeInTicks = 8, stayTicks = 50, fadeOutTicks = 8;

            @Override
            public Builder builder(String message) {
                Exceptions.validate(message, StringUtils::isNotBlank);
                String[] array = splitString(message, 2);
                return builder(array[0], array.length == 2 ? array[1] : null);
            }
        };

        formatManager = new FormatManagerImpl();
        placeholderManager = new PlaceholderManagerImpl();
        channelManager = new ChannelManagerImpl(this);
        characterManager = new CharacterManagerImpl(this);
        gameRuleManager = new GameRuleManagerImpl(this);

        langPrefix = "&e " +
                (plugin != null ? plugin.getName() : "Plugin") +
                " &8»&7";
        if (plugin != null && instance != null) instance = this;
    }

    /**
     * Retrieves the plugin instance associated with this TakionLib.
     *
     * @return the plugin (never {@code null})
     * @throws NullPointerException if the plugin is not set
     */
    @NotNull
    public final Plugin getPlugin() {
        return Objects.requireNonNull(plugin);
    }

    /**
     * Returns a copy of the preloaded MessageSender.
     * <p>
     * This allows modifications and sending of messages without altering the original sender.
     * </p>
     *
     * @return a copied {@link MessageSender} instance
     */
    public final MessageSender getLoadedSender() {
        return loadedSender.copy();
    }

    /**
     * Loads a mapping from string keys to configuration sections from the provided configuration.
     * <p>
     * Only keys with a corresponding non-null configuration section are included.
     * </p>
     *
     * @param section the configuration section to load from
     * @return a {@link TreeMap} mapping keys to their respective configuration sections
     */
    @NotNull
    protected TreeMap<String, ConfigurationSection> loadMapFromConfiguration(ConfigurationSection section) {
        if (section == null) return new TreeMap<>();

        TreeMap<String, ConfigurationSection> loaded = new TreeMap<>();
        for (String s : section.getKeys(false)) {
            ConfigurationSection c = section.getConfigurationSection(s);
            if (c != null) loaded.put(s, c);
        }

        return loaded;
    }

    /**
     * Retrieves loaded webhook configurations.
     *
     * @return an empty {@link TreeMap} (to be implemented as needed)
     */
    @NotNull
    public TreeMap<String, ConfigurationSection> getLoadedWebhooks() {
        return new TreeMap<>();
    }

    /**
     * Retrieves loaded bossbar configurations.
     *
     * @return an empty {@link TreeMap} (to be implemented as needed)
     */
    @NotNull
    public TreeMap<String, ConfigurationSection> getLoadedBossbars() {
        return new TreeMap<>();
    }

    /**
     * Replaces the language prefix key in a string with the actual language prefix or an empty string.
     * <p>
     * This is used to dynamically insert the plugin's language prefix into messages.
     * </p>
     *
     * @param string the input string
     * @param remove if {@code true}, the prefix is removed; otherwise, it is replaced with {@link #langPrefix}
     * @return the resulting string with the language prefix key replaced
     */
    public String replacePrefixKey(String string, boolean remove) {
        if (StringUtils.isBlank(string)) return string;

        String temp = remove ? "" : getLangPrefix();
        return string.replace(getLangPrefixKey(), temp);
    }

    /**
     * Splits a string using the configured line separator, with an optional limit on the number of splits.
     *
     * @param s     the string to split
     * @param limit the maximum number of splits (0 for no limit)
     * @return an array of substrings
     */
    public String[] splitString(String s, int limit) {
        return s.split(getLineSeparator(), limit);
    }

    /**
     * Splits a string using the configured line separator with no limit.
     *
     * @param s the string to split
     * @return an array of substrings
     */
    public String[] splitString(String s) {
        return splitString(s, 0);
    }

    /**
     * Replaces placeholders and applies player formatting functions on a string.
     * <p>
     * This method uses the {@link PlaceholderManager} and a {@link PlayerFormatter} to process
     * the string, and then applies a character action for further formatting.
     * </p>
     *
     * @param parser the player context for placeholder replacement
     * @param string the input message string
     * @return the processed string after placeholder and function application
     */
    public String replace(Player parser, String string) {
        return StringApplier.simplified(string)
                .apply(s -> placeholderManager.replace(parser, s))
                .apply(s -> PlainFormat.PLACEHOLDER_API.accept(parser, s))
                .apply(s -> {
                    StringFormat format = formatManager.get("character");
                    return format.accept(s);
                }).toString();
    }

    /**
     * Colorizes a string and replaces placeholders for a target and parser.
     *
     * @param target the target player to receive the colored text; if {@code null}, the parser is used
     * @param parser the player context for formatting
     * @param string the input message string
     * @return the final colorized and formatted message
     */
    public String colorize(Player target, Player parser, String string) {
        return PrismaticAPI.colorize(
                target == null ? parser : target, replace(parser, string));
    }

    /**
     * Colorizes a string using a single player as context.
     *
     * @param player the player to use for formatting context
     * @param string the input message string
     * @return the colorized message
     */
    public String colorize(Player player, String string) {
        return colorize(null, player, string);
    }

    /**
     * Colorizes a string without any player context.
     *
     * @param string the input message string
     * @return the colorized message
     */
    public String colorize(String string) {
        return colorize(null, string);
    }

    public static TakionLib fromPlugin(Plugin plugin) {
        return plugin != null ? (instance.plugin == plugin ? instance : new TakionLib(plugin)) : NO_PLUGIN;
    }

    /**
     * Returns the singleton instance of TakionLib.
     * <p>
     * This method is used to access the library from anywhere in the codebase.
     * </p>
     *
     * @return the singleton instance of TakionLib
     */
    @NotNull
    public static TakionLib getLib() {
        return instance;
    }
}
