package me.croabeast.takion;

import lombok.RequiredArgsConstructor;
import me.croabeast.prismatic.chat.ChatProcessor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Default Takion implementation for PrismaticAPI chat component processing.
 *
 * <p>This processor is owned by {@link TakionLib} and is exposed through
 * {@link TakionLib#getChatProcessor()} so downstream plugins can reuse or replace it without creating
 * their own bridge for normal Takion upgrades.</p>
 *
 * @since 1.6.3
 */
@RequiredArgsConstructor
final class TakionChatProcessor implements ChatProcessor {

    private final TakionLib lib;

    @NotNull
    public String colorize(@Nullable Player player, String string) {
        return lib.colorize(player, string);
    }

    @NotNull
    public String prepare(String string) {
        return lib.getCharacterManager().align(lib.prepareText(string));
    }

    @NotNull
    public String getLineSeparator() {
        String regex = lib.getLineSeparator();
        return regex.startsWith("\\Q") && regex.endsWith("\\E") ?
                regex.substring(2, regex.length() - 2) :
                regex;
    }

    @NotNull
    public String getLineSeparatorRegex() {
        return lib.getLineSeparator();
    }
}
