package me.croabeast.takion.rule;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.croabeast.common.util.ServerInfoUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
@UtilityClass
class RuleUtils {

    static final Map<String, GameRule<?>> RULE_MAP = new LinkedHashMap<>();
    private final double VERSION = ServerInfoUtils.SERVER_VERSION;

    private abstract class RuleImpl<T> implements GameRule<T> {

        @Getter
        final String name;
        @Getter
        final Class<T> type;

        final T def;
        final double min, last;

        RuleImpl(String name, Class<T> type, T def, double min, double last) {
            this.name = name;
            this.type = type;
            this.def = def;
            this.min = min;
            this.last = last;
            RULE_MAP.put(name, this);
        }

        @NotNull
        public T getDefault() {
            return def;
        }

        @Override
        public boolean setValue(World world, T value) {
            return VERSION >= min && VERSION <= last && world.setGameRuleValue(name, String.valueOf(value));
        }
    }

    final class BooleanRule extends RuleImpl<Boolean> {

        BooleanRule(String name, boolean def, double min, double last) {
            super(name, boolean.class, def, min, last);
        }

        @NotNull
        public Boolean getValue(World world) throws RuntimeException {
            if (VERSION < min || VERSION > last)
                throw new IllegalArgumentException("GameRule '" + name + "' not supported in this version.");

            String value = world.getGameRuleValue(name);
            if (StringUtils.isBlank(value) || !value.matches("(?i)true|false"))
                throw new IllegalArgumentException("Value is not a Boolean type.");

            return Boolean.parseBoolean(value);
        }
    }

    final class IntRule extends RuleImpl<Integer> {

        IntRule(String name, int def, double min, double last) {
            super(name, int.class, def, min, last);
        }

        @Override
        public @NotNull Integer getValue(World world) throws RuntimeException {
            if (VERSION < min || VERSION > last)
                throw new IllegalArgumentException("GameRule '" + name + "' not supported in this version.");

            String value = world.getGameRuleValue(name);
            if (StringUtils.isBlank(value))
                throw new IllegalArgumentException("Value is not a Integer type.");

            return Integer.parseInt(value);
        }
    }

    @NotNull
    GameRule<Boolean> boolRule(String name, boolean def, double min) {
        return new BooleanRule(name, def, min, 0);
    }

    @NotNull
    GameRule<Integer> intRule(String name, int def, double min, double last) {
        return new IntRule(name, def, min, last);
    }

    @NotNull
    GameRule<Integer> intRule(String name, int def, double min) {
        return intRule(name, def, min, 0);
    }
}
