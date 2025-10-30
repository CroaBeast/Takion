package me.croabeast.takion.rule;

import org.bukkit.World;

public interface GameRuleManager {

    boolean isLoaded();

    void load();

    void unload();

    <T> T getLoadedValue(World world, GameRule<T> rule);
}
