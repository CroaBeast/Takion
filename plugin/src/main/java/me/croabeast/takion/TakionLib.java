package me.croabeast.takion;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.croabeast.common.Colorizer;
import me.croabeast.common.CommonServices;
import me.croabeast.common.Regex;
import me.croabeast.common.util.Exceptions;
import me.croabeast.prismatic.PrismaticAPI;
import me.croabeast.prismatic.chat.ChatProcessor;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
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
 *   <li><b>Text Processing:</b> Utilizes {@link PrismaticAPI} and a configurable {@link ChatProcessor}
 *       for colorization, component parsing, and string modifications.</li>
 * </ul>
 * </p>
 * <p>
 * The library is designed to be initialized by a plugin. Instances of {@code TakionLib} are managed in a global map
 * (via {@code TakionPlugin}) and can be retrieved using static methods such as {@link #fromPlugin(Plugin)} and {@link #getLib()}.
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
public class TakionLib implements Colorizer {

    private static final TakionLib NO_PLUGIN = TakionPlugin.NO_PLUGIN_INSTANCE;
    private static final Map<String, TakionLib> CALLER_CACHE = new ConcurrentHashMap<>();

    /**
     * The plugin instance associated with this TakionLib.
     */
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
     * Processor used by PrismaticAPI chat components when Takion sends interactive chat.
     *
     * <p>The default processor keeps Takion behavior intact by applying placeholder replacement, small-caps
     * formatting, character alignment, colorization, and the configured line separator. Plugins can replace
     * it through {@link #setChatProcessor(ChatProcessor)} when they need custom component preprocessing.</p>
     */
    @Getter
    @Setter(AccessLevel.NONE)
    @NotNull
    private ChatProcessor chatProcessor = new TakionChatProcessor(this);

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

        if (plugin != null) {
            this.scheduler = GlobalScheduler.getScheduler(plugin);
            CommonServices.setPlugin(() -> TakionLib.getLib().getPlugin());
        }

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
        if (plugin != null) TakionPlugin.LIBRARIES.put(plugin, this);
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
     * Sets the processor used by PrismaticAPI chat components created by Takion.
     *
     * <p>Changing this value affects interactive chat parsing, including messages sent through the chat
     * channel. Use the default processor unless a plugin needs to inject additional preprocessing.</p>
     *
     * @param chatProcessor processor to use for interactive chat components
     * @return this library instance
     * @since 1.6.3
     */
    @NotNull
    public TakionLib setChatProcessor(@NotNull ChatProcessor chatProcessor) {
        this.chatProcessor = Objects.requireNonNull(chatProcessor, "chatProcessor");
        return this;
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
     * This method uses the {@link PlaceholderManager} to process the string, and
     * then applies a character action for further formatting.
     * </p>
     *
     * @param parser the player context for placeholder replacement
     * @param string the input message string
     * @return the processed string after placeholder and function application
     */
    public String replace(Player parser, String string) {
        return replace(parser, string, true);
    }

    public String replace(Player parser, String string, boolean processPlayerHead) {
        if (StringUtils.isBlank(string)) return string;

        String temp = placeholderManager.replace(parser, string);
        temp = PlainFormat.PLACEHOLDER_API.accept(parser, temp);

        StringFormat playerHead = formatManager.get("PLAYER_HEAD");
        if (processPlayerHead && playerHead != null)
            temp = playerHead.accept(parser, temp);

        StringFormat character = formatManager.get("character");
        return character != null ? character.accept(temp) : temp;
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
        return colorize(target, parser, string, true);
    }

    public String colorize(Player target, Player parser, String string, boolean processPlayerHead) {
        return PrismaticAPI.colorize(
                target == null ? parser : target, replace(parser, string, processPlayerHead));
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

    /**
     * Retrieves the TakionLib instance associated with the given plugin.
     *
     * @param plugin the plugin for which to retrieve the TakionLib instance
     * @return the corresponding TakionLib instance, or the default if not found
     */
    public static TakionLib fromPlugin(Plugin plugin) {
        return plugin != null ?
                TakionPlugin.LIBRARIES.getOrDefault(plugin, TakionPlugin.NO_PLUGIN_INSTANCE) :
                TakionPlugin.NO_PLUGIN_INSTANCE;
    }

    /**
     * Retrieves the TakionLib instance associated with the providing plugin (determined from the call stack).
     *
     * <p> Not recommended for use in any context other than the main plugin class.
     *
     * @return the TakionLib instance, or the default instance if none is found
     */
    @NotNull
    public static TakionLib getLib() {
        if (TakionPlugin.LIBRARIES.isEmpty())
            return NO_PLUGIN;

        if (TakionPlugin.LIBRARIES.size() == 1)
            return TakionPlugin.LIBRARIES.values().iterator().next();

        TakionLib resolved = resolveCallerLib();
        return resolved != null ? resolved : NO_PLUGIN;
    }

    /**
     * Retrieves the plugin that provided the call to TakionLib.
     * <p>
     * This method examines the call stack to determine the plugin responsible for the call,
     * then returns the plugin using {@link JavaPlugin#getProvidingPlugin(Class)}.
     * </p>
     *
     * @return the providing plugin, or {@code null} if not determinable
     */
    static Plugin getProvidingPlugin() {
        if (TakionPlugin.LIBRARIES.isEmpty())
            return null;

        if (TakionPlugin.LIBRARIES.size() == 1)
            return TakionPlugin.LIBRARIES.keySet().iterator().next();

        TakionLib resolved = resolveCallerLib();
        return resolved != null ? resolved.plugin : null;
    }

    static void unregister(Plugin plugin) {
        if (plugin == null) return;

        TakionLib removed = TakionPlugin.LIBRARIES.remove(plugin);
        if (removed == null) return;

        CALLER_CACHE.entrySet().removeIf(entry -> entry.getValue() == removed);
    }

    private static TakionLib resolveCallerLib() {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (int i = 2; i < stackTrace.length; i++) {
                String className = stackTrace[i].getClassName();
                if (isInternalCaller(className))
                    continue;

                TakionLib cached = CALLER_CACHE.get(className);
                if (cached != null)
                    return cached;

                TakionLib resolved = resolveCallerLib(className);
                if (resolved != null) {
                    CALLER_CACHE.put(className, resolved);
                    return resolved;
                }
            }
        } catch (Exception ignored) {}

        return NO_PLUGIN;
    }

    private static TakionLib resolveCallerLib(String className) {
        TakionLib resolved = null;
        for (Map.Entry<Plugin, TakionLib> entry : TakionPlugin.LIBRARIES.entrySet()) {
            ClassLoader loader = entry.getKey().getClass().getClassLoader();
            try {
                Class.forName(className, false, loader);
                if (resolved != null && resolved != entry.getValue())
                    return null;

                resolved = entry.getValue();
            } catch (ClassNotFoundException ignored) {}
        }
        if (resolved != null)
            return resolved;

        try {
            return fromPlugin(JavaPlugin.getProvidingPlugin(Class.forName(className)));
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isInternalCaller(String className) {
        return className.startsWith("java.") ||
                className.startsWith("javax.") ||
                className.startsWith("sun.") ||
                className.startsWith("jdk.") ||
                className.startsWith("org.bukkit.") ||
                className.startsWith("me.croabeast.takion.") ||
                className.startsWith("me.croabeast.common.");
    }
}
