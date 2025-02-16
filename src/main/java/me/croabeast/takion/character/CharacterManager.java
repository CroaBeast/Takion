package me.croabeast.takion.character;

import me.croabeast.lib.util.ArrayUtils;

public interface CharacterManager {

    CharacterInfo DEFAULT = new CharacterInfo('a', 5);

    CharacterInfo getInfo(char c);

    Character toCharacter(String string);

    default CharacterInfo getInfo(String string) {
        Character c = toCharacter(string);
        return c == null ? DEFAULT : getInfo(c);
    }

    void addCharacter(char c, int length);

    default void addCharacter(String string, int length) {
        Character c = toCharacter(string);
        if (c != null) addCharacter(c, length);
    }

    void removeCharacters(char... chars);

    default void removeCharacters(String... strings) {
        if (ArrayUtils.isArrayEmpty(strings))
            return;

        for (String input : strings) {
            Character character = toCharacter(input);
            if (character != null)
                removeCharacters(character);
        }
    }
}
