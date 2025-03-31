package me.croabeast.file;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a configuration unit used for handling permissions, groups, and priorities
 * in a configuration section.
 * <p>
 * A {@code ConfigurableUnit} provides methods to access the underlying {@link ConfigurationSection},
 * retrieve its name, permission, group, and priority settings, and perform permission and group checks
 * on a {@link CommandSender}. It serves as a basic unit for representing configuration-based access control.
 * </p>
 * <p>
 * The interface also includes static factory methods to create new instances based on an existing
 * {@link ConfigurationSection} or another {@code ConfigurableUnit}.
 * </p>
 *
 * @see ConfigurationSection
 * @see CommandSender
 */
public interface ConfigurableUnit {

    /**
     * Gets the configuration section associated with this unit.
     *
     * @return the configuration section (never {@code null}).
     * @throws NullPointerException if the configuration section is null.
     */
    @NotNull
    ConfigurationSection getSection() throws NullPointerException;

    /**
     * Gets the name of the configuration section.
     *
     * @return the name of the configuration section.
     */
    @NotNull
    default String getName() {
        return getSection().getName();
    }

    /**
     * Gets the permission associated with this unit.
     *
     * @return the permission string; if not specified, returns "DEFAULT".
     */
    @NotNull
    default String getPermission() {
        return getSection().getString("permission", "DEFAULT");
    }

    /**
     * Checks if the given command sender has the permission associated with this unit.
     *
     * @param sender the command sender.
     * @return {@code true} if the sender has the permission, {@code false} otherwise.
     */
    default boolean hasPerm(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }

    /**
     * Gets the group associated with this unit.
     *
     * @return the group string, or {@code null} if not specified.
     */
    @Nullable
    default String getGroup() {
        return getSection().getString("group");
    }

    /**
     * Checks if the given command sender is in the group associated with this unit.
     *
     * @param sender the command sender.
     * @return {@code true} if the sender is in the group, {@code false} otherwise.
     */
    boolean isInGroup(CommandSender sender);

    /**
     * Checks if the group associated with this unit is not blank and the given sender is in that group.
     *
     * @param sender the command sender.
     * @return {@code true} if the group is not blank and the sender is in that group, {@code false} otherwise.
     */
    default boolean isInGroupNonNull(CommandSender sender) {
        return StringUtils.isNotBlank(getGroup()) && isInGroup(sender);
    }

    /**
     * Checks if the group associated with this unit is blank or the given sender is in that group.
     *
     * @param sender the command sender.
     * @return {@code true} if the group is blank or the sender is in that group, {@code false} otherwise.
     */
    default boolean isInGroupAsNull(CommandSender sender) {
        return StringUtils.isBlank(getGroup()) || isInGroup(sender);
    }

    /**
     * Gets the priority associated with this unit.
     * <p>
     * The priority is determined by the "priority" value in the configuration section. If not specified,
     * it returns 0 when the permission is "DEFAULT" (case-insensitive), and 1 otherwise.
     * </p>
     *
     * @return the priority value.
     */
    default int getPriority() {
        int def = getPermission().matches("(?i)DEFAULT") ? 0 : 1;
        return getSection().getInt("priority", def);
    }

    /**
     * Creates a new {@code ConfigurableUnit} instance based on the provided configuration section.
     * <p>
     * This factory method returns a simple implementation of {@code ConfigurableUnit} where the group check
     * always returns {@code true}.
     * </p>
     *
     * @param section the configuration section.
     * @return a new {@code ConfigurableUnit} instance.
     * @throws NullPointerException if the configuration section is null.
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
     * Creates a new {@code ConfigurableUnit} instance based on the provided {@code ConfigurableUnit}.
     *
     * @param unit the existing {@code ConfigurableUnit} instance.
     * @return a new {@code ConfigurableUnit} instance based on the unit's configuration section.
     */
    static ConfigurableUnit of(ConfigurableUnit unit) {
        return of(unit.getSection());
    }
}
