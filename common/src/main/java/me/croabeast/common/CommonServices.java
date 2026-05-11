package me.croabeast.common;

import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.function.Supplier;

public final class CommonServices {

    private static volatile Supplier<Plugin> pluginSupplier;

    private CommonServices() {}

    public static void setPluginSupplier(Supplier<Plugin> supplier) {
        pluginSupplier = Objects.requireNonNull(supplier);
    }

    public static Plugin getPlugin() {
        Supplier<Plugin> supplier = pluginSupplier;
        if (supplier == null)
            throw new IllegalStateException("No plugin supplier has been configured.");

        return Objects.requireNonNull(supplier.get(), "Plugin supplier returned null.");
    }
}
