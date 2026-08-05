package me.croabeast.takion;

import me.croabeast.takion.token.Token;
import me.croabeast.takion.action.Action;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

final class BlankSpacesAction implements Action {

    private static final Pattern PATTERN = Pattern.compile("(?i)<add_space:(\\d+)>");

    @NotNull
    public String getId() {
        return "blank_spaces";
    }

    @NotNull
    public Pattern getPattern() {
        return PATTERN;
    }

    @Override
    @NotNull
    public Set<String> getChannels() {
        return Collections.singleton("chat");
    }

    @Override
    public @NonNull Boolean resolve(@NotNull Token.Context context, @NotNull MatchResult match) {
        int count;
        try {
            count = Integer.parseInt(match.group(1));
        } catch (Exception ignored) {
            return false;
        }

        if (count <= 0 || context.getTargets().isEmpty()) return false;

        boolean atLeastOneIsSent = false;
        for (Player player : context.getTargets()) {
            if (player == null) continue;

            for (int i = 0; i < count; i++)
                player.sendMessage("");

            if (!atLeastOneIsSent)
                atLeastOneIsSent = true;
        }

        return atLeastOneIsSent;
    }
}
