package me.croabeast.takion;

import me.croabeast.lib.MetricsLoader;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class TakionPlugin extends JavaPlugin {

    static final Map<Plugin, TakionLib> libs = new HashMap<>();
    static final TakionLib noPluginInstance = new TakionLib(null);

    private TakionLib lib;

    @Override
    public void onLoad() {
        this.lib = new TakionLib(this);

        Plugin plugin = lib.getVaultHolder().getPlugin();

        MetricsLoader.initialize(this, 25287)
                .addDrillDownPie(
                        "permissionPlugin", "Permission Plugin",
                        plugin != null ? plugin.getName() : null,
                        "None"
                );

        lib.getLogger().log("&eTakion &7was loaded successfully.");
    }

    @Override
    public void onDisable() {
        lib.getLogger().log("&eTakion &7was disabled successfully.");
    }
}
