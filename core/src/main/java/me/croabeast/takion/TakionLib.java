package me.croabeast.takion;

import me.croabeast.takion.token.Token;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.croabeast.common.Colorizer;
import me.croabeast.common.CommonServices;
import me.croabeast.common.util.Exceptions;
import me.croabeast.prismatic.PrismaticAPI;
import me.croabeast.scheduler.GlobalScheduler;
import me.croabeast.takion.action.ActionManager;
import me.croabeast.takion.channel.ChannelManager;
import me.croabeast.takion.character.CharacterManager;
import me.croabeast.takion.format.FormatManager;
import me.croabeast.takion.format.PlainFormat;
import me.croabeast.takion.format.StringFormat;
import me.croabeast.takion.logger.TakionLogger;
import me.croabeast.takion.message.MessageSender;
import me.croabeast.takion.message.TitleManager;
import me.croabeast.takion.marker.Marker;
import me.croabeast.takion.marker.MarkerManager;
import me.croabeast.takion.placeholder.PlaceholderManager;
import me.croabeast.takion.rule.GameRuleManager;
import me.croabeast.takion.tag.TagManager;
import me.croabeast.takion.tag.TagRenderer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
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
 *   <li><b>Text Processing:</b> Utilizes {@link PrismaticAPI} and a configurable {@link TakionMarkup}
 *       for colorization, component parsing, and string modifications.</li>
 * </ul>
 * </p>
 * <p>
 * The library is designed to be initialized by a plugin. Instances of {@code TakionLib} are managed in a global map
 * (via {@link TakionRegistry}) and can be retrieved using static methods such as {@link #fromPlugin(Plugin)} and {@link #getLib()}.
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

    private static final Map<String, TakionLib> CALLER_CACHE = new ConcurrentHashMap<>();

    /** Version where player heads and sprites became native Adventure object components. */
    private static final double OBJECT_TAG_VERSION = 21.9;

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

    @NotNull
    private final TagManager tagManager;

    @NotNull
    private final ActionManager actionManager;

    @NotNull
    private final MarkerManager markerManager;

    @NotNull
    private final TagRenderer<String> tagRenderer;

    @Getter(AccessLevel.NONE)
    @NotNull
    private final LegacyTagRenderer legacyTagRenderer;

    /**
     * Adventure renderer, created on first use.
     *
     * <p>The generic parameter is erased, so this field does not name a Kyori class in its
     * descriptor and holding it never forces Adventure to load.
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TagRenderer<net.kyori.adventure.text.Component> adventureRenderer;

    @Getter(AccessLevel.NONE)
    @NotNull
    private final ActionExecutor actionExecutor;

    /**
     * Processor used by PrismaticAPI chat components when Takion sends interactive chat.
     *
     * <p>The default processor keeps Takion behavior intact by applying placeholder replacement, small-caps
     * formatting, character alignment, colorization, and the configured line separator. Plugins can replace
     * it through {@link #setMarkup(TakionMarkup)} when they need custom component preprocessing.</p>
     */
    @Getter
    @Setter(AccessLevel.NONE)
    @NotNull
    private TakionMarkup markup = new TakionMarkup(this);

    /**
     * A preloaded instance of {@link MessageSender} used as a template.
     *
     * <p>Read through {@link #getLoadedSender()}, which hands out a copy. The generated setter is
     * suppressed because writing the template while its copies are in flight is not something a
     * consumer should be able to do by accident.
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final MessageSender loadedSender = new MessageSender(this);

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
                String[] array = splitByMarker(message, 2, Pattern.quote("<n>"));
                return builder(array[0], array.length == 2 ? array[1] : null);
            }
        };

        formatManager = new FormatManagerImpl();
        placeholderManager = new PlaceholderManagerImpl();
        channelManager = new ChannelManagerImpl(this);
        characterManager = new CharacterManagerImpl(this);
        gameRuleManager = new GameRuleManagerImpl(this);
        tagManager = new TagManagerImpl();
        actionManager = new ActionManagerImpl();
        markerManager = new MarkerManagerImpl(getDefaultLangPrefix(plugin));
        legacyTagRenderer = new LegacyTagRenderer(this);
        tagRenderer = legacyTagRenderer;
        actionExecutor = new ActionExecutor(this);

        if (plugin != null) TakionRegistry.register(plugin, this);
    }

    private static String getDefaultLangPrefix(Plugin plugin) {
        return "&e " + (plugin != null ? plugin.getName() : "Plugin") + " &8»&7";
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
     * @param markup markup format to use when parsing interactive messages
     * @return this library instance
     * @since 1.6.3
     */
    @NotNull
    public TakionLib setMarkup(@NotNull TakionMarkup markup) {
        this.markup = Objects.requireNonNull(markup, "markup");
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
     * Replaces the language prefix key using the registered {@code lang_prefix} marker.
     *
     * @param string the input string
     * @param remove if {@code true}, the prefix marker is removed instead
     * @return the resulting string with the language prefix marker applied
     */
    public String replacePrefixKey(String string, boolean remove) {
        if (StringUtils.isBlank(string)) return string;

        Map<String, Object> options = new HashMap<>();
        options.put("remove", remove);
        return applyMarker("lang_prefix", null, string, options);
    }

    /**
     * Splits a string using the registered {@code line_separator} marker.
     *
     * @param string the string to split
     * @param limit  the maximum number of splits (0 for no limit)
     * @return an array of substrings
     */
    public String[] splitString(String string, int limit) {
        return splitByMarker(string, limit, Pattern.quote("<n>"));
    }

    /**
     * Splits a string using the registered {@code line_separator} marker with no limit.
     *
     * @param string the string to split
     * @return an array of substrings
     */
    public String[] splitString(String string) {
        return splitString(string, 0);
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
        if (StringUtils.isBlank(string)) return string;

        String temp = placeholderManager.replace(parser, string);
        temp = PlainFormat.PLACEHOLDER_API.accept(parser, temp);

        return prepareText(temp);
    }

    /**
     * Resolves a single placeholder token, first through the registered placeholders and then
     * through PlaceholderAPI.
     *
     * <p>This is what a compiled message uses instead of {@link #replace(Player, String)}: the
     * parser already located the tokens, so a lookup replaces the scan of the whole message once
     * per registered placeholder.
     *
     * @param parser the player context
     * @param token  the token with its delimiters
     * @return the value, or {@code null} when nothing owns the token
     */
    public String resolvePlaceholder(Player parser, String token) {
        if (parser == null || StringUtils.isBlank(token)) return null;

        String value = placeholderManager.resolve(parser, token);
        if (value == null) {
            String papi = PlainFormat.PLACEHOLDER_API.accept(parser, token);
            if (papi.equals(token)) return null;
            value = papi;
        }

        // The message itself was prepared while parsing; a value only exists now, so it is prepared
        // as it arrives. That keeps markup inside a value working without reprocessing the whole
        // message on every render.
        return prepareText(value);
    }

    /**
     * Colorizes text whose placeholders were already resolved.
     *
     * <p>Same as {@link #colorize(Player, Player, String)} minus the placeholder pass, which scans
     * the message once per registered placeholder. The text formats still run here, after the
     * values are in place, so a value carrying markup is processed exactly as before.
     *
     * @param target the player receiving the text, or {@code null}
     * @param parser the player context for the remaining formats
     * @param string the input message string
     * @return the colorized message
     */
    public String colorizeResolved(Player target, Player parser, String string) {
        if (StringUtils.isBlank(string)) return string;

        // Only the PlaceholderAPI sweep is kept, as a net for syntax the parser does not tokenize.
        // The text formats already ran: on the message while parsing, and on each value as it was
        // resolved.
        String temp = PlainFormat.PLACEHOLDER_API.accept(parser, string);
        return PrismaticAPI.colorize(target == null ? parser : target, temp);
    }

    /**
     * Applies Takion text-only formats without running PrismaticAPI color parsing.
     *
     * @param string the input message string
     * @return the message after text-only Takion formats are applied
     */
    public String prepareText(String string) {
        if (StringUtils.isBlank(string)) return string;

        final StringFormat format = formatManager.get("SMALL_CAPS");
        string = format == null ? string : format.accept(null, string);

        return tagRenderer.render(null, null, string);
    }

    /**
     * Returns whether Adventure output can be produced at runtime.
     *
     * <p>Adventure is optional. This checks the classes Takion and PrismaticAPI need before
     * {@link #buildComponent(Player, Player, String)} is safe to call.
     *
     * @return {@code true} when Adventure is available
     * @since 2.0.0
     */
    public boolean isAdventureAvailable() {
        return isClassAvailable("net.kyori.adventure.text.Component")
                && isClassAvailable("net.kyori.adventure.text.minimessage.MiniMessage")
                && PrismaticAPI.isAdventureAvailable();
    }

    /**
     * Returns whether tags can render as native Adventure object components.
     *
     * <p>Player heads and sprites became components in 1.21.9; below that they fall back to text.
     *
     * @return {@code true} when object tags are supported
     * @since 2.0.0
     */
    public boolean supportsObjectTags() {
        return isAdventureAvailable()
                && isClassAvailable("net.kyori.adventure.text.object.ObjectContents")
                && me.croabeast.vnc.VNC.SERVER_VERSION >= OBJECT_TAG_VERSION;
    }

    /**
     * Builds an Adventure component from a raw message.
     *
     * <p>Guard with {@link #isAdventureAvailable()} when Adventure is optional in the consuming
     * plugin.
     *
     * @param receiver player receiving the output, or {@code null}
     * @param parser   player used to resolve placeholders, or {@code null}
     * @param string   raw message
     * @return the built component
     * @since 2.0.0
     */
    @NotNull
    public net.kyori.adventure.text.Component buildComponent(Player receiver, Player parser, String string) {
        return adventureRenderer().render(receiver, parser, string);
    }

    /**
     * Builds an Adventure component from a raw message.
     *
     * @param parser player used to resolve placeholders, or {@code null}
     * @param string raw message
     * @return the built component
     * @since 2.0.0
     */
    @NotNull
    public net.kyori.adventure.text.Component buildComponent(Player parser, String string) {
        return buildComponent(null, parser, string);
    }

    /**
     * Builds an Adventure component from a raw message, without player context.
     *
     * @param string raw message
     * @return the built component
     * @since 2.0.0
     */
    @NotNull
    public net.kyori.adventure.text.Component buildComponent(String string) {
        return buildComponent(null, null, string);
    }

    private TagRenderer<net.kyori.adventure.text.Component> adventureRenderer() {
        TagRenderer<net.kyori.adventure.text.Component> renderer = adventureRenderer;
        return renderer != null ?
                renderer :
                (adventureRenderer = me.croabeast.takion.adventure.AdventureTagRenderer.of(this));
    }

    private static boolean isClassAvailable(String name) {
        try {
            Class.forName(name, false, TakionLib.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Executes registered message actions without treating them as text formats.
     *
     * @param targets players affected by the action
     * @param parser  player used as parse context
     * @param channel message channel id
     * @param string  input message string
     * @return true when at least one action had an effect
     */
    public boolean executeActions(Collection<? extends Player> targets, Player parser, String channel, String string) {
        return actionExecutor.execute(targets, parser, channel, string);
    }

    /**
     * Renders the tags of a string to their measurement form, used by the alignment pass.
     */
    String measureTags(String string) {
        return legacyTagRenderer.renderMeasure(null, null, string);
    }

    public String applyMarker(String id, Player parser, String string) {
        return applyMarker(id, parser, string, null);
    }

    public String applyMarker(String id, Player parser, String string, Map<String, Object> options) {
        if (string == null) return null;
        return markerManager.apply(id, createMarkerContext(parser, options), string);
    }

    private Token.Context createMarkerContext(Player parser, Map<String, Object> options) {
        Map<String, Object> values = new HashMap<>();
        if (options != null) values.putAll(options);

        return new Token.SimpleContext()
                .setLib(this)
                .setParser(parser)
                .setOptions(values);
    }

    private String[] splitByMarker(String string, int limit, String fallbackRegex) {
        Marker marker = markerManager.getMarker("line_separator");
        return marker == null ? string.split(fallbackRegex, limit) : marker.getPattern().split(string, limit);
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

    /**
     * Retrieves the TakionLib instance associated with the given plugin.
     *
     * @param plugin the plugin for which to retrieve the TakionLib instance
     * @return the corresponding TakionLib instance, or the default if not found
     */
    public static TakionLib fromPlugin(Plugin plugin) {
        return plugin != null ?
                TakionRegistry.get(plugin) :
                TakionRegistry.NO_PLUGIN;
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
        if (TakionRegistry.isEmpty())
            return TakionRegistry.NO_PLUGIN;

        if (TakionRegistry.size() == 1)
            return TakionRegistry.values().iterator().next();

        TakionLib resolved = resolveCallerLib();
        return resolved != null ? resolved : TakionRegistry.NO_PLUGIN;
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
        if (TakionRegistry.isEmpty())
            return null;

        if (TakionRegistry.size() == 1)
            return TakionRegistry.getPlugins().iterator().next();

        TakionLib resolved = resolveCallerLib();
        return resolved != null ? resolved.plugin : null;
    }

    static void unregister(Plugin plugin) {
        if (plugin == null) return;

        TakionLib removed = TakionRegistry.remove(plugin);
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

        return TakionRegistry.NO_PLUGIN;
    }

    private static TakionLib resolveCallerLib(String className) {
        TakionLib resolved = null;
        for (Map.Entry<Plugin, TakionLib> entry : TakionRegistry.entries()) {
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
