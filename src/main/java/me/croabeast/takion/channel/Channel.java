package me.croabeast.takion.channel;


import me.croabeast.takion.message.MessageSender;
import me.croabeast.lib.Regex;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

public interface Channel {

    @NotNull
    String getName();

    @NotNull
    String getPrefix();

    void setPrefix(@NotNull String prefix);

    @Nullable
    @Regex
    String getPattern();

    void setPattern(@Nullable @Regex String pattern);

    @NotNull
    Matcher matcher(String string);

    String formatString(Player target, Player parser, String string);

    MessageSender.Flag getFlag();

    boolean send(Collection<? extends Player> targets, Player parser, String message);

    default boolean send(Collection<? extends Player> targets, String input) {
        return send(targets, null, input);
    }

    default boolean send(Player target, Player parser, String input) {
        Set<Player> targets = new HashSet<>();
        targets.add(target != null ? target : parser);
        return send(targets, parser, input);
    }

    default boolean send(Player player, String input) {
        return send(player, player, input);
    }

    default boolean send(String input) {
        return send((Collection<? extends Player>) null, input);
    }
}
