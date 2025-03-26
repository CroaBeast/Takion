package me.croabeast.takion.message;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import me.croabeast.lib.file.Configurable;
import me.croabeast.takion.TakionLib;
import me.croabeast.lib.util.ArrayUtils;
import me.croabeast.lib.util.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

@Getter
public class AnimatedBossbar {

    private static final Map<UUID, AnimatedBossbar> CACHE = new HashMap<>();
    @Getter(AccessLevel.NONE)
    private final Random random = new Random();

    private final UUID uuid = UUID.randomUUID();

    private final Plugin plugin;
    private final Map<Player, BossBar> bossbars;

    private int taskId = -1;

    private final List<String> messages;
    private final List<BarColor> colors;
    private final List<BarStyle> styles;

    private Progress type = Progress.DECREASE;
    private double staleProgress = 1.0;

    private final Set<String> randomElements;
    private final Set<String> syncElements;

    @Accessors(chain = true)
    @Setter
    private double duration = 0.0;

    public AnimatedBossbar(Plugin plugin, List<String> messages, List<BarColor> colors, List<BarStyle> styles) {
        this.plugin = Objects.requireNonNull(plugin);

        this.messages = messages;
        this.colors = colors;
        this.styles = styles;

        bossbars = new HashMap<>();

        randomElements = new HashSet<>();
        syncElements = new HashSet<>();

        this.messages.replaceAll(TextUtils.STRIP_FIRST_SPACES);
        CACHE.put(uuid, this);
    }

    public AnimatedBossbar(Plugin plugin, String message, BarColor color, BarStyle style) {
        this(plugin, ArrayUtils.toList(message), ArrayUtils.toList(color), ArrayUtils.toList(style));
    }

    public AnimatedBossbar(Plugin plugin, String message) {
        this(plugin, message, BarColor.WHITE, BarStyle.SOLID);
    }

    public AnimatedBossbar(Plugin plugin) {
        this(plugin, "", BarColor.WHITE, BarStyle.SOLID);
    }

    public AnimatedBossbar(Plugin plugin, ConfigurationSection section) {
        this(plugin);
        Objects.requireNonNull(section);

        setMessages(Configurable.toStringList(section, "messages"));
        setColors(Configurable.toStringList(section, "colors"));
        setStyles(Configurable.toStringList(section, "styles"));

        setRandomElements(Configurable.toStringList(section, "randomize"));
        setSynchronizeElements(Configurable.toStringList(section, "synchronize"));

        setProgressType(section.getString("progress-type", ""));
        setStaleProgress(section.getDouble("stale-progress"));

        setDuration(section.getDouble("duration"));
    }

    public AnimatedBossbar setMessages(List<String> messages) {
        Objects.requireNonNull(messages);
        messages.replaceAll(TextUtils.STRIP_FIRST_SPACES);

        this.messages.clear();
        this.messages.addAll(messages);

        return this;
    }

    public AnimatedBossbar setMessages(String... messages) {
        return setMessages(ArrayUtils.toList(messages));
    }

    public AnimatedBossbar setColors(Collection<BarColor> colors) {
        Objects.requireNonNull(colors);

        this.colors.clear();
        this.colors.addAll(colors);
        return this;
    }

    public AnimatedBossbar setColors(BarColor... colors) {
        return setColors(ArrayUtils.toList(colors));
    }

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

    public AnimatedBossbar setColors(String... colors) {
        return setColors(ArrayUtils.toList(colors));
    }

    public AnimatedBossbar setStyles(Collection<BarStyle> styles) {
        Objects.requireNonNull(styles);

        this.styles.clear();
        this.styles.addAll(styles);
        return this;
    }

    public AnimatedBossbar setStyles(BarStyle... styles) {
        return setStyles(ArrayUtils.toList(styles));
    }

    public AnimatedBossbar setStyles(List<String> styles) {
        Objects.requireNonNull(styles);
        this.styles.clear();

        for (String s : styles)
            this.styles.add(Holder.styleOf(s));
        return this;
    }

    public AnimatedBossbar setStyles(String... styles) {
        return setStyles(ArrayUtils.toList(styles));
    }

    public AnimatedBossbar setProgressType(Progress progress) {
        this.type = progress;
        return this;
    }

    public AnimatedBossbar setProgressType(String progress) {
        try {
            return setProgressType(Progress.valueOf(progress.toUpperCase(Locale.ENGLISH)));
        } catch (Exception e) {
            return this;
        }
    }

    public AnimatedBossbar setSynchronizeElements(Collection<String> elements) {
        Objects.requireNonNull(elements);

        this.syncElements.clear();
        this.syncElements.addAll(elements);
        return this;
    }

    public AnimatedBossbar setSynchronizeElements(String... elements) {
        return setSynchronizeElements(ArrayUtils.toList(elements));
    }

    public AnimatedBossbar setRandomElements(Collection<String> elements) {
        Objects.requireNonNull(elements);

        this.randomElements.clear();
        this.randomElements.addAll(elements);
        return this;
    }

    public AnimatedBossbar setRandomElements(String... elements) {
        return setRandomElements(ArrayUtils.toList(elements));
    }

    public AnimatedBossbar setStaleProgress(double progress) {
        this.staleProgress = Math.max(0.0, Math.min(progress, 1.0));
        return this;
    }

    public AnimatedBossbar addViewer(Player player) {
        if (bossbars.containsKey(player))
            return this;

        BossBar bossBar = Holder.emptyBossbar();
        bossBar.addPlayer(player);

        bossbars.put(player, bossBar);
        return this;
    }

    public AnimatedBossbar addViewers(Collection<? extends Player> players) {
        Objects.requireNonNull(players).forEach(this::addViewer);
        return this;
    }

    public AnimatedBossbar removeViewer(Player player) {
        BossBar bossBar = bossbars.remove(player);
        if (bossBar != null)
            bossBar.removePlayer(player);

        return this;
    }

    public AnimatedBossbar removeViewers(Collection<? extends Player> players) {
        Objects.requireNonNull(players).forEach(this::removeViewer);
        return this;
    }

    boolean isRandom(String element) {
        return randomElements.contains(element);
    }

    boolean isSync(String element) {
        return !syncElements.isEmpty() && syncElements.contains(element);
    }

    public void startAnimation() {
        stopAnimation();

        if (duration < 0.0) return;

        final int steps = (int) (duration * 20);
        double progressStep = 1.0 / steps;

        if (type == Progress.STATIC)
            bossbars.values().forEach(b -> b.setProgress(staleProgress));

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (tick >= steps) {
                    stopAnimation();
                    return;
                }

                int index = tick % Math.max(
                        messages != null ? messages.size() : 1,
                        Math.max(
                                colors != null ? colors.size() : 1,
                                styles != null ? styles.size() : 1
                        )
                );

                bossbars.keySet().removeIf(p -> !p.isOnline());

                bossbars.forEach((player, bossBar) -> {
                    if (type != Progress.STATIC) {
                        final double step = tick * progressStep,
                                result = type == Progress.INCREASE ?
                                        Math.min(1.0, step) :
                                        Math.max(0.0, 1.0 - step);

                        bossBar.setProgress(result);
                    }

                    if (messages != null && !messages.isEmpty()) {
                        String message = isRandom("messages") ?
                                messages.get(random.nextInt(messages.size())) :
                                messages.get(
                                        isSync("messages") ?
                                                index :
                                                tick % messages.size()
                                );

                        bossBar.setTitle(TakionLib.fromPlugin(plugin).colorize(player, message));
                    }

                    if (colors != null && !colors.isEmpty()) {
                        BarColor color = isRandom("colors") ?
                                colors.get(random.nextInt(colors.size())) :
                                colors.get(
                                        isSync("colors") ?
                                                index :
                                                tick % colors.size()
                                );

                        bossBar.setColor(color);
                    }

                    if (styles != null && !styles.isEmpty()) {
                        BarStyle style = isRandom("styles") ?
                                styles.get(random.nextInt(styles.size())) :
                                styles.get(
                                        isSync("styles") ?
                                                index :
                                                tick % styles.size()
                                );

                        bossBar.setStyle(style);
                    }
                });

                tick++;
            }
        }, 0L, 1L);
    }

    public void stopAnimation() {
        if (taskId == -1) return;

        Bukkit.getScheduler().cancelTask(taskId);
        taskId = -1;
    }

    public void deleteBossBar() {
        stopAnimation();

        bossbars.values().forEach(BossBar::removeAll);
        bossbars.clear();

        CACHE.remove(uuid, this);
    }

    public enum Progress {
        INCREASE,
        DECREASE,
        STATIC
    }

    public static void unregister(Player player) {
        for (AnimatedBossbar bossbar : CACHE.values()) {
            bossbar.removeViewer(player);
            if (bossbar.bossbars.isEmpty()) bossbar.deleteBossBar();
        }
    }

    public static void unregisterAll() {
        CACHE.values().forEach(AnimatedBossbar::deleteBossBar);
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
