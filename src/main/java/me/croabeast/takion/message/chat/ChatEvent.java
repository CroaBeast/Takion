package me.croabeast.takion.message.chat;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a chat event that can be created and evaluated for content.
 * <p>
 * The {@code ChatEvent} interface defines a contract for generating a chat event object (of type {@code E})
 * based on a given {@link Player} (acting as the parser or originator of the event). It also provides a mechanism
 * to check if the generated event is considered empty (i.e., contains no meaningful data).
 * </p>
 * <p>
 * Additionally, a static helper method is available to safely determine if a {@code ChatEvent} is empty.
 * </p>
 *
 * @param <E> the type of event produced by this chat event
 */
public interface ChatEvent<E> {

    /**
     * Creates an event instance based on the provided player.
     * <p>
     * This method should generate and return a chat event of type {@code E} using the given {@link Player} as a context,
     * typically to include details such as the player's name, message, or any other relevant information.
     * </p>
     *
     * @param parser the player for whom the event is created.
     * @return a new instance of the event of type {@code E} (never {@code null}).
     */
    @NotNull
    E createEvent(Player parser);

    /**
     * Determines whether this chat event is empty.
     * <p>
     * An event is considered empty if it does not contain any meaningful data or if no event was generated.
     * </p>
     *
     * @return {@code true} if the event is empty; {@code false} otherwise.
     */
    boolean isEmpty();

    /**
     * Returns a string representation of this chat event.
     * <p>
     * This method should provide a human-readable description of the event, useful for debugging or logging purposes.
     * </p>
     *
     * @return a string representation of the event.
     */
    String toString();

    /**
     * Static helper method to determine if a given {@code ChatEvent} is empty.
     * <p>
     * This method safely checks whether the provided event is {@code null} or empty by invoking {@link #isEmpty()}.
     * </p>
     *
     * @param event the chat event to check.
     * @return {@code true} if the event is {@code null} or empty; {@code false} otherwise.
     */
    static boolean isEmpty(ChatEvent<?> event) {
        return event == null || event.isEmpty();
    }
}
