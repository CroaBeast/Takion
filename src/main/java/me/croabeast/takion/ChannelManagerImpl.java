package me.croabeast.takion;

import lombok.Getter;
import lombok.Setter;
import me.croabeast.lib.Regex;
import me.croabeast.lib.discord.Webhook;
import me.croabeast.lib.util.TextUtils;
import me.croabeast.takion.channel.Channel;
import me.croabeast.takion.channel.ChannelManager;
import me.croabeast.takion.message.AnimatedBossbar;
import me.croabeast.takion.message.MessageSender;
import me.croabeast.takion.message.MessageUtils;
import me.croabeast.takion.message.TitleManager;
import me.croabeast.takion.message.chat.ChatComponent;
import me.croabeast.takion.misc.StringAligner;
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

                boolean atLeastOneIsSent = false;

                for (final Player p : targets) {
                    final Player ps = parser == null ? p : parser;
                    String s = formatString(p, ps, message);

                    if (MessageUtils.sendActionBar(p, s) && !atLeastOneIsSent)
                        atLeastOneIsSent = true;
                }

                return atLeastOneIsSent;
            }
        });

        channels.put("chat", new ChannelImpl("chat") {
            @Override
            public String formatString(Player target, Player parser, String string) {
                return lib.colorize(target, parser, TextUtils.PARSE_INTERACTIVE_CHAT.apply(parser, string));
            }

            @Override
            public boolean send(Collection<? extends Player> targets, Player parser, String message) {
                if (targets == null || targets.isEmpty())
                    return false;

                if (StringUtils.isBlank(message)) {
                    targets.forEach(p -> p.sendMessage(message));
                    return false;
                }

                String temp = StringAligner.align(message);
                Matcher matcher = matcher(message);
                if (matcher.find())
                    temp = message.replace(matcher.group(), "");

                boolean atLeastOneIsSent = false;

                for (final Player p : targets) {
                    if (p == null) continue;

                    Player ps = parser == null ? p : parser;
                    String s = formatString(p, ps, temp);

                    if (!TextUtils.IS_JSON.test(temp)) {
                        p.sendMessage(s);
                        if (!atLeastOneIsSent)
                            atLeastOneIsSent = true;
                        continue;
                    }

                    BaseComponent[] components;
                    try {
                        components = ChatComponent.fromText(ps, s);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }

                    p.spigot().sendMessage(components);
                    if (!atLeastOneIsSent)
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
                } catch (Exception ignored) {}

                boolean atLeastOneIsSent = false;

                for (final Player p : targets) {
                    TitleManager.Builder b = lib.getTitleManager()
                            .builder(formatString(
                                    p,
                                    parser == null ? p : parser,
                                    message
                            ))
                            .setStay(time);

                    if (b.send(p) && !atLeastOneIsSent)
                        atLeastOneIsSent = true;
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
                    } catch (Exception ignored) {}

                    if (c == null && !(array.length == 1 && (c = bossbars.get(array[0])) == null))
                    {
                        bossbar = new AnimatedBossbar(lib.getPlugin(), message);

                        for (String arg : array) {
                            try {
                                bossbar.setDuration(Double.parseDouble(arg));
                                continue;
                            } catch (Exception ignored) {}

                            try {
                                bossbar.setColors(BarColor.valueOf(arg));
                                continue;
                            } catch (Exception ignored) {}

                            try {
                                bossbar.setStyles(BarStyle.valueOf(arg));
                            } catch (Exception ignored) {}
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
                    if (!atLeastOneIsSent)
                        atLeastOneIsSent = true;
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
                return id != null && new Webhook(id, message).send();
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

        ChannelImpl(String prefix, @Regex String pattern) {
            prefixes.add(prefix);
            this.name = prefix;
            this.pattern = pattern;
        }

        ChannelImpl(String prefix) {
            this(prefix, null);
        }

        @Override
        public void setPrefixes(@NotNull Collection<String> prefixes) {
            this.prefixes.clear();
            this.prefixes.addAll(prefixes);
        }

        @Override
        public void addPrefix(@NotNull String prefix) {
            this.prefixes.add(prefix);
        }

        @Override
        public void removePrefix(@NotNull String prefix) {
            this.prefixes.remove(prefix);
        }

        @Override
        public MessageSender.Flag getFlag() {
            return MessageSender.Flag.valueOf(name.toUpperCase(Locale.ENGLISH));
        }

        @NotNull
        public Matcher matcher(String string) {
            @Regex String regex = StringUtils.isBlank(pattern) ? "" : pattern,
                    prefixes = '(' + String.join("|", this.prefixes) + ')';

            String sensitive = caseSensitive ? "(?i)" : "";
            String start = getStartDelimiter(), end = getEndDelimiter();

            Pattern pattern = Pattern.compile(sensitive + start + prefixes + regex + end);
            return pattern.matcher(string);
        }
    }
}
