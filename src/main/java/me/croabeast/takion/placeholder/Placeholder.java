package me.croabeast.takion.placeholder;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.croabeast.lib.util.ReplaceUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.function.Function;

@Getter
public class Placeholder<T> {

    final String key;
    final Function<Player, T> function;

    @Accessors(chain = true)
    @Setter
    private boolean sensitive = false;

    public Placeholder(String key, Function<Player, T> function) {
        if (StringUtils.isBlank(key))
            throw new NullPointerException("Key is empty/null");

        this.key = key;
        this.function = Objects.requireNonNull(function);
    }

    public Placeholder(String key, T value) {
        this(key, player -> value);
    }

    public Placeholder(Placeholder<T> placeholder) {
        this.key = placeholder.key;
        this.function = placeholder.function;
        this.sensitive = placeholder.sensitive;
    }

    public String replace(Player player, String string) {
        return ReplaceUtils.replace(key, function.apply(player), string, sensitive);
    }

    @Override
    public String toString() {
        return "Placeholder{key='" + key + '\'' + ", sensitive=" + sensitive + '}';
    }
}
