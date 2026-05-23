package me.croabeast.takion.format;

/**
 * A {@link TextFormat} specialization that produces {@link String} results.
 * <p>
 * {@code StringFormat} is a convenient marker interface for formats which
 * transform or process input text into another string. All formatting logic
 * is defined via the inherited {@link #accept(String)} method and its
 * context‑aware overloads.
 * </p>
 *
 * @see TextFormat
 */
public interface StringFormat extends TextFormat<String> {

    /**
     * A built-in {@link StringFormat} that formats player head placeholders into
     * their corresponding display strings using {@code PlayerHeadUtils}.
     */
    StringFormat PLAYER_HEAD_FORMAT = PlayerHeadUtils.FORMAT;
}
