package me.croabeast.takion;

import me.croabeast.takion.character.SmallCaps;
import me.croabeast.takion.format.Format;
import me.croabeast.takion.format.FormatManager;
import me.croabeast.takion.format.StringFormat;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

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
    }

    @Override
    public <T> boolean load(String id, Format<T> format) {
        return formats.putIfAbsent(id.toUpperCase(Locale.ENGLISH), format) == null;
    }

    @Override
    public boolean remove(String id) {
        return formats.remove(id.toUpperCase(Locale.ENGLISH)) != null;
    }

    @SuppressWarnings("all")
    public <T> boolean editFormat(String id, Format<T> newFormat) {
        Format<?> format = formats.get(id = id.toUpperCase(Locale.ENGLISH));
        return format != null &&
                formats.remove(format) == format &&
                formats.put(id, newFormat) == null;
    }

    @Override
    public boolean editId(String oldId, String newId) {
        oldId = oldId.toUpperCase(Locale.ENGLISH);
        newId = newId.toUpperCase(Locale.ENGLISH);

        Format<?> format = formats.get(oldId);

        return format != null &&
                formats.remove(oldId) == format &&
                formats.put(newId, format) == null;
    }

    @Override
    public <T, F extends Format<T>> F get(String identifier) {
        return (F) formats.get(identifier.toUpperCase(Locale.ENGLISH));
    }
}
