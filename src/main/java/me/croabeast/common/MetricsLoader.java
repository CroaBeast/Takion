package me.croabeast.common;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.CustomChart;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * A utility class for integrating bStats metrics into a Bukkit/Spigot plugin.
 * <p>
 * This class simplifies the process of adding custom charts to track plugin usage
 * and statistics through bStats.
 * </p>
 */
public final class MetricsLoader {

    private final Metrics metrics;

    private MetricsLoader(JavaPlugin plugin, int id) {
        metrics = new Metrics(plugin, id);
    }

    /**
     * Adds a custom chart to the metrics.
     *
     * @param chart the custom chart to add
     * @return the current {@code MetricsLoader} instance for chaining
     */
    public MetricsLoader addChart(CustomChart chart) {
        if (chart == null) return this;

        metrics.addCustomChart(chart);
        return this;
    }

    /**
     * Adds a simple pie chart to the metrics.
     *
     * @param id    the identifier for the pie chart
     * @param value the value to be tracked in the chart
     * @return the current {@code MetricsLoader} instance for chaining
     */
    public MetricsLoader addSimplePie(String id, Object value) {
        return addChart(new SimplePie(id, value::toString));
    }

    /**
     * Adds a drill-down pie chart to the metrics, allowing for more detailed categorization.
     *
     * @param id    the identifier for the drill-down pie chart
     * @param title the title of the category
     * @param value the value to be categorized
     * @param def   the default category if the value is {@code null}
     * @return the current {@code MetricsLoader} instance for chaining
     */
    public MetricsLoader addDrillDownPie(String id, String title, Object value, String def) {
        return addChart(new DrilldownPie(id, () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            Map<String, Integer> entry = new HashMap<>();

            entry.put(title, 1);
            map.put(value == null ? def : value.toString(), entry);

            return map;
        }));
    }

    /**
     * Initializes the {@code MetricsLoader} for a given plugin.
     *
     * @param plugin the plugin instance
     * @param id     the bStats plugin ID
     * @return a new {@code MetricsLoader} instance
     */
    public static MetricsLoader initialize(JavaPlugin plugin, int id) {
        return new MetricsLoader(plugin, id);
    }
}
