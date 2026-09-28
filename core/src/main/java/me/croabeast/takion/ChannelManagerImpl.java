package me.croabeast.takion;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.croabeast.common.Regex;
import me.croabeast.common.discord.Webhook;
import me.croabeast.prismatic.element.Element;
import me.croabeast.prismatic.element.RenderContext;
import me.croabeast.takion.bossbar.AnimatedBossbar;
import me.croabeast.takion.channel.Channel;
import me.croabeast.takion.channel.ChannelManager;
import me.croabeast.takion.format.PlainFormat;
import me.croabeast.takion.message.MessageUtils;
import me.croabeast.takion.message.TitleManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ChannelManagerImpl implements ChannelManager {

    private final Map<String, Channel> channels = new LinkedHashMap<>();

    @Getter @Setter
    private String startDelimiter = Pattern.quote("["), endDelimiter = Pattern.quote("]");

    ChannelManagerImpl(TakionLib lib) {
        channels.put("action_bar", new ChannelImpl("action_bar") {
            @Override
            public String formatString(Player target, Player parser, String string) {
                return lib.colorize(target, parser, string);
            }

            @Override
            public boolean send(Collection<? extends Player> targets, Player parser, String message) {
                if (StringUtils.isBlank(message)) return false;

                if (targets == null || targets.isEmpty())
                    return false;

                Matcher matcher = matcher(message);
                if (matcher.find())
                    message = message.replace(matcher.group(), "");

                Element element = Element.parse(
                        PlainFormat.PLACEHOLDER_API.accept(parser, message), lib.getMarkup());
                boolean atLeastOneIsSent = false;

                for (final Player p : targets) {
                    final Player ps = parser == null ? p : parser;
                    String s = element.legacy(RenderContext.of(p, token -> lib.resolvePlaceholder(ps, token)));

                    if (MessageUtils.sendActionBar(p, s) && !atLeastOneIsSent)
                        atLeastOneIsSent = true;
                }

                return atLeastOneIsSent;
            }
        });

        channels.put("chat", new ChannelImpl("chat") {
            @Override
            public String formatString(Player target, Player parser, String string) {
                return lib.colorize(target, parser, PlainFormat.INTERACTIVE_CHAT.accept(parser, string));
            }

            @Override
            public boolean send(Collection<? extends Player> targets, Player parser, String message) {
                if (targets == null || targets.isEmpty())
                    return false;

                if (StringUtils.isBlank(message)) {
                    targets.forEach(p -> p.sendMessage(message));
                    return false;
                }

                // The prefix has to go first: aligning before stripping threw the aligned text away
                // when a prefix was present, and the center marker is anchored to the start, so it
                // never matched behind one either.
                Matcher matcher = matcher(message);
                String temp = matcher.find() ? message.replace(matcher.group(), "") : message;

                temp = lib.getCharacterManager().align(temp);

                // Both depend on the sender, not on the receiver, so they belong to the message.
                temp = PlainFormat.INTERACTIVE_CHAT.accept(parser, temp);
                temp = PlainFormat.PLACEHOLDER_API.accept(parser, temp);

                // Parsed once from the raw text instead of once per receiver from the colorized
                // text, and plain shares the parse with interactive: gating it on markup left the
                // common case rescanning the whole message once per registered placeholder, per
                // receiver, and made a bare URL clickable only when the message carried a tag.
                Element element = Element.parse(temp, lib.getMarkup());
                boolean interactive = element.hasEvents();
                boolean atLeastOneIsSent = false;

                for (final Player p : targets) {
                    if (p == null) continue;

                    final Player ps = parser == null ? p : parser;

                    // No custom formatter: colorizing is the plain Prismatic pipeline now, which is
                    // what lets a message without placeholders reuse its cached render across every
                    // receiver sharing a color profile.
                    RenderContext context = RenderContext.of(p, token -> lib.resolvePlaceholder(ps, token));

                    try {
                        if (interactive)
                            p.spigot().sendMessage(element.bungee(context));
                        else
                            p.sendMessage(element.legacy(context));
                    } catch (Exception e) {
                        lib.getLogger().log("Could not build the message: " + e.getMessage());
                        continue;
                    }

                    atLeastOneIsSent = true;
                }

                return atLeastOneIsSent;
            }
        });

        channels.put("title", new ChannelImpl("title", "(:\\d+)?") {
            @Override
            public String formatString(Player target, Player parser, String string) {
                return lib.colorize(target, parser, string);
            }

            @Override
            public boolean send(Collection<? extends Player> targets, Player parser, String message) {
                if (StringUtils.isBlank(message)) return false;

                if (targets == null || targets.isEmpty())
                    return false;

                Matcher matcher = matcher(message);
                String tempTime = null;

                boolean modify = matcher.find();

                try {
                    if (modify) tempTime = matcher.group(1).substring(1);
                } catch (Exception ignored) {}

                if (modify)
                    message = message.replace(matcher.group(), "");

                int time = lib.getTitleManager().getStayTicks();
                try {
                    if (tempTime != null)
                        time = Integer.parseInt(tempTime) * 20;
                } catch (NumberFormatException ignored) {}

                Element element = Element.parse(
                        PlainFormat.PLACEHOLDER_API.accept(parser, message), lib.getMarkup());
                boolean atLeastOneIsSent = false;

                for (final Player p : targets) {
                    final Player ps = parser == null ? p : parser;
                    TitleManager.Builder b = lib.getTitleManager()
                            .builder(element.legacy(RenderContext.of(p, token -> lib.resolvePlaceholder(ps, token))))
                            .setStay(time);

                    if (b.send(p) && !atLeastOneIsSent) atLeastOneIsSent = true;
                }

                return atLeastOneIsSent;
            }
        });

        channels.put("bossbar", new ChannelImpl("bossbar", "(:.+)?") {
            @Override
            public String formatString(Player target, Player parser, String string) {
                return string;
            }

            @Override
            public boolean send(Collection<? extends Player> targets, Player parser, String message) {
                if (StringUtils.isBlank(message)) return false;

                if (targets == null || targets.isEmpty())
                    return false;

                AnimatedBossbar bossbar;

                final Matcher matcher = matcher(message);
                if (matcher.find()) {
                    String arguments = matcher.group(1).substring(1);

                    String[] array = arguments.split(":");
                    message = message.replace(matcher.group(), "");

                    TreeMap<String, ConfigurationSection> bossbars = lib.getLoadedBossbars();
                    ConfigurationSection c = null;
                    try {
                        c = bossbars.get(bossbars.firstKey());
                    } catch (java.util.NoSuchElementException ignored) {}

                    if (c == null && !(array.length == 1 && (c = bossbars.get(array[0])) == null))
                    {
                        bossbar = new AnimatedBossbar(lib.getPlugin(), message);

                        for (String arg : array) {
                            try {
                                bossbar.setDuration(Double.parseDouble(arg));
                                continue;
                            } catch (NumberFormatException ignored) {}

                            try {
                                bossbar.setColors(BarColor.valueOf(arg));
                                continue;
                            } catch (IllegalArgumentException ignored) {}

                            try {
                                bossbar.setStyles(BarStyle.valueOf(arg));
                            } catch (IllegalArgumentException ignored) {}
                        }
                    }
                    else bossbar = new AnimatedBossbar(lib.getPlugin(), c);
                }
                else bossbar = new AnimatedBossbar(lib.getPlugin(), message);

                try {
                    bossbar.addViewers(targets).startAnimation();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });

        channels.put("json", new ChannelImpl("json") {
            @Override
            public String formatString(Player target, Player parser, String string) {
                return lib.colorize(target, parser, string);
            }

            @Override
            public boolean send(Collection<? extends Player> targets, Player parser, String message) {
                if (StringUtils.isBlank(message)) return false;

                if (targets == null || targets.isEmpty())
                    return false;

                Matcher matcher = matcher(message);
                if (matcher.find())
                    message = message.replace(matcher.group(), "");

                boolean atLeastOneIsSent = false;

                for (final Player p : targets) {
                    if (p == null) continue;

                    final Player ps = parser == null ? p : parser;
                    String s = formatString(p, ps, message);

                    BaseComponent[] components;
                    try {
                        components = ComponentSerializer.parse(s);
                    } catch (Exception e) {
                        continue;
                    }

                    p.spigot().sendMessage(components);
                    if (!atLeastOneIsSent) atLeastOneIsSent = true;
                }

                return atLeastOneIsSent;
            }
        });

        channels.put("webhook", new ChannelImpl("webhook", "(:.+)?") {
            @Override
            public String formatString(Player target, Player parser, String string) {
                return lib.replace(parser, string);
            }

            @Override
            public boolean send(Collection<? extends Player> targets, Player parser, String message) {
                String path = lib.getLoadedWebhooks().firstKey();

                Matcher matcher = matcher(message);
                if (matcher.find()) {
                    message = message.replace(matcher.group(), "");

                    String[] array = matcher
                            .group()
                            .replace(getStartDelimiter(), "")
                            .replace(getEndDelimiter(), "")
                            .split(":", 2);

                    String s = array.length == 2 ? array[1] : null;
                    if (s != null) path = s;
                }

                message = formatString(parser, parser, message);
                if (path == null) return false;

                ConfigurationSection id = lib.getLoadedWebhooks().get(path);
                return id != null && Webhook.fromConfig(id).send(message);
            }
        });
    }

    @NotNull
    public Channel identify(@NotNull String string) {
        Channel chat = channels.get("chat");
        if (StringUtils.isBlank(string)) return chat;

        Channel before = channels.get(string);
        if (before != null) return before;

        for (Channel channel : channels.values()) {
            Matcher matcher = channel.matcher(string);
            if (matcher.find()) return channel;
        }

        return chat;
    }

    @Getter @Setter
    private abstract class ChannelImpl implements Channel {

        private final List<String> prefixes = new ArrayList<>();

        private final String name;
        @Regex private String pattern;

        boolean caseSensitive = true;

        @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
        private Pattern compiled;

        ChannelImpl(String prefix, @Regex String pattern) {
            prefixes.add(prefix);
            this.name = prefix;
            this.pattern = pattern;
        }

        ChannelImpl(String prefix) {
            this(prefix, null);
        }

        @Override
        public Flag getFlag() {
            return Flag.valueOf(name.toUpperCase(Locale.ENGLISH));
        }

        @NotNull
        public Matcher matcher(String string) {
            @Regex String regex = StringUtils.isBlank(pattern) ? "" : pattern,
                    prefixes = '(' + String.join("|", this.prefixes) + ')';

            String sensitive = caseSensitive ? "(?i)" : "";
            String start = getStartDelimiter(), end = getEndDelimiter();

            // Prefixes, delimiters and flags can change at any time, so the regex text itself is the
            // cache key; the pattern is only recompiled when that text differs.
            String full = sensitive + start + prefixes + regex + end;
            Pattern pattern = compiled;
            if (pattern == null || !pattern.pattern().equals(full))
                compiled = pattern = Pattern.compile(full);

            return pattern.matcher(string);
        }
    }
}
