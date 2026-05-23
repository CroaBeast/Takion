package me.croabeast.common.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * A utility class that provides methods for replacing placeholders in strings
 * with corresponding values.
 */
@UtilityClass
public class ReplaceUtils {

    /**
     * Replaces occurrences of a key in a string with a specified value.
     *
     * @param key       the key string to be replaced
     * @param value     the value to replace the key with
     * @param string    the input string
     * @param sensitive a boolean flag indicating whether the replacement should be case-sensitive
     * @return the modified string after replacement
     */
    public String replace(String key, Object value, String string, boolean sensitive) {
        if (StringUtils.isBlank(string)) return string;

        if (StringUtils.isBlank(key) || value == null)
            return string;

        String val = !(value instanceof CommandSender) ?
                value.toString() :
                ((CommandSender) value).getName();

        return sensitive ?
                string.replace(key, val) :
                replaceIgnoreCase(key, val, string);
    }

    /**
     * Checks whether {@code string} contains {@code key}.
     *
     * @param key       the substring to search for
     * @param string    the input string to search in
     * @param sensitive {@code true} for case-sensitive matching; {@code false} for case-insensitive
     * @return {@code true} if {@code key} is found in {@code string}; {@code false} otherwise
     */
    public boolean contains(String key, String string, boolean sensitive) {
        if (StringUtils.isBlank(string) || StringUtils.isBlank(key))
            return false;

        return sensitive ?
                string.contains(key) :
                indexOfIgnoreCase(key, string, 0) >= 0;
    }

    private int indexOfIgnoreCase(String key, String string, int start) {
        int max = string.length() - key.length();
        for (int i = Math.max(start, 0); i <= max; i++)
            if (string.regionMatches(true, i, key, 0, key.length()))
                return i;
        return -1;
    }

    private String replaceIgnoreCase(String key, String value, String string) {
        int index = indexOfIgnoreCase(key, string, 0);
        if (index < 0) return string;

        int keyLength = key.length();
        int start = 0;
        StringBuilder builder = new StringBuilder(string.length());

        while (index >= 0) {
            builder.append(string, start, index).append(value);
            start = index + keyLength;
            index = indexOfIgnoreCase(key, string, start);
        }

        return builder.append(string, start, string.length()).toString();
    }

    /**
     * Replaces occurrences of a key in a string with a specified value. The replacement is case-insensitive.
     *
     * @param key    the key string to be replaced
     * @param value  the value to replace the key with
     * @param string the input string
     * @return the modified string after replacement
     */
    public String replace(String key, Object value, String string) {
        return replace(key, value, string, false);
    }

    /**
     * Checks if an array of keys is applicable for replacement with an array of values.
     *
     * @param <A> the type of elements in the key array
     * @param <B> the type of elements in the value array
     * @param as  the array of keys
     * @param bs  the array of values
     * @return {@code true} if the arrays are applicable for replacement; {@code false} otherwise
     */
    public <A, B> boolean isApplicable(A[] as, B[] bs) {
        return (as != null && bs != null) && (as.length <= bs.length);
    }

    /**
     * Replaces each occurrence of a set of keys in a string with their corresponding values.
     *
     * @param keys      the array of keys to be replaced
     * @param values    the array of values to replace the keys with
     * @param string    the input string
     * @param sensitive a boolean flag indicating whether the replacement should be case-sensitive
     * @return the modified string after replacement
     */
    public String replaceEach(String[] keys, Object[] values, String string, boolean sensitive) {
        if (StringUtils.isBlank(string) || !isApplicable(keys, values))
            return string;

        for (int i = 0; i < keys.length; i++)
            string = replace(keys[i], values[i], string, sensitive);

        return string;
    }

    /**
     * Replaces each occurrence of a set of keys in a string with their corresponding values. The replacement is case-insensitive.
     *
     * @param keys   the array of keys to be replaced
     * @param values the array of values to replace the keys with
     * @param string the input string
     * @return the modified string after replacement
     */
    public String replaceEach(String[] keys, Object[] values, String string) {
        return replaceEach(keys, values, string, false);
    }

    /**
     * Replaces each occurrence of a set of keys in a string with their corresponding values.
     *
     * @param keys      the collection of keys to be replaced
     * @param values    the collection of values to replace the keys with
     * @param string    the input string
     * @param sensitive a boolean flag indicating whether the replacement should be case-sensitive
     * @return the modified string after replacement
     */
    public String replaceEach(Collection<String> keys, Collection<Object> values, String string, boolean sensitive) {
        return replaceEach(keys.toArray(new String[0]), values.toArray(), string, sensitive);
    }

    /**
     * Replaces each occurrence of a set of keys in a string with their corresponding values. The replacement is case-insensitive.
     *
     * @param keys   the collection of keys to be replaced
     * @param values the collection of values to replace the keys with
     * @param string the input string
     * @return the modified string after replacement
     */
    public String replaceEach(Collection<String> keys, Collection<Object> values, String string) {
        return replaceEach(keys.toArray(new String[0]), values.toArray(), string);
    }

    /**
     * Replaces each occurrence of a set of keys in a string with their corresponding values.
     *
     * @param <T>       the type of the values to be replaced
     * @param <R>       the type of the new values
     * @param map       the map of keys to their corresponding values
     * @param function  the function to apply to each value before replacement
     * @param string    the input string
     * @param sensitive a boolean flag indicating whether the replacement should be case-sensitive
     * @return the modified string after replacement
     */
    public <T, R> String replaceEach(Map<String, T> map, Function<T, R> function, String string, boolean sensitive) {
        if (StringUtils.isBlank(string)) return string;
        if (map.isEmpty()) return string;

        for (Map.Entry<String, T> entry : map.entrySet()) {
            Object result = function != null ? function.apply(entry.getValue()) : entry.getValue();
            string = replace(entry.getKey(), result, string, sensitive);
        }

        return string;
    }

    /**
     * Replaces each occurrence of a set of keys in a string with their corresponding values. The replacement is case-insensitive.
     *
     * @param <T>      the type of the values to be replaced
     * @param <R>      the type of the new values
     * @param map      the map of keys to their corresponding values
     * @param function the function to apply to each value before replacement
     * @param string   the input string
     * @return the modified string after replacement
     */
    public <T, R> String replaceEach(Map<String, T> map, Function<T, R> function, String string) {
        return replaceEach(map, function, string, false);
    }

    /**
     * Replaces each occurrence of a set of keys in a string with their corresponding values.
     *
     * @param <T>       the type of the values to be replaced
     * @param map       the map of keys to their corresponding values
     * @param string    the input string
     * @param sensitive a boolean flag indicating whether the replacement should be case-sensitive
     * @return the modified string after replacement
     */
    public <T> String replaceEach(Map<String, ? extends T> map, String string, boolean sensitive) {
        return replaceEach(map, null, string, sensitive);
    }

    /**
     * Replaces each occurrence of a set of keys in a string with their corresponding values. The replacement is case-insensitive.
     *
     * @param <T>    the type of the values to be replaced
     * @param map    the map of keys to their corresponding values
     * @param string the input string
     * @return the modified string after replacement
     */
    public <T> String replaceEach(Map<String, ? extends T> map, String string) {
        return replaceEach(map, string, false);
    }
}
