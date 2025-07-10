package me.croabeast.common;

import org.apache.commons.lang.StringUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * A utility class for rounding numbers to a specified number of decimal places.
 * <p>
 * The {@code Rounder} class supports various numeric types (e.g., {@link Double}, {@link Integer}, {@link Float}, etc.)
 * and provides methods to round a given number to a default of 2 decimal places or a custom amount.
 * Internally, it formats the number into a string using a customizable pattern and then parses it back
 * to the original numeric type.
 * </p>
 *
 * @param <T> the type of the number, which must extend {@link Number}
 */
@SuppressWarnings("unchecked")
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
     * Rounds the given number to the default of 2 decimal places.
     *
     * @param t the number to be rounded
     * @param <T> the type of the number
     * @return the rounded number with 2 decimal places
     */
    public static <T extends Number> T round(T t) {
        return new Rounder<>(t).result();
    }

    /**
     * Rounds the given number to the specified number of decimal places.
     *
     * @param decimalAmount the desired number of decimal places
     * @param t the number to be rounded
     * @param <T> the type of the number
     * @return the rounded number with the specified decimal places
     */
    public static <T extends Number> T round(int decimalAmount, T t) {
        return new Rounder<>(t).setAmount(decimalAmount).result();
    }
}
