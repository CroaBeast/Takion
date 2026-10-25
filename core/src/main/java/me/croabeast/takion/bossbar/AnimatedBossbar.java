package me.croabeast.takion.bossbar;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;

/**
 * Provides a dynamic, animated BossBar display for players.
 * <p>
 * The {@code AnimatedBossbar} class creates and manages a collection of BossBars that can display
 * animated messages, colors, and styles based on a configurable sequence. It supports dynamic updates,
 * allowing text, color, and style to be cycled over time. Animations can progress in different modes (increase,
 * decrease, or remain static) and use both random and synchronized selections for messages, colors, and styles.
 * </p>
 * <p>
 * A bar with a positive {@link #setDuration(double) duration} is deleted when it runs out and then runs
 * its {@link #setOnFinish(Runnable) finish callback}; a duration of 0 or less keeps it until it is stopped
 * or deleted, looping its animation every {@value #LOOP_TICKS} ticks. A bar with nothing to animate (one
 * message, color and style and a static or provided progress) runs no repeating task at all. Call
 * {@link #update()} to show changed messages, colors, styles or progress right away.
 * </p>
 * <p>
 * Titles and progress can differ per viewer through {@link #setTitleFormatter(BiFunction)} and
 * {@link #setProgressProvider(ToDoubleFunction)}. Viewers that quit are dropped on the next frame or
 * update; a bar that runs no task keeps them until {@link #removeViewer(Player)} or {@link #unregister(Player)}.
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
     * Ticks one loop of the animation lasts when the bar has no duration.
     */
    public static final int LOOP_TICKS = 100;

    /**
     * A cache that holds all active AnimatedBossbar instances, keyed by their unique UUID.
     */
    private static final Map<UUID, AnimatedBossbar> CACHE = new ConcurrentHashMap<>();

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
     * The BossBar shown to each viewer, keyed by the viewer's UUID.
     */
    private final Map<UUID, BossBar> bossbars = new ConcurrentHashMap<>();

    /**
     * The task driving the animation or the end of the duration, or {@code null} when none is needed.
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
     * The duration (in seconds) for the animation. 0 or less keeps the bar until it is stopped or deleted.
     */
    @Accessors(chain = true)
    @Setter
    private double duration = 0.0;

    /**
     * Ticks between two frames of an animated bar. Higher values send fewer packets.
     */
    @Accessors(chain = true)
    @Setter
    private int updateInterval = 1;

    /**
     * Builds the title each viewer sees from the current message, before it is colorized.
     */
    @Accessors(chain = true)
    @Setter
    private BiFunction<Player, String, String> titleFormatter = (player, message) -> message;

    /**
     * When set, gives each viewer's progress in place of the progress type.
     */
    @Accessors(chain = true)
    @Setter
    private ToDoubleFunction<Player> progressProvider = null;

    /**
     * Runs after the bar is deleted because its duration ran out.
     */
    @Accessors(chain = true)
    @Setter
    private Runnable onFinish = null;

    @Getter(AccessLevel.NONE)
    private boolean running = false;
    @Getter(AccessLevel.NONE)
    private int tick = 0;
    @Getter(AccessLevel.NONE)
    private final int[] segments = new int[Element.values().length], indices = new int[Element.values().length];
    @Getter(AccessLevel.NONE)
    private String lastMessage = null;

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

        this.messages = new ArrayList<>(messages);
        this.colors = new ArrayList<>(colors);
        this.styles = new ArrayList<>(styles);

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
        setUpdateInterval(section.getInt("update-interval", 1));
    }

    /**
     * Updates the messages used by this AnimatedBossbar.
     * <p>
     * The provided list of messages will replace any existing messages.
     * Leading spaces are stripped from each message; the given list is left untouched.
     * </p>
     *
     * @param messages a list of new messages
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar setMessages(List<String> messages) {
        List<String> copy = new ArrayList<>(Objects.requireNonNull(messages));
        copy.replaceAll(PlainFormat.TRIM_START_SPACES::accept);
        this.messages.clear();
        this.messages.addAll(copy);
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
        List<BarColor> copy = new ArrayList<>(Objects.requireNonNull(colors));
        this.colors.clear();
        this.colors.addAll(copy);
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
     * Each string is parsed, ignoring case, into a {@link BarColor}. If parsing fails, {@link BarColor#WHITE} is used.
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
                color = BarColor.valueOf(c.trim().toUpperCase(Locale.ENGLISH));
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
        List<BarStyle> copy = new ArrayList<>(Objects.requireNonNull(styles));
        this.styles.clear();
        this.styles.addAll(copy);
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
     * Each string is parsed, ignoring case, into a {@link BarStyle}. If parsing fails, {@link BarStyle#SOLID} is used.
     * </p>
     *
     * @param styles a list of strings representing styles
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar setStyles(List<String> styles) {
        Objects.requireNonNull(styles);
        this.styles.clear();
        for (String s : styles) {
            BarStyle style = BarStyle.SOLID;
            try {
                style = BarStyle.valueOf(s.trim().toUpperCase(Locale.ENGLISH));
            } catch (Exception ignored) {}
            this.styles.add(style);
        }
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
        this.staleProgress = clamp(progress);
        return this;
    }

    /**
     * Adds a viewer (player) to this AnimatedBossbar.
     * <p>
     * A new BossBar is created for the player if one does not already exist. When the animation is
     * running, the player sees the current frame right away.
     * </p>
     *
     * @param player the player to add
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar addViewer(Player player) {
        CACHE.putIfAbsent(uuid, this);
        if (bossbars.containsKey(player.getUniqueId()))
            return this;

        BossBar bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
        if (running) {
            int period = period();
            apply(player, bossBar, pick(messages, Element.MESSAGES, period),
                    pick(colors, Element.COLORS, period),
                    pick(styles, Element.STYLES, period), step(period), true);
        }

        bossBar.addPlayer(player);
        bossbars.put(player.getUniqueId(), bossBar);
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
        BossBar bossBar = bossbars.remove(player.getUniqueId());
        if (bossBar != null)
            bossBar.removeAll();
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
     * Checks if a player is one of this bar's viewers.
     *
     * @param player the player to check
     * @return {@code true} if the player sees this bar
     */
    public boolean hasViewer(Player player) {
        return bossbars.containsKey(player.getUniqueId());
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
     * Checks if the animation is currently running, from {@link #startAnimation()} until it is
     * stopped, deleted or its duration runs out.
     *
     * @return {@code true} if the animation is running; {@code false} otherwise
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Checks if the bar changes on its own over time, so it needs a task for every frame.
     *
     * @return {@code true} if there is more than one message, color or style, or a moving progress
     */
    public boolean isAnimated() {
        return messages.size() > 1 || colors.size() > 1 || styles.size() > 1 ||
                (progressProvider == null && type != Progress.STATIC);
    }

    /**
     * Starts the animation of the BossBar, restarting it from its first frame if it is already running.
     * <p>
     * The first frame is shown right away. An animated bar then updates every {@link #getUpdateInterval()}
     * ticks; a bar with nothing to animate only waits for its duration to run out, or for nothing at all
     * when it has no duration.
     * </p>
     */
    public void startAnimation() {
        stopAnimation();

        tick = 0;
        lastMessage = null;
        Arrays.fill(segments, -1);
        running = true;
        render(true);

        int total = totalTicks();
        if (isAnimated()) {
            int interval = Math.max(1, updateInterval);
            task = lib.getScheduler().runTaskTimer(plugin, () -> {
                tick += interval;
                if (total > 0 && tick >= total) {
                    finish();
                    return;
                }

                render(false);
                if (bossbars.isEmpty()) deleteBossbar();
            }, interval, interval);
        }
        else if (total > 0)
            task = lib.getScheduler().runTaskLater(plugin, this::finish, total);
    }

    /**
     * Shows the current messages, colors, styles and progress to every viewer right away, without
     * restarting the animation. Titles and provided progress are evaluated again.
     *
     * @return this AnimatedBossbar instance (for chaining)
     */
    public AnimatedBossbar update() {
        if (running) render(true);
        return this;
    }

    /**
     * Stops the current animation if it is running. The viewers keep seeing the last frame.
     */
    public void stopAnimation() {
        running = false;
        if (task != null) {
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
     * The AnimatedBossbar instance is also removed from the global cache. Adding a viewer again
     * registers it back.
     * </p>
     */
    public void deleteBossbar() {
        clearBossbar();
        CACHE.remove(uuid, this);
    }

    private void finish() {
        Runnable callback = onFinish;
        deleteBossbar();
        if (callback != null) callback.run();
    }

    private int totalTicks() {
        return duration > 0 ? (int) Math.min(Integer.MAX_VALUE, duration * 20) : 0;
    }

    private int period() {
        int total = totalTicks();
        return total > 0 ? total : LOOP_TICKS;
    }

    private double step(int period) {
        return (double) (tick % period) / period;
    }

    private <T> T pick(List<T> list, Element element, int period) {
        if (list.isEmpty()) return null;

        int size = list.size(), e = element.ordinal();
        int segment = (tick % period) / Math.max(1, period / size);

        if (segment != segments[e] || indices[e] >= size) {
            segments[e] = segment;
            indices[e] = isRandom(element) ? random.nextInt(size) : segment % size;
        }

        return list.get(indices[e]);
    }

    private void render(boolean force) {
        int period = period();
        double step = step(period);

        String message = pick(messages, Element.MESSAGES, period);
        BarColor color = pick(colors, Element.COLORS, period);
        BarStyle style = pick(styles, Element.STYLES, period);

        boolean retitle = force || !Objects.equals(message, lastMessage);
        lastMessage = message;

        bossbars.entrySet().removeIf(entry -> {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
                entry.getValue().removeAll();
                return true;
            }

            apply(player, entry.getValue(), message, color, style, step, retitle);
            return false;
        });
    }

    private void apply(Player player, BossBar bossBar, String message, BarColor color, BarStyle style,
                       double step, boolean retitle)
    {
        double progress = progressProvider != null ?
                clamp(progressProvider.applyAsDouble(player)) :
                type == Progress.INCREASE ? step :
                        type == Progress.DECREASE ? 1.0 - step : staleProgress;

        if (bossBar.getProgress() != progress) bossBar.setProgress(progress);

        if (retitle && message != null) {
            String title = lib.colorize(player, titleFormatter.apply(player, message));
            if (!title.equals(bossBar.getTitle())) bossBar.setTitle(title);
        }

        if (color != null && bossBar.getColor() != color) bossBar.setColor(color);
        if (style != null && bossBar.getStyle() != style) bossBar.setStyle(style);
    }

    private static double clamp(double progress) {
        return Math.max(0.0, Math.min(progress, 1.0));
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
}
