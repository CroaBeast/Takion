package me.croabeast.common;

import lombok.experimental.UtilityClass;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A shared service locator for the common {@link Plugin} instance used across the library.
 * <p>
 * {@code CommonServices} acts as a lightweight service locator that holds a lazily-provided
 * {@link Plugin} reference. It must be configured via {@link #setPlugin(Supplier)} before
 * any call to {@link #getPlugin()} is made; failing to do so will throw an
 * {@link IllegalStateException}.
 * </p>
 */
@UtilityClass
public final class CommonServices {

    /**
     * The supplier that provides the active {@link Plugin} instance.
     * <p>
     * Marked {@code volatile} to ensure visibility across threads.
     * </p>
     */
    private volatile Supplier<Plugin> pluginSupplier;

    /**
     * Registers the supplier that will provide the active {@link Plugin} instance.
     * <p>
     * This method must be called before any invocation of {@link #getPlugin()}.
     * </p>
     *
     * @param supplier a non-{@code null} supplier that returns the active plugin
     * @throws NullPointerException if {@code supplier} is {@code null}
     */
    public void setPlugin(Supplier<Plugin> supplier) {
        pluginSupplier = Objects.requireNonNull(supplier);
    }

    /**
     * Registers the supplier that will provide the active {@link Plugin} instance.
     *
     * @param supplier a non-{@code null} supplier that returns the active plugin
     * @deprecated use {@link #setPlugin(Supplier)} instead
     */
    @Deprecated
    public void setPluginSupplier(Supplier<Plugin> supplier) {
        setPlugin(supplier);
    }

    /**
     * Returns the active {@link Plugin} instance provided by the registered supplier.
     *
     * @return the current plugin instance (never {@code null})
     * @throws IllegalStateException if no supplier has been registered via {@link #setPlugin(Supplier)}
     * @throws NullPointerException  if the registered supplier returns {@code null}
     */
    public Plugin getPlugin() {
        Supplier<Plugin> supplier = pluginSupplier;
        if (supplier == null)
            throw new IllegalStateException("No plugin supplier has been configured.");

        return Objects.requireNonNull(supplier.get(), "Plugin supplier returned null.");
    }
}
