package me.croabeast.common.util;

import lombok.experimental.UtilityClass;
import me.croabeast.common.applier.StringApplier;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class that provides methods for replacing placeholders in strings
 * with corresponding values.
 */
@UtilityClass
public class ReplaceUtils {

    /**
     * Replaces occurrences of a key in a string with a specified value.
     *
     * @param key       The key string to be replaced
     * @param value     The value to replace the key with
     * @param string    The input string
     * @param sensitive A boolean flag indicating whether the replacement should be case-sensitive
     *
     * @return The modified string after replacement
     */
    public String replace(String key, Object value, String string, boolean sensitive) {
        if (StringUtils.isBlank(string)) return string;

        if (StringUtils.isBlank(key) || value == null)
            return string;

        String val = !(value instanceof CommandSender) ?
                value.toString() :
                ((CommandSender) value).getName();

        String temp = Pattern.quote(key);
        if (!sensitive) temp = "(?i)" + temp;

        Matcher m = Pattern.compile(temp).matcher(string);
        while (m.find())
            string = string.replace(m.group(), val);

        return string;
    }

    /**
     * Replaces occurrences of a key in a string with a specified value. The replacement is case-insensitive.
     *
     * @param key    The key string to be replaced
     * @param value  The value to replace the key with
     * @param string The input string
     *
     * @return The modified string after replacement
     */
    public String replace(String key, Object value, String string) {
        return replace(key, value, string, false);
    }

    /**
     * Checks if an array of keys is applicable for replacement with an array of values.
     *
     * @param <A> The type of elements in the key array
     * @param <B> The type of elements in the value array
     * @param as  The array of keys
     * @param bs  The array of values
     *
     * @return True if the arrays are applicable for replacement, false otherwise
     */
    public <A, B> boolean isApplicable(A[] as, B[] bs) {
        return (!ArrayUtils.isArrayEmpty(as) && !ArrayUtils.isArrayEmpty(bs)) && (as.length <= bs.length);
    }

    /**
     * Replaces each occurrence of a set of keys in a string with their corresponding values.
     *
     * @param keys      The array of keys to be replaced
     * @param values    The array of values to replace the keys with
     * @param string    The input string
     * @param sensitive A boolean flag indicating whether the replacement should be case-sensitive
     *
     * @return The modified string after replacement
     */
    public String replaceEach(String[] keys, Object[] values, String string, boolean sensitive) {
        if (StringUtils.isBlank(string) || !isApplicable(keys, values))
            return string;

        StringApplier applier = StringApplier.simplified(string);

        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            Object value = values[i];

            applier.apply(s -> replace(key, value, s, sensitive));
        }

        return applier.toString();
    }

    /**
     * Replaces each occurrence of a set of keys in a string with their corresponding values. The replacement is case-insensitive.
     *
     * @param keys   The array of keys to be replaced
     * @param values The array of values to replace the keys with
     * @param string The input string
     * @return The modified string after replacement
     */
    public String replaceEach(String[] keys, Object[] values, String string) {
        return replaceEach(keys, values, string, false);
    }

    /**
     * Replaces each occurrence of a set of keys in a string with their corresponding values.
     *
     * @param keys      The collection of keys to be replaced
     * @param values    The collection of values to replace the keys with
     * @param string    The input string
     * @param sensitive A boolean flag indicating whether the replacement should be case-sensitive
     * @return The modified string after replacement
     */
    public String replaceEach(Collection<String> keys, Collection<Object> values, String string, boolean sensitive) {
        return replaceEach(keys.toArray(new String[0]), values.toArray(), string, sensitive);
    }

    /**
     * Replaces each occurrence of a set of keys in a string with their corresponding values. The replacement is case-insensitive.
     *
     * @param keys   The collection of keys to be replaced
     * @param values The collection of values to replace the keys with
     * @param string The input string
     * @return The modified string after replacement
     */
    public String replaceEach(Collection<String> keys, Collection<Object> values, String string) {
        return replaceEach(keys.toArray(new String[0]), values.toArray(), string);
    }

    /**
     * Replaces each occurrence of a set of keys in a string with their corresponding values.
     *
     * @param <T>       The type of the values to be replaced
     * @param <R>       The type of the new values
     *
     * @param map       The map of keys to their corresponding values
     * @param function  The function to apply to each value before replacement
     * @param string    The input string
     * @param sensitive A boolean flag indicating whether the replacement should be case-sensitive
     *
     * @return The modified string after replacement
     */
    public <T, R> String replaceEach(Map<String, T> map, Function<T, R> function, String string, boolean sensitive) {
        if (StringUtils.isBlank(string)) return string;
        if (map.isEmpty()) return string;

        StringApplier applier = StringApplier.simplified(string);

        map.forEach((k, v) -> {
            Object result = function != null ? function.apply(v) : v;
            applier.apply(s -> replace(k, result, s, sensitive));
        });

        return applier.toString();
    }

    /**
     * Replaces each occurrence of a set of keys in a string with their corresponding values. The replacement is case-insensitive.
     *
     * @param <T>      The type of the values to be replaced
     * @param <R>      The type of the new values
     *
     * @param map      The map of keys to their corresponding values
     * @param function The function to apply to each value before replacement
     * @param string   The input string
     *
     * @return The modified string after replacement
     */
    public <T, R> String replaceEach(Map<String, T> map, Function<T, R> function, String string) {
        return replaceEach(map, function, string, false);
    }

    /**
     * Replaces each occurrence of a set of keys in a string with their corresponding values.
     *
     * @param <T>   The type of the values to be replaced
     * @param map   The map of keys to their corresponding values
     * @param string The input string
     * @param sensitive A boolean flag indicating whether the replacement should be case-sensitive
     * @return The modified string after replacement
     */
    public <T> String replaceEach(Map<String, ? extends T> map, String string, boolean sensitive) {
        return replaceEach(map, null, string, sensitive);
    }

    /**
     * Replaces each occurrence of a set of keys in a string with their corresponding values. The replacement is case-insensitive.
     *
     * @param <T>   The type of the values to be replaced
     * @param map   The map of keys to their corresponding values
     * @param string The input string
     * @return The modified string after replacement
     */
    public <T> String replaceEach(Map<String, ? extends T> map, String string) {
        return replaceEach(map, string, false);
    }
}
