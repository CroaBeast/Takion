package me.croabeast.takion;

import lombok.experimental.UtilityClass;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of the library instance owned by each plugin.
 *
 * <p>This lives in the core module so {@link TakionLib} can resolve an instance without depending
 * on the plugin module. Before, two classes named {@code me.croabeast.takion.TakionLib} existed,
 * one per module, and which of them won at runtime depended on class loader order.
 *
 * @since 2.0.0
 */
@UtilityClass
public class TakionRegistry {

    private final Map<Plugin, TakionLib> LIBRARIES = new ConcurrentHashMap<>();

    /**
     * Library used when no plugin owns the call.
     */
    final TakionLib NO_PLUGIN = new TakionLib(null);

    void register(@NotNull Plugin plugin, @NotNull TakionLib lib) {
        LIBRARIES.put(plugin, lib);
    }

    /**
     * Removes the library owned by a plugin.
     *
     * @param plugin plugin to unregister
     */
    public void unregister(@Nullable Plugin plugin) {
        if (plugin != null) LIBRARIES.remove(plugin);
    }

    /**
     * Returns every registered plugin.
     *
     * @return registered plugins
     */
    @NotNull
    public Collection<Plugin> getPlugins() {
        return LIBRARIES.keySet();
    }

    /**
     * Returns how many plugins are registered.
     *
     * @return registered count
     */
    public int size() {
        return LIBRARIES.size();
    }

    boolean isEmpty() {
        return LIBRARIES.isEmpty();
    }

    @NotNull
    TakionLib get(@Nullable Plugin plugin) {
        return plugin == null ? NO_PLUGIN : LIBRARIES.getOrDefault(plugin, NO_PLUGIN);
    }

    @NotNull
    Collection<TakionLib> values() {
        return LIBRARIES.values();
    }

    @NotNull
    Collection<Map.Entry<Plugin, TakionLib>> entries() {
        return LIBRARIES.entrySet();
    }

    @Nullable
    TakionLib remove(@Nullable Plugin plugin) {
        return plugin == null ? null : LIBRARIES.remove(plugin);
    }
}
