package me.croabeast.takion;

import me.croabeast.common.DependencyLoader;
import me.croabeast.common.MetricsLoader;
import me.croabeast.common.util.ArrayUtils;
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
        DependencyLoader loader = DependencyLoader.fromFolder(getDataFolder(), "libraries");
        loader.setComplexStructure(false);

        for (String artifact : ArrayUtils.toList(
                "CommandFramework", "AdvancementInfo", "YAML-API",
                "PrismaticAPI"
        ))
            loader.load(
                    "me.croabeast", artifact, "1.0",
                    "https://croabeast.github.io/repo/"
            );

        loader.load(
                "com.github.stefvanschie.inventoryframework",
                "IF", "0.10.19",
                DependencyLoader.MAVEN_REPO_URLS[0]
        );

        this.lib = new TakionLib(this);
        final Plugin plugin = lib.getVaultHolder().getPlugin();

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
