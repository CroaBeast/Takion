package me.croabeast.takion.chat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.croabeast.common.util.Exceptions;
import me.croabeast.takion.TakionLib;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
class ComponentImpl implements ChatComponent<ComponentImpl> {

    private final TakionLib lib;
    @NotNull
    private String message;

    ClickHolder clickEvent = null;
    HoverHolder hoverEvent = null;

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class ClickHolder {
        final Click click;
        final String input;

        ClickEvent create(Player player) {
            return new ClickEvent(click.asBukkit(), lib.colorize(player, input));
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @SuppressWarnings("deprecation")
    class HoverHolder {
        final List<String> list;

        HoverEvent create(Player player) {
            final BaseComponent[] contents = new BaseComponent[list.size()];
            int size = list.size();

            for (int i = 0; i < size; i++) {
                String s = lib.colorize(player, list.get(i));
                if (i != size - 1) s += "\n";
                contents[i] = new TextComponent(TextComponent.fromLegacyText(s));
            }

            return new HoverEvent(HoverEvent.Action.SHOW_TEXT, contents);
        }
    }

    @NotNull
    public ComponentImpl setMessage(@NotNull String message) {
        this.message = Exceptions.validate(message, StringUtils::isNotBlank);
        return instance();
    }

    @NotNull
    public ComponentImpl setClick(Click click, String input) {
        clickEvent = new ClickHolder(click, input);
        return instance();
    }

    @NotNull
    public ComponentImpl setHover(List<String> list) {
        hoverEvent = new HoverHolder(list);
        return instance();
    }

    @NotNull
    public ComponentImpl setHover(String string) {
        Pattern pattern = Pattern.compile("hover:\"(.*?)\"");
        Matcher matcher = pattern.matcher(string);
        while (matcher.find())
            string = string.replace(matcher.group(), matcher.group(1));
        return setHover(lib.splitString(string));
    }

    boolean hasClick() {
        return clickEvent != null && (clickEvent.click != null || StringUtils.isNotBlank(clickEvent.input));
    }

    boolean hasHover() {
        return hoverEvent != null && (hoverEvent.list != null && !hoverEvent.list.isEmpty());
    }

    @Override
    public boolean hasEvents() {
        return hasClick() || hasHover();
    }

    @NotNull
    public BaseComponent[] compile(Player player) {
        Matcher urlMatch = URL_PATTERN.matcher(message);
        if (urlMatch.find())
            setClick(Click.OPEN_URL, urlMatch.group());

        String line = lib.colorize(player, message);

        BaseComponent[] array = TextComponent.fromLegacyText(line);
        TextComponent comp = new TextComponent(array);

        if (clickEvent != null)
            comp.setClickEvent(clickEvent.create(player));
        if (hoverEvent != null)
            comp.setHoverEvent(hoverEvent.create(player));

        return new BaseComponent[] {comp};
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (BaseComponent component : compile(null))
            sb.append(component.toLegacyText());
        return sb.toString();
    }

    @NotNull
    public ComponentImpl instance() {
        return this;
    }
}
