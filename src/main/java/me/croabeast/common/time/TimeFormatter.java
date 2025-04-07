package me.croabeast.common.time;

import me.croabeast.takion.TakionLib;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Formats a duration given in seconds into a human-readable string using configurable time unit formats.
 * <p>
 * {@code TimeFormatter} uses a {@link TimeValues} instance to format time durations into strings.
 * It calculates the number of years, months, weeks, days, hours, minutes, and seconds contained within
 * a duration, and returns a formatted string where each non-zero unit is included with proper pluralization.
 * The resulting string is also colorized via the provided {@link TakionLib} instance.
 * </p>
 *
 * <p>
 * Constants representing the number of seconds in each time unit are provided:
 * <ul>
 *   <li>{@link #MINUTE}: 60 seconds</li>
 *   <li>{@link #HOUR}: 3600 seconds</li>
 *   <li>{@link #DAY}: 86400 seconds</li>
 *   <li>{@link #WEEK}: 604800 seconds</li>
 *   <li>{@link #MONTH}: 2592000 seconds (approx.)</li>
 *   <li>{@link #YEAR}: 31536000 seconds (approx.)</li>
 * </ul>
 * </p>
 *
 * <p>
 * Example usage:
 * <pre><code>
 * TakionLib lib = TakionLib.fromPlugin(plugin);
 * TimeFormatter formatter = new TimeFormatter(lib, TimeValues.DEFAULT_KEYS, 987654);
 * String formatted = formatter.formatTime(player);
 * System.out.println(formatted);
 * </code></pre>
 * </p>
 *
 * @see TimeValues
 * @see TakionLib
 */
public class TimeFormatter {

    /**
     * Number of seconds in one minute.
     */
    public static final int MINUTE = 60;
    /**
     * Number of seconds in one hour.
     */
    public static final int HOUR = 60 * MINUTE;
    /**
     * Number of seconds in one day.
     */
    public static final int DAY = 24 * HOUR;
    /**
     * Number of seconds in one week.
     */
    public static final int WEEK = 7 * DAY;
    /**
     * Approximate number of seconds in one month.
     */
    public static final int MONTH = 30 * DAY;
    /**
     * Approximate number of seconds in one year.
     */
    public static final int YEAR = 365 * DAY;

    /**
     * A regex pattern used to remove any pluralization suffix enclosed in parentheses.
     */
    private static final String PLURAL_REGEX = "\\s*\\([^)]*\\)\\s*";

    private final TakionLib lib;
    private final TimeValues values;
    private final long seconds;

    /**
     * Constructs a new {@code TimeFormatter} with the given library instance, time value formats,
     * and duration in seconds.
     *
     * @param lib     the {@link TakionLib} instance used for colorization and formatting utilities (must not be {@code null})
     * @param values  the {@link TimeValues} instance containing format strings; if {@code null}, default keys are used
     * @param seconds the duration in seconds to format
     */
    public TimeFormatter(TakionLib lib, TimeValues values, long seconds) {
        this.lib = Objects.requireNonNull(lib);
        this.values = values == null ? TimeValues.DEFAULT_KEYS : values;
        this.seconds = seconds;
    }

    /**
     * Constructs a new {@code TimeFormatter} with the given library instance and duration in seconds,
     * using default time value formats.
     *
     * @param lib     the {@link TakionLib} instance used for colorization and formatting utilities
     * @param seconds the duration in seconds to format
     */
    public TimeFormatter(TakionLib lib, long seconds) {
        this(lib, null, seconds);
    }

    /**
     * Calculates the whole number of time units (e.g. minutes, hours) in the given duration.
     *
     * @param seconds   the remaining seconds to convert
     * @param formatter the number of seconds that make up one unit (e.g. {@link #MINUTE})
     * @return the integer count of the time unit in the duration
     */
    private long getFixedTime(long seconds, long formatter) {
        return (seconds - (seconds % formatter)) / formatter;
    }

    /**
     * Returns a formatted string for a given time unit and its count.
     * <p>
     * If the count is 1, any pluralization (text within parentheses) is removed from the format string.
     * Otherwise, the format string is returned without parentheses.
     * </p>
     *
     * @param value  the numeric value of the time unit
     * @param string the format string for the time unit (e.g. "Second(s)")
     * @return a string representing the time unit and its count, properly pluralized
     */
    private String isPlural(long value, String string) {
        string = value + " " + string;
        if (value == 1)
            return string.replaceAll(PLURAL_REGEX, "");
        return string.replace("(", "").replace(")", "");
    }

    /**
     * Colorizes the provided string using the {@link TakionLib} instance.
     * <p>
     * The colorization is applied with respect to the target and parser players.
     * </p>
     *
     * @param target the target player to receive the colored text (may be {@code null})
     * @param parser the player context used for colorization (may be {@code null})
     * @param string the string to colorize
     * @return the colorized string
     */
    private String colorize(Player target, Player parser, String string) {
        return lib.colorize(target, parser, string);
    }

    /**
     * Formats the time duration into a human-readable string.
     * <p>
     * The method decomposes the total seconds into years, months, weeks, days, hours, minutes, and seconds,
     * concatenates the non-zero components using the configured splitter, and colorizes the final string.
     * </p>
     *
     * @param target the player to whom the formatted time is targeted (for colorization; may be {@code null})
     * @param parser the player context used for colorization (may be {@code null})
     * @return the formatted time string
     */
    public String formatTime(Player target, Player parser) {
        final String split = values.getSplitter();
        long result = this.seconds;
        if (result <= 0)
            return colorize(target, parser, isPlural(0, values.getSecondsFormat()));

        StringBuilder formattedTime = new StringBuilder();
        long years, months, weeks, days, hours, minutes;

        years = getFixedTime(result, YEAR);
        result -= (years * YEAR);
        if (years > 0) formattedTime.append(isPlural(years, values.getYearsFormat())).append(split);

        months = getFixedTime(result, MONTH);
        result -= (months * MONTH);
        if (months > 0) formattedTime.append(isPlural(months, values.getMonthsFormat())).append(split);

        weeks = getFixedTime(result, WEEK);
        result -= (weeks * WEEK);
        if (weeks > 0) formattedTime.append(isPlural(weeks, values.getWeeksFormat())).append(split);

        days = getFixedTime(result, DAY);
        result -= (days * DAY);
        if (days > 0) formattedTime.append(isPlural(days, values.getDaysFormat())).append(split);

        hours = getFixedTime(result, HOUR);
        result -= (hours * HOUR);
        if (hours > 0) formattedTime.append(isPlural(hours, values.getHoursFormat())).append(split);

        minutes = getFixedTime(result, MINUTE);
        result -= (minutes * MINUTE);
        if (minutes > 0) formattedTime.append(isPlural(minutes, values.getMinutesFormat())).append(split);

        if (result > 0) formattedTime.append(isPlural(result, values.getSecondsFormat() + split));

        int time = formattedTime.length(), s = split.length();
        return colorize(target, parser, formattedTime.substring(0, time - s));
    }

    /**
     * Formats the time duration into a human-readable string using the same player as both target and parser.
     *
     * @param player the player to use for both target and parsing context for colorization
     * @return the formatted time string
     */
    public String formatTime(Player player) {
        return formatTime(player, player);
    }

    /**
     * Formats the time duration into a human-readable string without any player context for colorization.
     *
     * @return the formatted time string
     */
    public String formatTime() {
        return formatTime(null, null);
    }
}
