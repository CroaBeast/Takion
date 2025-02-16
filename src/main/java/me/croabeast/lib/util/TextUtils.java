package me.croabeast.lib.util;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import me.croabeast.lib.PlayerFormatter;
import net.md_5.bungee.api.chat.ClickEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that stores static methods for text, string, configurations,
 * and more.
 *
 * @author CroaBeast
 * @since 1.0
 */
@UtilityClass
public class TextUtils {

    /**
     * Parse all the {@link PlaceholderAPI} placeholders of an input string if
     * {@link PlaceholderAPI} is enabled.
     *
     * <p> Use the <code>apply(Player, String)</code> method to apply it on a string.
     */
    public final PlayerFormatter PARSE_PLACEHOLDER_API;

    /**
     * Parse all the {@link InteractiveChat} values of an input string if
     * {@link InteractiveChat} is enabled.
     *
     * <p> Use the <code>apply(Player, String)</code> method to apply it on a string.
     */
    public final PlayerFormatter PARSE_INTERACTIVE_CHAT;

    /**
     * Strips the first spaces of an input string.
     * <p> Use the <code>apply(String)</code> method to apply it on a string.
     */
    public final UnaryOperator<String> STRIP_FIRST_SPACES;

    /**
     * Coverts the old Json format to the new in-built one.
     * <p> Use the <code>apply(String)</code> method to apply it on a string.
     */
    public final UnaryOperator<String> CONVERT_OLD_JSON;

    /**
     * Check if the line uses a valid json format on any part of the string.
     * <p> Use the <code>test(String)</code> method to check a string.
     */
    public final Predicate<String> IS_JSON;

    /**
     * Removes the in-built JSON pattern of a string, if there is any format.
     * <p> Use the <code>apply(String)</code> method to apply it on a string.
     */
    public final UnaryOperator<String> STRIP_JSON;

    /**
     * A regular expression pattern for matching URLs in a case-insensitive manner.
     *
     * <p> The pattern matches URLs that start with an optional protocol (http or https),
     * followed by a domain name consisting of at least two alphanumeric characters, and
     * ending with an optional path.
     *
     * <p> Note: This pattern is a simplified version that matches basic URL formats and
     * does not handle all possible cases. It may not validate all edge cases or specific
     * scenarios. For more comprehensive URL matching, consider using specialized URL
     * parsing libraries or additional validation.
     */
    public final Pattern URL_PATTERN;

    /**
     * The main pattern to identify the custom chat message format in a string.
     *
     * <p> Keep in mind that every string can only have one {@link ClickEvent.Action};
     * a click action has this format:
     * <pre> {@code
     * Available Actions: RUN, SUGGEST, URL and all ClickAction values.
     * "<ACTION>:<the click string>" -> "RUN:/me click to run"
     * } </pre>
     *
     * <pre> {@code
     * // • Examples:
     * String hover = "<hover:\"a hover line\">text to apply</text>";
     * String click = "<run:\"/click me\">text to apply</text>";
     * String mixed = "<hover:\"a hover line<n>another line\"|run:\"/command\">text to apply</text>";
     * } </pre>
     */
    public final Pattern FORMAT_CHAT_PATTERN;

    static {
        PARSE_PLACEHOLDER_API = (p, s) ->
                StringUtils.isBlank(s) || !Exceptions.isPluginEnabled("PlaceholderAPI") ?
                        s :
                        PlaceholderAPI.setPlaceholders(p, s);

        PARSE_INTERACTIVE_CHAT = (p, s) -> {
            if (StringUtils.isBlank(s) || !Exceptions.isPluginEnabled("InteractiveChat"))
                return s;

            try {
                return InteractiveChatAPI.markSender(s, p.getUniqueId());
            } catch (Exception e) {
                return s;
            }
        };

        STRIP_FIRST_SPACES = s -> {
            if (StringUtils.isBlank(s)) return s;
            String startLine = s;

            try {
                while (s.charAt(0) == ' ') s = s.substring(1);
                return s;
            } catch (IndexOutOfBoundsException e) {
                return startLine;
            }
        };

        CONVERT_OLD_JSON = s -> {
            if (StringUtils.isBlank(s)) return s;

            String p = "(?i)(hover|run|suggest|url)=\\[(.[^|\\[\\]]*)]";
            Matcher old = Pattern.compile(p).matcher(s);

            while (old.find()) {
                String temp = old.group(1) + ":\"" + old.group(2) + "\"";
                s = s.replace(old.group(), temp);
            }
            return s;
        };

        IS_JSON = s -> TextUtils.FORMAT_CHAT_PATTERN.matcher(s).find();

        STRIP_JSON = s -> {
            if (StringUtils.isBlank(s)) return s;

            s = CONVERT_OLD_JSON.apply(s);
            if (!IS_JSON.test(s)) return s;

            Matcher m = TextUtils.FORMAT_CHAT_PATTERN.matcher(s);

            while (m.find())
                s = s.replace(m.group(), m.group(7));

            return s;
        };

        URL_PATTERN = Pattern.compile("(?i)^(?:(https?)://)?([-\\w_.]{2,}\\.[a-z]{2,4})(/\\S*)?$");

        final String p = "(.[^|]*?):\"(.[^|]*?)\"";
        FORMAT_CHAT_PATTERN = Pattern.compile("<(" + p + "([|]" + p + ")?)>(.+?)</text>");
    }

    /**
     * Converts a {@link String} to a {@link List} from a configuration section if it's not a list.
     *
     * @param section a config file or section, can be null
     * @param path the path to locate the string or list
     * @param def a default string list if value is not found
     *
     * @return the converted string list or default value if section is null
     */
    public List<String> toList(ConfigurationSection section, String path, List<String> def) {
        if (section == null) return def;

        if (section.isList(path)) {
            List<?> raw = section.getList(path, def);
            if (raw == null || raw.isEmpty()) return def;

            List<String> list = new ArrayList<>();
            for (Object o : raw) list.add(o.toString());

            return list;
        }

        Object temp = section.get(path);
        return temp == null ? def : ArrayUtils.toList(temp.toString());
    }

    /**
     * Converts a {@link String} to a {@link List} from a configuration section if it's not a list.
     *
     * @param section a config file or section, can be null
     * @param path the path to locate the string or list
     *
     * @return the converted string list or an empty list if section is null
     */
    @NotNull
    public List<String> toList(ConfigurationSection section, String path) {
        return toList(section, path, new ArrayList<>());
    }
}
