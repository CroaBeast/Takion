package me.croabeast.takion;

import org.bukkit.plugin.java.JavaPlugin;

public final class TakionPlugin extends JavaPlugin {

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
