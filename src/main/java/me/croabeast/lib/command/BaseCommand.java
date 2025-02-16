package me.croabeast.lib.command;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a base command with permissions, execution logic, name, and aliases.
 */
public interface BaseCommand extends Permissible {

    /**
     * Gets the name of the command.
     * @return the name of the command.
     */
    @NotNull
    String getName();

    /**
     * Gets the list of aliases for the command.
     * @return the list of aliases.
     */
    @NotNull
    List<String> getAliases();

    /**
     * Gets the executable logic for the command.
     *
     * <p> This method should return an {@link Executable} object that contains the logic to be executed
     * when the command is invoked.
     *
     * @return the executable logic for the command.
     * @throws NullPointerException if the executable action is not set.
     */
    @NotNull
    Executable getExecutable();
}
