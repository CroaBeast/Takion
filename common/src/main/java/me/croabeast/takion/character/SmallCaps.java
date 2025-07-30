package me.croabeast.takion.character;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * An enumeration representing small capital letters as a specialized form of characters.
 * <p>
 * Each constant in {@code SmallCaps} implements {@link CharacterInfo} and provides a corresponding
 * small capital character along with a default length used for display calculations. This enum also
 * provides utility methods for stripping accents from characters and converting strings to small caps
 * or back to their normal representations.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre><code>
 * // Convert a normal string to small caps:
 * String smallCapsString = SmallCaps.toSmallCaps("Hello World");
 *
 * // Check if a character is in small caps:
 * boolean isSmall = SmallCaps.isSmallCaps('ᴀ');
 *
 * // Convert a small caps string back to normal:
 * String normalString = SmallCaps.toNormal(smallCapsString);
 * </code></pre>
 * </p>
 *
 * @see CharacterInfo
 */
@Getter
public enum SmallCaps implements CharacterInfo {
    A('ᴀ'),
    B('ʙ'),
    C('ᴄ'),
    D('ᴅ'),
    E('ᴇ'),
    F('ғ'),
    G('ɢ'),
    H('ʜ'),
    I('ɪ', 3),
    J('ᴊ'),
    K('ᴋ'),
    L('ʟ'),
    M('ᴍ'),
    N('ɴ'),
    O('ᴏ'),
    P('ᴘ'),
    Q('ǫ'),
    R('ʀ'),
    S('s'),
    T('ᴛ'),
    U('ᴜ'),
    V('ᴠ'),
    W('ᴡ'),
    X('x'),
    Y('ʏ'),
    Z('ᴢ');

    /**
     * The small capital character representation.
     */
    final char character;
    /**
     * The default (normal) character for this constant.
     * <p>
     * It is derived from the enum name in lowercase.
     */
    @Getter(AccessLevel.NONE)
    final char defaultValue;
    /**
     * The display length for this character, used for alignment calculations.
     */
    int length = 5;

    /**
     * Constructs a {@code SmallCaps} constant with the specified small capital character.
     * The default value is automatically derived from the enum constant name.
     *
     * @param character the small capital character.
     */
    SmallCaps(char character) {
        defaultValue = name().toLowerCase(Locale.ENGLISH).toCharArray()[0];
        this.character = character;
    }

    /**
     * Constructs a {@code SmallCaps} constant with the specified small capital character and display length.
     *
     * @param character the small capital character.
     * @param i         the display length for the character.
     */
    SmallCaps(char character, int i) {
        this(character);
        length = i;
    }

    /**
     * Compares the provided character to this constant's default value, ignoring case.
     *
     * @param c the character to compare.
     * @return {@code true} if the character matches (ignoring case); {@code false} otherwise.
     */
    private boolean equalsIgnoreCase(char c) {
        return (defaultValue + "").matches("(?i)" + Pattern.quote(String.valueOf(c)));
    }

    /**
     * Returns the bold length of this character.
     * <p>
     * Bold length is calculated as the defined length plus one.
     * </p>
     *
     * @return the bold length.
     */
    @Override
    public int getBoldLength() {
        return length + 1;
    }

    /**
     * Returns the small capital character as a string.
     *
     * @return the small capital character in string form.
     */
    @Override
    public String toString() {
        return String.valueOf(character);
    }

    /**
     * Removes accents from the provided string.
     *
     * @param string the string from which accents should be removed.
     * @return the string without any accents.
     */
    public static String stripAccents(String string) {
        if (StringUtils.isBlank(string)) return string;
        Normalizer.Form form = Normalizer.Form.NFKD;
        return Normalizer.normalize(string, form).replaceAll("\\p{M}", "");
    }

    /**
     * Removes accents from the given character.
     *
     * @param character the character to process.
     * @return the character without accent marks.
     */
    public static char stripAccent(char character) {
        return stripAccents(String.valueOf(character)).toCharArray()[0];
    }

    /**
     * Returns the corresponding {@code SmallCaps} constant for the given character.
     * <p>
     * If {@code strip} is {@code true}, accents are removed from the character before comparison.
     * </p>
     *
     * @param character the character to convert.
     * @param strip     if {@code true}, the character is normalized by stripping accents.
     * @return the corresponding {@code SmallCaps} constant, or {@code null} if no match is found.
     */
    private static SmallCaps valueOf(char character, boolean strip) {
        char c = strip ? stripAccent(character) : character;
        for (SmallCaps caps : values())
            if (caps.equalsIgnoreCase(c)) return caps;
        return null;
    }

    /**
     * Returns the corresponding {@code SmallCaps} constant for the given character.
     * <p>
     * Accents are stripped by default.
     * </p>
     *
     * @param character the character to convert.
     * @return the corresponding {@code SmallCaps} constant, or {@code null} if no match is found.
     */
    public static SmallCaps valueOf(char character) {
        return valueOf(character, true);
    }

    /**
     * Checks if the provided character is represented as a small capital letter.
     *
     * @param character the character to check.
     * @return {@code true} if the character is a small capital; {@code false} otherwise.
     */
    public static boolean isSmallCaps(char character) {
        return valueOf(character) != null;
    }

    /**
     * Checks if the provided string contains any small capital letters.
     *
     * @param string the string to check.
     * @return {@code true} if the string contains at least one small capital letter; {@code false} otherwise.
     */
    public static boolean hasSmallCaps(String string) {
        if (StringUtils.isBlank(string))
            return false;

        for (char c : string.toCharArray())
            if (isSmallCaps(c)) return true;
        return false;
    }

    /**
     * Converts the provided string to its small capital form.
     *
     * @param string the input string.
     * @return a new string where applicable characters are replaced with their small capital equivalents.
     */
    public static String toSmallCaps(String string) {
        if (StringUtils.isBlank(string))
            return string;

        char[] first = stripAccents(string).toCharArray();
        int length = first.length;

        char[] result = new char[length];
        for (int i = 0; i < length; i++) {
            SmallCaps caps = valueOf(first[i], false);
            result[i] = caps != null ?
                    caps.character : first[i];
        }

        return new String(result);
    }

    /**
     * Converts a small capital string back to its normal form.
     *
     * @param string the small capital string.
     * @return a new string where small capital characters are replaced by their default (normal) counterparts.
     */
    public static String toNormal(String string) {
        if (StringUtils.isBlank(string))
            return string;

        char[] first = string.toCharArray();
        int length = first.length;

        char[] result = new char[length];
        for (int i = 0; i < length; i++) {
            char c = first[i];

            if (isSmallCaps(c)) {
                SmallCaps sc = valueOf(c);
                if (sc != null) c = sc.defaultValue;
            }
            result[i] = c;
        }
        return new String(result);
    }
}
