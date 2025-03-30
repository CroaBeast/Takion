package me.croabeast.takion;

import lombok.Getter;
import lombok.Setter;
import me.croabeast.lib.Regex;
import me.croabeast.lib.util.ArrayUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an abstract action that can be performed on a given string
 * using a predefined regex pattern. This class allows executing an action
 * based on matching patterns, typically in a Minecraft plugin environment.
 *
 * @param <T> the type of the result returned by the action
 */
@Getter @Setter
public abstract class PatternAction<T> {

    /** The regex pattern used to match strings. */
    @Regex
    private String pattern;

    /**
     * Constructs a new {@code PatternAction} with the specified regex pattern.
     *
     * @param pattern the regex pattern to be used for matching
     */
    public PatternAction(@Regex String pattern) {
        this.pattern = pattern;
    }

    /**
     * Creates a {@link Matcher} for the given string using the stored regex pattern.
     *
     * @param string the input string to be matched against the pattern
     * @return a {@code Matcher} instance for the provided string
     */
    @NotNull
    public Matcher createMatcher(String string) {
        return Pattern.compile(getPattern()).matcher(string);
    }

    /**
     * Executes the action on a collection of players using the provided string.
     *
     * @param players the collection of players affected by this action (can be null)
     * @param string the input string to process
     * @return the result of the action execution
     */
    @NotNull
    public abstract T act(Collection<? extends Player> players, String string);

    /**
     * Executes the action on a single player using the provided string.
     * Internally calls {@link #act(Collection, String)} with a singleton list.
     *
     * @param player the player affected by this action
     * @param string the input string to process
     * @return the result of the action execution
     */
    @NotNull
    public T act(Player player, String string) {
        return act(ArrayUtils.toList(player), string);
    }

    /**
     * Executes the action using the provided string without any associated players.
     * This is useful when the action does not depend on a player context.
     *
     * @param string the input string to process
     * @return the result of the action execution
     */
    @NotNull
    public T act(String string) {
        return act((Collection<Player>) null, string);
    }
}
