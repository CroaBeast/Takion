package me.croabeast.lib.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Functional interface representing an executable command action.
 */
@FunctionalInterface
public interface Executable {

    /**
     * Executes an action based on the provided command sender and arguments.
     *
     * @param sender the command sender executing the action.
     * @param arguments the arguments passed to the command.
     *
     * @return the result of the action execution as a {@link State}.
     */
    @NotNull
    Executable.State executeAction(CommandSender sender, String[] arguments);

    /**
     * Converts this {@code Executable} into a Bukkit {@link CommandExecutor}.
     * @return a {@link CommandExecutor} that executes the action defined by this {@code Executable}.
     */
    @NotNull
    default CommandExecutor asBukkit() {
        return (sender, command, label, args) -> executeAction(sender, args).asBoolean();
    }

    @NotNull
    static Executable from(BiPredicate<CommandSender, String[]> predicate) {
        Objects.requireNonNull(predicate);
        return (s, a) -> predicate.test(s, a) ? State.TRUE : State.FALSE;
    }

    @NotNull
    static Executable from(boolean value) {
        return (sender, args) -> value ? State.TRUE : State.FALSE;
    }

    /**
     * Enum representing a result.
     */
    enum State {
        /**
         * Indicates an undefined state.
         */
        UNDEFINED,
        /**
         * Indicates a false state.
         */
        FALSE,
        /**
         * Indicates a true state.
         */
        TRUE;

        /**
         * Converts the {@code TriState} object to a boolean value.
         * @return {@code true} if the state is {@code TRUE}, {@code false} otherwise.
         */
        public boolean asBoolean() {
            return this == TRUE;
        }
    }
}
