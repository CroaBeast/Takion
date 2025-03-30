package me.croabeast.takion;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

/**
 * An abstraction for interfacing with various permission and chat systems,
 * typically provided by plugins like Vault, LuckPerms, and others.
 * <p>
 * The {@code VaultHolder} interface encapsulates a source object (e.g., a permission provider)
 * and exposes a unified API to access common functionalities such as:
 * <ul>
 *   <li>Retrieving the underlying source instance</li>
 *   <li>Getting the host plugin</li>
 *   <li>Querying group memberships, prefixes, and suffixes for players</li>
 *   <li>Fetching available groups in the system</li>
 * </ul>
 * </p>
 *
 * @param <T> The type of the underlying source (e.g., Chat, LuckPerms).
 */
public interface VaultHolder<T> {

    /**
     * Retrieves the underlying source object that provides the core functionality.
     * This could be an instance from a permission or chat plugin.
     *
     * @return the underlying source instance
     */
    @NotNull
    T getSource();

    /**
     * Returns the plugin associated with this holder.
     * <p>
     * Typically, this is the plugin that supplies the underlying source object.
     * </p>
     *
     * @return the associated {@link Plugin} or {@code null} if unavailable
     */
    Plugin getPlugin();

    /**
     * Checks whether the underlying source is currently enabled.
     * <p>
     * This can be used to determine if the related service is active before invoking any operations.
     * </p>
     *
     * @return {@code true} if the service is enabled; {@code false} otherwise
     */
    boolean isEnabled();

    /**
     * Applies a transformation function to the underlying source.
     * <p>
     * This is a convenience method that allows for fluent extraction of data
     * from the underlying source object.
     * </p>
     *
     * @param <V>      the type of the result
     * @param function the function to apply to the source
     * @return the result of applying the function to the source
     */
    default <V> V fromSource(Function<T, V> function) {
        return function.apply(getSource());
    }

    /**
     * Retrieves the primary group of the specified player.
     * <p>
     * Depending on the underlying system, this may return the most important group a player is assigned to.
     * </p>
     *
     * @param player the player whose primary group is requested
     * @return the primary group name, or {@code null} if unavailable
     */
    @Nullable
    String getPrimaryGroup(Player player);

    /**
     * Checks if the specified player is a member of a given group.
     * <p>
     * This method abstracts the logic for determining group membership,
     * which may vary between different permission systems.
     * </p>
     *
     * @param player the player to check
     * @param group  the name of the group
     * @return {@code true} if the player is in the group; {@code false} otherwise
     */
    boolean isInGroup(Player player, String group);

    /**
     * Retrieves all groups the specified player belongs to.
     *
     * @param player the player for whom to retrieve group memberships
     * @return a list of group names associated with the player
     */
    @NotNull
    List<String> getGroups(Player player);

    /**
     * Retrieves the prefix of the specified player.
     * <p>
     * The prefix is often used to display a player's rank or role before their name.
     * </p>
     *
     * @param player the player whose prefix is requested
     * @return the prefix string, or {@code null} if unavailable
     */
    @Nullable
    String getPrefix(Player player);

    /**
     * Retrieves the suffix of the specified player.
     * <p>
     * The suffix is commonly used to show additional information, such as a team or status indicator.
     * </p>
     *
     * @param player the player whose suffix is requested
     * @return the suffix string, or {@code null} if unavailable
     */
    @Nullable
    String getSuffix(Player player);

    /**
     * Retrieves the group prefix for the specified group in the given world.
     * <p>
     * Group prefixes are used to represent the group in chat or on display.
     * </p>
     *
     * @param world the world context (can be {@code null})
     * @param group the group name
     * @return the group prefix, or {@code null} if not defined
     */
    @Nullable
    String getGroupPrefix(World world, String group);

    /**
     * A convenience method to retrieve the group prefix without specifying a world.
     *
     * @param group the group name
     * @return the group prefix, or {@code null} if not defined
     */
    @Nullable
    default String getGroupPrefix(String group) {
        return getGroupPrefix(null, group);
    }

    /**
     * Retrieves the group suffix for the specified group in the given world.
     * <p>
     * Group suffixes can be used to display additional group-related information.
     * </p>
     *
     * @param world the world context (can be {@code null})
     * @param group the group name
     * @return the group suffix, or {@code null} if not defined
     */
    @Nullable
    String getGroupSuffix(World world, String group);

    /**
     * A convenience method to retrieve the group suffix without specifying a world.
     *
     * @param group the group name
     * @return the group suffix, or {@code null} if not defined
     */
    @Nullable
    default String getGroupSuffix(String group) {
        return getGroupSuffix(null, group);
    }

    /**
     * Retrieves all available groups from the underlying source.
     * <p>
     * This method returns a comprehensive list of all group names, which can be used for
     * administrative or display purposes.
     * </p>
     *
     * @return a list of all group names
     */
    @NotNull
    List<String> getGroups();
}
