package me.croabeast.lib.time;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import me.croabeast.lib.util.Exceptions;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
public class TimeValues {

    public static final TimeValues DEFAULT_KEYS = new TimeValues(false);

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

    private TimeValues toggleModification() {
        this.modify = !this.modify;
        return this;
    }

    @SneakyThrows
    private void checkModification() {
        Preconditions.checkArgument(modify, "Instance can't be modified");
    }

    public TimeValues setSplitter(String splitter) {
        checkModification();
        this.splitter = Exceptions.validate(StringUtils::isNotBlank, splitter);
        return this;
    }

    public TimeValues setSecondsFormat(String secondsFormat) {
        checkModification();
        this.secondsFormat = Exceptions.validate(StringUtils::isNotBlank, secondsFormat);
        return this;
    }

    public TimeValues setMinutesFormat(String minutesFormat) {
        checkModification();
        this.minutesFormat = Exceptions.validate(StringUtils::isNotBlank, minutesFormat);
        return this;
    }

    public TimeValues setHoursFormat(String hoursFormat) {
        checkModification();
        this.hoursFormat = Exceptions.validate(StringUtils::isNotBlank, hoursFormat);
        return this;
    }

    public TimeValues setDaysFormat(String daysFormat) {
        checkModification();
        this.daysFormat = Exceptions.validate(StringUtils::isNotBlank, daysFormat);
        return this;
    }

    public TimeValues setWeeksFormat(String weeksFormat) {
        checkModification();
        this.weeksFormat = Exceptions.validate(StringUtils::isNotBlank, weeksFormat);
        return this;
    }

    public TimeValues setMonthsFormat(String monthsFormat) {
        checkModification();
        this.monthsFormat = Exceptions.validate(StringUtils::isNotBlank, monthsFormat);
        return this;
    }

    public TimeValues setYearsFormat(String yearsFormat) {
        checkModification();
        this.yearsFormat = Exceptions.validate(StringUtils::isNotBlank, yearsFormat);
        return this;
    }

    public static TimeValues create() {
        return new TimeValues();
    }

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
