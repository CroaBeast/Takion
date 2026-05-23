package me.croabeast.takion.logger;

import me.croabeast.common.util.ArrayUtils;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Represents different levels of logging severity.
 * <p>
 * This enum provides a bridge between Bukkit's {@link Level} and custom log levels
 * used within the Takion framework. Each log level corresponds to one or more Java logging
 * levels, allowing for a flexible mapping of log messages to different levels of severity.
 * </p>
 */
public enum LogLevel {

    /**
     * Debug level: Used for highly detailed logging, including all levels of logs.
     * Typically used for diagnosing issues.
     */
    DEBUG(Level.ALL, Level.OFF),

    /**
     * Trace level: Provides fine-grained debugging information.
     * Used for tracking the execution flow and intermediate states.
     */
    TRACE(Level.FINE, Level.FINER, Level.FINEST),

    /**
     * Info level: Standard logging level for general information messages.
     * These messages indicate the normal operation of the application.
     */
    INFO(Level.INFO, Level.CONFIG),

    /**
     * Warning level: Indicates potential issues or non-critical problems
     * that should be looked into but do not cause immediate failures.
     */
    WARN(Level.WARNING),

    /**
     * Error level: Represents critical errors or failures that require immediate attention.
     * These messages usually indicate system failures or major malfunctions.
     */
    ERROR(Level.SEVERE);

    private final List<Level> levels;

    LogLevel(Level... levels) {
        this.levels = ArrayUtils.toList(levels);
    }

    /**
     * Converts this {@code LogLevel} to its corresponding Java {@link Level}.
     * <p>
     * If multiple levels are associated with this log level, the first one is returned.
     * </p>
     *
     * @return the primary {@link Level} associated with this log level
     */
    public Level toJava() {
        return levels.get(0);
    }

    /**
     * Returns the name of this log level in lowercase.
     * <p>
     * Useful for formatting or serialization purposes.
     * </p>
     *
     * @return the lowercase name of this log level
     */
    public String getName() {
        return name().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Returns the {@code LogLevel} corresponding to the given Java {@link Level}.
     * <p>
     * If the provided level does not match any predefined {@code LogLevel}, defaults to {@link #DEBUG}.
     * </p>
     *
     * @param level the Java logging level to map
     * @return the corresponding {@code LogLevel}, or {@link #DEBUG} if no match is found
     */
    public static LogLevel fromJava(Level level) {
        if (level == null) return DEBUG;

        for (LogLevel l : values())
            if (l.levels.contains(level)) return l;

        return DEBUG;
    }
}
