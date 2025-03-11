package me.croabeast.takion.message.chat;

import lombok.Setter;
import me.croabeast.takion.TakionLib;
import me.croabeast.takion.misc.StringAligner;
import me.croabeast.lib.applier.StringApplier;
import me.croabeast.lib.util.ArrayUtils;
import me.croabeast.lib.util.TextUtils;
import me.croabeast.prismatic.PrismaticAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatComponent {

    private final TakionLib lib;

    private final Map<Integer, Component> map = new LinkedHashMap<>();
    private int index = -1;

    @Setter
    private boolean parseURLs = true;

    private void parseURLs(String string) {
        Matcher urlMatcher = TextUtils.URL_PATTERN.matcher(string);
        int lastEnd = 0;

        while (urlMatcher.find()) {
            String t = string.substring(lastEnd, urlMatcher.start());

            if (!t.isEmpty())
                map.put(++index, new Component(t));

            if (parseURLs) {
                final String url = urlMatcher.group();

                ChatClick.Action a = ChatClick.Action.OPEN_URL;
                ChatClick c = new ChatClick(lib, a, url);

                map.put(++index, new Component(url).setClick(c));
            }

            lastEnd = urlMatcher.end();
        }

        if (lastEnd > (string.length() - 1)) return;
        map.put(++index, new Component(string.substring(lastEnd)));
    }

    private void updateMessageMapping(String string) {
        if (string == null) return;

        if (string.isEmpty()) {
            map.put(++index, new Component(string));
            return;
        }

        String line = StringApplier.simplified(string).
                apply(TextUtils.CONVERT_OLD_JSON).
                apply(lib.getSmallCapsAction()::act).
                apply(StringAligner::align).toString();

        Matcher match = TextUtils.FORMAT_CHAT_PATTERN.matcher(line);
        int last = 0;

        while (match.find()) {
            String temp = line.substring(last, match.start());
            if (!temp.isEmpty()) parseURLs(temp);

            String[] args = match.group(1).split("[|]", 2);
            String h = null, c = null;

            for (String s : args) {
                Matcher m = Pattern.compile("(?i)hover").matcher(s);
                if (m.find()) h = s; else c = s;
            }

            Component message = new Component(match.group(7));
            map.put(++index, message.setHandler(c, h));

            last = match.end();
        }

        if (last <= (line.length() - 1)) parseURLs(line.substring(last));
    }

    private ChatComponent(TakionLib lib, String message) {
        this.lib = Objects.requireNonNull(lib);
        updateMessageMapping(message);
    }

    private ChatComponent(ChatComponent builder) {
        Objects.requireNonNull(builder);

        lib = builder.lib;
        parseURLs = builder.parseURLs;

        if (builder.map.isEmpty()) return;

        map.putAll(builder.map);
        this.index = builder.index;
    }

    public ChatComponent setHover(int index, ChatHover hover) {
        if (index < 0 || ChatEvent.isEmpty(hover))
            return this;

        map.put(index, map.get(index).setHover(hover));
        return this;
    }

    public ChatComponent setHover(int index, List<String> hover) {
        return hover == null || hover.isEmpty() ?
                this :
                setHover(index, new ChatHover(lib, hover));
    }

    public ChatComponent setHover(int index, String... hover) {
        return setHover(index, ArrayUtils.toList(hover));
    }

    public ChatComponent setHover(ChatHover hover) {
        return setHover(index, hover);
    }

    public ChatComponent setHover(List<String> hover) {
        return setHover(index, hover);
    }

    public ChatComponent setHover(String... hover) {
        return setHover(index, hover);
    }

    public ChatComponent setHoverToAll(ChatHover hover) {
        if (ChatEvent.isEmpty(hover))
            return this;

        for (Component c : map.values())
            c.setHover(hover);

        return this;
    }

    public ChatComponent setHoverToAll(List<String> hover) {
        return setHoverToAll(new ChatHover(lib, hover));
    }

    public ChatComponent setHoverToAll(String... hover) {
        return setHoverToAll(ArrayUtils.toList(hover));
    }

    public ChatComponent setClick(int index, ChatClick click) {
        if (index < 0 || ChatEvent.isEmpty(click))
            return this;

        map.put(index, map.get(index).setClick(click));
        return this;
    }

    public ChatComponent setClick(int index, String string) {
        return setClick(index, new ChatClick(lib, string));
    }

    public ChatComponent setClick(ChatClick click) {
        return setClick(index, click);
    }

    public ChatComponent setClick(String string) {
        return setClick(index, string);
    }

    public ChatComponent setClickToAll(ChatClick click) {
        if (ChatEvent.isEmpty(click))
            return this;

        for (Component c : map.values())
            c.setClick(click);

        return this;
    }

    public ChatComponent setClickToAll(String string) {
        return setClickToAll(new ChatClick(lib, string));
    }

    public <T> ChatComponent append(T object) {
        if (object instanceof String) {
            updateMessageMapping((String) object);
            return this;
        }

        String initial = object.toString();

        try {
            Class<?> clazz = object.getClass();

            initial = (String) String.class
                    .getMethod("valueOf", clazz)
                    .invoke(null, object);
        } catch (Exception ignored) {}

        updateMessageMapping(initial);
        return this;
    }

    public BaseComponent[] compile(Player parser) {
        if (index < 0)
            throw new IllegalStateException("Builder is empty");

        BaseComponent[] components = new BaseComponent[map.size()];
        int count = 0;

        for (Component message : map.values())
            components[count++] = message.compile(parser);

        return components;
    }

    public ChatComponent copy() {
        return new ChatComponent(this);
    }

    @Override
    public String toString() {
        return "ChatComponent{map=" + map + '}';
    }

    public String toPatternString() {
        if (index == -1) return "";

        final StringBuilder builder = new StringBuilder();
        String split = lib.getLineSeparator();

        for (Component message : map.values()) {
            FormattingHandler handler = message.handler;

            if (handler.isEmpty()) {
                builder.append(message.string);
                continue;
            }

            ChatClick click = handler.click;
            ChatHover hover = handler.hover;

            boolean hasClick = !ChatEvent.isEmpty(click);
            boolean hasHover = !ChatEvent.isEmpty(hover);

            if (hasHover || hasClick) {
                if (hasHover) {
                    StringJoiner joiner = new StringJoiner(split);
                    hover.list.forEach(joiner::add);

                    String temp = (joiner + "")
                            .replaceAll("\\\\[QE]", "");

                    builder.append("<hover:\"")
                            .append(temp).append('"');
                }

                if (hasClick) {
                    builder.append(hasHover ? '|' : '<');

                    builder.append(click.action)
                            .append(":\"")
                            .append(click.string).append("\">");
                }
                else builder.append('>');
            }

            builder.append(message.string).append("</text>");
        }

        return builder.toString();
    }

    @NotNull
    public static ChatComponent create(TakionLib lib, @Nullable String message) {
        return new ChatComponent(lib, message);
    }

    @NotNull
    public static BaseComponent[] fromText(TakionLib lib, Player parser, String message) {
        return create(lib, message).compile(parser);
    }

    @NotNull
    public static BaseComponent[] fromText(TakionLib lib, String message) {
        return fromText(lib, null, message);
    }

    @NotNull
    public static BaseComponent[] fromText(Player parser, String message) {
        return create(TakionLib.getLib(), message).compile(parser);
    }

    @NotNull
    public static BaseComponent[] fromText(String message) {
        return fromText(TakionLib.getLib(), null, message);
    }

    private static class FormattingHandler {

        private ChatClick click = null;
        private ChatHover hover = null;

        private boolean isEmpty() {
            return ChatEvent.isEmpty(click) && ChatEvent.isEmpty(hover);
        }

        @Override
        public String toString() {
            return "{" + "click=" + click + ", hover=" + hover + '}';
        }
    }

    private class Component {

        private final FormattingHandler handler;
        private final String string;

        private Component(String message) {
            this.handler = new FormattingHandler();

            final int lastIndex = index - 1;
            String last = "";

            if (lastIndex >= 0 && !PrismaticAPI.startsWithColor(message)) {
                String temp = map.get(lastIndex).string;
                String color = PrismaticAPI.getLastColor(temp);

                last = StringUtils.isBlank(color) ? "" : color;
            }

            this.string = last + message;
        }

        Component setClick(ChatClick click) {
            handler.click = click;
            return this;
        }

        Component setHover(ChatHover hover) {
            handler.hover = hover;
            return this;
        }

        Component setHandler(String click, String hover) {
            if (click != null)
                try {
                    setClick(new ChatClick(lib, click));
                } catch (Exception ignored) {}

            if (hover != null) {
                String h = hover.split(":\"", 2)[1];
                h = h.substring(0, h.length() - 1);

                String[] array = lib.splitString(h);
                setHover(new ChatHover(lib, array));
            }

            return this;
        }

        private BaseComponent compile(Player parser) {
            Matcher urlMatch = TextUtils.URL_PATTERN.matcher(string);
            if (parseURLs && urlMatch.find())
                setClick(new ChatClick(lib, ChatClick.Action.OPEN_URL, string));

            BaseComponent[] array = TextComponent.fromLegacyText(string);
            TextComponent comp = new TextComponent(array);

            final ChatClick c = handler.click;
            final ChatHover h = handler.hover;

            if (!ChatEvent.isEmpty(c))
                comp.setClickEvent(c.createEvent(parser));

            if (!ChatEvent.isEmpty(h))
                comp.setHoverEvent(h.createEvent(parser));

            return comp;
        }
    }
}
