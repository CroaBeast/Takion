package me.croabeast.takion.character;

public interface CharacterInfo {

    char getCharacter();

    int getLength();

    default int getBoldLength() {
        return getLength() + (getCharacter() == ' ' ? 0 : 1);
    }

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
