package me.croabeast.command;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A builder class for creating and customizing {@link BukkitCommand} instances.
 *
 * <p> This class provides a fluent API for setting various properties and behaviors of a command,
 * including its executable logic and tab completion suggestions.
 */
@Getter
public final class CommandBuilder extends BukkitCommand {

    private boolean enabled = true, overriding = true;

    @Getter(AccessLevel.NONE)
    private BiFunction<CommandSender, String[], List<String>> completions;
    @Getter(AccessLevel.NONE)
    private Supplier<TabBuilder> builder = null;

    private CommandBuilder(Plugin plugin, String name) {
        super(plugin, name);
    }

    /**
     * Sets whether this command should override existing commands with the same name.
     *
     * @param override true if the command should override existing commands, false otherwise.
     * @return the current CommandBuilder instance.
     */
    @NotNull
    public CommandBuilder setOverriding(boolean override) {
        overriding = override;
        return this;
    }

    /**
     * Sets the completions list for this command using a function.
     *
     * @param function the function for the completions list.
     * @return the current CommandBuilder instance.
     */
    @NotNull
    public CommandBuilder setCompletions(BiFunction<CommandSender, String[], List<String>> function) {
        this.completions = function;
        return this;
    }

    /**
     * Sets the completions list for this command.
     *
     * @param completions the completions list.
     * @return the current CommandBuilder instance.
     */
    @NotNull
    public CommandBuilder setCompletions(List<String> completions) {
        this.completions = (s, a) -> completions;
        return this;
    }

    /**
     * Sets the tab completion builder for this command using a supplier.
     *
     * @param builder the supplier for the tab completion builder.
     * @return the current CommandBuilder instance.
     */
    @NotNull
    public CommandBuilder setCompletionBuilder(Supplier<TabBuilder> builder) {
        this.builder = builder;
        return this;
    }

    /**
     * Sets the tab completion builder for this command.
     *
     * @param builder the tab completion builder to set.
     * @return the current CommandBuilder instance.
     */
    @NotNull
    public CommandBuilder setCompletionBuilder(TabBuilder builder) {
        this.builder = () -> builder;
        return this;
    }

    /**
     * Applies a consumer function to this command.
     *
     * @param consumer the consumer to apply.
     * @return the current CommandBuilder instance.
     */
    @NotNull
    public CommandBuilder apply(@NotNull Consumer<BukkitCommand> consumer) {
        Objects.requireNonNull(consumer).accept(this);
        return this;
    }

    /**
     * Unsupported operation: the name of a command cannot be changed after it is created.
     *
     * @param name the new name.
     * @throws UnsupportedOperationException always.
     */
    @Override
    public boolean setName(@NotNull String name) {
        throw new UnsupportedOperationException("Name can not be changed");
    }

    /**
     * Unsupported operation: the label of a command cannot be changed after it is created.
     *
     * @param label the new label.
     * @throws UnsupportedOperationException always.
     */
    @Override
    public boolean setLabel(@NotNull String label) {
        throw new UnsupportedOperationException("Label can not be changed");
    }

    @NotNull
    public Supplier<Collection<String>> generateCompletions(CommandSender sender, String[] arguments) {
        return () -> completions.apply(sender, arguments);
    }

    @Override
    public TabBuilder getCompletionBuilder() {
        return builder == null ? null : builder.get();
    }

    /**
     * Registers the command with the Bukkit command map.
     * <p> If the command is already registered, this method does nothing.
     *
     * @return {@code true} if the command was successfully registered, {@code false} otherwise.
     */
    @Override
    public boolean register() {
        if (!enabled) enabled = true;
        return super.register();
    }

    /**
     * Unregisters the command from the Bukkit command map.
     * <p> If the command is not registered, this method does nothing.
     *
     * @return {@code true} if the command was successfully unregistered, {@code false} otherwise.
     */
    @Override
    public boolean unregister() {
        if (enabled) enabled = false;
        return super.unregister();
    }

    /**
     * Creates a new CommandBuilder instance for the specified plugin and command name.
     *
     * @param plugin the plugin that owns the command.
     * @param name   the name of the command.
     *
     * @return a new CommandBuilder instance.
     */
    public static CommandBuilder from(Plugin plugin, String name) {
        return new CommandBuilder(plugin, name);
    }
}
