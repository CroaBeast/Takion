package me.croabeast.lib.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.croabeast.lib.reflect.Craft;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiPredicate;

/**
 * Represents a custom Bukkit command that extends the functionality of the default Bukkit command.
 * <p> This abstract class provides additional features such as subcommands management, permissions handling, and error handling.
 */
@Accessors(chain = true)
public abstract class BukkitCommand extends org.bukkit.command.defaults.BukkitCommand implements Command, DefaultPermissible {

    @Getter
    private final NamespacedKey key;
    @Getter
    private final Plugin plugin;

    @Getter
    private boolean registered = false;
    final Set<BaseCommand> subCommands = new LinkedHashSet<>();
    /**
     * The executable logic associated with this command.
     */
    @Setter
    private Executable executable = null;

    @Getter(AccessLevel.NONE)
    @Setter @NotNull
    BiPredicate<CommandSender, Throwable> executingError, completingError;
    @Getter(AccessLevel.NONE)
    private Entry loadedCommand;

    @Getter(AccessLevel.NONE)
    @Setter @NotNull
    BiPredicate<CommandSender, String> wrongArgumentAction;

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Entry {
        @NotNull
        private final Plugin plugin;
        @NotNull
        private final org.bukkit.command.Command command;
    }

    protected BukkitCommand(Plugin plugin, String name) {
        super(name);
        this.plugin = Objects.requireNonNull(plugin);

        final UUID uuid = UUID.randomUUID();
        key = new NamespacedKey(plugin, uuid.toString());

        executingError = (s, e) -> {
            s.sendMessage(plugin.getName() + " Error executing the command " + getName());
            e.printStackTrace();
            return true;
        };

        completingError = (s, e) -> {
            s.sendMessage(plugin.getName() + " Error completing the command " + getName());
            e.printStackTrace();
            return true;
        };

        wrongArgumentAction = (s, e) -> true;
    }

    public boolean isWrongArgument(CommandSender sender, String arg) {
        return wrongArgumentAction.test(sender, arg);
    }

    @Override
    public boolean testPermissionSilent(@NotNull CommandSender target) {
        return isPermitted(target, false);
    }

    @Override
    public boolean testPermission(@NotNull CommandSender target) {
        return isPermitted(target);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        boolean success;

        if (!subCommands.isEmpty() && args.length > 0) {
            BaseCommand sub = getSubCommand(args[0]);
            if (sub == null)
                return wrongArgumentAction.test(sender, args[0]);

            int last = args.length - 1;

            if (sub.isPermitted(sender)) {
                final String[] newArgs = new String[last];

                if (args.length > 1)
                    System.arraycopy(args, 1, newArgs, 0, last);

                try {
                    success = sub.getExecutable()
                            .executeAction(sender, newArgs)
                            .asBoolean();
                } catch (Throwable e) {
                    success = executingError.test(sender, e);
                }

                return success;
            }
        }

        try {
            success = getExecutable()
                    .executeAction(sender, args)
                    .asBoolean();
        } catch (Throwable e) {
            success = executingError.test(sender, e);
        }

        return success;
    }

    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
        try {
            Collection<String> before = generateCompletions(sender, args).get();
            final TabBuilder builder = getCompletionBuilder();

            List<String> comps = builder != null && !builder.isEmpty() ?
                    builder.build(sender, args) :
                    before == null ? null : new ArrayList<>(before);

            return Objects.requireNonNull(comps, "Completions are null");
        }
        catch (Exception e) {
            completingError.test(sender, e);
            return super.tabComplete(sender, alias, args);
        }
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

    /**
     * Retrieves the subcommands of this command as an unmodifiable set.
     * <p> This method creates a copy of the original set of subcommands, ensuring that the returned set cannot be modified.
     *
     * @return an immutable set of subcommands.
     */
    @NotNull
    public Set<BaseCommand> getSubCommands() {
        return Collections.unmodifiableSet(subCommands);
    }

    @Override
    public void registerSubCommand(@NotNull BaseCommand sub) {
        Objects.requireNonNull(sub);

        for (BaseCommand command : subCommands)
            if (command.getName().equals(sub.getName()))
                    return;

        subCommands.add(sub);
    }

    private static void addPerm(String perm) {
        if (StringUtils.isBlank(perm)) return;

        try {
            Permission permission = new Permission(perm);
            Bukkit.getPluginManager().addPermission(permission);
        }
        catch (Exception ignored) {}
    }

    private static void removePerm(String perm) {
        if (!StringUtils.isBlank(perm))
            Bukkit.getPluginManager().removePermission(perm);
    }

    private static void loadCommandPermissions(BukkitCommand cmd, boolean loaded) {
        Set<BaseCommand> subCommands = cmd.subCommands;

        if (loaded) {
            removePerm(cmd.getPermission());

            if (!subCommands.isEmpty()) {
                subCommands.forEach(s -> removePerm(s.getPermission()));
                removePerm(cmd.getWildcardPermission());
            }
            return;
        }

        addPerm(cmd.getPermission());
        if (subCommands.isEmpty()) return;

        subCommands.forEach(s -> addPerm(s.getPermission()));
        addPerm(cmd.getWildcardPermission());
    }

    /**
     * Registers the command with the Bukkit command map.
     * <p> If the command is already registered or not enabled, this method does nothing.
     *
     * @return {@code true} if the command was successfully registered, {@code false} otherwise.
     */
    @Override
    public boolean register() {
        if (registered || !isEnabled()) return false;

        SimpleCommandMap map;
        Map<String, org.bukkit.command.Command> commands;

        try {
            map = Craft.Server.getCommandMap();
            commands = Craft.Server.getKnownCommands();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        String p = plugin.getName().toLowerCase();
        final String name = getName();

        org.bukkit.command.Command c = map.getCommand(name);

        if (isOverriding() && c != null) {
            commands.values()
                    .removeIf(c1 -> Objects.equals(c1, c));
            c.unregister(map);

            Plugin pl = !(c instanceof PluginIdentifiableCommand) ?
                    null :
                    ((PluginIdentifiableCommand) c).getPlugin();

            if (pl != null)
                loadedCommand = new Entry(pl, c);
        }

        loadCommandPermissions(this, true);
        map.register(p, this);

        return registered = true;
    }

    /**
     * Unregisters the command from the Bukkit command map.
     * <p> If the command is not registered or still enabled, this method does nothing.
     *
     * @return {@code true} if the command was successfully unregistered, {@code false} otherwise.
     */
    @Override
    public boolean unregister() {
        if (!registered || isEnabled()) return false;

        SimpleCommandMap map;
        Map<String, org.bukkit.command.Command> commands;

        try {
            map = Craft.Server.getCommandMap();
            commands = Craft.Server.getKnownCommands();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        org.bukkit.command.Command c = map.getCommand(getName());
        if (!Objects.equals(c, this)) return false;

        commands.values().removeIf(c1 -> Objects.equals(c1, c));

        c.unregister(map);
        loadCommandPermissions(this, false);

        if (loadedCommand != null) {
            map.register(
                    loadedCommand.plugin
                            .getName()
                            .toLowerCase(Locale.ENGLISH),
                    loadedCommand.command
            );
            loadedCommand = null;
        }

        registered = false;
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BukkitCommand)) return false;

        BukkitCommand that = (BukkitCommand) o;
        return Objects.equals(getKey(), that.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getKey());
    }
}
