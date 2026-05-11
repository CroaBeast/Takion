package me.croabeast.common;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface Colorizer {

    String colorize(Player target, Player parser, String string);
}
