package me.croabeast.takion.rule;

import org.bukkit.World;

/**
 * Manages the loading, unloading, and retrieval of {@link GameRule} values.
 * <p>
 * A {@code GameRuleManager} handles the lifecycle of game rule data — it can
 * load the current state of all rules, cache that state, and expose it for
 * later retrieval. This is useful for plugins that need to snapshot and restore
 * game rule settings, or that need to read rule values without querying the
 * world on every access.
 * </p>
 */
public interface GameRuleManager {

    /**
     * Returns {@code true} if the game rule data has been loaded and is currently available.
     *
     * @return {@code true} if loaded; {@code false} otherwise
     */
    boolean isLoaded();

    /**
     * Loads all game rule data, making it available via {@link #getLoadedValue(World, GameRule)}.
     * <p>
     * If data is already loaded, implementations may choose to reload or do nothing.
     * </p>
     */
    void load();

    /**
     * Discards the currently loaded game rule data.
     * <p>
     * After calling this method, {@link #isLoaded()} returns {@code false} and
     * {@link #getLoadedValue(World, GameRule)} may return {@code null} or throw.
     * </p>
     */
    void unload();

    /**
     * Returns the cached value of the given game rule for the specified world.
     * <p>
     * The manager must be loaded (see {@link #load()}) before calling this method.
     * </p>
     *
     * @param <T>   the value type of the game rule
     * @param world the world whose rule value is requested
     * @param rule  the game rule whose cached value is to be retrieved
     * @return the cached value, or {@code null} if not available
     */
    <T> T getLoadedValue(World world, GameRule<T> rule);
}
