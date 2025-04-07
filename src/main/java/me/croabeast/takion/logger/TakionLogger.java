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
 * <p>
 * <strong>Key Features:</strong>
 * <ul>
 *   <li>Enhanced message formatting using a chain of text transformations.</li>
 *   <li>Dynamic selection of logging backend (Paper or Bukkit).</li>
 *   <li>Integration with external APIs like Prismatic for colorization and stripping of JSON formatting.</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Usage Example:</strong>
 * <pre>
 *     TakionLib lib = TakionLib.fromPlugin(myPlugin);
 *     TakionLogger logger = new TakionLogger(lib);
 *     logger.setColored(true).setStripPrefix(false);
 *
 *     // Log multiple messages at INFO level
 *     logger.log(LogLevel.INFO, "This is a test message", "Another log entry");
 *
 *     // Create a Bukkit-compatible logger for integration with other systems
 *     Logger bukkitLogger = TakionLogger.createBukkit(myPlugin);
 *     bukkitLogger.info("Bukkit log message with enhanced formatting!");
 * </pre>
 * </p>
 *
 * @see TakionLib
 * @see LogLevel
 * @see PluginLogger
 */
public class TakionLogger {

    static final class PaperLogger {

        private final Class<?> clazz;
        private final Object logger;

        @SneakyThrows
        PaperLogger(String name) {
            logger = Class.forName("net.kyori.adventure.text.logger.slf4j.ComponentLogger")
                    .getMethod("logger", String.class).invoke(null, name);
            clazz = logger.getClass();
        }

        void log(LogLevel level, String string) {
            level = level != null ? level : LogLevel.INFO;
            try {
                Class<?> legacy = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
                Method method = clazz.getMethod(level.getName(), Class.forName("net.kyori.adventure.text.Component"));
                method.setAccessible(true);
                Method section = legacy.getMethod("legacySection");
                section.setAccessible(true);
                Object component = legacy.getMethod("deserialize", String.class)
                        .invoke(section.invoke(null), string);
                method.invoke(logger, component);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The TakionLib instance providing shared resources and configuration.
     */
    private final TakionLib lib;

    /**
     * The underlying Bukkit logger (either from the plugin or the global Bukkit logger).
     */
    @Getter
    private final Logger bukkit;

    /**
     * An optional Paper logger for enhanced logging on supported servers.
     */
    private PaperLogger paper;

    /**
     * Whether to strip prefixes from log messages.
     */
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
     * This formatter applies a simplified text transformation, replaces prefix keys,
     * ensures proper line separation, aligns characters, strips JSON formatting, and finally
     * colorizes (or strips) the text using Prismatic's API.
     * </p>
     */
    interface LibStringFunction {
        String get(String message, TakionLib lib, boolean strip, boolean colored);
    }

    /**
     * A static formatter function used to process log messages.
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
     * Constructs a new {@code TakionLogger} using the provided TakionLib instance.
     * <p>
     * If the usePlugin flag is true and a plugin instance is available from TakionLib, the plugin's logger is used;
     * otherwise, the global Bukkit logger is used.
     * Additionally, if the server is running Paper with a supported version, a PaperLogger is instantiated.
     * </p>
     *
     * @param lib       the TakionLib instance containing configuration and utilities
     * @param usePlugin whether to use the plugin's logger instead of the global Bukkit logger
     */
    public TakionLogger(@NotNull TakionLib lib, boolean usePlugin) {
        this.lib = lib;
        Plugin plugin = null;
        try {
            plugin = lib.getPlugin();
        } catch (Exception ignored) {}
        bukkit = usePlugin && plugin != null ? plugin.getLogger() : Bukkit.getLogger();
        if (ServerInfoUtils.PAPER_ENABLED && ServerInfoUtils.SERVER_VERSION >= 18.2) {
            try {
                paper = new PaperLogger(usePlugin && plugin != null ? plugin.getName() : "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Constructs a new {@code TakionLogger} with the default usePlugin value (true).
     *
     * @param lib the TakionLib instance containing configuration and utilities
     */
    public TakionLogger(@NotNull TakionLib lib) {
        this(lib, true);
    }

    /**
     * Inner class representing a single log line to be processed and output.
     * <p>
     * Each {@code LoggerLine} encapsulates a log level and a message (after formatting via {@link #FORMATTER}).
     * It then logs the message using either the PaperLogger (if available) or the Bukkit logger.
     * </p>
     */
    private class LoggerLine {
        private final LogLevel level;
        private final String message;

        /**
         * Constructs a new LoggerLine with the specified log level and message.
         * <p>
         * The message is immediately formatted using the static {@link #FORMATTER} function.
         * </p>
         *
         * @param level   the log level for this message
         * @param message the raw message to log
         */
        private LoggerLine(LogLevel level, String message) {
            this.level = level;
            if (message != null)
                message = FORMATTER.get(message, lib, isStripPrefix(), isColored());
            this.message = message;
        }

        /**
         * Outputs the log line using the appropriate logging backend.
         * <p>
         * If a PaperLogger is available, it is used to log the message. Otherwise, the Bukkit logger is used.
         * </p>
         */
        private void log() {
            if (paper != null) {
                paper.log(level, message);
                return;
            }
            bukkit.log(level.toJava(), message);
        }
    }

    /**
     * Logs a collection of messages at the specified log level.
     * <p>
     * Each non-null message in the collection is processed and output individually.
     * </p>
     *
     * @param level    the log level to use (default is INFO if null)
     * @param messages a collection of messages to log
     */
    public void log(LogLevel level, Collection<String> messages) {
        if (level == null) level = LogLevel.INFO;
        for (String message : CollectionBuilder.of(messages).filter(Objects::nonNull))
            new LoggerLine(level, message).log();
    }

    /**
     * Logs multiple messages at the specified log level.
     *
     * @param level    the log level to use (default is INFO if null)
     * @param messages an array of messages to log
     */
    public void log(LogLevel level, String... messages) {
        log(level, ArrayUtils.toList(messages));
    }

    /**
     * Logs multiple messages at the default log level (INFO).
     *
     * @param messages an array of messages to log
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
            private final TakionLib lib = TakionLib.fromPlugin(plugin);
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
