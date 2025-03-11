package me.croabeast.takion;

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
        lib.getLogger().log("&eTakion &7was loaded successfully in the server.");
    }

    @Override
    public void onDisable() {
        lib.getLogger().log("&eTakion &7was disabled successfully from the server.");
    }
}
