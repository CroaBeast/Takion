package me.croabeast.takion.chat;

import me.croabeast.common.builder.BaseBuilder;
import me.croabeast.common.reflect.Reflector;
import me.croabeast.common.util.ArrayUtils;
import me.croabeast.takion.TakionLib;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Represents a rich, mutable chat component that can carry click and hover actions.
 * <p>
 * A {@code ChatComponent<C>} wraps a legacy‐format {@link TextComponent} message and
 * allows optional {@link ClickEvent} and {@link HoverEvent} to be attached.
 * It implements {@link BaseBuilder BaseBuilder<C>} so all mutator methods return
 * {@code this} (of type {@code C}) for fluent chaining.
 * </p>
 * <p>
 * After configuring message, click, and hover, call {@link #compile(Player)} to obtain
 * the BungeeCord {@link BaseComponent} array ready for sending via Spigot.
 * </p>
 *
 * @param <C> the concrete subtype of this component (for fluent builders)
 * @see TextComponent
 * @see ClickEvent
 * @see HoverEvent
 */
public interface ChatComponent<C extends ChatComponent<C>> extends BaseBuilder<C> {

    /**
     * The internal URL‐matching {@link Pattern} used by BungeeCord's {@link TextComponent}
     * to auto-detect and underline URLs in legacy text.
     */
    Pattern URL_PATTERN = Reflector.of(TextComponent.class).get("url");

    /**
     * Returns the raw chat text of this component, without any events or legacy codes.
     *
     * @return the underlying message text (never {@code null})
     */
    @NotNull
    String getMessage();

    /**
     * Set the raw chat text of this component, replacing any existing content.
     * <p>
     * This method mutates the component in place and returns {@code this} for chaining.
     * </p>
     *
     * @param message the new message text (never {@code null})
     * @return this component, with the new message set
     */
    @NotNull
    C setMessage(@NotNull String message);

    /**
     * Attach a click action to this component.
     * <p>
     * The component is mutated in place, and this same instance is returned
     * for chaining. The {@code click} parameter determines which {@link ClickEvent.Action}
     * will be used, and {@code input} is the command, URL, or other argument.
     * </p>
     *
     * @param click the click action type
     * @param input the argument for the action (e.g. "/say hi", "http://...")
     * @return this component, with click event applied
     */
    @NotNull
    C setClick(Click click, String input);

    /**
     * Attach a click action by its name (alias) and argument.
     * <p>
     * Convenience for {@code setClick(Click.fromName(click), input)}.
     * </p>
     *
     * @param click the click action name or alias (e.g. "run", "url")
     * @param input the argument for the action
     * @return this component, with click event applied
     */
    @NotNull
    default C setClick(String click, String input) {
        return setClick(Click.fromName(click), input);
    }

    /**
     * Attach a click action by parsing a single {@code "action:argument"} string.
     * <p>
     * Splits on the first {@code ':'}; if none is present, uses an empty argument.
     * Quotation marks are removed before parsing.
     * </p>
     *
     * @param input the combined action and argument (e.g. "run:/tp 5 64 5")
     * @return this component, with click event applied
     */
    @NotNull
    default C setClick(String input) {
        String[] parts = input.replace("\"", "").split(":", 2);
        return setClick(parts[0], parts.length == 1 ? "" : parts[1]);
    }

    /**
     * Attach a hover tooltip to this component.
     * <p>
     * Each element in {@code list} becomes a separate line in the hover display.
     * Mutates this component and returns {@code this} for chaining.
     * </p>
     *
     * @param list the lines of hover text (never {@code null})
     * @return this component, with hover event applied
     */
    @NotNull
    C setHover(List<String> list);

    /**
     * Attach a hover tooltip via varargs.
     *
     * @param array the lines of hover text
     * @return this component, with hover event applied
     */
    @NotNull
    default C setHover(String... array) {
        return setHover(ArrayUtils.toList(array));
    }

    /**
     * Attach a single‐line hover tooltip to this component.
     *
     * @param string the hover text line
     * @return this component, with hover event applied
     */
    @NotNull
    C setHover(String string);

    /**
     * Check if this component has any click or hover events attached.
     *
     * @return {@code true} if events are present, {@code false} otherwise
     */
    boolean hasEvents();

    /**
     * Compile this component into an array of BungeeCord {@link BaseComponent}
     * suitable for sending via the Spigot API (e.g. {@code player.spigot().sendMessage(...)}).
     *
     * @param player the target {@link Player} context for placeholder resolution (might be {@code null})
     * @return the compiled {@link BaseComponent} array
     */
    @NotNull
    BaseComponent[] compile(Player player);

    /**
     * Create a new {@link ChatComponent} from raw text without player context,
     * using the specified {@link TakionLib}.
     *
     * @param lib     the TakionLib instance providing configuration
     * @param message the raw chat message
     * @return a fresh {@link ChatComponent} instance
     */
    static ChatComponent<?> fromString(TakionLib lib, String message) {
        return new ComponentImpl(lib, message);
    }

    /**
     * Create a new {@link ChatComponent} from raw text without player context,
     * using the default {@link TakionLib} singleton.
     *
     * @param message the raw chat message
     * @return a fresh {@link ChatComponent} instance
     */
    static ChatComponent<?> fromString(String message) {
        return fromString(TakionLib.getLib(), message);
    }

    /**
     * Supported click‐action types for {@link ChatComponent#setClick(Click, String)}.
     * <p>
     * Each constant maps a set of aliases (including the enum name itself)
     * to a BungeeCord {@link ClickEvent.Action}. Use {@link #fromName(String)}
     * to parse an alias into its constant.
     * </p>
     */
    enum Click {
        /**
         * Executes the given command as if the player typed it.
         */
        EXECUTE(ClickEvent.Action.RUN_COMMAND, "click", "run"),

        /**
         * Opens the specified URL in the player's default web browser.
         */
        OPEN_URL(ClickEvent.Action.OPEN_URL, "url"),

        /**
         * Opens a local file path on the player's system.
         */
        OPEN_FILE(ClickEvent.Action.OPEN_FILE, "file"),

        /**
         * Suggests the given command in the chat input bar.
         */
        SUGGEST(ClickEvent.Action.SUGGEST_COMMAND, "suggest"),

        /**
         * Changes the page in a book interface (e.g. written book GUI).
         */
        CHANGE_PAGE(ClickEvent.Action.CHANGE_PAGE, "page"),

        /**
         * Copies the specified text to the player's clipboard.
         */
        CLIPBOARD(ClickEvent.Action.COPY_TO_CLIPBOARD, "copy");

        private final ClickEvent.Action bukkit;
        private final List<String> names = new ArrayList<>();

        Click(ClickEvent.Action bukkit, String... extras) {
            this.bukkit = bukkit;
            names.add(name().toLowerCase(Locale.ENGLISH));
            names.add(bukkit.name().toLowerCase(Locale.ENGLISH));
            names.addAll(ArrayUtils.toList(extras));
        }

        /**
         * Get the underlying BungeeCord click‐action enum.
         *
         * @return the {@link ClickEvent.Action} corresponding to this constant
         */
        public ClickEvent.Action asBukkit() {
            return bukkit;
        }

        /**
         * Returns the primary alias (the first registered name) for this action.
         *
         * @return the primary alias string
         */
        @Override
        public String toString() {
            return names.get(0);
        }

        /**
         * Parse a case‐insensitive alias or enum name into a {@link Click} constant.
         * <p>
         * Returns {@link #SUGGEST} if the input is blank or unrecognized.
         * </p>
         *
         * @param name the alias or enum name to parse
         * @return the matching {@link Click} constant, or {@link #SUGGEST} if none found
         */
        public static Click fromName(String name) {
            if (StringUtils.isBlank(name)) {
                return SUGGEST;
            }
            String lower = name.toLowerCase(Locale.ENGLISH);
            for (Click type : values()) {
                if (type.names.contains(lower)) {
                    return type;
                }
            }
            return SUGGEST;
        }
    }
}
