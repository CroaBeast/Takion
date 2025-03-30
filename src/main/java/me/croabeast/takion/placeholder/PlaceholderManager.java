package me.croabeast.takion.placeholder;

import org.bukkit.entity.Player;

import java.util.function.Function;

/**
 * A manager interface for handling dynamic placeholders within text.
 * <p>
 * The {@code PlaceholderManager} provides methods to load, remove, and edit placeholders,
 * as well as to perform placeholder replacement in strings for a given player.
 * Placeholders are used to dynamically inject values (such as player data or other runtime information)
 * into text, making it more flexible and contextual.
 * </p>
 * <p>
 * Typical implementations of this interface manage a collection of {@link Placeholder} instances,
 * allowing them to be registered or deregistered at runtime.
 * </p>
 *
 * @see Placeholder
 */
public interface PlaceholderManager {

    /**
     * Loads a placeholder into the manager.
     * <p>
     * If a placeholder with the same key already exists, the behavior should be defined by the implementation
     * (e.g., replace the existing one or ignore the new one).
     * </p>
     *
     * @param <T>         the type of the value the placeholder represents
     * @param placeholder the {@link Placeholder} to load
     * @return {@code true} if the placeholder was loaded successfully; {@code false} otherwise
     */
    <T> boolean load(Placeholder<T> placeholder);

    /**
     * Creates and loads a placeholder with the specified key and value provider function.
     * <p>
     * This is a convenience method that internally creates a new {@link Placeholder} instance
     * and then loads it using {@link #load(Placeholder)}.
     * </p>
     *
     * @param <T>      the type of the value the placeholder represents
     * @param key      the unique key identifying the placeholder
     * @param function a function that generates a value based on a {@link Player}
     * @return {@code true} if the placeholder was loaded successfully; {@code false} otherwise
     */
    default <T> boolean load(String key, Function<Player, T> function) {
        return load(new Placeholder<>(key, function));
    }

    /**
     * Removes the placeholder with the specified key from the manager.
     *
     * @param key the key of the placeholder to remove
     * @return {@code true} if the placeholder was successfully removed; {@code false} otherwise
     */
    boolean remove(String key);

    /**
     * Edits an existing placeholder by changing its key.
     * <p>
     * This method allows the key of a loaded placeholder to be updated.
     * Implementations should ensure that the new key does not conflict with existing placeholders.
     * </p>
     *
     * @param oldKey the current key of the placeholder
     * @param newKey the new key to assign to the placeholder
     * @return {@code true} if the key was successfully updated; {@code false} otherwise
     */
    boolean edit(String oldKey, String newKey);

    /**
     * Replaces all occurrences of loaded placeholder keys in the given string with their corresponding values
     * for the specified player.
     * <p>
     * The replacement process takes into account the sensitivity flag to determine whether the matching
     * should be case-sensitive.
     * </p>
     *
     * @param player    the player whose context is used to compute the placeholder values
     * @param string    the string in which to perform the replacements
     * @param sensitive {@code true} if the replacement should be case-sensitive; {@code false} otherwise
     * @return the resulting string with all placeholder keys replaced by their computed values
     */
    String replace(Player player, String string, boolean sensitive);

    /**
     * Replaces all occurrences of loaded placeholder keys in the given string with their corresponding values
     * for the specified player.
     * <p>
     * This method uses a default non-sensitive mode for replacement.
     * </p>
     *
     * @param player the player whose context is used to compute the placeholder values
     * @param string the string in which to perform the replacements
     * @return the resulting string with placeholders replaced
     */
    default String replace(Player player, String string) {
        return replace(player, string, false);
    }

    /**
     * Loads the default set of placeholders into the manager.
     * <p>
     * This method is intended to initialize the manager with a predefined set of placeholder values,
     * ensuring that common placeholders are available for use.
     * </p>
     */
    void setDefaults();
}
