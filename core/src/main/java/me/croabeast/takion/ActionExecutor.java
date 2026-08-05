package me.croabeast.takion;

import me.croabeast.takion.token.Token;
import lombok.RequiredArgsConstructor;
import me.croabeast.takion.action.Action;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;

@RequiredArgsConstructor
final class ActionExecutor {

    private final TakionLib lib;

    boolean execute(Collection<? extends Player> targets, Player parser, String channel, String string) {
        if (StringUtils.isBlank(string)) return false;

        Token.Context context = createContext(targets, parser, channel);
        boolean executed = false;

        for (Action action : lib.getActionManager().getActions()) {
            if (!canExecute(action, channel)) continue;

            Matcher matcher = action.getPattern().matcher(string);
            while (matcher.find())
                if (action.resolve(context, matcher.toMatchResult()))
                    executed = true;
        }

        return executed;
    }

    private boolean canExecute(Action action, String channel) {
        Set<String> channels = action.getChannels();
        if (channels == null || channels.isEmpty()) return true;
        if (StringUtils.isBlank(channel)) return false;

        String normalized = channel.trim().toLowerCase(Locale.ENGLISH);
        for (String allowed : channels)
            if (normalized.equals(StringUtils.trimToEmpty(allowed).toLowerCase(Locale.ENGLISH)))
                return true;

        return false;
    }

    @NotNull
    private Token.Context createContext(Collection<? extends Player> targets, Player parser, String channel) {
        return new Token.SimpleContext()
                .setLib(lib)
                .setTargets(targets == null ? Collections.emptyList() : targets)
                .setParser(parser)
                .setChannel(channel);
    }
}
