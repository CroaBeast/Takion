package me.croabeast.takion.bossbar;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import me.croabeast.common.util.ArrayUtils;
import me.croabeast.file.Configurable;
import me.croabeast.scheduler.GlobalTask;
import me.croabeast.takion.TakionLib;
import me.croabeast.takion.format.PlainFormat;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * Provides a dynamic, animated BossBar display for players.
 * <p>
 * The {@code AnimatedBossbar} class creates and manages a collection of BossBars that can display
 * animated messages, colors, and styles based on a configurable sequence. It supports dynamic updates,
 * allowing text, color, and style to be cycled over time. Animations can progress in different modes (increase,
 * decrease, or remain static) and use both random and synchronized selections for messages, colors, and styles.
 * </p>
 * <p>
 * This class is designed to integrate with Bukkit/Spigot plugins, and it utilizes configuration sections
 * for initializing values. BossBars are cached for efficient management and can be assigned to individual
 * players or groups.
 * </p>
 * <p>
 * <strong>Example usage:</strong>
 * <pre><code>
 *     // Create an animated bossbar with custom messages, colors, and styles.
 *     AnimatedBossbar bossbar = new AnimatedBossbar(plugin,
 *             Arrays.asList("Welcome to the server!", "Good Luck!"),
 *             Arrays.asList(BarColor.RED, BarColor.BLUE),
 *             Arrays.asList(BarStyle.SOLID, BarStyle.SEGMENTED_10));
 *
 *     // Add viewers (players) to the bossbar here
 *     bossbar.addViewer(player1);
 *     bossbar.addViewer(player2);
 *
 *     // Start the animation with a duration (in seconds)
 *     bossbar.setDuration(10.0).startAnimation();
 *
 *     // Later, when no longer needed:
 *     bossbar.deleteBossBar(); </code></pre></p>
 *
 * @see BossBar
 * @see Plugin
 */
@SuppressWarnings("deprecation")
@Getter
public class AnimatedBossbar {

    /**
     * A cache that holds all active AnimatedBossbar instances, keyed by their unique UUID.
     */
    private static final Map<UUID, AnimatedBossbar> CACHE = new HashMap<>();

    /**
     * A random number generator used for selecting random messages, colors, or styles.
     */
    @Getter(AccessLevel.NONE)
    private final Random random = new Random();

    /**
     * A unique identifier for this AnimatedBossbar instance.
     */
    private final UUID uuid = UUID.randomUUID();

    /**
     * The plugin instance that created this AnimatedBossbar.
     */
    private final Plugin plugin;

    @Getter(AccessLevel.NONE)
    private final TakionLib lib;

    /**
     * A mapping between players and their associated BossBar objects.
     */
    private final Map<Player, BossBar> bossbars;

    /**
     * The task ID of the currently running animation scheduler, or -1 if no animation is active.
     */
    private GlobalTask task = null;

    /**
     * The list of messages to be displayed on the BossBar.
     */
    private final List<String> messages;

    /**
     * The list of {@link BarColor} values used to color the BossBar.
     */
    private final List<BarColor> colors;

    /**
     * The list of {@link BarStyle} values used to style the BossBar.
     */
    private final List<BarStyle> styles;

    /**
     * The progress type of the animation, which determines how the BossBar's progress value changes over time.
     * Can be {@link Progress#INCREASE}, {@link Progress#DECREASE}, or {@link Progress#STATIC}.
     */
    private Progress type = Progress.DECREASE;

    /**
     * The "stale" progress value used when the progress type is STATIC.
     * This value is a double between 0.0 and 1.0.
     */
    private double staleProgress = 1.0;

    /**
     * A map that holds the state of various elements (e.g., messages, colors, styles) for this BossBar.
     * Each element can be set to true (randomized) or false (not randomized).
     */
    @Getter(AccessLevel.NONE)
    private final EnumMap<Element, Boolean> elements = new EnumMap<>(Element.class);

    /**
     * The duration (in seconds) for the animation.
     * This can be set externally to control the speed of the BossBar's progress update.
     */
    @Accessors(chain = true)
    @Setter
    private double duration = 0.0;

    /**
     * Constructs a new AnimatedBossbar with the specified plugin, messages, colors, and styles.
     *
     * @param plugin   the plugin instance that manages this bossbar
     * @param messages a list of messages to display on the BossBar; leading spaces are stripped for formatting
     * @param colors   a list of BossBar colors to cycle through during the animation
     * @param styles   a list of BossBar styles to cycle through during the animation
     */
    public AnimatedBossbar(Plugin plugin, List<String> messages, List<BarColor> colors, List<BarStyle> styles) {
        this.plugin = Objects.requireNonNull(plugin);
        this.lib = TakionLib.fromPlugin(plugin);

        this.messages = messages;
        this.colors = colors;
        this.styles = styles;
        bossbars = new HashMap<>();

        for (Element element : Element.values())
            elements.put(element, false);

        // Remove any unnecessary leading spaces from messages.
        this.messages.replaceAll(PlainFormat.TRIM_START_SPACES::accept);
        CACHE.put(uuid, this);
    }

    /**
     * Constructs a new AnimatedBossbar with a single message, color, and style.
     *
     * @param plugin  the plugin instance managing this bossbar
     * @param message the message to display on the BossBar
     * @param color   the color of the BossBar
     * @param style   the style of the BossBar
     */
    public AnimatedBossbar(Plugin plugin, String message, BarColor color, BarStyle style) {
        this(plugin, ArrayUtils.toList(message), ArrayUtils.toList(color), ArrayUtils.toList(style));
    }

    /**
     * Constructs a new AnimatedBossbar with a single message, default color (WHITE), and default style (SOLID).
     *
     * @param plugin  the plugin instance managing this bossbar
     * @param message the message to display on the BossBar
     */
    public AnimatedBossbar(Plugin plugin, String message) {
        this(plugin, message, BarColor.WHITE, BarStyle.SOLID);
    }

    /**
     * Constructs a new AnimatedBossbar with no initial message.
     *
     * @param plugin the plugin instance managing this bossbar
     */
    public AnimatedBossbar(Plugin plugin) {
        this(plugin, "", BarColor.WHITE, BarStyle.SOLID);
    }

    /**
     * Constructs a new AnimatedBossbar using configuration settings from the provided section.
     *
     * @param plugin  the plugin instance managing this bossbar
     * @param section the configuration section containing bossbar settings
     */
    public AnimatedBossbar(Plugin plugin, ConfigurationSection section) {
        this(plugin);
        Objects.requireNonNull(section);
        setMessages(Configurable.toStringList(section, "messages"));
        setColors(Configurable.toStringList(section, "colors"));
        setStyles(Configurable.toStringList(section, "styles"));

        List<String> list = Configurable.toStringList(section, "randomize");
        for (Element element : Element.values())
            elements.put(element, list.contains(element.name()));

        setProgressType(section.getString("progress-type", ""));
        setStaleProgress(section.getDouble("stale-progress"));
        setDuration(section.getDouble("duration"));
    }

    /**
     * Updates the messages used by this AnimatedBossbar.
     * <p>
     * The provided list of messages will replace any existing messages.
     * Leading spaces are stripped from each message.
     * </p>
     *
     * @param messages a list of new messages
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar setMessages(List<String> messages) {
        Objects.requireNonNull(messages);
        messages.replaceAll(PlainFormat.TRIM_START_SPACES::accept);
        this.messages.clear();
        this.messages.addAll(messages);
        return this;
    }

    /**
     * Updates the messages using an array of strings.
     *
     * @param messages an array of new messages
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar setMessages(String... messages) {
        return setMessages(ArrayUtils.toList(messages));
    }

    /**
     * Sets the BossBar colors using a collection of {@link BarColor}.
     *
     * @param colors a collection of BossBar colors
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar setColors(Collection<BarColor> colors) {
        Objects.requireNonNull(colors);
        this.colors.clear();
        this.colors.addAll(colors);
        return this;
    }

    /**
     * Sets the BossBar colors using an array of {@link BarColor}.
     *
     * @param colors an array of BossBar colors
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar setColors(BarColor... colors) {
        return setColors(ArrayUtils.toList(colors));
    }

    /**
     * Sets the BossBar colors using a list of strings.
     * <p>
     * Each string is parsed into a {@link BarColor}. If parsing fails, {@link BarColor#WHITE} is used.
     * </p>
     *
     * @param colors a list of strings representing colors
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar setColors(List<String> colors) {
        Objects.requireNonNull(colors);
        this.colors.clear();
        for (String c : colors) {
            BarColor color = BarColor.WHITE;
            try {
                color = BarColor.valueOf(c);
            } catch (Exception ignored) {}
            this.colors.add(color);
        }
        return this;
    }

    /**
     * Sets the BossBar colors using an array of strings.
     *
     * @param colors an array of strings representing colors
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar setColors(String... colors) {
        return setColors(ArrayUtils.toList(colors));
    }

    /**
     * Sets the BossBar styles using a collection of {@link BarStyle}.
     *
     * @param styles a collection of BossBar styles
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar setStyles(Collection<BarStyle> styles) {
        Objects.requireNonNull(styles);
        this.styles.clear();
        this.styles.addAll(styles);
        return this;
    }

    /**
     * Sets the BossBar styles using an array of {@link BarStyle}.
     *
     * @param styles an array of BossBar styles
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar setStyles(BarStyle... styles) {
        return setStyles(ArrayUtils.toList(styles));
    }

    /**
     * Sets the BossBar styles using a list of strings.
     * <p>
     * Each string is converted to a {@link BarStyle} using the {@link Holder#styleOf(String)} method.
     * </p>
     *
     * @param styles a list of strings representing styles
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar setStyles(List<String> styles) {
        Objects.requireNonNull(styles);
        this.styles.clear();
        for (String s : styles)
            this.styles.add(Holder.styleOf(s));
        return this;
    }

    /**
     * Sets the BossBar styles using an array of strings.
     *
     * @param styles an array of strings representing styles
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar setStyles(String... styles) {
        return setStyles(ArrayUtils.toList(styles));
    }

    /**
     * Sets the progress animation type.
     *
     * @param progress the progress type (INCREASE, DECREASE, or STATIC)
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar setProgressType(Progress progress) {
        this.type = progress;
        return this;
    }

    /**
     * Sets the progress animation type using a string.
     * <p>
     * The provided string is converted to uppercase and matched to a {@link Progress} value.
     * If the conversion fails, the current progress type is retained.
     * </p>
     *
     * @param progress a string representing the progress type
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar setProgressType(String progress) {
        try {
            return setProgressType(Progress.valueOf(progress.toUpperCase(Locale.ENGLISH)));
        } catch (Exception e) {
            return this;
        }
    }

    /**
     * Sets the state of a specific element (e.g., messages, colors, styles) to be randomized.
     *
     * <p> If {@code random} is true, the element will be selected randomly during the animation.
     *
     * @param element the element identifier (e.g., Element.MESSAGES)
     * @param random  whether the element should be randomized
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar setElementState(Element element, boolean random) {
        Objects.requireNonNull(element, "Element cannot be null");
        elements.put(element, random);
        return this;
    }

    /**
     * Sets the "stale" progress value for static progress animations.
     * <p>
     * The progress value is clamped between 0.0 and 1.0.
     * </p>
     *
     * @param progress the desired progress value
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar setStaleProgress(double progress) {
        this.staleProgress = Math.max(0.0, Math.min(progress, 1.0));
        return this;
    }

    /**
     * Adds a viewer (player) to this AnimatedBossbar.
     * <p>
     * A new BossBar is created for the player if one does not already exist.
     * </p>
     *
     * @param player the player to add
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar addViewer(Player player) {
        if (bossbars.containsKey(player))
            return this;

        BossBar bossBar = Holder.emptyBossbar();
        bossBar.addPlayer(player);
        bossbars.put(player, bossBar);
        return this;
    }

    /**
     * Adds multiple viewers (players) to this AnimatedBossbar.
     *
     * @param players a collection of players to add
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar addViewers(Collection<? extends Player> players) {
        Objects.requireNonNull(players).forEach(this::addViewer);
        return this;
    }

    /**
     * Removes a viewer (player) from this AnimatedBossbar.
     *
     * @param player the player to remove
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar removeViewer(Player player) {
        BossBar bossBar = bossbars.remove(player);
        if (bossBar != null)
            bossBar.removePlayer(player);
        return this;
    }

    /**
     * Removes multiple viewers (players) from this AnimatedBossbar.
     *
     * @param players a collection of players to remove
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar removeViewers(Collection<? extends Player> players) {
        Objects.requireNonNull(players).forEach(this::removeViewer);
        return this;
    }

    /**
     * Checks if a given element identifier is set to be randomized.
     *
     * @param element the element identifier (e.g., "messages")
     * @return {@code true} if the element is randomized; {@code false} otherwise
     */
    boolean isRandom(Element element) {
        return elements.get(element);
    }

    /**
     * Checks if the animation is currently running.
     *
     * @return {@code true} if the animation is running; {@code false} otherwise
     */
    public boolean isRunning() {
        return task != null && task.isRunning();
    }

    /**
     * Starts the animation of the BossBar.
     * <p>
     * This method schedules a repeating task that updates the BossBar's title, color, style, and progress
     * according to the configured messages, colors, and styles. The progress of the BossBar can increase,
     * decrease, or remain static depending on the {@link Progress} setting.
     * </p>
     */
    public void startAnimation() {
        if (isRunning())
            throw new IllegalStateException("Animation is already running. Please stop it before starting a new one.");

        if (duration < 0.0) return;

        if (type == Progress.STATIC)
            bossbars.values().forEach(b -> b.setProgress(staleProgress));

        task = lib.getScheduler().runTaskTimer(plugin, new Runnable() {

            private int tick = 0;
            private final int totalTicks = (int) (duration * 20);

            @Override
            public void run() {
                if (tick >= totalTicks) {
                    deleteBossbar();
                    return;
                }

                bossbars.keySet().removeIf(p -> !p.isOnline());

                int tickPerMessage = messages.isEmpty() ? 1 : totalTicks / messages.size();
                int tickPerColor = colors.isEmpty() ? 1 : totalTicks / colors.size();
                int tickPerStyle = styles.isEmpty() ? 1 : totalTicks / styles.size();

                final int currentTick = tick; // para lambda

                bossbars.forEach((player, bossBar) -> {
                    if (type != Progress.STATIC) {
                        double step = tick * (1.0 / totalTicks);
                        double progressValue = type == Progress.INCREASE ? Math.min(1.0, step) : Math.max(0.0, 1.0 - step);
                        bossBar.setProgress(progressValue);
                    }

                    if (!messages.isEmpty() && tick % tickPerMessage == 0) {
                        int idxMsg = isRandom(Element.MESSAGES)
                                ? random.nextInt(messages.size())
                                : (currentTick / tickPerMessage) % messages.size();
                        String message = messages.get(idxMsg);
                        bossBar.setTitle(lib.colorize(player, message));
                    }

                    if (!colors.isEmpty() && tick % tickPerColor == 0) {
                        int idxColor = isRandom(Element.COLORS)
                                ? random.nextInt(colors.size())
                                : (currentTick / tickPerColor) % colors.size();
                        bossBar.setColor(colors.get(idxColor));
                    }

                    if (!styles.isEmpty() && tick % tickPerStyle == 0) {
                        int idxStyle = isRandom(Element.STYLES)
                                ? random.nextInt(styles.size())
                                : (currentTick / tickPerStyle) % styles.size();
                        bossBar.setStyle(styles.get(idxStyle));
                    }
                });

                tick++;
            }
        }, 0L, 1L);
    }

    /**
     * Stops the current animation if it is running.
     * <p>
     * Cancels the scheduled task and resets the task ID to -1.
     * </p>
     */
    public void stopAnimation() {
        if (isRunning()) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Clears all BossBars associated with this AnimatedBossbar.
     * <p>
     * Stops any active animations and removes all BossBars from the cache.
     * </p>
     */
    private void clearBossbar() {
        stopAnimation();
        bossbars.values().forEach(BossBar::removeAll);
        bossbars.clear();
    }

    /**
     * Deletes this AnimatedBossbar, stopping any active animations and removing all BossBars.
     * <p>
     * The AnimatedBossbar instance is also removed from the global cache.
     * </p>
     */
    public void deleteBossbar() {
        clearBossbar();
        CACHE.remove(uuid, this);
    }

    /**
     * Unregisters a specific player from all AnimatedBossbar instances.
     * <p>
     * This method removes the player from each bossbar in the cache and deletes any AnimatedBossbar
     * that no longer has viewers.
     * </p>
     *
     * @param player the player to unregister
     */
    public static void unregister(Player player) {
        CACHE.values().removeIf(b -> {
            b.removeViewer(player);
            
            if (!b.bossbars.isEmpty())
                return false;

            b.clearBossbar();
            return true;
        });
    }

    /**
     * Unregisters all AnimatedBossbar instances associated with a specific plugin.
     * <p>
     * This method clears the cache and deletes all AnimatedBossbar instances.
     * </p>
     *
     * @param plugin the plugin whose bossbars should be unregistered
     */
    public static void unregisterAll(Plugin plugin) {
        CACHE.values().removeIf(b -> {
            if (Objects.equals(b.plugin, plugin)) {
                b.clearBossbar();
                return true;
            }
            return false;
        });
    }

    /**
     * Unregisters all AnimatedBossbar instances associated with any plugin.
     * <p>
     * This method clears the cache and deletes all AnimatedBossbar instances.
     * </p>
     */
    public static void unregisterAll() {
        CACHE.values().forEach(AnimatedBossbar::clearBossbar);
        CACHE.clear();
    }

    @UtilityClass
    private static class Holder {

        BossBar emptyBossbar() {
            return Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
        }

        BarStyle styleOf(String string) {
            try {
                return Objects.requireNonNull(BarStyle.valueOf(string));
            } catch (Exception e) {
                return BarStyle.SOLID;
            }
        }
    }
}
