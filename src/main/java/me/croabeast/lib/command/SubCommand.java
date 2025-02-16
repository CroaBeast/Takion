package me.croabeast.lib.command;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * A class representing a subcommand that is part of a parent {@link Command}.
 */
@Getter
public class SubCommand implements BaseCommand {

    private final String name;
    private final String permission;

    private final Command parent;
    private final List<String> aliases = new ArrayList<>();

    @Setter
    private Executable executable = null;

    /**
     * Constructs a new {@code SubCommand} with the specified parent command and name.
     *
     * @param parent the parent command of this subcommand.
     * @param name the name of this subcommand, which can include aliases separated by semicolons.
     *
     * @throws NullPointerException if the parent command or name is null.
     */
    public SubCommand(Command parent, String name) {
        this.parent = Objects.requireNonNull(parent, "Parent cannot be null");

        if (StringUtils.isBlank(name))
            throw new NullPointerException("Name is empty");

        List<String> list = new ArrayList<>(Arrays.asList(name.split(";")));

        this.name = list.get(0);
        this.permission = parent.getPermission() + '.' + this.name;

        if (list.size() == 1) return;
        for (int i = 1; i < list.size(); i++) aliases.add(list.get(i));
    }

    /**
     * Checks if the provided {@link CommandSender} has permission to execute this subcommand.
     * <p> This method checks both the specific permission for this subcommand and the wildcard permission of the parent command.
     *
     * @param sender the command sender to check permissions for.
     * @param log whether to log the permission check result.
     *
     * @return {@code true} if the sender has permission, {@code false} otherwise.
     */
    @Override
    public boolean isPermitted(CommandSender sender, boolean log) {
        final BiPredicate<CommandSender, String> checker = DefaultPermissible.DEFAULT_CHECKER;
        return checker.test(sender, parent.getWildcardPermission()) || checker.test(sender, permission);
    }

    /**
     * Retrieves the executable logic associated with this command.
     *
     * @return the executable logic.
     * @throws NullPointerException if the executable action is not set.
     */
    @NotNull
    public Executable getExecutable() {
        return Objects.requireNonNull(executable, "Executable action is not set");
    }
}
