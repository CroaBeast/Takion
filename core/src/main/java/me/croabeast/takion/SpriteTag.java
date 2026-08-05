package me.croabeast.takion;

import me.croabeast.takion.token.Token;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.croabeast.takion.tag.Tag;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

final class SpriteTag implements Tag {

    private static final Pattern PATTERN = Pattern.compile("(?i)<sprite:([^>]+)>");
    private static final String MEASURE_MARKER = "\u25A0";

    @NotNull
    public String getId() {
        return "sprite";
    }

    @NotNull
    public Pattern getPattern() {
        return PATTERN;
    }

    @NotNull
    public Token.Rendered resolve(@NotNull Token.Context context, @NotNull MatchResult match) {
        SpriteArguments arguments = parseArguments(match.group(1));
        if (arguments == null) return new Token.SimpleRendered();

        Map<String, Object> data = new HashMap<>();
        if (arguments.getAtlas() != null) data.put("atlas", arguments.getAtlas());
        data.put("sprite", arguments.getSprite());

        return new Token.SimpleRendered()
                .setLegacy("")
                .setMeasure(MEASURE_MARKER)
                .setData(data);
    }

    private SpriteArguments parseArguments(String raw) {
        if (StringUtils.isBlank(raw)) return null;

        String value = raw.trim();

        String[] pair = splitPair(value, "|");
        if (pair == null) pair = splitPair(value, ",");
        if (pair == null) pair = splitPair(value, ":");

        if (pair == null) {
            String sprite = parseKey(value);
            return sprite == null ? null : new SpriteArguments(null, sprite);
        }

        String atlas = parseKey(pair[0]);
        String sprite = parseKey(pair[1]);
        return atlas == null || sprite == null ? null : new SpriteArguments(atlas, sprite);
    }

    private String[] splitPair(String value, String delimiter) {
        int index = value.indexOf(delimiter);
        if (index < 0) return null;

        String first = value.substring(0, index).trim();
        String second = value.substring(index + delimiter.length()).trim();
        return StringUtils.isBlank(first) || StringUtils.isBlank(second) ?
                null :
                new String[] { first, second };
    }

    private String parseKey(String value) {
        if (StringUtils.isBlank(value)) return null;

        String key = value.trim();
        return key.matches("[a-z0-9._/-]+(:[a-z0-9._/-]+)?") ? key : null;
    }

    @RequiredArgsConstructor
    @Getter
    private static final class SpriteArguments {

        private final String atlas;
        private final String sprite;
    }
}
