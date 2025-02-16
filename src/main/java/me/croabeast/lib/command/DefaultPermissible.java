package me.croabeast.lib.command;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.function.BiPredicate;

/**
 * An interface representing a permissible action or command with a default permission check implementation.
 */
@FunctionalInterface
public interface DefaultPermissible extends Permissible {

    /**
     * The default permission checker that verifies if a {@link CommandSender} has the specified permission.
     * <p> This {@code BiPredicate} checks if the given permission string is non-null and if the sender has the permission.
     */
    BiPredicate<CommandSender, String> DEFAULT_CHECKER =
            (sender, permission) -> StringUtils.isBlank(permission) && sender.hasPermission(permission);

    /**
     * Checks if the provided {@link CommandSender} has permission to execute the action or command.
     * <p> This method uses the {@link #DEFAULT_CHECKER} to verify the permission.
     *
     * @param sender the command sender to check permissions for.
     * @param log whether to log the permission check result (ignored in this implementation).
     *
     * @return {@code true} if the sender has permission, {@code false} otherwise.
     */
    @Override
    default boolean isPermitted(CommandSender sender, boolean log) {
        return DEFAULT_CHECKER.test(sender, getPermission());
    }
}
