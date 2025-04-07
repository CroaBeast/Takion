package me.croabeast.takion.message.chat;

import lombok.RequiredArgsConstructor;
import me.croabeast.takion.TakionLib;
import me.croabeast.common.util.ArrayUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a chat hover event that displays additional information when a player hovers over a chat component.
 * <p>
 * The {@code ChatHover} class implements the {@link ChatEvent} interface for {@link HoverEvent} objects,
 * allowing the creation of hover events based on a list of strings. These strings are processed (e.g., colorized)
 * using a {@link TakionLib} instance and then converted into a set of {@link BaseComponent} objects to form
 * the content of the hover event.
 * </p>
 * <p>
 * The hover text is built from a list of strings, which can be provided as an array of strings. If multiple lines
 * are present, they will be separated by newline characters.
 * </p>
 *
 * @see ChatEvent
 * @see HoverEvent
 * @see TakionLib
 */
@RequiredArgsConstructor
public final class ChatHover implements ChatEvent<HoverEvent> {

    /**
     * The TakionLib instance used to process and colorize the hover text.
     */
    private final TakionLib lib;

    /**
     * A list of strings that represent the hover text content.
     */
    final List<String> list;

    /**
     * Constructs a new {@code ChatHover} with the specified TakionLib instance and an array of strings.
     * <p>
     * The array is converted to a list internally.
     * </p>
     *
     * @param lib   the TakionLib instance (must not be {@code null}).
     * @param array the array of strings representing the hover text.
     */
    public ChatHover(TakionLib lib, String... array) {
        this(lib, ArrayUtils.toList(array));
    }

    /**
     * Creates a {@link HoverEvent} based on the hover text list and the given player.
     * <p>
     * Each string in the list is processed using the {@link TakionLib#colorize(Player, String)} method to apply
     * color codes and then converted from legacy text to a {@link BaseComponent}. If there are multiple lines,
     * a newline character is added after each line except the last.
     * </p>
     *
     * @param parser the player that serves as the context for colorization.
     * @return a new {@link HoverEvent} configured to show the processed hover text.
     */
    @SuppressWarnings("deprecation")
    @NotNull
    public HoverEvent createEvent(Player parser) {
        final BaseComponent[] contents = new BaseComponent[list.size()];
        int size = list.size();

        for (int i = 0; i < size; i++) {
            String s = lib.colorize(parser, list.get(i));
            if (i != size - 1) {
                s += "\n";
            }
            contents[i] = new TextComponent(TextComponent.fromLegacyText(s));
        }

        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, contents);
    }

    /**
     * Determines whether this hover event contains any content.
     *
     * @return {@code true} if the list of hover text strings is empty; {@code false} otherwise.
     */
    @Override
    public boolean isEmpty() {
        return ArrayUtils.isArrayEmpty(list);
    }

    /**
     * Returns a string representation of this ChatHover.
     * <p>
     * If the hover text list is empty, it returns an empty set notation "{}". Otherwise, it appends a reset code
     * ("§r") to the last line and returns the list as a string.
     * </p>
     *
     * @return a string representation of the hover event.
     */
    @Override
    public String toString() {
        if (this.isEmpty()) return "{}";

        int last = list.size() - 1;
        list.set(last, list.get(last) + "§r");

        return '{' + list.toString() + '}';
    }
}
