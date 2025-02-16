package me.croabeast.takion.placeholder;

import org.bukkit.entity.Player;

import java.util.function.Function;

public interface PlaceholderManager {

    <T> boolean load(Placeholder<T> placeholder);

    default <T> boolean load(String key, Function<Player, T> function) {
        return load(new Placeholder<>(key, function));
    }

    boolean remove(String key);

    boolean edit(String oldKey, String newKey);

    String replace(Player player, String string, boolean sensitive);

    default String replace(Player player, String string) {
        return replace(player, string, false);
    }

    void setDefaults();
}
