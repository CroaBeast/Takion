package me.croabeast.takion.character;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class CharacterInfo {

    private final char character;
    private final int length;

    public int getBoldLength() {
        return length + (character == ' ' ? 0 : 1);
    }
}
