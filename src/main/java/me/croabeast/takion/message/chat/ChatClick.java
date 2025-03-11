package me.croabeast.takion.message.chat;

import lombok.RequiredArgsConstructor;
import me.croabeast.takion.TakionLib;
import me.croabeast.lib.util.ArrayUtils;
import me.croabeast.lib.util.Exceptions;
import net.md_5.bungee.api.chat.ClickEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@RequiredArgsConstructor
public final class ChatClick implements ChatEvent<ClickEvent> {

    private final TakionLib lib;

    final Action action;
    final String string;

    public ChatClick(TakionLib lib, String message) {
        this.lib = Objects.requireNonNull(lib);

        Exceptions.validate(StringUtils::isNotBlank, message);
        String[] array = message.replace("\"", "").split(":", 2);

        this.action = Action.from(array[0]);
        this.string = array.length == 1 ? "" : array[1];
    }

    @NotNull
    public ClickEvent createEvent(Player parser) {
        return new ClickEvent(action.bukkit, lib.replace(parser, string));
    }

    @Override
    public boolean isEmpty() {
        return StringUtils.isBlank(string);
    }

    @Override
    public String toString() {
        return "{action=" + action + ", input='" + string + "'}";
    }

    public enum Action {
        RUN_CMD(ClickEvent.Action.RUN_COMMAND, "click", "run"),
        OPEN_URL(ClickEvent.Action.OPEN_URL, "url"),
        OPEN_FILE(ClickEvent.Action.OPEN_FILE, "file"),
        SUGGEST_CMD(ClickEvent.Action.SUGGEST_COMMAND, "suggest"),
        CHANGE_PAGE(ClickEvent.Action.CHANGE_PAGE),
        CLIPBOARD(ClickEvent.Action.COPY_TO_CLIPBOARD, "copy");

        private final ClickEvent.Action bukkit;
        private final List<String> names = new LinkedList<>();

        Action(ClickEvent.Action bukkit, String... extras) {
            this.bukkit = bukkit;
            names.add(name().toLowerCase(Locale.ENGLISH));

            if (!ArrayUtils.isArrayEmpty(extras))
                names.addAll(ArrayUtils.toList(extras));
        }

        public ClickEvent.Action asBukkit() {
            return bukkit;
        }

        @Override
        public String toString() {
            return names.get(0);
        }

        public static Action from(String name) {
            if (StringUtils.isBlank(name)) return SUGGEST_CMD;

            for (Action type : values()) {
                String temp = name.toLowerCase(Locale.ENGLISH);
                if (type.names.contains(temp)) return type;
            }

            return SUGGEST_CMD;
        }
    }
}
