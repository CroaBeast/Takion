package me.croabeast.lib;

import org.apache.commons.lang.StringUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * The {@code Rounder} object rounds a number using a mutable
 * amount of fixed decimals.
 *
 * @param <T> the number class type
 *
 * @author CroaBeast
 * @since 1.4
 */
public final class Rounder<T extends Number> {

    private final T number;
    private final Class<T> clazz;

    private int decimalAmount = 2;

    private Rounder(T t) {
        number = t;
        clazz = (Class<T>) t.getClass();
    }

    private Rounder<T> setAmount(int i) {
        this.decimalAmount = i;
        return this;
    }

    private String getRoundString() {
        String s = "#." + StringUtils.repeat("#", decimalAmount);
        if (decimalAmount == 0) s = "#";

        DecimalFormatSymbols d = DecimalFormatSymbols.getInstance(Locale.ENGLISH);
        return new DecimalFormat(s, d).format(number);
    }

    private T result() {
        String round = getRoundString();
        Number n = null;

        if (clazz == Double.class)
            n = Double.parseDouble(round);
        if (clazz == Long.class)
            n = Long.parseLong(round);
        if (clazz == Float.class)
            n = Float.parseFloat(round);
        if (clazz == Byte.class)
            n = Byte.parseByte(round);
        if (clazz == Integer.class)
            n = Integer.parseInt(round);
        if (clazz == Short.class)
            n = Short.parseShort(round);

        return clazz.cast(n);
    }

    /**
     * Rounds a number to a two-decimal number.
     *
     * @param t a number
     * @return the rounded number
     *
     * @param <T> a number class
     */
    public static <T extends Number> T round(T t) {
        return new Rounder<>(t).result();
    }

    /**
     * Rounds a number to another number with a fixed amount of decimals.
     *
     * @param decimalAmount a fixed amount of decimals
     * @param t a number
     * @return the rounded number
     *
     * @param <T> a number class
     */
    public static <T extends Number> T round(int decimalAmount, T t) {
        return new Rounder<>(t).setAmount(decimalAmount).result();
    }
}
