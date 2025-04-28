package me.croabeast.takion.chat;

import lombok.Getter;
import me.croabeast.common.applier.StringApplier;
import me.croabeast.common.util.ArrayUtils;
import me.croabeast.prismatic.PrismaticAPI;
import me.croabeast.takion.TakionLib;
import me.croabeast.takion.format.Format;
import me.croabeast.takion.format.StringFormat;
import net.md_5.bungee.api.chat.BaseComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
class MultiCompImpl implements MultiComponent {

    private final TakionLib lib;
    private final String message;

    private final LinkedList<Segment> list = new LinkedList<>();

    private class Segment {

        final ChatComponent<?> component;
        String lastColor;

        Segment(ChatComponent<?> component) {
            this.component = component;

            String message = this.component.getMessage();
            lastColor = PrismaticAPI.getEndColor(message);

            final Segment segment;
            try {
                segment = list.getLast();
            } catch (Exception e) {
                return;
            }

            if (PrismaticAPI.startsWithColor(message) ||
                    segment.lastColor == null)
                return;

            message = segment.lastColor + message;
            this.component.setMessage(message);

            message = this.component.getMessage();
            lastColor = PrismaticAPI.getEndColor(message);
        }

        Segment(String message) {
            this(format.accept(message));
        }

        BaseComponent[] compile(Player player) {
            return component.compile(player);
        }
    }

    private static String stripLegacyFormat(String string) {
        if (StringUtils.isBlank(string)) return string;

        String p = "(?i)(hover|run|suggest|url)=\\[(.[^|\\[\\]]*)]";
        Matcher old = Pattern.compile(p).matcher(string);

        while (old.find()) {
            String temp = old.group(1) + ":\"" + old.group(2) + "\"";
            string = string.replace(old.group(), temp);
        }
        return string;
    }

    private Format<ChatComponent<?>> format = new Format<ChatComponent<?>>() {
        @Getter
        final String regex = DEFAULT_REGEX;

        @NotNull
        public ComponentImpl accept(Player player, String string) {
            ComponentImpl component = new ComponentImpl(lib, string);

            Matcher matcher = format.matcher(string);
            if (matcher.find()) {
                String[] args = matcher.group(1).split("[|]", 2);

                String h = null, c = null;
                for (String s : args) {
                    Matcher m = Pattern.compile("(?i)hover").matcher(s);
                    if (m.find()) h = s; else c = s;
                }

                component = new ComponentImpl(lib, matcher.group(20));
                if (c != null) component.setClick(c);
                if (h != null) component.setHover(h);
            }

            return component;
        }

        @Override
        public String removeFormat(String string) {
            string = stripLegacyFormat(string);
            Matcher matcher = format.matcher(string);
            while (matcher.find())
                string = string.replace(matcher.group(), matcher.group(20));
            return string;
        }
    };

    private String formatMessage(String string) {
        return StringApplier.simplified(string)
                .apply(MultiCompImpl::stripLegacyFormat)
                .apply(s -> {
                    StringFormat format = lib.getFormatManager().get("SMALL_CAPS");
                    return format.accept(s);
                })
                .apply(lib.getCharacterManager()::align)
                .toString();
    }

    private void splitByUrl(String text) {
        Matcher urlM = ChatComponent.URL_PATTERN.matcher(text);
        int end = 0;

        while (urlM.find()) {
            String before = text.substring(end, urlM.start());
            if (!before.isEmpty()) list.add(new Segment(before));


            String url = urlM.group();
            ChatComponent<?> clickComp = ChatComponent.fromString(lib, url);
            list.add(new Segment(clickComp.setClick(Click.OPEN_URL, url)));
            end = urlM.end();
        }

        String tail = text.substring(end);
        if (!tail.isEmpty()) list.add(new Segment(tail));
    }

    private void splitToSegments(String line) {
        Matcher tagM = format.matcher(line);
        int lastEnd = 0;

        while (tagM.find()) {
            String between = line.substring(lastEnd, tagM.start());
            splitByUrl(between);

            list.add(new Segment(tagM.group()));
            lastEnd = tagM.end();
        }

        splitByUrl(line.substring(lastEnd));
    }

    MultiCompImpl(TakionLib lib, @NotNull String message) {
        this.lib = lib;
        this.message = message;
        splitToSegments(formatMessage(message));
    }

    private MultiCompImpl(MultiCompImpl component) {
        this.lib = component.lib;
        this.message = component.message;
        this.format = component.format;

        List<Segment> list = component.list;
        if (!list.isEmpty())
            this.list.addAll(list);
    }

    @NotNull
    public MultiComponent setFormat(@NotNull Format<ChatComponent<?>> format) {
        this.format = format;
        return instance();
    }

    @NotNull
    public MultiComponent copy() {
        return new MultiCompImpl(this);
    }

    @NotNull
    public MultiComponent append(String message) {
        splitToSegments(formatMessage(message));
        return instance();
    }

    @NotNull
    public MultiComponent append(@NotNull ChatComponent<?> component) {
        list.add(new Segment(component));
        return instance();
    }

    @NotNull
    public MultiComponent setClickToAll(Click click, String input) {
        list.forEach(s -> s.component.setClick(click, input));
        return instance();
    }

    @NotNull
    public MultiComponent setHoverToAll(List<String> list) {
        this.list.forEach(s -> s.component.setHover(list));
        return instance();
    }

    @NotNull
    public MultiComponent setHoverToAll(String string) {
        Pattern pattern = Pattern.compile("hover:\"(.*?)\"");
        Matcher matcher = pattern.matcher(string);
        while (matcher.find())
            string = string.replace(matcher.group(), matcher.group(1));
        return setHoverToAll(lib.splitString(string));
    }

    @NotNull
    public MultiComponent setClick(Click click, String input) {
        int lastIndex = list.size() - 1;
        if (lastIndex >= 0)
            list.get(lastIndex).component.setClick(click, input);
        return instance();
    }

    @NotNull
    public MultiComponent setHover(List<String> list) {
        int lastIndex = this.list.size() - 1;
        if (lastIndex >= 0)
            this.list.get(lastIndex).component.setHover(list);
        return instance();
    }

    @NotNull
    public MultiComponent setHover(String string) {
        Pattern pattern = Pattern.compile("hover:\"(.*?)\"");
        Matcher matcher = pattern.matcher(string);
        while (matcher.find())
            string = string.replace(matcher.group(), matcher.group(1));
        return setHover(lib.splitString(string));
    }

    @NotNull
    public BaseComponent[] compile(Player player) {
        List<BaseComponent> components = new ArrayList<>();
        for (Segment segment : list)
            components.addAll(ArrayUtils.toList(segment.compile(player)));
        return components.toArray(new BaseComponent[0]);
    }

    @NotNull
    public MultiComponent instance() {
        return this;
    }
}
