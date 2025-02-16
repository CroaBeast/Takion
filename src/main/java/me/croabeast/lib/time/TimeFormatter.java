package me.croabeast.lib.time;

import lombok.var;
import me.croabeast.takion.TakionLib;
import org.bukkit.entity.Player;

public class TimeFormatter {

    /**
     * The seconds that are stored in a minute.
     */
    public static final int MINUTE = 60;
    /**
     * The seconds that are stored in an hour.
     */
    public static final int HOUR = 60 * MINUTE;
    /**
     * The seconds that are stored in a day.
     */
    public static final int DAY = 24 * HOUR;
    /**
     * The seconds that are stored in a week.
     */
    public static final int WEEK = 7 * DAY;
    /**
     * The seconds that are stored in a month.
     */
    public static final int MONTH = 30 * DAY;
    /**
     * The seconds that are stored in a year.
     */
    public static final int YEAR = 365 * DAY;

    private static final String PLURAL_REGEX = "\\s*\\([^)]*\\)\\s*";

    private final TimeValues values;
    private final long seconds;

    /**
     * Creates a new parser using a {@link TimeValues} instance and an amount of seconds.
     *
     * @param values a {@link TimeValues} instance
     * @param seconds time in seconds
     */
    public TimeFormatter(TimeValues values, long seconds) {
        this.values = values == null ? TimeValues.DEFAULT_KEYS : values;
        this.seconds = seconds;
    }

    /**
     * Creates a new parser using an amount of seconds.
     *
     * @param seconds time in seconds
     */
    public TimeFormatter(long seconds) {
        this(null, seconds);
    }

    private static long getFixedTime(long seconds, long formatter) {
        return (seconds - (seconds % formatter)) / formatter;
    }

    private static String isPlural(long value, String string) {
        string = value + " " + string;
        if (value == 1)
            return string.replaceAll(PLURAL_REGEX, "");

        return string.replace("(", "").replace(")", "");
    }

    private static String colorize(Player target, Player parser, String string) {
        return TakionLib.getLib().colorize(target, parser, string);
    }

    public String formatTime(Player target, Player parser) {
        final String split = values.getSplitter();
        long result = this.seconds;

        if (result <= 0)
            return colorize(target, parser, isPlural(0, values.getSecondsFormat()));

        var formattedTime = new StringBuilder();
        long years, months, weeks, days, hours, mins;

        years = getFixedTime(result, YEAR);
        result = result - (years * YEAR);

        if (years > 0) formattedTime
                .append(isPlural(years, values.getYearsFormat()))
                .append(split);

        months = getFixedTime(result, MONTH);
        result = result - (months * MONTH);

        if (months > 0) formattedTime
                .append(isPlural(months, values.getMonthsFormat()))
                .append(split);

        weeks = getFixedTime(result, WEEK);
        result = result - (weeks * WEEK);

        if (weeks > 0) formattedTime
                .append(isPlural(weeks, values.getWeeksFormat()))
                .append(split);

        days = getFixedTime(result, DAY);
        result = result - (days * DAY);

        if (days > 0) formattedTime
                .append(isPlural(days, values.getDaysFormat()))
                .append(split);

        hours = getFixedTime(result, HOUR);
        result = result - (hours * HOUR);

        if (hours > 0) formattedTime
                .append(isPlural(hours, values.getHoursFormat()))
                .append(split);

        mins = getFixedTime(result, MINUTE);
        result = result - (mins * MINUTE);

        if (mins > 0) formattedTime
                .append(isPlural(mins, values.getMinutesFormat()))
                .append(split);

        if (result > 0) formattedTime
                .append(isPlural(result, values.getSecondsFormat() + split));

        int time = formattedTime.length(), s = split.length();
        return colorize(target, parser, formattedTime.substring(0, time - s));
    }

    public String formatTime(Player player) {
        return formatTime(player, player);
    }

    public String formatTime() {
        return formatTime(null, null);
    }
}
