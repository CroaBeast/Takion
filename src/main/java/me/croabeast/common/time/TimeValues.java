package me.croabeast.common.time;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import me.croabeast.common.util.Exceptions;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a collection of string formats for different time units.
 * <p>
 * {@code TimeValues} holds configurable formatting strings for seconds, minutes, hours,
 * days, weeks, months, and years. It is designed to support multiple languages and custom formatting.
 * The class includes pre-defined instances such as {@link #DEFAULT_KEYS} and {@link #SPANISH_KEYS}.
 * Once an instance is "locked" (i.e. modification is disabled), its values cannot be changed.
 * </p>
 * <p>
 * The formatting strings are used in conjunction with {@link TimeFormatter} to produce human-readable
 * representations of time durations.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre><code>
 * // Using default keys:
 * TimeValues defaults = TimeValues.DEFAULT_KEYS;
 *
 * // Customizing for Spanish:
 * TimeValues spanish = TimeValues.SPANISH_KEYS;
 *
 * // Load from configuration:
 * ConfigurationSection section = ...;
 * TimeValues configValues = TimeValues.fromSection(section);
 * </code></pre>
 * </p>
 */
@Getter
public class TimeValues {

    /**
     * Default time unit keys in English.
     */
    public static final TimeValues DEFAULT_KEYS = new TimeValues(false);

    /**
     * Predefined time unit keys for Spanish language.
     */
    public static final TimeValues SPANISH_KEYS = new TimeValues()
            .setSecondsFormat("Segundo(s)")
            .setMinutesFormat("Minuto(s)")
            .setHoursFormat("Hora(s)")
            .setDaysFormat("Día(s)")
            .setWeeksFormat("Semana(s)")
            .setMonthsFormat("Mes(es)")
            .setYearsFormat("Año(s)").toggleModification();

    @Getter(AccessLevel.NONE)
    private boolean modify = true;

    /**
     * The delimiter used to separate time unit strings in the final formatted output.
     */
    @NotNull
    private String splitter = ", ";

    private String secondsFormat = "Second(s)";
    private String minutesFormat = "Minute(s)";
    private String hoursFormat = "Hour(s)";
    private String daysFormat = "Day(s)";
    private String weeksFormat = "Week(s)";
    private String monthsFormat = "Month(s)";
    private String yearsFormat = "Year(s)";

    private TimeValues(boolean b) {
        this.modify = b;
    }

    private TimeValues() {}

    private TimeValues(TimeValues values) {
        modify = values.modify;
        splitter = values.getSplitter();

        secondsFormat = values.getSecondsFormat();
        minutesFormat = values.getMinutesFormat();
        hoursFormat = values.getHoursFormat();
        daysFormat = values.getDaysFormat();
        weeksFormat = values.getWeeksFormat();
        monthsFormat = values.getMonthsFormat();
        yearsFormat = values.getYearsFormat();
    }

    /**
     * Toggles the modification flag, allowing or disallowing further modifications to this instance.
     *
     * @return this instance after toggling modification
     */
    private TimeValues toggleModification() {
        this.modify = !this.modify;
        return this;
    }

    /**
     * Ensures that this instance is modifiable.
     *
     * @throws IllegalArgumentException if the instance is locked from modifications
     */
    @SneakyThrows
    private void checkModification() {
        Preconditions.checkArgument(modify, "Instance can't be modified");
    }

    /**
     * Sets the splitter string used between time unit segments.
     *
     * @param splitter the new splitter string (must not be blank)
     * @return this {@code TimeValues} instance for chaining
     */
    public TimeValues setSplitter(String splitter) {
        checkModification();
        this.splitter = Exceptions.validate(StringUtils::isNotBlank, splitter);
        return this;
    }

    /**
     * Sets the format string for seconds.
     *
     * @param secondsFormat the format string for seconds (must not be blank)
     * @return this {@code TimeValues} instance for chaining
     */
    public TimeValues setSecondsFormat(String secondsFormat) {
        checkModification();
        this.secondsFormat = Exceptions.validate(StringUtils::isNotBlank, secondsFormat);
        return this;
    }

    /**
     * Sets the format string for minutes.
     *
     * @param minutesFormat the format string for minutes (must not be blank)
     * @return this {@code TimeValues} instance for chaining
     */
    public TimeValues setMinutesFormat(String minutesFormat) {
        checkModification();
        this.minutesFormat = Exceptions.validate(StringUtils::isNotBlank, minutesFormat);
        return this;
    }

    /**
     * Sets the format string for hours.
     *
     * @param hoursFormat the format string for hours (must not be blank)
     * @return this {@code TimeValues} instance for chaining
     */
    public TimeValues setHoursFormat(String hoursFormat) {
        checkModification();
        this.hoursFormat = Exceptions.validate(StringUtils::isNotBlank, hoursFormat);
        return this;
    }

    /**
     * Sets the format string for days.
     *
     * @param daysFormat the format string for days (must not be blank)
     * @return this {@code TimeValues} instance for chaining
     */
    public TimeValues setDaysFormat(String daysFormat) {
        checkModification();
        this.daysFormat = Exceptions.validate(StringUtils::isNotBlank, daysFormat);
        return this;
    }

    /**
     * Sets the format string for weeks.
     *
     * @param weeksFormat the format string for weeks (must not be blank)
     * @return this {@code TimeValues} instance for chaining
     */
    public TimeValues setWeeksFormat(String weeksFormat) {
        checkModification();
        this.weeksFormat = Exceptions.validate(StringUtils::isNotBlank, weeksFormat);
        return this;
    }

    /**
     * Sets the format string for months.
     *
     * @param monthsFormat the format string for months (must not be blank)
     * @return this {@code TimeValues} instance for chaining
     */
    public TimeValues setMonthsFormat(String monthsFormat) {
        checkModification();
        this.monthsFormat = Exceptions.validate(StringUtils::isNotBlank, monthsFormat);
        return this;
    }

    /**
     * Sets the format string for years.
     *
     * @param yearsFormat the format string for years (must not be blank)
     * @return this {@code TimeValues} instance for chaining
     */
    public TimeValues setYearsFormat(String yearsFormat) {
        checkModification();
        this.yearsFormat = Exceptions.validate(StringUtils::isNotBlank, yearsFormat);
        return this;
    }

    /**
     * Creates a new modifiable instance of {@code TimeValues}.
     *
     * @return a new {@code TimeValues} instance with default formats
     */
    public static TimeValues create() {
        return new TimeValues();
    }

    /**
     * Creates a {@code TimeValues} instance from a configuration section.
     * <p>
     * The configuration should contain keys such as "splitter", "seconds", "minutes", "hours",
     * "days", "weeks", "months", and "years". If a key is missing or invalid, the corresponding
     * default value is retained.
     * </p>
     *
     * @param section the configuration section containing time format settings
     * @return a new {@code TimeValues} instance loaded from the configuration
     * @throws NullPointerException if the configuration section is {@code null}
     */
    public static TimeValues fromSection(ConfigurationSection section) {
        Objects.requireNonNull(section);

        TimeValues values = new TimeValues(DEFAULT_KEYS);
        values.toggleModification();

        try {
            values.setSplitter(section.getString("splitter"));
        } catch (Exception ignored) {}

        try {
            values.setSecondsFormat(section.getString("seconds"));
        } catch (Exception ignored) {}

        try {
            values.setMinutesFormat(section.getString("minutes"));
        } catch (Exception ignored) {}

        try {
            values.setHoursFormat(section.getString("hours"));
        } catch (Exception ignored) {}

        try {
            values.setDaysFormat(section.getString("days"));
        } catch (Exception ignored) {}

        try {
            values.setWeeksFormat(section.getString("weeks"));
        } catch (Exception ignored) {}

        try {
            values.setMonthsFormat(section.getString("months"));
        } catch (Exception ignored) {}

        try {
            values.setYearsFormat(section.getString("years"));
        } catch (Exception ignored) {}

        return values;
    }
}
