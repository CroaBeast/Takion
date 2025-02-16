package me.croabeast.takion.misc;

import lombok.experimental.UtilityClass;
import me.croabeast.takion.TakionLib;
import me.croabeast.takion.character.CharacterInfo;
import me.croabeast.lib.applier.StringApplier;
import me.croabeast.lib.util.TextUtils;
import me.croabeast.prismatic.PrismaticAPI;
import org.apache.commons.lang.StringUtils;

@UtilityClass
public class StringAligner {

    public String align(TakionLib lib, int limit, String string) {
        if (StringUtils.isBlank(string)) return string;

        String prefix = lib.getCenterPrefix();
        if (StringUtils.isBlank(prefix) ||
                !string.startsWith(prefix)) return string;

        final String before = string.replace(prefix, "");

        String temp = StringApplier.simplified(before)
                .apply(PrismaticAPI::stripAll)
                .apply(TextUtils.STRIP_JSON)
                .apply(lib.getCharacterAction()::act).toString();

        int size = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : temp.toCharArray()) {
            if (c == '§') {
                previousCode = true;
                continue;
            }

            else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
                continue;
            }

            CharacterInfo info = lib.getCharacterManager().getInfo(c);
            size += isBold ?
                    info.getBoldLength() : info.getLength();
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

    public String align(int limit, String string) {
        return align(TakionLib.getLib(), limit, string);
    }

    public String align(String string) {
        return align(154, string);
    }
}
