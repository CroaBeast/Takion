package me.croabeast.takion.logger;

import me.croabeast.lib.util.ArrayUtils;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public enum LogLevel {
    DEBUG(Level.ALL, Level.OFF),
    TRACE(Level.FINE, Level.FINER, Level.FINEST),
    INFO(Level.INFO, Level.CONFIG),
    WARN(Level.WARNING),
    ERROR(Level.SEVERE);

    private final List<Level> levels;

    LogLevel(Level... levels) {
        this.levels = ArrayUtils.toList(levels);
    }

    public Level toJava() {
        return levels.get(0);
    }

    public String getName() {
        return name().toLowerCase(Locale.ENGLISH);
    }

    public static LogLevel from(Level level) {
        if (level == null) return DEBUG;

        for (LogLevel l : values())
            if (l.levels.contains(level)) return l;

        return DEBUG;
    }
}
