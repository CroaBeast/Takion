package me.croabeast.takion;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.croabeast.lib.Regex;
import me.croabeast.lib.util.Exceptions;
import me.croabeast.takion.channel.ChannelManager;
import me.croabeast.takion.character.CharacterManager;
import me.croabeast.takion.character.SmallCaps;
import me.croabeast.takion.logger.TakionLogger;
import me.croabeast.takion.message.MessageSender;
import me.croabeast.takion.message.TitleManager;
import me.croabeast.takion.placeholder.PlaceholderManager;
import me.croabeast.lib.PlayerFormatter;
import me.croabeast.lib.applier.StringApplier;
import me.croabeast.lib.util.TextUtils;
import me.croabeast.prismatic.PrismaticAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
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
 * The library is designed to be initialized by a plugin. Instances of {@code TakionLib} are managed in a global map
 * (via {@link TakionPlugin}) and can be retrieved using static methods such as {@link #fromPlugin(Plugin)} and {@link #getLib()}.
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
@Getter
@Setter
public class TakionLib {

    /**
     * The plugin instance associated with this TakionLib.
     */
    private final Plugin plugin;

    /**
     * The logger for server-level logs (configured to not use plugin logger).
     */
    private TakionLogger serverLogger;

    /**
     * The logger for plugin-specific logs.
     */
    private TakionLogger logger;

    /**
     * The Vault holder used for integrating with external permission/chat APIs.
     */
    @NotNull
    private final VaultHolder<?> vaultHolder;

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
     * Action to be performed when blank spaces are detected in a message.
     * <p>
     * This is a {@link PatternAction} that, when triggered, can insert additional spaces.
     * </p>
     */
    @NotNull
    private final PatternAction<Boolean> blankSpacesAction;

    /**
     * Action to be applied for character modifications in messages.
     */
    @NotNull
    private final PatternAction<String> characterAction;

    /**
     * Action to be applied for converting text to small capital letters.
     */
    @NotNull
    private final PatternAction<String> smallCapsAction;

    /**
     * A pre-loaded instance of {@link MessageSender} used as a template.
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

        titleManager = new TitleManager() {
            @Setter @Getter
            private int fadeInTicks = 8, stayTicks = 50, fadeOutTicks = 8;

            @Override
            public Builder builder(String message) {
                Exceptions.validate(StringUtils::isNotBlank, message);
                String[] array = splitString(message, 2);
                return builder(array[0], array.length == 2 ? array[1] : null);
            }
        };

        vaultHolder = HolderUtils.loadHolder();

        channelManager = new ChannelMngr(this);
        placeholderManager = new PlaceholderMngr();
        characterManager = new CharacterMngr(this);

        langPrefix = "&e " + (plugin != null ? plugin.getName() : "Plugin") + " &8»&7";

        blankSpacesAction = new PatternAction<Boolean>("(?i)<add_space:(\\d+)>") {
            @Override
            public @NotNull Boolean act(Collection<? extends Player> targets, String string) {
                if ((targets == null ||
                        targets.isEmpty()) || StringUtils.isBlank(string))
                    return false;

                Matcher matcher = createMatcher(string);
                if (!matcher.find()) return false;

                int count = 0;
                try {
                    count = Integer.parseInt(matcher.group(1));
                } catch (Exception ignored) {}
                if (count <= 0) return false;

                boolean atLeastOneIsSent = false;
                for (Player player : targets) {
                    if (player == null) continue;

                    for (int i = 0; i < count; i++)
                        player.sendMessage("");

                    if (!atLeastOneIsSent)
                        atLeastOneIsSent = true;
                }

                return atLeastOneIsSent;
            }
        };
        characterAction = new StringAction("<[Uu]:([a-fA-F\\d]{4})>") {
            @NotNull
            public String act(String string) {
                if (StringUtils.isBlank(string)) return string;

                Matcher m = createMatcher(string);
                while (m.find()) {
                    char c = (char) Integer.parseInt(m.group(1), 16);
                    string = string.replace(m.group(), c + "");
                }

                return string;
            }
        };
        @Regex String s = "(small_caps|sc)";
        smallCapsAction = new StringAction("(?i)<" + s + ">(.+?)</" + s + ">") {
            @NotNull
            public String act(String string) {
                if (StringUtils.isBlank(string))
                    return string;

                Matcher matcher = createMatcher(string);
                while (matcher.find())
                    string = string.replace(
                            matcher.group(),
                            SmallCaps.toSmallCaps(matcher.group(2))
                    );

                return string;
            }
        };

        TakionPlugin.libs.put(plugin, this);
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
     * Returns a copy of the pre-loaded MessageSender.
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
        PlayerFormatter papi = TextUtils.PARSE_PLACEHOLDER_API;
        return StringApplier.simplified(string)
                .apply(s -> placeholderManager.replace(parser, s))
                .apply(s -> papi.apply(parser, s))
                .apply(characterAction::act).toString();
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
     * Sets the TakionLib instance of the target plugin to that of the source plugin.
     *
     * @param source the plugin whose TakionLib instance should be used
     * @param target the plugin to set with the source's TakionLib instance
     */
    public static void setLibFromSource(Plugin source, Plugin target) {
        TakionPlugin.libs.put(target, TakionPlugin.libs.get(source));
    }

    /**
     * Sets the default TakionLib instance for the specified plugin.
     *
     * @param plugin the plugin for which to set the default TakionLib instance
     */
    public static void setLibAsDefault(Plugin plugin) {
        TakionPlugin.libs.put(plugin, TakionPlugin.noPluginInstance);
    }

    /**
     * Retrieves the TakionLib instance associated with the given plugin.
     *
     * @param plugin the plugin for which to retrieve the TakionLib instance
     * @return the corresponding TakionLib instance, or the default if not found
     */
    public static TakionLib fromPlugin(Plugin plugin) {
        return plugin != null ?
                TakionPlugin.libs.getOrDefault(plugin, TakionPlugin.noPluginInstance) :
                TakionPlugin.noPluginInstance;
    }

    /**
     * Retrieves the TakionLib instance associated with the providing plugin (determined from the call stack).
     *
     * @return the TakionLib instance, or the default instance if none is found
     */
    @NotNull
    public static TakionLib getLib() {
        return fromPlugin(getProvidingPlugin());
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
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            Class<?> thisClass = TakionLib.class, result = TakionLib.class;
            for (int i = 2; i < stackTrace.length; i++) {
                final StackTraceElement stack = stackTrace[i];
                if (stack.getClassName().equals(thisClass.getName()))
                    continue;
                try {
                    result = Class.forName(stack.getClassName());
                } catch (Exception ignored) {}
            }
            return JavaPlugin.getProvidingPlugin(result);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * A private abstract class for performing string-based actions via regular expressions.
     * <p>
     * {@code StringAction} extends {@link PatternAction} to allow actions that operate on strings,
     * such as converting text to small caps or processing Unicode values.
     * </p>
     */
    private static abstract class StringAction extends PatternAction<String> {

        StringAction(@Regex String pattern) {
            super(pattern);
        }

        @Override
        public @NotNull String act(Collection<? extends Player> players, String string) {
            return act(string);
        }

        @Override
        public @NotNull String act(Player player, String string) {
            return act(string);
        }

        /**
         * Processes the input string and returns the modified string.
         *
         * @param string the input string to process
         * @return the modified string after action is applied
         */
        @NotNull
        public abstract String act(String string);
    }
}
