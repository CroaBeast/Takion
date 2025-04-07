package me.croabeast.common;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a custom event listener with built-in registration management.
 * <p>
 * This interface extends both the standard Bukkit {@link Listener} and the {@link Registrable} interface,
 * providing a convenient way to register and unregister event listeners in a Bukkit/Spigot plugin.
 * It also maintains an internal {@code Status} to keep track of the registration state.
 * </p>
 *
 * @see Listener
 * @see Registrable
 */
public interface CustomListener extends Listener, Registrable {

    /**
     * Returns the current registration status of this listener.
     *
     * @return the {@link Status} object representing whether this listener is registered.
     */
    @NotNull
    Status getStatus();

    /**
     * Checks whether this listener is registered.
     * <p>
     * This is a convenience method inherited from {@link Registrable}.
     * </p>
     *
     * @return {@code true} if the listener is currently registered, {@code false} otherwise.
     */
    @Override
    default boolean isRegistered() {
        return getStatus().registered;
    }

    /**
     * Registers this listener with the specified plugin.
     * <p>
     * If the listener is already registered or the plugin is {@code null},
     * the registration will not occur and {@code false} is returned.
     * Upon successful registration, the internal status is updated.
     * </p>
     *
     * @param plugin the {@link Plugin} with which to register this listener.
     * @return {@code true} if registration was successful, {@code false} otherwise.
     */
    default boolean register(Plugin plugin) {
        if (getStatus().registered || plugin == null)
            return false;

        Bukkit.getPluginManager().registerEvents(this, plugin);
        getStatus().registered = true;

        return true;
    }

    /**
     * Registers this listener using the plugin that provides the {@code CustomListener} class.
     * <p>
     * This is a convenience method that automatically fetches the providing plugin via
     * {@link JavaPlugin#getProvidingPlugin(Class)}.
     * </p>
     *
     * @return {@code true} if registration was successful, {@code false} otherwise.
     */
    default boolean register() {
        return register(JavaPlugin.getProvidingPlugin(CustomListener.class));
    }

    /**
     * Unregisters this listener from all event handlers.
     * <p>
     * If the listener is not registered, no action is taken.
     * Upon successful unregistration, the internal status is updated.
     * </p>
     *
     * @return {@code true} if unregistration was successful, {@code false} otherwise.
     */
    default boolean unregister() {
        if (!getStatus().registered)
            return false;

        HandlerList.unregisterAll(this);
        getStatus().registered = false;
        return true;
    }

    /**
     * A simple status holder for tracking the registration state of a {@link CustomListener}.
     * <p>
     * This inner class provides a mutable flag indicating whether the listener is currently registered.
     * It is automatically used by implementations of {@link CustomListener} to manage their state.
     * </p>
     */
    final class Status {
        /**
         * Indicates whether the listener is currently registered.
         */
        private boolean registered = false;
    }
}
