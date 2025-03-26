package me.croabeast.takion;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.croabeast.lib.util.Exceptions;
import me.croabeast.takion.channel.ChannelManager;
import me.croabeast.takion.character.CharacterInfo;
import me.croabeast.takion.character.CharacterManager;
import me.croabeast.takion.character.DefaultCharacter;
import me.croabeast.takion.character.SmallCaps;
import me.croabeast.takion.logger.TakionLogger;
import me.croabeast.takion.message.MessageSender;
import me.croabeast.takion.message.TitleManager;
import me.croabeast.takion.placeholder.Placeholder;
import me.croabeast.takion.placeholder.PlaceholderManager;
import me.croabeast.lib.PlayerFormatter;
import me.croabeast.lib.Regex;
import me.croabeast.lib.Rounder;
import me.croabeast.lib.applier.StringApplier;
import me.croabeast.lib.util.ArrayUtils;
import me.croabeast.lib.util.TextUtils;
import me.croabeast.prismatic.PrismaticAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
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

@Getter @Setter
public class TakionLib {

    @Getter(AccessLevel.NONE)
    private final Plugin plugin;

    private TakionLogger serverLogger, logger;

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
    private MessageSender loadedSender = new MessageSender(this);

    public TakionLib(Plugin plugin) {
        this.plugin = plugin;

        this.serverLogger = new TakionLogger(this, false);
        this.logger = new TakionLogger(this);

        titleManager = new TitleManager() {
            @Setter @Getter
            private int fadeInTicks = 8, stayTicks = 50, fadeOutTicks = 8;

            @Override
            public Builder builder(String message) {
                Exceptions.validate(StringUtils::isNotBlank, message);

                String[] array = splitString(message, 2);
                return builder(array[0], array.length == 2 ? array[1] : null);
            }
        };

        channelManager = new ChannelManagerImpl(this);

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
                for (DefaultCharacter c : DefaultCharacter.values())
                    map.put(c.getCharacter(), c);

                for (SmallCaps caps : SmallCaps.values())
                    map.put(caps.getCharacter(), caps);
            }

            @Override
            public CharacterInfo getInfo(char c) {
                CharacterInfo info = map.getOrDefault(c, null);
                return info == null ? DEFAULT_INFO : info;
            }

            @Override
            public void addCharacter(char c, int length) {
                map.put(c, CharacterInfo.of(c, length));
            }

            @Override
            public void removeCharacters(Character... chars) {
                if (!ArrayUtils.isArrayEmpty(chars)) for (char c : chars) map.remove(c);
            }

            @Override
            public String align(int limit, String string) {
                if (StringUtils.isBlank(string)) return string;

                final String prefix = getCenterPrefix();
                if (StringUtils.isBlank(prefix) ||
                        !string.startsWith(prefix)) return string;

                final String before = string.replace(prefix, "");

                String temp = StringApplier.simplified(before)
                        .apply(PrismaticAPI::stripAll)
                        .apply(TextUtils.STRIP_JSON)
                        .apply(characterAction::act).toString();

                int size = 0;
                boolean previousCode = false;
                boolean isBold = false;

                for (char c : temp.toCharArray()) {
                    if (c == '§') {
                        previousCode = true;
                        continue;
                    }

                    else if (previousCode) {
                        previousCode = false;
                        isBold = c == 'l' || c == 'L';
                        continue;
                    }

                    CharacterInfo info = getInfo(c);
                    size += isBold ?
                            info.getBoldLength() : info.getLength();
                    size++;
                }

                int toCompensate = limit - (size / 2);
                int compensated = 0;

                StringBuilder sb = new StringBuilder();
                while (compensated < toCompensate) {
                    sb.append(' ');
                    compensated += 4;
                }

                return sb + before;
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

        TakionPlugin.libs.put(plugin, this);
    }

    static Plugin getProvidingPlugin() {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            Class<?> thisClass = TakionLib.class, result = TakionLib.class;

            for (int i = 2; i < stackTrace.length; i++) {
                final StackTraceElement stack = stackTrace[i];
                if (stack.getClassName().equals(thisClass.getName()))
                    continue;

                try {
                    result = Class.forName(stack.getClassName());
                } catch (Exception ignored) {}
            }

            return JavaPlugin.getProvidingPlugin(result);
        } catch (Exception e) {
            return null;
        }
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

    @NotNull
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
        TakionPlugin.libs.put(target, TakionPlugin.libs.get(source));
    }

    public static void setLibAsDefault(Plugin plugin) {
        TakionPlugin.libs.put(plugin, TakionPlugin.noPluginInstance);
    }

    public static TakionLib fromPlugin(Plugin plugin) {
        return plugin != null ?
                TakionPlugin.libs.getOrDefault(plugin, TakionPlugin.noPluginInstance) :
                TakionPlugin.noPluginInstance;
    }

    @NotNull
    public static TakionLib getLib() {
        return fromPlugin(getProvidingPlugin());
    }
}
