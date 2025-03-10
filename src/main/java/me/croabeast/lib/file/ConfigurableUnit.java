package me.croabeast.lib.file;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a configuration unit used for handling permissions and groups in a configuration section.
 */
public interface ConfigurableUnit {

    /**
     * Gets the configuration section associated with this unit.
     *
     * @return The configuration section.
     * @throws NullPointerException If the configuration section is null.
     */
    @NotNull ConfigurationSection getSection() throws NullPointerException;

    /**
     * Gets the name of the configuration section.
     * @return The name of the configuration section.
     */
    @NotNull
    default String getName() {
        return getSection().getName();
    }

    /**
     * Gets the permission associated with this unit.
     * @return The permission string. If not specified, returns "DEFAULT".
     */
    @NotNull
    default String getPermission() {
        return getSection().getString("permission", "DEFAULT");
    }

    /**
     * Checks if the given command sender has the permission associated with this unit.
     * @param sender The command sender.
     * @return True if the sender has the permission, false otherwise.
     */
    default boolean hasPerm(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }

    /**
     * Gets the group associated with this unit.
     * @return The group string, or null if not specified.
     */
    @Nullable
    default String getGroup() {
        return getSection().getString("group");
    }

    /**
     * Checks if the given command sender is in the group associated with this unit.
     *
     * @param sender The command sender.
     * @return True if the sender is in the group, false otherwise.
     */
    boolean isInGroup(CommandSender sender);

    /**
     * Checks if the group associated with this unit is not null and the given sender is in that group.
     *
     * @param sender The command sender.
     * @return True if the group is not null and the sender is in that group, false otherwise.
     */
    default boolean isInGroupNonNull(CommandSender sender) {
        return StringUtils.isNotBlank(getGroup()) && isInGroup(sender);
    }

    /**
     * Checks if the group associated with this unit is null or the given sender is in that group.
     *
     * @param sender The command sender.
     * @return True if the group is null or the sender is in that group, false otherwise.
     */
    default boolean isInGroupAsNull(CommandSender sender) {
        return StringUtils.isBlank(getGroup()) || isInGroup(sender);
    }

    /**
     * Gets the priority associated with this unit.
     * @return The priority value. If not specified, returns 0 for "DEFAULT" permission, and 1 otherwise.
     */
    default int getPriority() {
        int def = getPermission().matches("(?i)DEFAULT") ? 0 : 1;
        return getSection().getInt("priority", def);
    }

    /**
     * Creates a new ConfigUnit instance based on the provided configuration section.
     *
     * @param section The configuration section.
     *
     * @return A new ConfigUnit instance.
     * @throws NullPointerException If the configuration section is null.
     */
    static ConfigurableUnit of(ConfigurationSection section) {
        return new ConfigurableUnit() {
            @NotNull
            public ConfigurationSection getSection() {
                return section;
            }

            @Override
            public boolean isInGroup(CommandSender sender) {
                return true;
            }
        };
    }

    /**
     * Creates a new ConfigUnit instance based on the provided ConfigUnit instance.
     *
     * @param unit The ConfigUnit instance.
     * @return A new ConfigUnit instance.
     */
    static ConfigurableUnit of(ConfigurableUnit unit) {
        return of(unit.getSection());
    }
}
