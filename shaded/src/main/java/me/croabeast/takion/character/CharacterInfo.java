package me.croabeast.takion.character;

/**
 * Represents information about a character including its visual width.
 * <p>
 * This interface provides methods to retrieve the character itself and its
 * associated length (which might be used for formatting or layout purposes).
 * </p>
 * Additionally, it offers a method to calculate the "bold" length, which takes
 * into account an extra width for non-space characters.
 */
public interface CharacterInfo {

    /**
     * Retrieves the character represented by this instance.
     *
     * @return the character
     */
    char getCharacter();

    /**
     * Retrieves the base length associated with this character.
     * <p>
     * The length can be interpreted as the visual or logical width of the character.
     * </p>
     *
     * @return the length of the character
     */
    int getLength();

    /**
     * Calculates the bold length of the character.
     * <p>
     * This is computed as the base length plus an extra unit of length for non-space characters.
     * For space characters, the bold length is equal to the base length.
     * </p>
     *
     * @return the bold length of the character
     */
    default int getBoldLength() {
        return getLength() + (getCharacter() == ' ' ? 0 : 1);
    }

    /**
     * Creates a new {@code CharacterInfo} instance with the specified character and length.
     * <p>
     * This factory method provides a convenient way to obtain a {@code CharacterInfo}
     * implementation without needing to explicitly create a new class.
     * </p>
     *
     * @param c the character
     * @param i the base length of the character
     * @return a new {@code CharacterInfo} instance
     */
    static CharacterInfo of(char c, int i) {
        return new CharacterInfo() {
            @Override
            public char getCharacter() {
                return c;
            }

            @Override
            public int getLength() {
                return i;
            }

            @Override
            public String toString() {
                return "CharacterInfo{character='" + c + "', length=" + i + "}";
            }
        };
    }
}
