package me.croabeast.common;

import me.croabeast.common.util.ArrayUtils;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.CustomChart;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A utility class for integrating bStats metrics into a Bukkit/Spigot/Paper plugin.
 * <p>
 * This class simplifies the process of adding custom charts to track plugin usage and statistics
 * through bStats. It provides methods for adding various types of charts including simple pie charts,
 * drilldown pie charts, and single-line charts.
 * </p>
 * <p>
 * Example usage:
 * <pre><code>
 * MetricsLoader metrics = MetricsLoader.initialize(this, ${bStatsID});
 * metrics.addSimplePie("plugin_usage", () -&gt; "active")
 *        .addSingleLine("command_count", 42)
 *        .addDrillDownPie("active_plugins", "Using Metrics", pluginList);
 * </code></pre>
 * </p>
 */
public final class MetricsLoader {

    /**
     * The bStats {@link Metrics} instance that handles the communication with the bStats service.
     */
    private final Metrics metrics;

    /**
     * Constructs a new {@code MetricsLoader} for the specified plugin and bStats ID.
     *
     * @param plugin the {@link JavaPlugin} instance that is integrating bStats
     * @param id     the bStats plugin ID (this value should be replaced with a variable version/tag when updating)
     */
    private MetricsLoader(JavaPlugin plugin, int id) {
        metrics = new Metrics(plugin, id);
    }

    /**
     * Adds a custom chart to the metrics dashboard.
     *
     * @param chart the custom chart to add (must not be {@code null})
     * @return the current {@code MetricsLoader} instance for method chaining
     */
    public MetricsLoader addChart(CustomChart chart) {
        if (chart == null) return this;
        metrics.addCustomChart(chart);
        return this;
    }

    /**
     * Adds a simple pie chart to the metrics.
     * <p>
     * The value is converted to a string via its {@code toString()} method.
     * </p>
     *
     * @param id    the identifier for the pie chart
     * @param value the value to be tracked in the pie chart (must not be {@code null})
     * @return the current {@code MetricsLoader} instance for method chaining
     */
    public MetricsLoader addSimplePie(String id, Object value) {
        return addChart(new SimplePie(id, value::toString));
    }

    /**
     * Adds a single-line chart to the metrics.
     *
     * @param id    the identifier for the chart
     * @param value the integer value to be tracked on the chart
     * @return the current {@code MetricsLoader} instance for method chaining
     */
    public MetricsLoader addSingleLine(String id, int value) {
        return addChart(new SingleLineChart(id, () -> value));
    }

    /**
     * Adds a drilldown pie chart to the metrics based on a collection of objects.
     * <p>
     * Each object in the collection is converted to its string representation, and the chart
     * displays the count of occurrences under the specified title.
     * </p>
     *
     * @param id    the identifier for the drilldown pie chart
     * @param title the title of the drilldown category
     * @param list  the collection of objects to track; if empty, no chart is added.
     * @return the current {@code MetricsLoader} instance for method chaining
     */
    public MetricsLoader addDrillDownPie(String id, String title, Collection<?> list) {
        if (ArrayUtils.isIterableEmpty(list)) return this;
        return addChart(new DrilldownPie(id, () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            for (Object o : list)
                map.computeIfAbsent(o.toString(), k -> new HashMap<>()).put(title, 1);
            return map;
        }));
    }

    /**
     * Adds a drilldown pie chart to the metrics based on a single object.
     *
     * @param id    the identifier for the drilldown pie chart
     * @param title the title of the drilldown category
     * @param value the value to track; must not be {@code null}
     * @return the current {@code MetricsLoader} instance for method chaining
     */
    public MetricsLoader addDrillDownPie(String id, String title, Object value) {
        Objects.requireNonNull(value);
        return addDrillDownPie(id, title, ArrayUtils.toList(value));
    }

    /**
     * Initializes the {@code MetricsLoader} for a given plugin.
     *
     * @param plugin the {@link JavaPlugin} instance integrating bStats metrics
     * @param id     the bStats plugin ID (use a variable for version control)
     * @return a new {@code MetricsLoader} instance
     */
    public static MetricsLoader initialize(JavaPlugin plugin, int id) {
        return new MetricsLoader(plugin, id);
    }
}
