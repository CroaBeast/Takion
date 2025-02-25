package me.croabeast.takion;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.croabeast.takion.channel.Channel;
import me.croabeast.takion.channel.ChannelManager;
import me.croabeast.takion.character.CharacterInfo;
import me.croabeast.takion.character.CharacterManager;
import me.croabeast.takion.character.DefaultCharacter;
import me.croabeast.takion.character.SmallCaps;
import me.croabeast.takion.logger.TakionLogger;
import me.croabeast.takion.message.AnimatedBossbar;
import me.croabeast.takion.message.MessageSender;
import me.croabeast.takion.message.MessageUtils;
import me.croabeast.takion.message.TitleManager;
import me.croabeast.takion.message.chat.ChatComponent;
import me.croabeast.takion.misc.PatternAction;
import me.croabeast.takion.misc.StringAligner;
import me.croabeast.takion.placeholder.Placeholder;
import me.croabeast.takion.placeholder.PlaceholderManager;
import me.croabeast.lib.PlayerFormatter;
import me.croabeast.lib.Regex;
import me.croabeast.lib.Rounder;
import me.croabeast.lib.applier.StringApplier;
import me.croabeast.lib.discord.Webhook;
import me.croabeast.lib.util.ArrayUtils;
import me.croabeast.lib.util.TextUtils;
import me.croabeast.prismatic.PrismaticAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class TakionLib {

    private static final TakionLib NO_PLUGIN_INSTANCE = new TakionLib(null);
    private static final Map<Plugin, TakionLib> LIB_MAP = new HashMap<>();

    @Getter(AccessLevel.NONE)
    private final Plugin plugin;
    private TakionLogger logger;

    private final ChannelManager channelManager;
    private final TitleManager titleManager;

    private final PlaceholderManager placeholderManager;
    private final CharacterManager characterManager;

    private String langPrefix;
    private String langPrefixKey = "<P>";

    @Regex
    private String lineSeparator = Pattern.quote("<n>");
    private String centerPrefix = "[C]";

    private final PatternAction<Boolean> blankSpacesAction;
    private final PatternAction<String> characterAction, smallCapsAction;

    @Getter(AccessLevel.NONE)
    private MessageSender loadedSender = new MessageSender();

    public TakionLib(Plugin plugin) {
        this.plugin = plugin;
        this.logger = new TakionLogger(this);

        titleManager = new TitleManager() {
            @Setter @Getter
            private int fadeInTicks = 8, stayTicks = 50, fadeOutTicks = 8;
        };

        channelManager = new ChannelManager() {

            private final Map<String, Channel> channels = new HashMap<>();
            private final Map<String, Channel> defaults = new HashMap<>();

            @Getter @Setter
            private String startDelimiter = "[", endDelimiter = "]";

            {
                channels.put("action_bar", new ChannelImpl("action_bar") {
                    @Override
                    public String formatString(Player target, Player parser, String string) {
                        return colorize(target, parser, string);
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
                        return colorize(target, parser, TextUtils.PARSE_INTERACTIVE_CHAT.apply(parser, string));
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
                        return colorize(target, parser, string);
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

                        int time = titleManager.getStayTicks();
                        try {
                            if (tempTime != null)
                                time = Integer.parseInt(tempTime) * 20;
                        } catch (Exception ignored) {}

                        boolean atLeastOneIsSent = false;

                        for (final Player p : targets) {
                            TitleManager.Builder b = titleManager
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

                            TreeMap<String, ConfigurationSection> bossbars = getLoadedBossbars();
                            ConfigurationSection c = null;
                            try {
                                c = bossbars.get(bossbars.firstKey());
                            } catch (Exception ignored) {}

                            if (c == null && !(array.length == 1 && (c = bossbars.get(array[0])) == null))
                            {
                                bossbar = new AnimatedBossbar(plugin, message);

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
                            else bossbar = new AnimatedBossbar(plugin, c);
                        }
                        else bossbar = new AnimatedBossbar(plugin, message);

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
                        return colorize(target, parser, string);
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
                        return replace(parser, string);
                    }

                    @Override
                    public boolean send(Collection<? extends Player> targets, Player parser, String message) {
                        String path = getLoadedWebhooks().firstKey();

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

                        ConfigurationSection id = getLoadedWebhooks().get(path);
                        return id != null && new Webhook(id, message).send();
                    }
                });

                defaults.putAll(channels);
            }

            @Override
            public void setDefaults() {
                channels.clear();
                channels.putAll(defaults);
            }

            @NotNull
            public Channel identify(@NotNull String string) {
                Channel chat = channels.get("chat");
                if (StringUtils.isBlank(string)) return chat;

                Channel before;
                if ((before = channels.get(string)) != null)
                    return before;

                for (Channel channel : defaults.values()) {
                    Matcher matcher = channel.matcher(string);
                    if (matcher.find()) return channel;
                }

                return chat;
            }
        };

        placeholderManager = new PlaceholderManager() {

            private final Set<Placeholder<?>> placeholders = new HashSet<>();
            private final Set<Placeholder<?>> defaults = new HashSet<>();

            private <N extends Number> Function<Player, N> fromLoc(Function<Location, N> function) {
                return p -> Rounder.round(function.apply(p.getLocation()));
            }

            {
                load0("{player}", HumanEntity::getName);
                load0("{playerDisplayName}", Player::getDisplayName);

                load0("{playerUUID}", Entity::getUniqueId);
                load0("{playerWorld}", p -> p.getWorld().getName());
                load0("{playerGameMode}", HumanEntity::getGameMode);

                load0("{playerX}", fromLoc(Location::getX));
                load0("{playerY}", fromLoc(Location::getY));
                load0("{playerZ}", fromLoc(Location::getZ));

                load0("{playerYaw}", fromLoc(Location::getYaw));
                load0("{playerPitch}", fromLoc(Location::getPitch));
            }

            private <T> boolean load0(Placeholder<T> placeholder, boolean def) {
                if (def)
                    defaults.add(new Placeholder<>(placeholder));

                return placeholders.add(placeholder);
            }

            private <T> void load0(String key, Function<Player, T> function) {
                load0(new Placeholder<>(key, function), true);
            }

            private Placeholder<?> remove0(String key) {
                Placeholder<?> result = null;

                for (Placeholder<?> replacer : placeholders) {
                    if (!replacer.getKey().equals(key))
                        continue;

                    placeholders.remove(result = replacer);
                    break;
                }

                return result;
            }

            @Override
            public boolean remove(String key) {
                return remove0(key) != null;
            }

            @Override
            public <T> boolean load(Placeholder<T> placeholder) {
                return load0(placeholder, false);
            }

            @Override
            public boolean edit(String oldKey, String newKey) {
                Placeholder<?> placeholder = remove0(oldKey);
                return placeholder != null &&
                        load0(new Placeholder<>(newKey, placeholder.getFunction()), false);
            }

            @Override
            public String replace(Player player, String string, boolean sensitive) {
                if (player == null || StringUtils.isBlank(string))
                    return string;

                StringApplier applier = StringApplier.simplified(string);
                for (Placeholder<?> placeholder : placeholders)
                    applier.apply(s -> placeholder.replace(player, s));

                return applier.toString();
            }

            public void setDefaults() {
                placeholders.clear();
                placeholders.addAll(defaults);
            }
        };

        characterManager = new CharacterManager() {

            private final Map<Character, CharacterInfo> map = new LinkedHashMap<>();

            {
                for (DefaultCharacter character : DefaultCharacter.values()) {
                    char c = character.getCharacter();
                    map.put(c, new CharacterInfo(c, character.getLength()));
                }

                for (SmallCaps caps : SmallCaps.values()) {
                    char c = caps.getCharacter();
                    map.put(c, new CharacterInfo(c, caps.getLength()));
                }
            }

            @Override
            public CharacterInfo getInfo(char c) {
                CharacterInfo info = map.getOrDefault(c, null);
                return info == null ? DEFAULT : info;
            }

            @Override
            public Character toCharacter(String string) {
                if (StringUtils.isBlank(string)) return null;

                char[] array = string.toCharArray();
                return array.length != 1 ? null : array[0];
            }

            @Override
            public void addCharacter(char c, int length) {
                map.put(c, new CharacterInfo(c, length));
            }

            @Override
            public void removeCharacters(char... chars) {
                if (!ArrayUtils.isArrayEmpty(chars)) for (char c : chars) map.remove(c);
            }
        };

        langPrefix = "&e " + (plugin != null ? plugin.getName() : "Plugin") + " &8»&7";

        blankSpacesAction = new PatternAction<Boolean>("(?i)<add_space:(\\d+)>") {
            @Override
            public @NotNull Boolean act(Collection<? extends Player> targets, String string) {
                if (targets == null || targets.isEmpty())
                    return false;

                if (StringUtils.isBlank(string))
                    return false;

                Matcher matcher = createMatcher(string);
                if (!matcher.find()) return false;

                int count = 0;
                try {
                    count = Integer.parseInt(matcher.group(1));
                } catch (Exception ignored) {
                }

                if (count <= 0) return false;

                boolean atLeastOneIsSent = false;
                for (Player player : targets) {
                    if (player == null) continue;

                    for (int i = 0; i < count; i++)
                        player.sendMessage("");
                    if (!atLeastOneIsSent)
                        atLeastOneIsSent = true;
                }

                return atLeastOneIsSent;
            }
        };

        characterAction = new StringAction("<[Uu]:([a-fA-F\\d]{4})>") {
            @NotNull
            public String act(String string) {
                if (StringUtils.isBlank(string)) return string;

                Matcher m = createMatcher(string);
                while (m.find()) {
                    char c = (char) Integer.parseInt(m.group(1), 16);
                    string = string.replace(m.group(), c + "");
                }

                return string;
            }
        };

        @Regex String s = "(small_caps|sc)";
        smallCapsAction = new StringAction("(?i)<" + s + ">(.+?)</" + s + ">") {

            @NotNull
            public String act(String string) {
                if (StringUtils.isBlank(string))
                    return string;

                Matcher matcher = createMatcher(string);
                while (matcher.find()) {
                    String text = matcher.group(2);

                    string = string.replace(
                            matcher.group(),
                            SmallCaps.toSmallCaps(text)
                    );
                }

                return string;
            }
        };

        LIB_MAP.put(this.plugin, this);
    }

    static Plugin getProvidingPlugin() {
        try {
            return JavaPlugin.getProvidingPlugin(TakionLib.class);
        } catch (Exception e) {
            return null;
        }
    }

    public TakionLib() {
        this(getProvidingPlugin());
    }

    private static abstract class StringAction extends PatternAction<String> {

        StringAction(@Regex String pattern) {
            super(pattern);
        }

        @Override
        public @NotNull String act(Collection<? extends Player> players, String string) {
            return act(string);
        }

        @Override
        public @NotNull String act(Player player, String string) {
            return act(string);
        }

        @NotNull
        public abstract String act(String string);
    }

    @Getter @Setter
    private abstract class ChannelImpl implements Channel {

        private final String name;
        private String prefix;
        @Regex
        private String pattern;

        ChannelImpl(String prefix, @Regex String pattern) {
            this.name = this.prefix = prefix;
            this.pattern = pattern;
        }

        ChannelImpl(String prefix) {
            this(prefix, null);
        }

        @Override
        public MessageSender.Flag getFlag() {
            return MessageSender.Flag.valueOf(name.toUpperCase(Locale.ENGLISH));
        }

        @NotNull
        public Matcher matcher(String string) {
            @Regex String regex = StringUtils.isBlank(pattern) ? "" : pattern;

            String start = channelManager.getStartDelimiter();
            String end = channelManager.getEndDelimiter();

            Pattern pattern = Pattern.compile(start + prefix + regex + end);
            return pattern.matcher(start);
        }
    }

    public final Plugin getPlugin() {
        return Objects.requireNonNull(plugin);
    }

    public final MessageSender getLoadedSender() {
        return loadedSender.copy();
    }

    @NotNull
    protected TreeMap<String, ConfigurationSection> loadMapFromConfiguration(ConfigurationSection section) {
        if (section == null) return new TreeMap<>();

        TreeMap<String, ConfigurationSection> loaded = new TreeMap<>();
        for (String s : section.getKeys(false)) {
            ConfigurationSection c = section.getConfigurationSection(s);
            if (c != null) loaded.put(s, c);
        }

        return loaded;
    }

    @NotNull
    public TreeMap<String, ConfigurationSection> getLoadedWebhooks() {
        return new TreeMap<>();
    }

    @NotNull
    public TreeMap<String, ConfigurationSection> getLoadedBossbars() {
        return new TreeMap<>();
    }

    public String replacePrefixKey(String string, boolean remove) {
        if (StringUtils.isBlank(string)) return string;

        String temp = remove ? "" : getLangPrefix();
        return string.replace(getLangPrefixKey(), temp);
    }

    public String[] splitString(String s, int limit) {
        return s.split(getLineSeparator(), limit);
    }

    public String[] splitString(String s) {
        return splitString(s, 0);
    }

    public String replace(Player parser, String string) {
        PlayerFormatter papi = TextUtils.PARSE_PLACEHOLDER_API;

        return StringApplier.simplified(string)
                .apply(s -> placeholderManager.replace(parser, s))
                .apply(s -> papi.apply(parser, s))
                .apply(characterAction::act).toString();
    }

    public String colorize(Player target, Player parser, String string) {
        return PrismaticAPI.colorize(
                target == null ? parser : target, replace(parser, string));
    }

    public String colorize(Player player, String string) {
        return colorize(null, player, string);
    }

    public String colorize(String string) {
        return colorize(null, string);
    }

    public static void setLibFromSource(Plugin source, Plugin target) {
        LIB_MAP.put(target, LIB_MAP.get(source));
    }

    public static void setLibAsDefault(Plugin plugin) {
        LIB_MAP.put(plugin, NO_PLUGIN_INSTANCE);
    }

    public static TakionLib fromPlugin(Plugin plugin) {
        return plugin == null ? NO_PLUGIN_INSTANCE : LIB_MAP.get(plugin);
    }

    @NotNull
    public static TakionLib getLib() {
        return fromPlugin(getProvidingPlugin());
    }
}
