package me.croabeast.takion.message.chat;

import lombok.Setter;
import me.croabeast.common.Copyable;
import me.croabeast.takion.TakionLib;
import me.croabeast.common.applier.StringApplier;
import me.croabeast.common.util.ArrayUtils;
import me.croabeast.common.util.TextUtils;
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

/**
 * Represents a chat component builder that processes text into interactive chat components.
 * <p>
 * The {@code ChatComponent} class is responsible for parsing a raw chat string into a structured map
 * of components that can contain clickable and hoverable elements. It supports processing URL links,
 * applying formatting via {@link TakionLib}, and setting click and hover events through {@link ChatClick}
 * and {@link ChatHover} instances.
 * </p>
 * <p>
 * The builder maintains an internal mapping of indices to {@link Component} objects. It provides methods to:
 * <ul>
 *   <li>Append additional text to the component.</li>
 *   <li>Set click and hover events on specific components or for all components.</li>
 *   <li>Compile the constructed components into an array of {@link BaseComponent} for display.</li>
 *   <li>Generate a pattern string representation of the constructed message.</li>
 *   <li>Create a copy of the current chat component.</li>
 * </ul>
 * </p>
 * <p>
 * Example usage:
 * <pre><code>
 * // Create a ChatComponent from a raw message
 * ChatComponent chatComp = ChatComponent.create(library, "Hello, visit https://example.com for more info!");
 *
 * // Set a hover event on the last component
 * chatComp.setHover("Click here to open URL!");
 *
 * // Compile the components for a player
 * BaseComponent[] components = chatComp.compile(player);
 *
 * // Send the components as a chat message (using your preferred method)
 * </code></pre>
 * </p>
 *
 * @see TakionLib
 * @see ChatClick
 * @see ChatHover
 * @see BaseComponent
 */
public final class ChatComponent implements Copyable<ChatComponent> {

    /**
     * The TakionLib instance used for processing, colorizing, and handling actions.
     */
    private final TakionLib lib;

    /**
     * A mapping of component indices to their corresponding {@link Component} objects.
     * <p>
     * Each entry in the map represents a segment of the final chat message with its own formatting and event data.
     * </p>
     */
    private final Map<Integer, Component> map = new LinkedHashMap<>();

    /**
     * Tracks the last used index in the map.
     */
    private int index = -1;

    /**
     * Flag to indicate whether URLs in the text should be parsed and converted into clickable links.
     */
    @Setter
    private boolean parseURLs = true;

    /**
     * Parses URLs in the provided string and updates the internal component mapping.
     *
     * @param string the input text to parse for URLs.
     */
    private void parseURLs(String string) {
        Matcher urlMatcher = TextUtils.URL_PATTERN.matcher(string);
        int lastEnd = 0;

        while (urlMatcher.find()) {
            String t = string.substring(lastEnd, urlMatcher.start());

            if (!t.isEmpty())
                map.put(++index, new Component(t));

            if (parseURLs) {
                final String url = urlMatcher.group();
                // Create a ChatClick event to open the URL.
                ChatClick.Action a = ChatClick.Action.OPEN_URL;
                ChatClick c = new ChatClick(lib, a, url);
                map.put(++index, new Component(url).setClick(c));
            }

            lastEnd = urlMatcher.end();
        }

        if (lastEnd <= (string.length() - 1))
            map.put(++index, new Component(string.substring(lastEnd)));
    }

    /**
     * Updates the internal component mapping by processing the given string.
     * <p>
     * The method applies formatting, converts legacy JSON, applies small-caps and alignment actions,
     * and uses regex patterns to identify sections with click and hover events.
     * </p>
     *
     * @param string the raw input string to process.
     */
    private void updateMessageMapping(String string) {
        if (string == null) return;

        if (string.isEmpty()) {
            map.put(++index, new Component(string));
            return;
        }

        final String line = StringApplier.simplified(string)
                .apply(TextUtils.CONVERT_OLD_JSON)
                .apply(lib.getSmallCapsAction()::act)
                .apply(lib.getCharacterManager()::align)
                .toString();

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

    /**
     * Constructs a new {@code ChatComponent} using the specified TakionLib instance and message.
     *
     * @param lib     the TakionLib instance for processing actions and colorizing.
     * @param message the raw chat message to parse and process.
     */
    private ChatComponent(TakionLib lib, String message) {
        this.lib = Objects.requireNonNull(lib);
        updateMessageMapping(message);
    }

    /**
     * Copy constructor to create a deep copy of the provided ChatComponent.
     *
     * @param builder the ChatComponent instance to copy.
     */
    private ChatComponent(ChatComponent builder) {
        Objects.requireNonNull(builder);

        lib = builder.lib;
        parseURLs = builder.parseURLs;

        if (builder.map.isEmpty()) return;
        map.putAll(builder.map);
        this.index = builder.index;
    }

    /**
     * Sets a hover event on the component at the specified index.
     *
     * @param index the index of the component.
     * @param hover the {@link ChatHover} event to set.
     * @return this {@code ChatComponent} instance.
     */
    public ChatComponent setHover(int index, ChatHover hover) {
        if (index < 0 || ChatEvent.isEmpty(hover))
            return this;

        map.put(index, map.get(index).setHover(hover));
        return this;
    }

    /**
     * Sets a hover event on the component at the specified index using a list of strings.
     *
     * @param index the index of the component.
     * @param hover a list of strings representing the hover text.
     * @return this {@code ChatComponent} instance.
     */
    public ChatComponent setHover(int index, List<String> hover) {
        return hover == null || hover.isEmpty() ? this : setHover(index, new ChatHover(lib, hover));
    }

    /**
     * Sets a hover event on the component at the specified index using an array of strings.
     *
     * @param index the index of the component.
     * @param hover an array of strings representing the hover text.
     * @return this {@code ChatComponent} instance.
     */
    public ChatComponent setHover(int index, String... hover) {
        return setHover(index, ArrayUtils.toList(hover));
    }

    /**
     * Sets a hover event on the last component in the builder.
     *
     * @param hover the {@link ChatHover} event to set.
     * @return this {@code ChatComponent} instance.
     */
    public ChatComponent setHover(ChatHover hover) {
        return setHover(index, hover);
    }

    /**
     * Sets a hover event on the last component in the builder using a list of strings.
     *
     * @param hover a list of strings representing the hover text.
     * @return this {@code ChatComponent} instance.
     */
    public ChatComponent setHover(List<String> hover) {
        return setHover(index, hover);
    }

    /**
     * Sets a hover event on the last component in the builder using an array of strings.
     *
     * @param hover an array of strings representing the hover text.
     * @return this {@code ChatComponent} instance.
     */
    public ChatComponent setHover(String... hover) {
        return setHover(index, hover);
    }

    /**
     * Applies the specified hover event to all components in the builder.
     *
     * @param hover the {@link ChatHover} event to set for all components.
     * @return this {@code ChatComponent} instance.
     */
    public ChatComponent setHoverToAll(ChatHover hover) {
        if (ChatEvent.isEmpty(hover)) return this;
        for (Component c : map.values()) c.setHover(hover);
        return this;
    }

    /**
     * Applies the specified hover event to all components in the builder using a list of strings.
     *
     * @param hover a list of strings representing the hover text.
     * @return this {@code ChatComponent} instance.
     */
    public ChatComponent setHoverToAll(List<String> hover) {
        return setHoverToAll(new ChatHover(lib, hover));
    }

    /**
     * Applies the specified hover event to all components in the builder using an array of strings.
     *
     * @param hover an array of strings representing the hover text.
     * @return this {@code ChatComponent} instance.
     */
    public ChatComponent setHoverToAll(String... hover) {
        return setHoverToAll(ArrayUtils.toList(hover));
    }

    /**
     * Sets a click event on the component at the specified index.
     *
     * @param index the index of the component.
     * @param click the {@link ChatClick} event to set.
     * @return this {@code ChatComponent} instance.
     */
    public ChatComponent setClick(int index, ChatClick click) {
        if (index < 0 || ChatEvent.isEmpty(click))
            return this;

        map.put(index, map.get(index).setClick(click));
        return this;
    }

    /**
     * Sets a click event on the component at the specified index using a string.
     *
     * @param index  the index of the component.
     * @param string the string used to create a {@link ChatClick} event.
     * @return this {@code ChatComponent} instance.
     */
    public ChatComponent setClick(int index, String string) {
        return setClick(index, new ChatClick(lib, string));
    }

    /**
     * Sets a click event on the last component in the builder.
     *
     * @param click the {@link ChatClick} event to set.
     * @return this {@code ChatComponent} instance.
     */
    public ChatComponent setClick(ChatClick click) {
        return setClick(index, click);
    }

    /**
     * Sets a click event on the last component in the builder using a string.
     *
     * @param string the string used to create a {@link ChatClick} event.
     * @return this {@code ChatComponent} instance.
     */
    public ChatComponent setClick(String string) {
        return setClick(index, string);
    }

    /**
     * Applies the specified click event to all components in the builder.
     *
     * @param click the {@link ChatClick} event to set for all components.
     * @return this {@code ChatComponent} instance.
     */
    public ChatComponent setClickToAll(ChatClick click) {
        if (ChatEvent.isEmpty(click)) return this;
        for (Component c : map.values()) c.setClick(click);
        return this;
    }

    /**
     * Applies the specified click event to all components in the builder using a string.
     *
     * @param string the string used to create a {@link ChatClick} event.
     * @return this {@code ChatComponent} instance.
     */
    public ChatComponent setClickToAll(String string) {
        return setClickToAll(new ChatClick(lib, string));
    }

    /**
     * Appends the specified object to the current message.
     * <p>
     * If the object is a String, it is processed and added to the builder; otherwise,
     * the object's {@code toString()} value is used.
     * </p>
     *
     * @param object the object to append.
     * @param <T>    the type of the object.
     * @return this {@code ChatComponent} instance.
     */
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

    /**
     * Compiles the constructed message components into an array of {@link BaseComponent} for display.
     *
     * @param parser the player for which to compile the components.
     * @return an array of {@link BaseComponent} representing the complete chat message.
     * @throws IllegalStateException if the builder is empty.
     */
    public BaseComponent[] compile(Player parser) {
        if (index < 0)
            throw new IllegalStateException("Builder is empty");

        BaseComponent[] components = new BaseComponent[map.size()];
        int count = 0;

        for (Component message : map.values())
            components[count++] = message.compile(parser);
        return components;
    }

    /**
     * Creates a copy of this {@code ChatComponent} instance.
     *
     * @return a new {@code ChatComponent} with the same configuration.
     */
    @NotNull
    public ChatComponent copy() {
        return new ChatComponent(this);
    }

    /**
     * Returns a string representation of this {@code ChatComponent}.
     *
     * @return a string representation of the internal component mapping.
     */
    @Override
    public String toString() {
        return "ChatComponent{map=" + map + '}';
    }

    /**
     * Returns a pattern string representation of the chat message.
     * <p>
     * The pattern string is constructed by concatenating the components, including special formatting tags
     * for hover and click events.
     * </p>
     *
     * @return a pattern string representing the chat message.
     */
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

                    String temp = (joiner + "").replaceAll("\\\\[QE]", "");
                    builder.append("<hover:\"").append(temp).append('"');
                }
                if (hasClick) {
                    builder.append(hasHover ? '|' : '<')
                            .append(click.action)
                            .append(":\"")
                            .append(click.string).append("\">");
                }
                else builder.append('>');
            }

            builder.append(message.string).append("</text>");
        }

        return builder.toString();
    }

    /**
     * Creates a new {@code ChatComponent} instance using the provided {@link TakionLib} instance and message.
     *
     * @param lib     the TakionLib instance (must not be {@code null}).
     * @param message the raw chat message.
     * @return a new {@code ChatComponent} instance.
     */
    @NotNull
    public static ChatComponent create(TakionLib lib, @Nullable String message) {
        return new ChatComponent(lib, message);
    }

    /**
     * Creates a new array of {@link BaseComponent} from the given text.
     *
     * @param lib     the TakionLib instance.
     * @param parser  the player context.
     * @param message the raw chat message.
     * @return an array of {@link BaseComponent} representing the compiled message.
     */
    @NotNull
    public static BaseComponent[] fromText(TakionLib lib, Player parser, String message) {
        return create(lib, message).compile(parser);
    }

    /**
     * Creates a new array of {@link BaseComponent} from the given text using a default player context.
     *
     * @param lib     the TakionLib instance.
     * @param message the raw chat message.
     * @return an array of {@link BaseComponent} representing the compiled message.
     */
    @NotNull
    public static BaseComponent[] fromText(TakionLib lib, String message) {
        return fromText(lib, null, message);
    }

    /**
     * Creates a new array of {@link BaseComponent} from the given text for a specific player.
     *
     * @param parser  the player context.
     * @param message the raw chat message.
     * @return an array of {@link BaseComponent} representing the compiled message.
     */
    @NotNull
    public static BaseComponent[] fromText(Player parser, String message) {
        return create(TakionLib.getLib(), message).compile(parser);
    }

    /**
     * Creates a new array of {@link BaseComponent} from the given text using a default player context.
     *
     * @param message the raw chat message.
     * @return an array of {@link BaseComponent} representing the compiled message.
     */
    @NotNull
    public static BaseComponent[] fromText(String message) {
        return fromText(TakionLib.getLib(), null, message);
    }

    /**
     * Private helper class to encapsulate formatting data for a component.
     */
    private static class FormattingHandler {

        private ChatClick click = null;
        private ChatHover hover = null;

        /**
         * Checks if both click and hover events are empty.
         *
         * @return {@code true} if both click and hover are empty; {@code false} otherwise.
         */
        private boolean isEmpty() {
            return ChatEvent.isEmpty(click) && ChatEvent.isEmpty(hover);
        }

        @Override
        public String toString() {
            return "{" + "click=" + click + ", hover=" + hover + '}';
        }
    }

    /**
     * Represents an individual text component within the chat message.
     * <p>
     * Each {@code Component} holds a string of text and an associated {@link FormattingHandler} that
     * may contain click and hover events. The component can compile itself into a {@link BaseComponent}
     * for display in chat.
     * </p>
     */
    private class Component {

        private final FormattingHandler handler;
        private final String string;

        /**
         * Constructs a new Component with the provided text.
         *
         * @param message the text for this component.
         */
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

        /**
         * Sets a click event on this component.
         *
         * @param click the {@link ChatClick} event to set.
         * @return this Component instance.
         */
        Component setClick(ChatClick click) {
            handler.click = click;
            return this;
        }

        /**
         * Sets a hover event on this component.
         *
         * @param hover the {@link ChatHover} event to set.
         * @return this Component instance.
         */
        Component setHover(ChatHover hover) {
            handler.hover = hover;
            return this;
        }

        /**
         * Sets click and hover events on this component using provided parameters.
         *
         * @param click the click event string.
         * @param hover the hover event string.
         * @return this Component instance with handlers configured.
         */
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

        /**
         * Compiles this component into a {@link BaseComponent} for display in chat.
         * <p>
         * The text is converted from legacy format, and click and hover events are attached if present.
         * </p>
         *
         * @param parser the player context for event creation.
         * @return a compiled {@link BaseComponent} representing this component.
         */
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
