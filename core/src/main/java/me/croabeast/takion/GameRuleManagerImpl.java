package me.croabeast.takion;

import lombok.RequiredArgsConstructor;
import me.croabeast.scheduler.GlobalTask;
import me.croabeast.takion.rule.GameRule;
import me.croabeast.takion.rule.GameRuleManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
final class GameRuleManagerImpl implements GameRuleManager {

    final Map<World, Map<GameRule<?>, Object>> data = new ConcurrentHashMap<>();

    private final TakionLib library;
    private GlobalTask task = null;

    @Override
    public boolean isLoaded() {
        return task != null;
    }

    @NotNull
    Map<GameRule<?>, Object> getValues(World world) {
        return data.computeIfAbsent(world, w -> new ConcurrentHashMap<>());
    }

    @Override
    public void load() {
        if (isLoaded()) return;

        task = library.getScheduler().runTask(() -> {
            for (World world : Bukkit.getServer().getWorlds()) {
                Map<GameRule<?>, Object> map = getValues(world);

                for (GameRule<?> rule : GameRule.getRules())
                    try {
                        map.put(rule, rule.getValue(world));
                    } catch (Exception ignored) {}

                data.put(world, map);
            }
        });
    }

    @Override
    public void unload() {
        if (!isLoaded()) return;

        data.clear();

        if (task.isRunning()) task.cancel();
        task = null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getLoadedValue(World world, GameRule<T> rule) {
        return (T) getValues(world).get(rule);
    }
}
