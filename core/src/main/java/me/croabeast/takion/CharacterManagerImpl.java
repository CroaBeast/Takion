package me.croabeast.takion;

import me.croabeast.common.applier.StringApplier;
import me.croabeast.common.util.ArrayUtils;
import me.croabeast.prismatic.PrismaticAPI;
import me.croabeast.takion.character.CharacterInfo;
import me.croabeast.takion.character.CharacterManager;
import me.croabeast.takion.character.DefaultCharacter;
import me.croabeast.takion.character.SmallCaps;
import me.croabeast.takion.chat.MultiComponent;
import me.croabeast.takion.format.StringFormat;
import org.apache.commons.lang.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

final class CharacterManagerImpl implements CharacterManager {

    private final Map<Character, CharacterInfo> map = new LinkedHashMap<>();
    private final TakionLib lib;

    CharacterManagerImpl(TakionLib lib) {
        this.lib = lib;

        for (DefaultCharacter c : DefaultCharacter.values())
            map.put(c.getCharacter(), c);

        for (SmallCaps caps : SmallCaps.values())
            map.put(caps.getCharacter(), caps);
    }

    @Override
    public CharacterInfo getInfo(char c) {
        CharacterInfo info = map.getOrDefault(c, null);
        return info == null ? DEFAULT_INFO : info;
    }

    @Override
    public void addCharacter(char c, int length) {
        map.put(c, CharacterInfo.of(c, length));
    }

    @Override
    public void removeCharacters(Character... chars) {
        if (!ArrayUtils.isArrayEmpty(chars)) for (char c : chars) map.remove(c);
    }

    @Override
    public String align(int limit, String string) {
        if (StringUtils.isBlank(string)) return string;

        final String prefix = lib.getCenterPrefix();
        if (StringUtils.isBlank(prefix) || !string.startsWith(prefix))
            return string;

        String before = string.replace(prefix, "");
        String temp = StringApplier.simplified(before)
                .apply(PrismaticAPI::stripAll)
                .apply(MultiComponent.DEFAULT_FORMAT::removeFormat)
                .apply(s -> {
                    StringFormat format = lib.getFormatManager().get("character");
                    return format.accept(s);
                }).toString();

        int size = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : temp.toCharArray()) {
            if (c == '§') {
                previousCode = true;
                continue;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
                continue;
            }

            CharacterInfo info = getInfo(c);
            size += isBold ? info.getBoldLength() : info.getLength();
            size++;
        }

        int toCompensate = limit - (size / 2);
        int compensated = 0;

        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(' ');
            compensated += 4;
        }

        return sb + before;
    }
}
