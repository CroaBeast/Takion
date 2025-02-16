package me.croabeast.lib.command;

import org.bukkit.command.CommandSender;

/**
 * An interface representing a permissible action or command.
 */
public interface Permissible {

    /**
     * Gets the permission node associated with this action or command.
     * @return the permission node as a {@link String}.
     */
    String getPermission();

    /**
     * Checks if the provided {@link CommandSender} has permission to execute the action or command.
     *
     * @param sender the command sender to check permissions for.
     * @param log whether to log the permission check result.
     *
     * @return {@code true} if the sender has permission, {@code false} otherwise.
     */
    boolean isPermitted(CommandSender sender, boolean log);

    /**
     * Checks if the provided {@link CommandSender} has permission to execute the action or command.
     * <p> This method logs the permission check result by default.
     *
     * @param sender the command sender to check permissions for.
     * @return {@code true} if the sender has permission, {@code false} otherwise.
     */
    default boolean isPermitted(CommandSender sender) {
        return isPermitted(sender, true);
    }
}