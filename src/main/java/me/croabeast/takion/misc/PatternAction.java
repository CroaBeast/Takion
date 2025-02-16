package me.croabeast.takion.misc;

import lombok.Getter;
import lombok.Setter;
import me.croabeast.lib.Regex;
import me.croabeast.lib.util.ArrayUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter @Setter
public abstract class PatternAction<T> {

    @Regex
    private String pattern;

    public PatternAction(@Regex String pattern) {
        this.pattern = pattern;
    }

    @NotNull
    public Matcher createMatcher(String string) {
        return Pattern.compile(getPattern()).matcher(string);
    }

    @NotNull
    public abstract T act(Collection<? extends Player> players, String string);

    @NotNull
    public T act(Player player, String string) {
        return act(ArrayUtils.toList(player), string);
    }

    @NotNull
    public T act(String string) {
        return act((Collection<Player>) null, string);
    }
}
