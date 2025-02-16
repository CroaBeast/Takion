package me.croabeast.lib.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Represents a command for classes that can provide tab completion suggestions.
 */
@FunctionalInterface
public interface Completable {

    /**
     * Generates a collection of suggested completions for a command.
     *
     * @param sender the command sender requesting the completions.
     * @param arguments the arguments provided by the sender.
     *
     * @return a supplier providing a collection of suggested completions, or null if none are available.
     */
    @NotNull
    Supplier<Collection<String>> generateCompletions(CommandSender sender, String[] arguments);

    /**
     * Retrieves the builder responsible for generating tab completions.
     *
     * @return a TabBuilder for generating tab completions, or null if not applicable.
     */
    @Nullable
    default TabBuilder getCompletionBuilder() {
        return null;
    }
}