package me.croabeast.takion;

import lombok.AccessLevel;
import lombok.Getter;
import me.croabeast.common.CollectionBuilder;
import me.croabeast.common.DependencyLoader;
import me.croabeast.common.MetricsLoader;
import me.croabeast.common.reflect.Reflector;
import me.croabeast.vault.ChatAdapter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class TakionPlugin extends JavaPlugin {

    static final TakionLib noPluginInstance = new TakionLib(null);
    static final Map<Plugin, TakionLib> libs = new HashMap<>();

    @Getter(AccessLevel.NONE)
    private ChatAdapter<?> holder;
    TakionLib lib;

    @Override
    public void onLoad() {
        DependencyLoader loader = DependencyLoader.fromFolder(getDataFolder(), "libraries");
        loader.setComplexStructure(false);

        try {
            final YamlConfiguration c = new YamlConfiguration();

            saveResource("dependencies.yml", true);
            c.load(new File(getDataFolder(), "dependencies.yml"));

            if (!loader.loadFromConfiguration(c))
                throw new IllegalStateException("Dependencies not loaded");
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }

        saveDefaultConfig();
        reloadConfig();

        lib = new TakionLib(this);
        lib.getLogger().log("&eTakion &7was loaded successfully.");
    }

    @Override
    public void onEnable() {
        Plugin plugin = (holder = ChatAdapter.create()).getPlugin();

        lib.getScheduler().runTaskLater(() -> {
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
                                    .map(p -> {
                                        Reflector r = Reflector.of(p.getClass());

                                        boolean b;
                                        try {
                                            b = r.get(plugin, "allowTakionMetrics");
                                        } catch (Exception e) {
                                            try {
                                                b = r.get("allowTakionMetrics");
                                            } catch (Exception e1) {
                                                return "Not disclosed";
                                            }
                                        }

                                        return b ? p.getName() : "Not disclosed";
                                    })
                                    .toList()
                    );

            if (!getConfig().getBoolean("metrics-advise")) return;
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
                    "&eThe only data shown is how many plugins are using Takion and its names (if allowed).",
                    "&6If you want to hide this message, go to config.yml and set metrics-advise to false."
            );
        }, 5);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        lib.getScheduler().cancelAll();
        lib.getLogger().log("&eTakion &7was disabled successfully.");
    }
}
