package me.croabeast.takion.logger;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import me.croabeast.takion.TakionLib;
import me.croabeast.takion.misc.StringAligner;
import me.croabeast.lib.CollectionBuilder;
import me.croabeast.lib.applier.StringApplier;
import me.croabeast.lib.util.ArrayUtils;
import me.croabeast.lib.util.ServerInfoUtils;
import me.croabeast.lib.util.TextUtils;
import me.croabeast.prismatic.PrismaticAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@Accessors(chain = true)
public class TakionLogger {

    interface RawLogger {
        void log(LogLevel level, String message);
    }

    @SneakyThrows
    static Class<?> from(String name) {
        return Class.forName("net.kyori.adventure.text." + name);
    }

    static class PaperLogger implements RawLogger {

        private final Class<?> clazz;
        private final Object logger;

        @SneakyThrows
        PaperLogger(Plugin plugin) {
            if (ServerInfoUtils.PAPER_ENABLED) {
                String name = plugin != null ? plugin.getName() : "";

                logger = from("logger.slf4j.ComponentLogger")
                        .getMethod("logger", String.class).invoke(null, name);

                clazz = logger.getClass();
                return;
            }

            throw new IllegalAccessException("Paper is not being used");
        }

        @Override
        public void log(LogLevel level, String string) {
            level = level != null ? level : LogLevel.INFO;

            try {
                Class<?> legacy = from("serializer.legacy.LegacyComponentSerializer");

                Method method = clazz.getMethod(level.getName(), from("Component"));
                method.setAccessible(true);

                Method section = legacy.getMethod("legacySection");
                section.setAccessible(true);

                method.invoke(logger,
                        legacy
                                .getMethod("deserialize", String.class)
                                .invoke(
                                        section.invoke(null),
                                        string
                                ));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private final TakionLib lib;

    private final Logger bukkitLogger;
    private RawLogger paperLogger;

    /**
     * Whether to strip prefixes from log messages.
     */
    @Getter @Setter
    private boolean stripPrefix = false;
    /**
     * Whether to apply color to log messages.
     */
    @Getter @Setter
    private boolean colored = true;

    interface LibStringFunction {
        String get(String message, TakionLib lib, boolean strip, boolean colored);
    }

    static final LibStringFunction FORMATTER =
            (message, l, st, b) -> StringApplier.simplified(message)
                    .apply(s -> l.replacePrefixKey(s, st))
                    .apply(s -> {
                        String split = l.getLineSeparator();
                        final String r = "\\\\[QE]";

                        return s.replaceAll(
                                split,
                                "&f" + split.replaceAll(r, "")
                        );
                    })
                    .apply(StringAligner::align)
                    .apply(TextUtils.STRIP_JSON)
                    .apply(b ?
                            PrismaticAPI::colorize :
                            PrismaticAPI::stripAll
                    )
                    .toString();

    private static final boolean USE_PAPER_LOGGER =
            ServerInfoUtils.PAPER_ENABLED && ServerInfoUtils.SERVER_VERSION >= 18.2;

    public TakionLogger(@NotNull TakionLib lib) {
        this.lib = lib;

        Plugin plugin = null;
        try {
            plugin = lib.getPlugin();
        } catch (Exception ignored) {}

        bukkitLogger = plugin != null ?
                plugin.getLogger() :
                Bukkit.getLogger();

        if (USE_PAPER_LOGGER)
            try {
                paperLogger = new PaperLogger(plugin);
            } catch (Exception ignored) {}
    }

    private class Record {

        private final LogLevel level;
        private final String message;

        private Record(LogLevel level, String message) {
            this.level = level;

            if (message != null)
                message = FORMATTER.get(message, lib, isStripPrefix(), isColored());

            this.message = message;
        }

        private void log0() {
            if (paperLogger != null) {
                paperLogger.log(level, message);
                return;
            }

            bukkitLogger.log(level.toJava(), message);
        }
    }

    public void log(LogLevel level, List<String> messages) {
        if (level == null)
            level = LogLevel.INFO;

        if (messages != null && messages.size() == 1) {
            new Record(level, messages.get(0)).log0();
            return;
        }

        for (String message : CollectionBuilder
                .of(messages)
                .filter(Objects::nonNull))
            new Record(level, message).log0();
    }

    public void log(LogLevel level, String... messages) {
        log(level, ArrayUtils.toList(messages));
    }

    public void log(String... messages) {
        log(null, messages);
    }

    static final RawLogger BUKKIT_DEFAULT_LOGGER = (l, s) -> Bukkit.getLogger().log(l != null ? l.toJava() : Level.INFO, s);
    private static RawLogger paperServerLogger = null;

    private static RawLogger getServerLogger() {
        if (!USE_PAPER_LOGGER) return BUKKIT_DEFAULT_LOGGER;

        return paperServerLogger != null ?
                paperServerLogger :
                (paperServerLogger = new PaperLogger(null));
    }

    public static void doLog(LogLevel level, List<String> messages) {
        final RawLogger logger = getServerLogger();
        LogLevel result = level != null ? level : LogLevel.INFO;

        TakionLib lib = TakionLib.getLib();

        boolean strip = lib.getLogger().isStripPrefix();
        boolean colored = lib.getLogger().isColored();

        if (messages != null && messages.size() == 1) {
            String message = messages.get(0);
            if (message != null)
                message = FORMATTER.get(message, lib, strip, colored);

            logger.log(level, message);
            return;
        }

        for (String message : CollectionBuilder.of(messages)
                .filter(Objects::nonNull)
                .apply(s -> FORMATTER.get(s, lib, strip, colored)))
            logger.log(result, message);
    }

    public static void doLog(LogLevel level, String... messages) {
        doLog(level, ArrayUtils.toList(messages));
    }

    public static void doLog(String... messages) {
        doLog(null, messages);
    }

    public static TakionLogger getLogger() {
        return TakionLib.getLib().getLogger();
    }
}
