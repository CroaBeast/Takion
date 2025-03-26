package me.croabeast.takion.character;

import me.croabeast.lib.util.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface CharacterManager {

    CharacterInfo DEFAULT_INFO = CharacterInfo.of('a', 5);

    CharacterInfo getInfo(char c);

    default CharacterInfo getInfo(String string) {
        Character c = toCharacter(string);
        return c == null ? DEFAULT_INFO : getInfo(c);
    }

    void addCharacter(char c, int length);

    default void addCharacter(String string, int length) {
        Character c = toCharacter(string);
        if (c != null) addCharacter(c, length);
    }

    void removeCharacters(Character... chars);

    default void removeCharacters(String... strings) {
        if (ArrayUtils.isArrayEmpty(strings)) return;

        List<Character> list = new ArrayList<>();
        for (String input : strings) {
            Character c = toCharacter(input);
            if (c != null) list.add(c);
        }

        removeCharacters(list.toArray(new Character[0]));
    }

    String align(int limit, String string);

    default String align(String string) {
        return align(154, string);
    }

    @Nullable
    static Character toCharacter(String string) {
        if (StringUtils.isBlank(string)) return null;

        char[] array = string.toCharArray();
        return array.length != 1 ? null : array[0];
    }
}
