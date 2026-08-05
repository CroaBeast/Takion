package me.croabeast.takion.adventure;

import me.croabeast.takion.token.Token;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.croabeast.prismatic.PrismaticAPI;
import me.croabeast.takion.TakionLib;
import me.croabeast.takion.format.PlainFormat;
import me.croabeast.takion.format.StringFormat;
import me.croabeast.takion.tag.Tag;
import me.croabeast.takion.tag.TagRenderer;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class AdventureTagRenderer implements TagRenderer<Component> {

    private final TakionLib lib;

    /**
     * Creates a renderer for a library instance.
     *
     * @param lib owning library
     * @return a new renderer
     */
    @NotNull
    public static AdventureTagRenderer of(@NotNull TakionLib lib) {
        return new AdventureTagRenderer(lib);
    }

    @Override
    @NotNull
    public Component render(
            @Nullable Player receiver,
            @Nullable Player parser,
            @Nullable String string
    ) {
        if (StringUtils.isBlank(string))
            return Component.text(StringUtils.defaultString(string));

        String result = lib.getPlaceholderManager().replace(parser, string);
        result = PlainFormat.PLACEHOLDER_API.accept(parser, result);
        result = applyStringFormat(result);

        Token.Context context = createContext(receiver, parser);
        List<ComponentToken> tokens = new ArrayList<>();

        for (Tag tag : lib.getTagManager().getTags()) {
            Matcher matcher = tag.getPattern().matcher(result);
            StringBuffer buffer = new StringBuffer();

            while (matcher.find()) {
                MatchResult match = matcher.toMatchResult();
                Token.Rendered tagResult = tag.resolve(context, match);
                String token = "__TAKION_TAG_" + tokens.size() + "__";

                tokens.add(new ComponentToken(token, renderTag(tag, context, match, tagResult)));
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(token));
            }

            matcher.appendTail(buffer);
            result = buffer.toString();
        }

        Component component = lib.isAdventureAvailable() ?
                PrismaticAPI.adventure().colorize(receiver == null ? parser : receiver, result) :
                Component.text(result);

        for (ComponentToken token : tokens)
            component = component.replaceText(builder -> builder
                    .matchLiteral(token.getToken())
                    .replacement(token.getComponent()));

        return component;
    }

    @NotNull
    private Component renderTag(Tag tag, Token.Context context, MatchResult match, Token.Rendered result) {
        if (tag instanceof AdventureTag)
            return ((AdventureTag) tag).renderComponent(context, match, result);

        if (lib.supportsObjectTags()) {
            Component component = renderObjectTag(tag, result);
            if (component != null) return component;
        }

        return Component.text(result.getLegacy());
    }

    @Nullable
    private Component renderObjectTag(Tag tag, Token.Rendered result) {
        String id = tag.getId().toLowerCase(Locale.ENGLISH);

        if ("player_head".equals(id)) return ObjectTagComponents.playerHead(result);
        if ("sprite".equals(id)) return ObjectTagComponents.sprite(result);

        return null;
    }

    private Token.Context createContext(Player receiver, Player parser) {
        return new Token.SimpleContext()
                .setLib(lib)
                .setReceiver(receiver)
                .setParser(parser)
                .setOptions(new HashMap<>());
    }

    private String applyStringFormat(String string) {
        StringFormat format = lib.getFormatManager().get("SMALL_CAPS");
        return format == null ? string : format.accept(null, string);
    }

    @RequiredArgsConstructor
    @Getter
    private static final class ComponentToken {

        private final String token;
        private final Component component;
    }
}
