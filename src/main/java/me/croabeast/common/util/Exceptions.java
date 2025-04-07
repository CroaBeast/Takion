package me.croabeast.common.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A utility class for handling various exceptions and plugin-related checks.
 */
@UtilityClass
public class Exceptions {

    /**
     * Checks if a plugin with the given name is enabled or not.
     *
     * @param name the name of the plugin to check
     * @param checkRunning whether to check if the plugin is running or not
     *
     * @return true if the plugin is not null and is enabled, if
     *         checkRunning is true; false otherwise
     */
    public boolean isPluginEnabled(String name, boolean checkRunning) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        return plugin != null && (!checkRunning || plugin.isEnabled());
    }

    /**
     * Checks if a plugin with the given name is enabled or not, assuming checkRunning is true.
     *
     * @param name the name of the plugin to check
     * @return true if the plugin is not null and is enabled, false otherwise
     */
    public boolean isPluginEnabled(String name) {
        return isPluginEnabled(name, true);
    }

    /**
     * Checks if a collection of plugins are enabled or not, based on a boolean flag.
     *
     * @param isInclusive whether to check if all plugins are enabled (true) or any plugin is enabled (false)
     * @param names the collection of plugin names to check
     *
     * @return true if the collection is not empty and the plugins match the isInclusive flag, false otherwise
     */
    public boolean arePluginsEnabled(boolean isInclusive, Collection<String> names) {
        if (names.isEmpty()) return false;

        if (names.size() == 1)
            return isPluginEnabled(names.toArray(new String[0])[0]);

        for (String name : names) {
            boolean isEnabled = isPluginEnabled(name);

            if (!isInclusive) {
                if (isEnabled) return true;
            } else {
                if (!isEnabled) return false;
            }
        }

        return isInclusive;
    }

    /**
     * Checks if an array of plugins is enabled or not, based on a boolean flag.
     *
     * @param isInclusive whether to check if all plugins are enabled (true) or any plugin is enabled (false)
     * @param names the array of plugin names to check
     *
     * @return true if the array is not empty and the plugins match the isInclusive flag, false otherwise
     */
    public boolean arePluginsEnabled(boolean isInclusive, String... names) {
        return arePluginsEnabled(isInclusive, ArrayUtils.toList(names));
    }

    /**
     * Validates the specified object against the given predicate. If the predicate test fails,
     * it throws the specified throwable.
     *
     * @param <O>       The type of the object to be validated.
     * @param <T>       The type of the throwable to be thrown.
     *
     * @param predicate The predicate used for validation.
     * @param object    The object to be validated.
     * @param throwable The throwable to throw if the predicate test fails.
     * @return The validated object if it passes the predicate.
     *
     * @throws T If the predicate test fails, it throws the specified throwable.
     * @throws NullPointerException If the object is null.
     */
    @NotNull
    public <O, T extends Throwable> O validate(Predicate<O> predicate, O object, T throwable) throws T {
        Objects.requireNonNull(object);
        if (!predicate.test(object)) throw throwable;
        return object;
    }

    /**
     * Validates an object against a specified predicate.
     *
     * @param <O>       The type of the object to be validated.
     * @param predicate The predicate used for validation.
     * @param object    The object to be validated.
     * @param errorMessage The error message for the exception.
     *
     * @return The validated object if it passes the predicate.
     * @throws NullPointerException If the object is null.
     * @throws IllegalStateException If the object does not pass the predicate.
     */
    @NotNull
    public <O> O validate(Predicate<O> predicate, O object, String errorMessage) throws IllegalStateException {
        return validate(predicate, object, new IllegalStateException(errorMessage));
    }

    /**
     * Validates an object against a specified predicate.
     *
     * @param <O>       The type of the object to be validated.
     * @param predicate The predicate used for validation.
     * @param object    The object to be validated.
     *
     * @return The validated object if it passes the predicate.
     * @throws NullPointerException If the object is null.
     * @throws IllegalStateException If the object does not pass the predicate.
     */
    @NotNull
    public <O> O validate(Predicate<O> predicate, O object) throws IllegalStateException {
        return validate(predicate, object, new IllegalStateException());
    }

    /**
     * Validates an object based on a boolean condition.
     *
     * @param <O>    The type of the object to be validated.
     * @param b      The boolean condition to validate against.
     * @param object The object to be validated.
     *
     * @return The validated object if the condition is true.
     * @throws NullPointerException If the object is null.
     * @throws IllegalStateException If the condition is false.
     */
    @NotNull
    public <O> O validate(boolean b, O object) {
        return validate(t -> b, object);
    }

    /**
     * Returns the caller class from the current thread's stack trace at a given index.
     *
     * @param index the index of the stack trace element to get the class from
     *
     * @return the class object of the caller class
     * @throws ClassNotFoundException if the class name is not found
     * @throws IllegalStateException  If the index is less than 0
     */
    @NotNull
    public Class<?> getCallerClass(int index) throws ClassNotFoundException {
        return Class.forName(Thread.currentThread().getStackTrace()[validate(i -> i >= 0, index)].getClassName());
    }

    /**
     * Checks if a class has access to the plugin, otherwise throws a throwable.
     *
     * @param plugin The plugin to verify
     * @param clazz the class to check
     * @param throwable the throwable to throw if the class does not have access
     *
     * @param <T> the Throwable class
     * @throws T if the class does not have access to the plugin
     */
    public <T extends Throwable> void hasPluginAccess(Plugin plugin, Class<?> clazz, T throwable) throws T {
        Objects.requireNonNull(clazz);

        Plugin previous = null;
        try {
            previous = JavaPlugin.getProvidingPlugin(clazz);
        } catch (Exception ignored) {}

        if (Objects.equals(previous, plugin))
            return;

        throw throwable;
    }
}