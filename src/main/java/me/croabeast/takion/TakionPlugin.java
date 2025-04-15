package me.croabeast.takion;

import me.croabeast.common.CollectionBuilder;
import me.croabeast.common.DependencyLoader;
import me.croabeast.common.MetricsLoader;
import me.croabeast.common.reflect.Reflector;
import me.croabeast.common.util.ArrayUtils;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class TakionPlugin extends JavaPlugin {

    static final Map<Plugin, TakionLib> libs = new HashMap<>();
    static final TakionLib noPluginInstance = new TakionLib(null);

    @SuppressWarnings("all")
    private VaultHolder<?> holder;
    private TakionLib lib;

    @Override
    public void onLoad() {
        DependencyLoader loader = DependencyLoader.fromFolder(getDataFolder(), "libraries");
        loader.setComplexStructure(false);

        for (String artifact : ArrayUtils.toList(
                "CommandFramework", "YAML-API", "PrismaticAPI",
                "AdvancementInfo"
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

        holder = VaultHolder.loadHolder();
        lib = new TakionLib(this);

        lib.getLogger().log("&eTakion &7was loaded successfully.");
    }

    private static String verifyPluginName(Plugin plugin) {
        try {
            boolean b = Reflector.from(() -> plugin).get("allowTakionMetrics");
            return b ? plugin.getName() : "Not disclosed";
        }
        catch (Exception e) {
            return "Not disclosed";
        }
    }

    @Override
    public void onEnable() {
        final Plugin plugin = holder.getPlugin();

        MetricsLoader.initialize(this, 25287)
                .addDrillDownPie(
                        "permissionPlugin", "Permission Plugin",
                        plugin != null ? plugin.getName() : "None"
                )
                .addSingleLine("pluginsCount", libs.size())
                .addDrillDownPie(
                        "usagePlugins", "Plugins Using Takion",
                        CollectionBuilder.of(libs.keySet())
                                .remove(this)
                                .map(TakionPlugin::verifyPluginName)
                                .toList()
                );
    }

    @Override
    public void onDisable() {
        lib.getLogger().log("&eTakion &7was disabled successfully.");
    }
}
