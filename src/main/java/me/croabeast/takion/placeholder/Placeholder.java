package me.croabeast.takion.placeholder;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.croabeast.lib.util.ReplaceUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a dynamic placeholder that can be replaced in strings with a value computed for a given player.
 * <p>
 * A {@code Placeholder} is defined by a unique key and a function that generates a value based on a {@link Player}.
 * This value can then be substituted into any string containing the placeholder key. Optionally, the replacement
 * can be marked as case-sensitive.
 * </p>
 *
 * @param <T> the type of the value to be inserted into the placeholder
 */
@Getter
public class Placeholder<T> {

    /**
     * The unique key identifying this placeholder.
     * This key is used to search for and replace occurrences in a target string.
     */
    final String key;

    /**
     * A function that takes a {@link Player} and returns the value of type {@code T} to be used as the replacement.
     */
    final Function<Player, T> function;

    /**
     * Determines whether the placeholder replacement should be case-sensitive.
     * <p>
     * When set to {@code true}, only exact case matches of the key will be replaced.
     * The default is {@code false}.
     * </p>
     */
    @Accessors(chain = true)
    @Setter
    private boolean sensitive = false;

    /**
     * Constructs a new {@code Placeholder} with the specified key and value generator function.
     *
     * @param key      the unique key for this placeholder (must not be blank)
     * @param function a function that computes the placeholder's value based on a {@link Player}
     * @throws NullPointerException if the key is blank or the function is null
     */
    public Placeholder(String key, Function<Player, T> function) {
        if (StringUtils.isBlank(key))
            throw new NullPointerException("Key is empty/null");

        this.key = key;
        this.function = Objects.requireNonNull(function);
    }

    /**
     * Constructs a new {@code Placeholder} with a constant value.
     * <p>
     * The resulting placeholder will always return the given value, regardless of the player.
     * </p>
     *
     * @param key   the unique key for this placeholder (must not be blank)
     * @param value the constant value to be used for replacement
     * @see #Placeholder(String, Function)
     */
    public Placeholder(String key, T value) {
        this(key, player -> value);
    }

    /**
     * Copy constructor that creates a new {@code Placeholder} based on an existing one.
     *
     * @param placeholder the placeholder to copy
     */
    public Placeholder(Placeholder<T> placeholder) {
        this.key = placeholder.key;
        this.function = placeholder.function;
        this.sensitive = placeholder.sensitive;
    }

    /**
     * Replaces all occurrences of the placeholder key in the provided string with the computed value for the given player.
     * <p>
     * The replacement is performed using {@link ReplaceUtils#replace(String, Object, String, boolean)},
     * which handles conversion of the value and optional case sensitivity.
     * </p>
     *
     * @param player the player for whom the value is computed
     * @param string the string in which to replace the placeholder key
     * @return the resulting string with all placeholder keys replaced by the computed value
     */
    public String replace(Player player, String string) {
        return ReplaceUtils.replace(key, function.apply(player), string, sensitive);
    }

    /**
     * Returns a string representation of this placeholder, including its key and sensitivity flag.
     *
     * @return a string representation of the placeholder
     */
    @Override
    public String toString() {
        return "Placeholder{key='" + key + '\'' + ", sensitive=" + sensitive + '}';
    }
}
