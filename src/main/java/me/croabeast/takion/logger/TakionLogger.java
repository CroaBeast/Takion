package me.croabeast.takion.logger;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import me.croabeast.takion.TakionLib;
import me.croabeast.common.CollectionBuilder;
import me.croabeast.common.applier.StringApplier;
import me.croabeast.common.util.ArrayUtils;
import me.croabeast.common.util.ServerInfoUtils;
import me.croabeast.common.util.TextUtils;
import me.croabeast.prismatic.PrismaticAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A sophisticated logger that bridges between Bukkit's built-in logging system and enhanced logging
 * capabilities provided by external APIs (such as Paper and Prismatic).
 * <p>
 * The {@code TakionLogger} offers advanced message formatting, including prefix replacement, text
 * colorization, and alignment based on configurable settings. It dynamically adapts to the server environment:
 * if running on a Paper server with a supported version, it uses the {@code PaperLogger} implementation;
 * otherwise, it falls back to Bukkit's logger.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Enhanced message formatting using a chain of text transformations.</li>
 *   <li>Dynamic selection of logging backend (Paper or Bukkit).</li>
 *   <li>Integration with external APIs like Prismatic for colorization and stripping of JSON formatting.</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre><code>
 * TakionLib lib = TakionLib.fromPlugin(myPlugin);
 * TakionLogger logger = new TakionLogger(lib);
 * logger.setColored(true).setStripPrefix(false);
 *
 * // Log multiple messages at INFO level
 * logger.log(LogLevel.INFO, "This is a test message", "Another log entry");
 *
 * // Create a Bukkit-compatible logger for integration with other systems
 * Logger bukkitLogger = TakionLogger.createBukkit(myPlugin);
 * bukkitLogger.info("Bukkit log message with enhanced formatting!");
 * </code></pre>
 *
 * @see TakionLib
 * @see LogLevel
 * @see PluginLogger
 */
public class TakionLogger {

    /**
     * Internal interface defining the contract for a logging implementation.
     */
    private interface Loggable {
        /**
         * Logs a single message at the specified level.
         *
         * @param level   the log level to use
         * @param message the message to log
         */
        void log(LogLevel level, String message);
    }

    /**
     * Provides Paper-specific logging capabilities using Adventure's Component API.
     */
    static final class PaperLogger implements Loggable {

        private final Class<?> clazz;
        private final Object logger;

        /**
         * Constructs a new {@code PaperLogger} instance that routes log output
         * through Adventure's SLF4J ComponentLogger.
         *
         * @param name the logger name (usually the plugin name)
         * @throws Exception if the Paper ComponentLogger class cannot be found or invoked
         */
        @SneakyThrows
        PaperLogger(String name) {
            logger = Class.forName("net.kyori.adventure.text.logger.slf4j.ComponentLogger")
                    .getMethod("logger", String.class)
                    .invoke(null, name);
            clazz = logger.getClass();
        }

        /**
         * Deserializes a legacy-formatted string into an Adventure {@link Component}.
         *
         * @param string the legacy-formatted string (using section signs)
         * @return the parsed {@link Component}
         */
        Component deserialize(String string) {
            return LegacyComponentSerializer.legacySection().deserialize(string);
        }

        /**
         * Logs a message at the specified level using the underlying Paper logger.
         *
         * @param level  the {@link LogLevel} to use (defaults to INFO if {@code null})
         * @param string the message to log
         */
        @Override
        public void log(LogLevel level, String string) {
            level = level != null ? level : LogLevel.INFO;
            try {
                Method method = clazz.getMethod(level.getName(), Component.class);
                method.setAccessible(true);
                method.invoke(logger, deserialize(string));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** The {@link TakionLib} instance providing shared resources and configuration. */
    private final TakionLib lib;

    /** The underlying logging backend (either Bukkit or plugin logger). */
    @Getter
    private final Loggable bukkit;

    /** Optional Paper logger for enhanced output on supported servers. */
    private Loggable paper;

    /** Whether to strip prefixes from log messages before formatting. */
    @Accessors(chain = true)
    @Getter @Setter
    private boolean stripPrefix = false;

    /**
     * Whether to apply colorization to log messages.
     */
    @Accessors(chain = true)
    @Getter @Setter
    private boolean colored = true;

    /**
     * Functional interface to format a log message with a series of text transformations.
     * <p>
     * This formatter applies:
     * <ol>
     *   <li>Prefix key replacement</li>
     *   <li>Line separator handling and alignment</li>
     *   <li>JSON stripping</li>
     *   <li>Colorization or stripping via PrismaticAPI</li>
     * </ol>
     * </p>
     */
    interface LibStringFunction {
        /**
         * Applies the formatting to the given message.
         *
         * @param message the original message
         * @param lib     the {@link TakionLib} context
         * @param strip   whether to strip prefixes
         * @param colored whether to colorize
         * @return the formatted string
         */
        String get(String message, TakionLib lib, boolean strip, boolean colored);
    }

    /**
     * Default static formatter used by {@link TakionLogger}.
     */
    static final LibStringFunction FORMATTER =
            (message, l, st, b) -> StringApplier.simplified(message)
                    .apply(s -> l.replacePrefixKey(s, st))
                    .apply(s -> {
                        String split = l.getLineSeparator();
                        final String r = "\\\\[QE]";
                        return s.replaceAll(split, "&f" + split.replaceAll(r, ""));
                    })
                    .apply(l.getCharacterManager()::align)
                    .apply(TextUtils.STRIP_JSON)
                    .apply(b ? PrismaticAPI::colorize : PrismaticAPI::stripAll)
                    .toString();

    /**
     * Constructs a new {@code TakionLogger}.
     * <p>
     * If {@code usePlugin} is {@code true} and a plugin is available from {@code lib},
     * the plugin's {@link PluginLogger} is used; otherwise, the global {@link Bukkit#getLogger()} is used.
     * If running on Paper ≥ 1.18.2, initializes a new {@code PaperLogger}.
     * </p>
     *
     * @param lib       the {@link TakionLib} instance
     * @param usePlugin whether to use the plugin-specific logger
     */
    public TakionLogger(@NotNull TakionLib lib, boolean usePlugin) {
        this.lib = Objects.requireNonNull(lib);
        Plugin plugin = null;
        try {
            plugin = lib.getPlugin();
        } catch (Exception ignored) {}

        usePlugin = usePlugin && plugin != null;
        Logger logger = usePlugin ? plugin.getLogger() : Bukkit.getLogger();
        bukkit = (level, msg) -> logger.log(level.toJava(), msg);

        if (ServerInfoUtils.PAPER_ENABLED && ServerInfoUtils.SERVER_VERSION >= 18.2) {
            try {
                paper = new PaperLogger(usePlugin ? plugin.getName() : "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Constructs a new {@code TakionLogger} using the plugin logger by default.
     *
     * @param lib the {@link TakionLib} instance
     */
    public TakionLogger(@NotNull TakionLib lib) {
        this(lib, true);
    }

    /**
     * Internal helper that formats and logs a collection of messages.
     */
    private class LogCollection {
        private final Collection<String> collection;
        private final LogLevel level;

        LogCollection(LogLevel level, Collection<String> collection) {
            this.level = level;
            this.collection = CollectionBuilder.of(collection)
                    .filter(Objects::nonNull)
                    .apply(s -> FORMATTER.get(s, lib, isStripPrefix(), isColored()))
                    .toList();
        }

        void log() {
            collection.forEach(s -> (paper == null ? bukkit : paper).log(level, s));
        }
    }

    /**
     * Logs a collection of messages at the given level.
     *
     * @param level    the {@link LogLevel} to use (INFO if {@code null})
     * @param messages the messages to log
     */
    public void log(LogLevel level, Collection<String> messages) {
        if (level == null) level = LogLevel.INFO;
        new LogCollection(level, messages).log();
    }

    /**
     * Logs multiple messages at the specified level.
     *
     * @param level    the {@link LogLevel} to use (INFO if {@code null})
     * @param messages the messages to log
     */
    public void log(LogLevel level, String... messages) {
        log(level, ArrayUtils.toList(messages));
    }

    /**
     * Logs multiple messages at the default level (INFO).
     *
     * @param messages the messages to log
     */
    public void log(String... messages) {
        log(null, messages);
    }

    /**
     * Creates a Bukkit-compatible logger that applies enhanced formatting using the TakionLib formatter.
     * <p>
     * This logger wraps a {@link PluginLogger} and overrides the {@code log(LogRecord)} method to process
     * the message before output.
     * </p>
     *
     * @param plugin the plugin for which to create the logger
     * @return a {@link Logger} instance that integrates with Bukkit's logging system
     */
    public static Logger createBukkit(Plugin plugin) {
        return new PluginLogger(Objects.requireNonNull(plugin)) {

            final TakionLib lib = TakionLib.fromPlugin(plugin);

            @Override
            public void log(@NotNull LogRecord record) {
                record.setMessage(FORMATTER.get(
                        record.getMessage(),
                        lib,
                        lib.getLogger().isStripPrefix(),
                        lib.getLogger().isColored()
                ));
                super.log(record);
            }
        };
    }
}
