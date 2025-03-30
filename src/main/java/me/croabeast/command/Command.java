package me.croabeast.command;

import me.croabeast.lib.Registrable;
import org.bukkit.Keyed;
import org.bukkit.command.PluginIdentifiableCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a {@link BaseCommand} with additional functionalities such as enabling/disabling,
 * registering/unregistering, and managing subcommands.
 */
public interface Command extends BaseCommand, Completable, PluginIdentifiableCommand, Keyed, Registrable {

    /**
     * Checks if the command is enabled.
     *
     * @return true if the command is enabled, false otherwise.
     */
    boolean isEnabled();

    /**
     * Checks if the command is overriding another command.
     *
     * @return true if the command is overriding, false otherwise.
     */
    boolean isOverriding();

    /**
     * Gets the subcommands of this command.
     * @return a set of subcommands.
     */
    @NotNull
    Set<BaseCommand> getSubCommands();

    /**
     * Registers a subcommand to this command.
     * @param sub the subcommand to register.
     */
    void registerSubCommand(@NotNull BaseCommand sub);

    /**
     * Retrieves a subcommand by its name or alias.
     *
     * @param name the name or alias of the subcommand to retrieve.
     * @return the corresponding subcommand if found, otherwise null.
     */
    @Nullable
    default BaseCommand getSubCommand(String name) {
        if (name == null || name.isEmpty())
            return null;

        for (BaseCommand command : getSubCommands()) {
            Set<String> names = new HashSet<>(command.getAliases());
            names.add(command.getName());

            if (names.contains(name)) return command;
        }

        return null;
    }

    /**
     * Gets the wildcard permission for this command, if any subcommands exist.
     * @return the wildcard permission, or null if there are no subcommands.
     */
    @Nullable
    default String getWildcardPermission() {
        return getSubCommands().isEmpty() ? null : getPermission() + ".*";
    }

    /**
     * Checks if this command is equal to another object.
     *
     * @param o the object to compare to.
     * @return true if the object is equal to this command, false otherwise.
     */
    @Override
    boolean equals(Object o);

    /**
     * Returns a string representation of this command.
     * @return string representation of this command.
     */
    @Override
    String toString();
}
