package me.croabeast.common;

/**
 * Represents an object that can be loaded and unloaded dynamically.
 * <p>
 * This interface provides a standard contract for managing the lifecycle
 * of objects that require initialization and cleanup. Implementing classes
 * should define what "loading" and "unloading" mean in their specific context.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre><code>
 * public class ConfigManager implements Loadable {
 *     private boolean loaded;
 *
 *     {@literal @}Override
 *     public boolean isLoaded() {
 *         return loaded;
 *     }
 *
 *     {@literal @}Override
 *     public void load() {
 *         // Load configuration files
 *         loaded = true;
 *     }
 *
 *     {@literal @}Override
 *     public void unload() {
 *         // Release resources
 *         loaded = false;
 *     }
 * }</code></pre>
 */
public interface Loadable {

    /**
     * Checks whether this object is currently loaded.
     *
     * @return {@code true} if the object is loaded, {@code false} otherwise
     */
    boolean isLoaded();

    /**
     * Loads the object, initializing necessary resources.
     * <p>
     * This method should be implemented to perform any setup operations
     * required before using the object.
     * </p>
     */
    void load();

    /**
     * Unloads the object, releasing any held resources.
     * <p>
     * This method should clean up any allocated memory, close file streams,
     * or reset states as necessary.
     * </p>
     */
    void unload();
}
