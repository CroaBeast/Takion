package me.croabeast.takion;

import lombok.Getter;
import me.croabeast.common.CollectionBuilder;
import me.croabeast.common.CustomListener;
import me.croabeast.common.DependencyLoader;
import me.croabeast.common.MetricsLoader;
import me.croabeast.common.reflect.Reflector;
import me.croabeast.common.updater.Platform;
import me.croabeast.common.updater.UpdateChecker;
import me.croabeast.common.updater.UpdateResult;
import me.croabeast.takion.character.SmallCaps;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class TakionPlugin extends JavaPlugin {

    static final TakionLib noPluginInstance = new TakionLib(null);
    static final Map<Plugin, TakionLib> libs = new HashMap<>();

    @SuppressWarnings("all")
    private VaultHolder<?> holder;
    TakionLib lib;

    @Override
    public void onLoad() {
        DependencyLoader loader =
                DependencyLoader.fromFolder(getDataFolder(), "libraries");
        loader.setComplexStructure(false);

        try {
            YamlConfiguration c = new YamlConfiguration();

            saveResource("dependencies.yml", false);
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

    interface UpdateDisplay {
        void display(String... strings);
    }

    @Override
    public void onEnable() {
        Plugin plugin = (holder = VaultHolder.loadHolder()).getPlugin();
        final UpdateChecker updater = UpdateChecker.of(this);

        BiConsumer<Player, UpdateResult> consumer = (player, result) -> {
            String latest = result.getLatest(), current = result.getLocal();
            String prefix = SmallCaps.toSmallCaps("[updater]");

            UpdateDisplay display = player == null ? lib.getLogger()::log :
                    strings -> {
                if (player.isOp() || player.hasPermission("takion.update"))
                    lib.getLoadedSender()
                            .setTargets(player).setLogger(false).send(strings);
            };

            switch (result.getReason()) {
                case NEW_UPDATE:
                    display.display(
                            prefix + " &8» &eUpdate Available!",
                            prefix + " &7A new version of Takion was found, please download it.",
                            prefix + " &7Remember, old versions won't receive any support.",
                            prefix + " &7New version:" +
                                    " &6" + latest + "&7, Current version: " + current,
                            prefix + " &7Link:&b https://modrinth.com/plugin/takion"
                    );
                    break;
                case UP_TO_DATE:
                    break;
                case UNRELEASED_VERSION:
                    display.display(
                            prefix + " &8» &aDevelopment build found!",
                            prefix + " &7This version of Takion seems to be on development.",
                            prefix + " &7Errors, bugs and/or inconsistencies might occur.",
                            prefix + " &7Current version:" +
                                    " &6" + current + "&7, Latest version: " + latest
                    );
                    break;
                default:
                    final Throwable throwable = result.getThrowable();
                    display.display(
                            prefix + " &7Not able to verify any checks for updates from Takion.",
                            prefix + " &7Reason: &c" + result.getReason()
                    );
                    if (throwable != null) throwable.printStackTrace();
                    break;
            }
        };
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            if (getConfig().getBoolean("updater.on-start"))
                updater.requestCheck("takion", Platform.MODRINTH)
                        .whenComplete((result, e) -> consumer.accept(null, result));

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

        new CustomListener() {
            @Getter
            private final Status status = new Status();

            @EventHandler(priority = EventPriority.HIGHEST)
            private void onJoin(PlayerJoinEvent event) {
                if (!getConfig().getBoolean("updater.send-op")) return;

                updater.requestCheck("takion", Platform.MODRINTH)
                        .whenComplete((r, e) ->
                                consumer.accept(event.getPlayer(), r));
            }
        }.register(this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        lib.getLogger().log("&eTakion &7was disabled successfully.");
    }
}
