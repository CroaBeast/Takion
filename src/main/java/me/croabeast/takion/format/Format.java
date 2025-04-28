package me.croabeast.takion.format;

import me.croabeast.common.Regex;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines the contract for a single text‐based format.
 * <p>
 * A {@link Format} encapsulates:
 * <ul>
 *   <li>A regular expression, returned by {@link #getRegex()}, that locates segments in an input string.</li>
 *   <li>Utility methods to strip those segments ({@link #removeFormat(String)}) or to test for their presence ({@link #isFormatted(String)}).</li>
 *   <li>Transformation logic ({@link #accept(Player, String)} and {@link #accept(String)}) that produces a value of type {@code T}
 *       based on the matched text and optional {@link Player} context.</li>
 * </ul>
 * </p>
 *
 * @param <T> the type of the result produced when this format is applied
 */
public interface Format<T> {

    /**
     * Obtain the regex pattern string used to identify this format within input text.
     * <p>
     * This string is automatically wrapped into a {@link Pattern} by {@link #matcher(String)}.
     * Patterns should use non-greedy quantifiers (e.g. ".+?") to avoid over-matching.
     * </p>
     *
     * @return a valid Java regex, annotated with {@link Regex} to denote its purpose
     */
    @NotNull @Regex
    String getRegex();

    /**
     * Create a {@link Matcher} for this format against the provided input.
     *
     * @param string the text to match against (never {@code null})
     * @return a {@link Matcher} configured with {@link #getRegex()} on {@code string}
     */
    @NotNull
    default Matcher matcher(String string) {
        Pattern pattern = Pattern.compile(getRegex());
        return pattern.matcher(string);
    }

    /**
     * Check whether the input contains at least one occurrence of this format.
     *
     * @param string text in which to search for format markers
     * @return {@code true} if at least one match is found; {@code false} otherwise
     */
    default boolean isFormatted(String string) {
        return matcher(string).find();
    }

    /**
     * Strip all substrings matching this format from the input text.
     * <p>
     * This default implementation simply finds each match via {@link #matcher(String)}
     * and removes it. Implementations with more complex needs or performance constraints
     * are encouraged to override this method with a tailored removal algorithm.
     * </p>
     *
     * @param string the original text containing format markers
     * @return a new string with all matching substrings removed
     */
    default String removeFormat(String string) {
        if (StringUtils.isBlank(string)) return string;
        Matcher m = matcher(string);
        while (m.find()) {
            string = string.replace(m.group(), "");
        }
        return string;
    }

    /**
     * Transform the given text using this format in the context of a single {@link Player}.
     * <p>
     * Implementations must override this method to perform the desired transformation
     * based on the raw input string and optionally the player context.
     * </p>
     *
     * @param player the player context (may be {@code null})
     * @param string the text to transform (never {@code null})
     * @return the result of applying this format
     */
    @NotNull
    T accept(Player player, String string);

    /**
     * Transform the given text using this format without any player context.
     *
     * @param string the text to transform (never {@code null})
     * @return the result of applying this format
     */
    @NotNull
    default T accept(String string) {
        return accept(null, string);
    }
}
