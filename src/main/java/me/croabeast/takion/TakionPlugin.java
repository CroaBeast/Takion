package me.croabeast.takion;

import me.croabeast.common.CollectionBuilder;
import me.croabeast.common.DependencyLoader;
import me.croabeast.common.MetricsLoader;
import me.croabeast.common.reflect.Reflector;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class TakionPlugin extends JavaPlugin {

    static final Map<Plugin, TakionLib> libs = new HashMap<>();
    static final TakionLib noPluginInstance = new TakionLib(null);

    @SuppressWarnings("all")
    private VaultHolder<?> holder;
    private TakionLib lib;

    private static String verifyPluginName(Plugin plugin) {
        Reflector reflector = Reflector.of(plugin.getClass());

        boolean b;
        try {
            b = reflector.get(plugin, "allowTakionMetrics");
        } catch (Exception e) {
            try {
                b = reflector.get("allowTakionMetrics");
            } catch (Exception e1) {
                return "Not disclosed";
            }
        }

        return b ? plugin.getName() : "Not disclosed";
    }

    @Override
    public void onLoad() {
        DependencyLoader loader =
                DependencyLoader.fromFolder(getDataFolder(), "libraries");
        loader.setComplexStructure(false);

        try {
            YamlConfiguration c = new YamlConfiguration();

            saveResource("dependencies.yml", true);
            c.load(new File(getDataFolder(), "dependencies.yml"));

            if (!loader.loadFromConfiguration(c))
                throw new IllegalStateException("Dependencies not loaded");
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }

        lib = new TakionLib(this);
        lib.getLogger().log("&eTakion &7was loaded successfully.");
    }

    @Override
    public void onEnable() {
        final Plugin plugin = (holder = VaultHolder.loadHolder()).getPlugin();

        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            MetricsLoader.initialize(this, 25287)
                    .addDrillDownPie(
                            "permissionPlugin", "Permission Plugin",
                            plugin != null ? plugin.getName() : "None"
                    )
                    .addSingleLine("pluginsCount", libs.size() - 1)
                    .addDrillDownPie(
                            "usagePlugins", "Plugins Using Takion",
                            CollectionBuilder.of(libs.keySet())
                                    .remove(this)
                                    .map(TakionPlugin::verifyPluginName)
                                    .toList()
                    );
            lib.getLogger().log(
                    "&eMetrics initialized to track data from plugins using Takion as a dependency.",
                    "&eBy default, plugins names using Takion won't be disclosed in our Metrics.",
                    "If you want to allow Takion to show your plugin's name, please do the following:",
                    " - Add a boolean variable named 'allowTakionMetrics' in your plugin class and set it true.",
                    " - The access of this variable might be public, but we recommend to make it private.",
                    " - Examples:",
                    "   • private final boolean allowTakionMetrics = true;",
                    "   • boolean allowTakionMetrics = true;",
                    "   • public static final boolean allowTakionMetrics = true;",
                    "   • static boolean allowTakionMetrics = true;",
                    "   • protected boolean allowTakionMetrics = true;",
                    "&eThe only data we show is how many plugins are using Takion and its names (if allowed)."
            );
        }, 5);
    }

    @Override
    public void onDisable() {
        lib.getLogger().log("&eTakion &7was disabled successfully.");
    }
}
