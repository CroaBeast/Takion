package me.croabeast.takion;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.croabeast.common.CollectionBuilder;
import me.croabeast.common.util.ArrayUtils;
import me.croabeast.common.util.Exceptions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.milkbowl.vault.chat.Chat;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A utility class for loading and providing a VaultHolder instance.
 * <p>
 * {@code HolderUtils} serves as a central point to load the appropriate integration
 * with permission/chat APIs such as Vault and LuckPerms. It defines three nested classes:
 * <ul>
 *     <li>{@link BasicHolder} – integrates with Vault's Chat API.</li>
 *     <li>{@link LuckHolder} – integrates with LuckPerms.</li>
 *     <li>{@link NoHolder} – a fallback implementation when neither Vault nor LuckPerms is available.</li>
 * </ul>
 *
 * @see VaultHolder
 * @see net.milkbowl.vault.chat.Chat
 * @see LuckPerms
 */
@UtilityClass
class HolderUtils {

    /**
     * A VaultHolder implementation that integrates with Vault's Chat API.
     * <p>
     * The {@code BasicHolder} retrieves the Chat provider from Bukkit's ServicesManager.
     * It provides access to various chat-related methods, such as obtaining the primary group,
     * prefix, suffix, and group-related information for players.
     * </p>
     */
    @Getter
    static final class BasicHolder implements VaultHolder<Chat> {

        /**
         * The Vault Chat API provider.
         */
        private final Chat source;

        /**
         * Constructs a new {@code BasicHolder}.
         * <p>
         * Retrieves the Chat provider from Bukkit's ServicesManager. If no provider is found,
         * an {@link NullPointerException} is thrown.
         * </p>
         */
        BasicHolder() {
            source = Objects.requireNonNull(Bukkit.getServer().getServicesManager().getRegistration(Chat.class)).getProvider();
        }

        /**
         * Returns the plugin associated with the Chat provider.
         *
         * @return the {@link Plugin} whose name matches the Chat provider's name
         */
        public Plugin getPlugin() {
            return Bukkit.getPluginManager().getPlugin(source.getName());
        }

        /**
         * Checks if the Chat provider is enabled.
         *
         * @return {@code true} if the Chat provider is enabled; {@code false} otherwise
         */
        @Override
        public boolean isEnabled() {
            return source.isEnabled();
        }

        /**
         * Retrieves the primary group for the specified player.
         *
         * @param player the player whose primary group is requested
         * @return the primary group as a {@link String}, or {@code null} if not available
         */
        @Nullable
        public String getPrimaryGroup(Player player) {
            return source.getPrimaryGroup(player);
        }

        /**
         * Checks if the player belongs to the specified group.
         *
         * @param player the player to check
         * @param group  the group name to check for membership
         * @return {@code true} if the player is in the group; {@code false} otherwise
         */
        @Override
        public boolean isInGroup(Player player, String group) {
            return source.playerInGroup(player, group);
        }

        /**
         * Retrieves a list of groups that the specified player belongs to.
         *
         * @param player the player whose groups are requested
         * @return a {@link List} of group names
         */
        @NotNull
        public List<String> getGroups(Player player) {
            return ArrayUtils.toList(source.getPlayerGroups(player));
        }

        /**
         * Retrieves the prefix for the specified player.
         *
         * @param player the player whose prefix is requested
         * @return the player's prefix as a {@link String}, or {@code null} if not set
         */
        @Nullable
        public String getPrefix(Player player) {
            return source.getPlayerPrefix(player);
        }

        /**
         * Retrieves the suffix for the specified player.
         *
         * @param player the player whose suffix is requested
         * @return the player's suffix as a {@link String}, or {@code null} if not set
         */
        @Nullable
        public String getSuffix(Player player) {
            return source.getPlayerSuffix(player);
        }

        /**
         * Retrieves the prefix for the specified group in the given world.
         *
         * @param world the world context
         * @param group the group name
         * @return the group's prefix as a {@link String}, or {@code null} if not set
         */
        @Nullable
        public String getGroupPrefix(World world, String group) {
            return source.getGroupPrefix(world, group);
        }

        /**
         * Retrieves the suffix for the specified group in the given world.
         *
         * @param world the world context
         * @param group the group name
         * @return the group's suffix as a {@link String}, or {@code null} if not set
         */
        @Nullable
        public String getGroupSuffix(World world, String group) {
            return source.getGroupSuffix(world, group);
        }

        /**
         * Retrieves a list of all groups known to the Chat provider.
         *
         * @return a {@link List} of group names
         */
        @NotNull
        public List<String> getGroups() {
            return ArrayUtils.toList(source.getGroups());
        }

        /**
         * Returns a string representation of this {@code BasicHolder}.
         *
         * @return a descriptive string including the provider name and associated plugin name
         */
        @Override
        public String toString() {
            return "VaultHolder{provider='VaultAPI', plugin='" + source.getName() + "'}";
        }
    }

    /**
     * A VaultHolder implementation that integrates with LuckPerms.
     * <p>
     * {@code LuckHolder} retrieves the LuckPerms provider from Bukkit's ServicesManager and provides
     * methods to access group and metadata information from LuckPerms. It utilizes LuckPerms' API to fetch
     * user and group data, and applies permission checks based on player permissions.
     * </p>
     */
    @Getter
    static final class LuckHolder implements VaultHolder<LuckPerms> {

        /**
         * The LuckPerms provider instance.
         */
        private final LuckPerms source;

        /**
         * Constructs a new {@code LuckHolder} instance.
         * <p>
         * Retrieves the LuckPerms provider from Bukkit's ServicesManager. An exception is thrown if the provider
         * is not found.
         * </p>
         */
        LuckHolder() {
            source = Objects.requireNonNull(Bukkit.getServer().getServicesManager().getRegistration(LuckPerms.class)).getProvider();
        }

        /**
         * Returns the plugin associated with LuckPerms.
         *
         * @return the {@link Plugin} for LuckPerms (by name "LuckPerms")
         */
        @Override
        public Plugin getPlugin() {
            return Bukkit.getPluginManager().getPlugin("LuckPerms");
        }

        /**
         * Checks if LuckPerms is enabled.
         *
         * @return {@code true} if LuckPerms is enabled; {@code false} otherwise
         */
        @Override
        public boolean isEnabled() {
            return Exceptions.isPluginEnabled("LuckPerms");
        }

        /**
         * Retrieves a value for a user by applying the provided function to the LuckPerms {@link User} object.
         *
         * @param <T>      the type of the value to retrieve
         * @param player   the player for whom to retrieve the value
         * @param function a function that extracts a value from a {@link User}
         * @return the extracted value, or {@code null} if the user is not found
         */
        <T> T getAsUser(Player player, Function<User, T> function) {
            User user = source.getUserManager().getUser(player.getUniqueId());
            return user != null ? function.apply(user) : null;
        }

        /**
         * Retrieves the primary group for the specified player from LuckPerms.
         *
         * @param player the player whose primary group is requested
         * @return the primary group as a {@link String}, or {@code null} if not available
         */
        @Nullable
        public String getPrimaryGroup(Player player) {
            return getAsUser(player, User::getPrimaryGroup);
        }

        /**
         * Checks if the player has the specified group permission.
         * <p>
         * This implementation uses the player's permissions (prefixed with "group.") as a simple check.
         * </p>
         *
         * @param player the player to check
         * @param group  the group name
         * @return {@code true} if the player has the group permission; {@code false} otherwise
         */
        @Override
        public boolean isInGroup(Player player, String group) {
            return player.hasPermission("group." + group);
        }

        /**
         * Retrieves a list of group names for the specified player by filtering LuckPerms nodes.
         *
         * @param player the player whose groups are to be retrieved
         * @return a {@link List} of group names
         */
        @NotNull
        public List<String> getGroups(Player player) {
            CollectionBuilder<Node> builder = getAsUser(player, u -> CollectionBuilder.of(u.getNodes()));
            return builder != null ?
                    builder.filter(n -> n instanceof InheritanceNode)
                            .map(n -> ((InheritanceNode) n).getGroupName())
                            .toList() :
                    new ArrayList<>();
        }

        /**
         * Retrieves the prefix for the specified player from LuckPerms.
         *
         * @param player the player whose prefix is requested
         * @return the prefix as a {@link String}, or {@code null} if not available
         */
        @Nullable
        public String getPrefix(Player player) {
            return getAsUser(player, u -> u.getCachedData().getMetaData().getPrefix());
        }

        /**
         * Retrieves the suffix for the specified player from LuckPerms.
         *
         * @param player the player whose suffix is requested
         * @return the suffix as a {@link String}, or {@code null} if not available
         */
        @Nullable
        public String getSuffix(Player player) {
            return getAsUser(player, u -> u.getCachedData().getMetaData().getSuffix());
        }

        /**
         * Retrieves the prefix for the specified group from LuckPerms.
         *
         * @param group the group name
         * @return the group's prefix as a {@link String}, or {@code null} if not available
         */
        @Nullable
        public String getGroupPrefix(String group) {
            Group g = source.getGroupManager().getGroup(group);
            return g != null ? g.getCachedData().getMetaData().getPrefix() : null;
        }

        /**
         * Retrieves the group prefix for the specified group in the given world.
         * <p>
         * This method delegates to {@link #getGroupPrefix(String)}.
         * </p>
         *
         * @param world the world context (ignored in this implementation)
         * @param group the group name
         * @return the group's prefix as a {@link String}, or {@code null} if not available
         */
        @Nullable
        public String getGroupPrefix(World world, String group) {
            return getGroupPrefix(group);
        }

        /**
         * Retrieves the suffix for the specified group from LuckPerms.
         *
         * @param group the group name
         * @return the group's suffix as a {@link String}, or {@code null} if not available
         */
        @Nullable
        public String getGroupSuffix(String group) {
            Group g = source.getGroupManager().getGroup(group);
            return g != null ? g.getCachedData().getMetaData().getSuffix() : null;
        }

        /**
         * Retrieves the group suffix for the specified group in the given world.
         * <p>
         * This method delegates to {@link #getGroupSuffix(String)}.
         * </p>
         *
         * @param world the world context (ignored in this implementation)
         * @param group the group name
         * @return the group's suffix as a {@link String}, or {@code null} if not available
         */
        @Nullable
        public String getGroupSuffix(World world, String group) {
            return getGroupSuffix(group);
        }

        /**
         * Retrieves a list of all group names loaded by LuckPerms.
         *
         * @return a {@link List} of group names
         */
        @NotNull
        public List<String> getGroups() {
            return CollectionBuilder
                    .of(source.getGroupManager().getLoadedGroups())
                    .map(Group::getName).toList();
        }

        /**
         * Returns a string representation of this {@code LuckHolder}.
         *
         * @return a descriptive string including the provider name and LuckPerms version
         */
        @Override
        public String toString() {
            return "VaultHolder{provider='LuckPerms', version=" + getPlugin().getDescription().getVersion() + "}";
        }
    }

    /**
     * A fallback VaultHolder implementation used when no valid permission/chat provider is found.
     * <p>
     * The {@code NoHolder} throws exceptions or returns default values for all operations,
     * indicating that no underlying source is available.
     * </p>
     */
    static final class NoHolder implements VaultHolder<Object> {

        /**
         * Always throws an IllegalStateException as there is no underlying source.
         *
         * @return never returns normally
         * @throws IllegalStateException always thrown
         */
        @NotNull
        public Object getSource() {
            throw new IllegalStateException("No source was found");
        }

        /**
         * Returns {@code null} since there is no associated plugin.
         *
         * @return {@code null}
         */
        @Override
        public Plugin getPlugin() {
            return null;
        }

        /**
         * Indicates that this holder is not enabled.
         *
         * @return {@code false}
         */
        @Override
        public boolean isEnabled() {
            return false;
        }

        /**
         * Always throws an IllegalStateException as there is no underlying source to apply the function.
         *
         * @param function the function to apply
         * @param <V>      the expected return type
         * @return never returns normally
         * @throws IllegalStateException always thrown
         */
        @Override
        public <V> V fromSource(Function<Object, V> function) {
            throw new IllegalStateException("No source was found");
        }

        /**
         * Returns {@code null} as there is no primary group.
         *
         * @param player the player
         * @return {@code null}
         */
        @Nullable
        public String getPrimaryGroup(Player player) {
            return null;
        }

        /**
         * Indicates that the player is not in any group.
         *
         * @param player the player
         * @param group  the group name
         * @return {@code false}
         */
        @Override
        public boolean isInGroup(Player player, String group) {
            return false;
        }

        /**
         * Returns an empty list of groups for the specified player.
         *
         * @param player the player
         * @return an empty {@link ArrayList}
         */
        @NotNull
        public List<String> getGroups(Player player) {
            return new ArrayList<>();
        }

        /**
         * Returns {@code null} since there is no prefix available.
         *
         * @param player the player
         * @return {@code null}
         */
        @Nullable
        public String getPrefix(Player player) {
            return null;
        }

        /**
         * Returns {@code null} since there is no suffix available.
         *
         * @param player the player
         * @return {@code null}
         */
        @Nullable
        public String getSuffix(Player player) {
            return null;
        }

        /**
         * Returns {@code null} as there is no group prefix.
         *
         * @param world the world context
         * @param group the group name
         * @return {@code null}
         */
        @Nullable
        public String getGroupPrefix(World world, String group) {
            return null;
        }

        /**
         * Returns {@code null} as there is no group suffix.
         *
         * @param world the world context
         * @param group the group name
         * @return {@code null}
         */
        @Nullable
        public String getGroupSuffix(World world, String group) {
            return null;
        }

        /**
         * Returns an empty list since no groups are available.
         *
         * @return an empty {@link ArrayList}
         */
        @Override
        public @NotNull List<String> getGroups() {
            return new ArrayList<>();
        }

        /**
         * Returns a string representation indicating that no provider was found.
         *
         * @return a descriptive string for the fallback holder
         */
        @Override
        public String toString() {
            return "VaultHolder{provider='NONE'}";
        }
    }
}
