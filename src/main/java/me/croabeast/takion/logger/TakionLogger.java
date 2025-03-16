package me.croabeast.takion.logger;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import me.croabeast.takion.TakionLib;
import me.croabeast.lib.CollectionBuilder;
import me.croabeast.lib.applier.StringApplier;
import me.croabeast.lib.util.ArrayUtils;
import me.croabeast.lib.util.ServerInfoUtils;
import me.croabeast.lib.util.TextUtils;
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

public class TakionLogger {

    static final class PaperLogger {

        private final Class<?> clazz;
        private final Object logger;

        @SneakyThrows
        PaperLogger(String name) {
            logger = Class
                    .forName("net.kyori.adventure.text.logger.slf4j.ComponentLogger")
                    .getMethod("logger", String.class).invoke(null, name);

            clazz = logger.getClass();
        }

        void log(LogLevel level, String string) {
            level = level != null ? level : LogLevel.INFO;

            try {
                Class<?> legacy = Class.forName(
                        "net.kyori.adventure.text.serializer." +
                                "legacy.LegacyComponentSerializer"
                );

                Method method = clazz.getMethod(
                        level.getName(),
                        Class.forName("net.kyori.adventure.text.Component")
                );
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private final TakionLib lib;

    @Getter
    private final Logger bukkit;
    private PaperLogger paper;

    @Accessors(chain = true)
    @Getter @Setter
    private boolean stripPrefix = false, colored = true;

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
                    .apply(l.getCharacterManager()::align)
                    .apply(TextUtils.STRIP_JSON)
                    .apply(b ?
                            PrismaticAPI::colorize :
                            PrismaticAPI::stripAll
                    )
                    .toString();

    public TakionLogger(@NotNull TakionLib lib, boolean usePlugin) {
        this.lib = lib;

        Plugin plugin = null;
        try {
            plugin = lib.getPlugin();
        } catch (Exception ignored) {}

        bukkit = usePlugin && plugin != null ? plugin.getLogger() : Bukkit.getLogger();

        if (ServerInfoUtils.PAPER_ENABLED && ServerInfoUtils.SERVER_VERSION >= 18.2)
            try {
                paper = new PaperLogger(usePlugin && plugin != null ? plugin.getName() : "");
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public TakionLogger(@NotNull TakionLib lib) {
        this(lib, true);
    }

    private class LoggerLine {

        private final LogLevel level;
        private final String message;

        private LoggerLine(LogLevel level, String message) {
            this.level = level;

            if (message != null)
                message = FORMATTER.get(message, lib, isStripPrefix(), isColored());

            this.message = message;
        }

        private void log() {
            if (paper != null) {
                paper.log(level, message);
                return;
            }

            bukkit.log(level.toJava(), message);
        }
    }

    public void log(LogLevel level, Collection<String> messages) {
        if (level == null) level = LogLevel.INFO;

        for (String message : CollectionBuilder
                .of(messages)
                .filter(Objects::nonNull))
            new LoggerLine(level, message).log();
    }

    public void log(LogLevel level, String... messages) {
        log(level, ArrayUtils.toList(messages));
    }

    public void log(String... messages) {
        log(null, messages);
    }

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