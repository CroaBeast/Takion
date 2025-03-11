package me.croabeast.takion.message.chat;

import lombok.RequiredArgsConstructor;
import me.croabeast.takion.TakionLib;
import me.croabeast.lib.util.ArrayUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@RequiredArgsConstructor
public final class ChatHover implements ChatEvent<HoverEvent> {

    private final TakionLib lib;
    final List<String> list;

    public ChatHover(TakionLib lib, String... array) {
        this(lib, ArrayUtils.toList(array));
    }

    @SuppressWarnings("deprecation")
    @NotNull
    public HoverEvent createEvent(Player parser) {
        final BaseComponent[] contents = new BaseComponent[list.size()];
        int size = list.size();

        for (int i = 0; i < size; i++) {
            String s = lib.colorize(parser, list.get(i));
            if (i != size - 1) s += "\n";

            contents[i] = new TextComponent(TextComponent.fromLegacyText(s));
        }

        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, contents);
    }

    public boolean isEmpty() {
        return ArrayUtils.isArrayEmpty(list);
    }

    @Override
    public String toString() {
        if (this.isEmpty()) return "{}";

        int last = list.size() - 1;
        list.set(last, list.get(last) + "§r");

        return '{' + list.toString() + '}';
    }
}
