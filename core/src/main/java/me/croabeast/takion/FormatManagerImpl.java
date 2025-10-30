package me.croabeast.takion;

import me.croabeast.takion.character.SmallCaps;
import me.croabeast.takion.format.ContextualFormat;
import me.croabeast.takion.format.Format;
import me.croabeast.takion.format.FormatManager;
import me.croabeast.takion.format.StringFormat;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

@SuppressWarnings("unchecked")
final class FormatManagerImpl implements FormatManager {

    private final Map<String, Format<?>> formats = new HashMap<>();

    {
        formats.put("SMALL_CAPS", new StringFormat() {
            @NotNull
            public String getRegex() {
                String s = "(small_caps|sc)";
                return "(?i)<" + s + ">(.+?)</" + s + ">";
            }

            @NotNull
            public String accept(String string) {
                if (StringUtils.isBlank(string)) return string;

                Matcher matcher = matcher(string);
                while (matcher.find())
                    string = string.replace(
                            matcher.group(), SmallCaps.toSmallCaps(matcher.group(2)));

                return string;
            }

            @Override
            public String removeFormat(String string) {
                if (StringUtils.isBlank(string)) return string;

                Matcher matcher = matcher(string);
                while (matcher.find())
                    string = string.replace(matcher.group(), matcher.group(2));

                return string;
            }
        });
        formats.put("CHARACTER", new StringFormat() {
            @NotNull
            public String getRegex() {
                return "<[Uu]:([a-fA-F\\d]{4})>";
            }

            @NotNull
            public String accept(String string) {
                if (StringUtils.isBlank(string)) return string;

                Matcher m = matcher(string);
                while (m.find()) {
                    char c = (char) Integer.parseInt(m.group(1), 16);
                    string = string.replace(m.group(), c + "");
                }

                return string;
            }

            @Override
            public String removeFormat(String string) {
                return this.accept(string);
            }
        });
        formats.put("BLANK_SPACES", new ContextualFormat<Boolean>() {
            @NotNull
            public String getRegex() {
                return "(?i)<add_space:(\\d+)>";
            }

            @NotNull
            public Boolean accept(Collection<? extends Player> players, String string) {
                if ((players == null ||
                        players.isEmpty()) || StringUtils.isBlank(string))
                    return false;

                Matcher matcher = matcher(string);
                if (!matcher.find()) return false;

                int count = 0;
                try {
                    count = Integer.parseInt(matcher.group(1));
                } catch (Exception ignored) {}
                if (count <= 0) return false;

                boolean atLeastOneIsSent = false;
                for (Player player : players) {
                    if (player == null) continue;

                    for (int i = 0; i < count; i++)
                        player.sendMessage("");

                    if (!atLeastOneIsSent)
                        atLeastOneIsSent = true;
                }

                return atLeastOneIsSent;
            }
        });
    }

    @Override
    public <T> boolean load(String id, Format<T> format) {
        return formats.putIfAbsent(id.toUpperCase(Locale.ENGLISH), format) == null;
    }

    @Override
    public boolean remove(String id) {
        return formats.remove(id.toUpperCase(Locale.ENGLISH)) != null;
    }

    @Override
    public <T> boolean editFormat(String id, Format<T> newFormat) {
        id = id.toUpperCase(Locale.ENGLISH);
        final Format<?> format = formats.get(id);

        return format != null &&
                formats.remove(format) == format &&
                formats.put(id, newFormat) == null;
    }

    @Override
    public boolean editId(String oldId, String newId) {
        oldId = oldId.toUpperCase(Locale.ENGLISH);
        newId = newId.toUpperCase(Locale.ENGLISH);

        final Format<?> format = formats.get(oldId);

        return format != null &&
                formats.remove(oldId) == format &&
                formats.put(newId, format) == null;
    }

    @Override
    public <T, F extends Format<T>> F get(String identifier) {
        return (F) formats.get(identifier.toUpperCase(Locale.ENGLISH));
    }
}
