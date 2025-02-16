package me.croabeast.takion.message.chat;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface ChatEvent<E> {

    @NotNull
    E createEvent(Player parser);

    boolean isEmpty();

    String toString();

    static boolean isEmpty(ChatEvent<?> event) {
        return event == null || event.isEmpty();
    }
}
