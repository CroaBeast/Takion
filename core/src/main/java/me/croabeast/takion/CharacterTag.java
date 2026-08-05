package me.croabeast.takion;

import me.croabeast.takion.token.Token;
import me.croabeast.takion.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

final class CharacterTag implements Tag {

    private static final Pattern PATTERN = Pattern.compile("(?i)<u:([a-fA-F\\d]{4})>");

    @NotNull
    public String getId() {
        return "character";
    }

    @NotNull
    public Pattern getPattern() {
        return PATTERN;
    }

    @NotNull
    public Token.Rendered resolve(@NotNull Token.Context context, @NotNull MatchResult match) {
        String value = String.valueOf((char) Integer.parseInt(match.group(1), 16));
        return new Token.SimpleRendered()
                .setLegacy(value)
                .setMeasure(value);
    }
}
