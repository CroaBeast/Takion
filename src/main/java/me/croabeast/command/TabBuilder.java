package me.croabeast.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * A builder class for constructing tab completion arguments for commands.
 */
public final class TabBuilder {

    private final Map<Integer, Set<TabObject<?>>> map = new LinkedHashMap<>();
    private BiPredicate<CommandSender, String> permPredicate = Permissible::hasPermission;

    private Set<TabObject<?>> fromIndex(int index) {
        return map.getOrDefault(index, new LinkedHashSet<>());
    }

    /**
     * Sets a custom permission predicate for checking permissions.
     *
     * @param predicate the predicate to set.
     * @return this {@link TabBuilder} instance.
     */
    public TabBuilder setPermissionPredicate(BiPredicate<CommandSender, String> predicate) {
        permPredicate = Objects.requireNonNull(predicate);
        return this;
    }

    private TabBuilder addArg0(int index, BiPredicate<CommandSender, String[]> predicate, BiFunction<CommandSender, String[], String> arg) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(arg);

        Set<TabObject<?>> args = fromIndex(index);
        args.add(new TabObject<String>(predicate, arg) {
            @Override
            Class<String> getType() {
                return String.class;
            }
        });

        map.put(index, args);
        return this;
    }

    private TabBuilder addCollectionArg0(int index, BiPredicate<CommandSender, String[]> predicate, BiFunction<CommandSender, String[], Collection<String>> arg) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(arg);

        Set<TabObject<?>> args = fromIndex(index);
        args.add(new TabObject<Collection<String>>(predicate, arg) {
            @Override
            Class<Collection<String>> getType() {
                return null;
            }
        });

        map.put(index, args);
        return this;
    }

    /**
     * Adds a single argument to the tab completion at the specified index.
     *
     * @param index the index to add the argument at.
     * @param predicate the predicate to test.
     * @param argument the argument function.
     *
     * @return this {@link TabBuilder} instance.
     */
    public TabBuilder addArgument(int index, BiPredicate<CommandSender, String[]> predicate, BiFunction<CommandSender, String[], String> argument) {
        return addArg0(index, predicate, argument);
    }

    /**
     * Adds a single argument to the tab completion at the specified index.
     *
     * @param index the index to add the argument at.
     * @param predicate the predicate to test.
     * @param argument the argument string.
     *
     * @return this {@link TabBuilder} instance.
     */
    public TabBuilder addArgument(int index, BiPredicate<CommandSender, String[]> predicate, String argument) {
        return addArg0(index, predicate, (s, a) -> argument);
    }

    /**
     * Adds a single argument to the tab completion at the specified index with a permission check.
     *
     * @param index the index to add the argument at.
     * @param permission the permission node.
     * @param argument the argument function.
     *
     * @return this {@link TabBuilder} instance.
     */
    public TabBuilder addArgument(int index, String permission, BiFunction<CommandSender, String[], String> argument) {
        return addArg0(index, (s, a) -> permPredicate.test(s, permission), argument);
    }

    /**
     * Adds a single argument to the tab completion at the specified index with a permission check.
     *
     * @param index the index to add the argument at.
     * @param permission the permission node.
     * @param argument the argument string.
     *
     * @return this {@link TabBuilder} instance.
     */
    public TabBuilder addArgument(int index, String permission, String argument) {
        return addArgument(index, permission, (s, a) -> argument);
    }

    /**
     * Adds a single argument to the tab completion at the specified index.
     *
     * @param index the index to add the argument at.
     * @param argument the argument function.
     *
     * @return this {@link TabBuilder} instance.
     */
    public TabBuilder addArgument(int index, BiFunction<CommandSender, String[], String> argument) {
        return addArgument(index, (s, a) -> true, argument);
    }

    /**
     * Adds a single argument to the tab completion at the specified index.
     *
     * @param index the index to add the argument at.
     * @param argument the argument string.
     *
     * @return this {@link TabBuilder} instance.
     */
    public TabBuilder addArgument(int index, String argument) {
        return addArgument(index, (s, a) -> argument);
    }

    /**
     * Adds multiple arguments to the tab completion at the specified index.
     *
     * @param index the index to add the arguments at.
     * @param predicate the predicate to test.
     * @param function the arguments function.
     *
     * @return this {@link TabBuilder} instance.
     */
    public TabBuilder addArguments(int index, BiPredicate<CommandSender, String[]> predicate, BiFunction<CommandSender, String[], Collection<String>> function) {
        return addCollectionArg0(index, predicate, function);
    }

    /**
     * Adds multiple arguments to the tab completion at the specified index.
     *
     * @param index the index to add the arguments at.
     * @param predicate the predicate to test.
     * @param arguments the arguments' collection.
     *
     * @return this {@link TabBuilder} instance.
     */
    public TabBuilder addArguments(int index, BiPredicate<CommandSender, String[]> predicate, Collection<String> arguments) {
        return addCollectionArg0(index, predicate, (s, a) -> Objects.requireNonNull(arguments));
    }

    /**
     * Adds multiple arguments to the tab completion at the specified index.
     *
     * @param index the index to add the arguments at.
     * @param predicate the predicate to test.
     * @param arguments the arguments array.
     *
     * @return this {@link TabBuilder} instance.
     */
    public TabBuilder addArguments(int index, BiPredicate<CommandSender, String[]> predicate, String... arguments) {
        return addArguments(index, predicate, Arrays.asList(arguments));
    }

    /**
     * Adds multiple arguments to the tab completion at the specified index.
     *
     * @param index the index to add the arguments at.
     * @param function the arguments function.
     *
     * @return this {@link TabBuilder} instance.
     */
    public TabBuilder addArguments(int index, BiFunction<CommandSender, String[], Collection<String>> function) {
        return addArguments(index, (s, a) -> true, function);
    }

    /**
     * Adds multiple arguments to the tab completion at the specified index.
     *
     * @param index the index to add the arguments at.
     * @param arguments the arguments' collection.
     *
     * @return this {@link TabBuilder} instance.
     */
    public TabBuilder addArguments(int index, Collection<String> arguments) {
        return addArguments(index, (s, a) -> true, arguments);
    }

    /**
     * Adds multiple arguments to the tab completion at the specified index.
     *
     * @param index the index to add the arguments at.
     * @param arguments the arguments array.
     *
     * @return this {@link TabBuilder} instance.
     */
    public TabBuilder addArguments(int index, String... arguments) {
        return addArguments(index, (s, a) -> true, arguments);
    }

    /**
     * Checks if the tab completion map is empty.
     * @return {@code true} if the map is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Builds the tab completion list for the given {@link CommandSender} and arguments.
     *
     * @param sender the command sender.
     * @param args the command arguments.
     *
     * @return a list of tab completion suggestions.
     */
    @SuppressWarnings("unchecked")
    public List<String> build(CommandSender sender, String[] args) {
        List<String> list = new LinkedList<>();

        fromIndex(args.length - 1)
                .stream()
                .filter(o -> o.predicate.test(sender, args))
                .collect(Collectors.toSet())
                .forEach(o -> {
                    if (o == null) return;

                    if (Objects.equals(String.class, o.getType())) {
                        list.add((String) o
                                .function
                                .apply(sender, args));
                        return;
                    }

                    list.addAll(((Collection<String>) o
                            .function
                            .apply(sender, args)));
                });

        final String t = args[args.length - 1];
        return list.stream()
                .filter(s -> s.regionMatches(true, 0, t, 0, t.length()))
                .collect(Collectors.toList());
    }

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    private static abstract class TabObject<T> {

        final BiPredicate<CommandSender, String[]> predicate;
        final BiFunction<CommandSender, String[], T> function;

        abstract Class<T> getType();
    }
}
