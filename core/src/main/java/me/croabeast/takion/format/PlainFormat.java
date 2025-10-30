package me.croabeast.takion.format;

import com.loohp.interactivechat.api.InteractiveChatAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import me.croabeast.common.util.Exceptions;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;

/**
 * A specialization of {@link Format} for formats that do not rely on regular-expression matching.
 * <p>
 * {@code PlainFormat} is intended for cases where your formatting logic cannot be captured
 * (or need not be captured) by a single regex. By default, the regex-based methods are stubbed
 * out—implementors should override them only if they actually require regex support.
 * </p>
 * Otherwise, all matching and processing should occur in the {@link Format#accept(Player, String)} methods directly.
 *
 * @param <T> the result type produced by this format when applied to input text
 * @see Format
 */
@FunctionalInterface
public interface PlainFormat<T> extends Format<T> {

    /**
     * A {@link PlainFormat} implementation that integrates with PlaceholderAPI.
     * <p>
     * If the input {@code string} is non‑blank and the PlaceholderAPI plugin is present
     * (enabled), it will replace any PlaceholderAPI placeholders in the text for the
     * provided {@link Player}; otherwise, it returns the original text.
     * </p>
     */
    PlainFormat<String> PLACEHOLDER_API = new PlainFormat<String>() {
        @NotNull
        public String accept(Player player, String string) {
            return StringUtils.isBlank(string) ||
                    !Exceptions.isPluginEnabled("PlaceholderAPI") ?
                    string :
                    PlaceholderAPI.setPlaceholders(player, string);
        }

        @Override
        public String removeFormat(String string) {
            return accept(string);
        }

        @Override
        public boolean isFormatted(String string) {
            return Exceptions.isPluginEnabled("PlaceholderAPI") &&
                    (PlaceholderAPI.containsPlaceholders(string) ||
                            PlaceholderAPI.containsBracketPlaceholders(string));
        }
    };

    /**
     * A {@link PlainFormat} implementation that integrates with InteractiveChat.
     * <p>
     * If the input {@code string} is non‑blank and the InteractiveChat plugin is present
     * (enabled), it will mark the text so that clicking it in chat triggers an
     * interactive chat action for the provided {@link Player};
     * otherwise, it returns the original text.
     * </p>
     */
    PlainFormat<String> INTERACTIVE_CHAT = (player, string) -> {
        if (StringUtils.isBlank(string) ||
                !Exceptions.isPluginEnabled("InteractiveChat"))
            return string;

        try {
            return InteractiveChatAPI
                    .markSender(string, player.getUniqueId());
        } catch (Exception e) {
            return string;
        }
    };

    /**
     * A {@link PlainFormat} implementation that trims leading spaces from the input string.
     * <p>
     * If the input {@code string} is non‑blank, it removes all leading spaces
     * and returns the trimmed result; otherwise, it returns the original text.
     * </p>
     */
    PlainFormat<String> TRIM_START_SPACES = (player, string) -> {
        if (StringUtils.isBlank(string)) return string;
        String startLine = string;

        try {
            while (string.charAt(0) == ' ') string = string.substring(1);
            return string;
        } catch (IndexOutOfBoundsException e) {
            return startLine;
        }
    };

    /**
     * Returns the regular expression used for matching format tokens in the input.
     * <p>
     * Since {@code PlainFormat} implementations typically do not use regex matching,
     * the default implementation returns an empty string. Override only if your plain
     * format needs to expose a regex for some reason.
     * </p>
     *
     * @return an empty string by default
     */
    @NotNull
    default String getRegex() {
        return "";
    }

    /**
     * Creates a {@link Matcher} for the given input text.
     * <p>
     * The default implementation always throws {@link UnsupportedOperationException}
     * because plain formats do not support regex-based matching out of the box.
     * If you need regex matching, override this method with your own {@code Pattern}
     * compilation and matching logic.
     * </p>
     * Otherwise, perform any necessary matching directly in your {@code accept(...)} implementation.
     * </p>
     *
     * @param string the input text to be matched
     * @return a {@link Matcher} for the provided input
     *
     * @throws UnsupportedOperationException always, unless overridden by the implementor
     */
    @NotNull
    default Matcher matcher(String string) {
        throw new UnsupportedOperationException("matcher");
    }

    /**
     * Checks whether this format applies to the given input.
     * <p>
     * The default implementation always returns {@code false}. If your plain format
     * can detect its pattern without regex (for example, via prefix checks or other
     * heuristics), override this method to provide that logic.
     * </p>
     *
     * @param string the input text to test
     * @return {@code true} if this format should process the input; {@code false} otherwise
     */
    @Override
    default boolean isFormatted(String string) {
        return false;
    }

    /**
     * Removes the formatting syntax from the input text.
     * <p>
     * The default implementation returns the input unchanged. If your plain format
     * supports stripping its own custom markers or tokens, override this method to
     * implement the removal logic.
     * </p>
     *
     * @param string the input text from which to remove formatting
     * @return the text with formatting removed
     */
    @Override
    default String removeFormat(String string) {
        return string;
    }
}
