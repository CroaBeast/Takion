package me.croabeast.takion.character;

import me.croabeast.common.util.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides functionality for managing character information used for layout and formatting.
 * <p>
 * A {@code CharacterManager} allows you to register custom characters with specific widths,
 * retrieve character information, remove characters, and align strings based on character widths.
 * </p>
 * This is particularly useful when handling text rendering or custom UI layouts where each character
 * might have a different visual width.
 */
public interface CharacterManager {

    /**
     * The default character information used when no specific info is found.
     * Defaults to the character 'a' with a base length of 5.
     */
    CharacterInfo DEFAULT_INFO = CharacterInfo.of('a', 5);

    /**
     * Retrieves the {@link CharacterInfo} for the given character.
     *
     * @param c the character for which to retrieve information
     * @return the {@link CharacterInfo} corresponding to the character
     */
    CharacterInfo getInfo(char c);

    /**
     * Retrieves the {@link CharacterInfo} for the first character of the provided string.
     * <p>
     * If the input string is null, empty, or contains more than one character, the default
     * character information is returned.
     * </p>
     *
     * @param string the string representing the character (should be of length 1)
     * @return the {@link CharacterInfo} for the character, or {@code DEFAULT_INFO} if invalid
     */
    default CharacterInfo getInfo(String string) {
        Character c = toCharacter(string);
        return c == null ? DEFAULT_INFO : getInfo(c);
    }

    /**
     * Registers a custom character with its associated visual length.
     * <p>
     * This allows the {@code CharacterManager} to handle custom formatting based on character widths.
     * </p>
     *
     * @param c      the character to add
     * @param length the visual length (width) of the character
     */
    void addCharacter(char c, int length);

    /**
     * Registers a custom character from a string with its associated visual length.
     * <p>
     * The string should contain exactly one character; otherwise, the operation is ignored.
     * </p>
     *
     * @param string the string representing the character to add
     * @param length the visual length (width) of the character
     */
    default void addCharacter(String string, int length) {
        Character c = toCharacter(string);
        if (c != null) addCharacter(c, length);
    }

    /**
     * Removes the specified characters from the manager.
     *
     * @param chars the characters to remove
     */
    void removeCharacters(Character... chars);

    /**
     * Removes characters specified by strings from the manager.
     * <p>
     * Each string should contain exactly one character; if not, it is ignored.
     * </p>
     *
     * @param strings an array of strings representing the characters to remove
     */
    default void removeCharacters(String... strings) {
        if (ArrayUtils.isArrayEmpty(strings)) return;

        List<Character> list = new ArrayList<>();
        for (String input : strings) {
            Character c = toCharacter(input);
            if (c != null) list.add(c);
        }

        removeCharacters(list.toArray(new Character[0]));
    }

    /**
     * Aligns a string based on a specified limit using character information.
     * <p>
     * This method adjusts the string's layout so that it fits within a given visual width (limit),
     * using the character lengths defined in the manager.
     * </p>
     *
     * @param limit  the maximum allowed width for the string
     * @param string the string to be aligned
     * @return the aligned string
     */
    String align(int limit, String string);

    /**
     * Aligns a string based on a default width limit.
     * <p>
     * The default limit is set to 154. This method provides a convenient overload if no custom limit is required.
     * </p>
     *
     * @param string the string to be aligned
     * @return the aligned string using the default width limit
     */
    default String align(String string) {
        return align(154, string);
    }

    /**
     * Converts a string into a {@link Character} if it contains exactly one character.
     * <p>
     * If the input string is blank or contains more than one character, {@code null} is returned.
     * </p>
     *
     * @param string the string to convert
     * @return the {@link Character} if the string is valid; otherwise, {@code null}
     */
    @Nullable
    static Character toCharacter(String string) {
        if (StringUtils.isBlank(string)) return null;

        char[] array = string.toCharArray();
        return array.length != 1 ? null : array[0];
    }
}
