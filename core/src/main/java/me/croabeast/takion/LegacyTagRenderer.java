package me.croabeast.takion;

import me.croabeast.takion.token.Token;
import lombok.RequiredArgsConstructor;
import me.croabeast.takion.tag.Tag;
import me.croabeast.takion.tag.TagRenderer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.regex.Matcher;

@RequiredArgsConstructor
final class LegacyTagRenderer implements TagRenderer<String> {

    private final TakionLib lib;

    @NotNull
    public String render(
            @Nullable Player receiver,
            @Nullable Player parser,
            @Nullable String string
    ) {
        if (StringUtils.isBlank(string)) return StringUtils.defaultString(string);

        Token.Context context = createContext(receiver, parser);
        String result = string;

        for (Tag tag : lib.getTagManager().getTags()) {
            Matcher matcher = tag.getPattern().matcher(result);
            StringBuffer buffer = new StringBuffer();

            while (matcher.find()) {
                String legacy = tag.resolve(context, matcher.toMatchResult()).getLegacy();
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(legacy));
            }

            matcher.appendTail(buffer);
            result = buffer.toString();
        }

        return result;
    }

    @NotNull
    String renderMeasure(
            @Nullable Player receiver,
            @Nullable Player parser,
            @Nullable String string
    ) {
        if (StringUtils.isBlank(string)) return StringUtils.defaultString(string);

        Token.Context context = createContext(receiver, parser);
        String result = string;

        for (Tag tag : lib.getTagManager().getTags()) {
            Matcher matcher = tag.getPattern().matcher(result);
            StringBuffer buffer = new StringBuffer();

            while (matcher.find()) {
                String measure = tag.resolve(context, matcher.toMatchResult()).getMeasure();
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(measure));
            }

            matcher.appendTail(buffer);
            result = buffer.toString();
        }

        return result;
    }

    private Token.Context createContext(Player receiver, Player parser) {
        return new Token.SimpleContext()
                .setLib(lib)
                .setReceiver(receiver)
                .setParser(parser)
                .setOptions(new HashMap<>());
    }
}
