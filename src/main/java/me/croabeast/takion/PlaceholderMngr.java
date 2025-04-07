package me.croabeast.takion;

import me.croabeast.common.Rounder;
import me.croabeast.common.applier.StringApplier;
import me.croabeast.takion.placeholder.Placeholder;
import me.croabeast.takion.placeholder.PlaceholderManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

final class PlaceholderMngr implements PlaceholderManager {

    private final Set<Placeholder<?>> placeholders = new HashSet<>();
    private final Set<Placeholder<?>> defaults = new HashSet<>();

    private <N extends Number> Function<Player, N> fromLoc(Function<Location, N> function) {
        return p -> Rounder.round(function.apply(p.getLocation()));
    }

    {
        load0("{player}", HumanEntity::getName);
        load0("{playerDisplayName}", Player::getDisplayName);
        load0("{playerUUID}", Entity::getUniqueId);
        load0("{playerWorld}", p -> p.getWorld().getName());
        load0("{playerGameMode}", HumanEntity::getGameMode);
        load0("{playerX}", fromLoc(Location::getX));
        load0("{playerY}", fromLoc(Location::getY));
        load0("{playerZ}", fromLoc(Location::getZ));
        load0("{playerYaw}", fromLoc(Location::getYaw));
        load0("{playerPitch}", fromLoc(Location::getPitch));
    }

    private <T> boolean load0(Placeholder<T> placeholder, boolean def) {
        if (def) defaults.add(new Placeholder<>(placeholder));
        return placeholders.add(placeholder);
    }

    private <T> void load0(String key, Function<Player, T> function) {
        load0(new Placeholder<>(key, function), true);
    }

    private Placeholder<?> remove0(String key) {
        Placeholder<?> result = null;
        for (Placeholder<?> replacer : placeholders) {
            if (!replacer.getKey().equals(key))
                continue;
            placeholders.remove(result = replacer);
            break;
        }
        return result;
    }

    @Override
    public boolean remove(String key) {
        return remove0(key) != null;
    }

    @Override
    public <T> boolean load(Placeholder<T> placeholder) {
        return load0(placeholder, false);
    }

    @Override
    public boolean edit(String oldKey, String newKey) {
        Placeholder<?> placeholder = remove0(oldKey);
        return placeholder != null && load0(new Placeholder<>(newKey, placeholder.getFunction()), false);
    }

    @Override
    public String replace(Player player, String string, boolean sensitive) {
        if (player == null || StringUtils.isBlank(string))
            return string;

        StringApplier applier = StringApplier.simplified(string);
        for (Placeholder<?> placeholder : placeholders)
            applier.apply(s -> placeholder.replace(player, s));

        return applier.toString();
    }

    public void setDefaults() {
        placeholders.clear();
        placeholders.addAll(defaults);
    }
}
