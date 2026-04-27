package me.croabeast.takion.rule;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.croabeast.vnc.VNC;
import org.apache.commons.lang.StringUtils;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("deprecation")
@UtilityClass
class RuleUtils {

    final Map<String, GameRule<?>> RULE_MAP = new LinkedHashMap<>();
    private final double VERSION = VNC.SERVER_VERSION;

    private interface RuntimeGameRule {

        String getLegacyName();

        String getLegacyFieldName();

        @Nullable
        String getModernFieldName();

        boolean isInverted();
    }

    static final class ResolvedRule {

        final org.bukkit.GameRule<?> rule;
        final String stringName;
        final boolean inverted;

        ResolvedRule(org.bukkit.GameRule<?> rule, String stringName, boolean inverted) {
            this.rule = rule;
            this.stringName = stringName;
            this.inverted = inverted;
        }
    }

    private abstract class RuleImpl<T> implements GameRule<T>, RuntimeGameRule {

        @Getter
        final String name;
        @Getter
        final Class<T> type;

        final String legacyFieldName;
        final String modernFieldName;
        final boolean inverted;

        final T def;
        final double min, last;

        RuleImpl(
                String name, Class<T> type, T def,
                double min, double last,
                @Nullable String modernFieldName, boolean inverted
        ) {
            this.name = name;
            this.type = type;
            this.legacyFieldName = toConstantName(name);
            this.modernFieldName = modernFieldName;
            this.inverted = inverted;
            this.def = def;
            this.min = min;
            this.last = last;
            RULE_MAP.put(name, this);
        }

        @NotNull
        public T getDefault() {
            return def;
        }

        final boolean isSupported() {
            return VERSION >= min && (last == 0 || VERSION <= last);
        }

        @Override
        public String getLegacyName() {
            return name;
        }

        @Override
        public String getLegacyFieldName() {
            return legacyFieldName;
        }

        @Override
        public String getModernFieldName() {
            return modernFieldName;
        }

        @Override
        public boolean isInverted() {
            return inverted;
        }

        final boolean isModern(ResolvedRule rule) {
            return rule != null && rule.inverted == inverted &&
                    StringUtils.isNotBlank(modernFieldName) &&
                    Objects.equals(rule.stringName, toSnakeCase(modernFieldName));
        }

        @SuppressWarnings("unchecked")
        final T coerceValue(Object value, boolean invert) {
            if (type == boolean.class) {
                boolean result;

                if (value instanceof Boolean)
                    result = (Boolean) value;
                else if (value instanceof String && ((String) value).matches("(?i)true|false"))
                    result = Boolean.parseBoolean((String) value);
                else
                    throw new IllegalArgumentException("Value is not a Boolean type.");

                return (T) Boolean.valueOf(invert ? !result : result);
            }

            if (type == int.class) {
                if (value instanceof Number)
                    return (T) Integer.valueOf(((Number) value).intValue());

                if (value instanceof String && StringUtils.isNotBlank((String) value))
                    return (T) Integer.valueOf(Integer.parseInt((String) value));

                throw new IllegalArgumentException("Value is not a Integer type.");
            }

            return (T) value;
        }

        final Object normalizeValue(T value, boolean invert) {
            if (type == boolean.class && value instanceof Boolean)
                return invert ? !((Boolean) value) : value;

            return value;
        }

        @Override
        public boolean setValue(World world, T value) {
            if (!isSupported()) return false;

            ResolvedRule resolved = resolveRule(this);
            if (resolved != null) {
                Boolean success = trySetTypedValue(world, resolved, normalizeValue(value, isModern(resolved)));
                if (success != null) return success;
            }

            for (ResolvedRule candidate : getStringCandidates(this)) {
                Object normalized = normalizeValue(value, candidate.inverted);
                Boolean success = trySetStringValue(world, candidate.stringName, String.valueOf(normalized));
                if (Boolean.TRUE.equals(success))
                    return true;
            }

            return false;
        }
    }

    final class BooleanRule extends RuleImpl<Boolean> {

        BooleanRule(
                String name, boolean def, double min, double last,
                @Nullable String modernFieldName, boolean inverted
        ) {
            super(name, boolean.class, def, min, last, modernFieldName, inverted);
        }

        @NotNull
        public Boolean getValue(World world) throws RuntimeException {
            if (!isSupported())
                throw new IllegalArgumentException("GameRule '" + name + "' not supported in this version.");

            ResolvedRule resolved = resolveRule(this);
            if (resolved != null) {
                Object value = tryGetTypedValue(world, resolved);
                if (value != null)
                    return coerceValue(value, isModern(resolved));
            }

            for (ResolvedRule candidate : getStringCandidates(this)) {
                String value = tryGetStringValue(world, candidate.stringName);
                if (StringUtils.isNotBlank(value))
                    return coerceValue(value, candidate.inverted);
            }

            throw new IllegalArgumentException("Value is not a Boolean type.");
        }
    }

    final class IntRule extends RuleImpl<Integer> {

        IntRule(
                String name, int def, double min, double last,
                @Nullable String modernFieldName
        ) {
            super(name, int.class, def, min, last, modernFieldName, false);
        }

        @Override
        public @NotNull Integer getValue(World world) throws RuntimeException {
            if (!isSupported())
                throw new IllegalArgumentException("GameRule '" + name + "' not supported in this version.");

            ResolvedRule resolved = resolveRule(this);
            if (resolved != null) {
                Object value = tryGetTypedValue(world, resolved);
                if (value != null)
                    return coerceValue(value, false);
            }

            for (ResolvedRule candidate : getStringCandidates(this)) {
                String value = tryGetStringValue(world, candidate.stringName);
                if (StringUtils.isNotBlank(value))
                    return coerceValue(value, false);
            }

            throw new IllegalArgumentException("Value is not a Integer type.");
        }
    }

    @NotNull
    GameRule<Boolean> boolRule(String name, boolean def, double min) {
        return boolRule(name, null, false, def, min, 0);
    }

    @NotNull
    GameRule<Boolean> boolRule(String name, boolean def, double min, double last) {
        return boolRule(name, null, false, def, min, last);
    }

    @NotNull
    GameRule<Boolean> boolRule(
            String name, @Nullable String modernFieldName, boolean inverted,
            boolean def, double min
    ) {
        return boolRule(name, modernFieldName, inverted, def, min, 0);
    }

    @NotNull
    GameRule<Boolean> boolRule(
            String name, @Nullable String modernFieldName, boolean inverted,
            boolean def, double min, double last
    ) {
        return new BooleanRule(name, def, min, last, modernFieldName, inverted);
    }

    @NotNull
    GameRule<Integer> intRule(String name, int def, double min, double last) {
        return intRule(name, null, def, min, last);
    }

    @NotNull
    GameRule<Integer> intRule(String name, @Nullable String modernFieldName, int def, double min, double last) {
        return new IntRule(name, def, min, last, modernFieldName);
    }

    @NotNull
    GameRule<Integer> intRule(String name, @Nullable String modernFieldName, int def, double min) {
        return intRule(name, modernFieldName, def, min, 0);
    }

    @NotNull
    GameRule<Integer> intRule(String name, int def, double min) {
        return intRule(name, def, min, 0);
    }

    @NotNull
    org.bukkit.GameRule<?> asBukkit(GameRule<?> rule) {
        ResolvedRule resolved = resolveRule(rule);
        return Objects.requireNonNull(resolved, "GameRule '" + rule.getName() + "' is not available on this server.").rule;
    }

    @Nullable
    private ResolvedRule resolveRule(GameRule<?> rule) {
        if (!(rule instanceof RuntimeGameRule)) return null;

        RuntimeGameRule runtime = (RuntimeGameRule) rule;
        String modernFieldName = runtime.getModernFieldName();

        org.bukkit.GameRule<?> legacyField = resolveField(org.bukkit.GameRule.class, runtime.getLegacyFieldName());
        if (legacyField != null)
            return new ResolvedRule(legacyField, runtime.getLegacyName(), false);

        org.bukkit.GameRule<?> legacyName = org.bukkit.GameRule.getByName(runtime.getLegacyName());
        if (legacyName != null)
            return new ResolvedRule(legacyName, runtime.getLegacyName(), false);

        if (StringUtils.isNotBlank(modernFieldName)) {
            org.bukkit.GameRule<?> modernField = resolveField(loadClass("org.bukkit.GameRules"), modernFieldName);
            if (modernField != null)
                return new ResolvedRule(modernField, toSnakeCase(modernFieldName), runtime.isInverted());

            org.bukkit.GameRule<?> modernName = org.bukkit.GameRule.getByName(toSnakeCase(modernFieldName));
            if (modernName != null)
                return new ResolvedRule(modernName, toSnakeCase(modernFieldName), runtime.isInverted());
        }

        return null;
    }

    @NotNull
    private List<ResolvedRule> getStringCandidates(RuntimeGameRule rule) {
        String modernFieldName = rule.getModernFieldName();

        if (StringUtils.isBlank(modernFieldName))
            return Collections.singletonList(new ResolvedRule(null, rule.getLegacyName(), false));

        String modernName = toSnakeCase(modernFieldName);
        if (Objects.equals(rule.getLegacyName(), modernName) && !rule.isInverted())
            return Collections.singletonList(new ResolvedRule(null, rule.getLegacyName(), false));

        return Arrays.asList(
                new ResolvedRule(null, rule.getLegacyName(), false),
                new ResolvedRule(null, modernName, rule.isInverted())
        );
    }

    @Nullable
    private Boolean trySetTypedValue(World world, ResolvedRule resolved, Object value) {
        Method method = findWorldMethod(world, "setGameRule", 2);
        if (method == null || resolved.rule == null) return null;

        try {
            Object result = method.invoke(world, resolved.rule, value);
            return result instanceof Boolean ? (Boolean) result : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private Boolean trySetStringValue(World world, String name, String value) {
        try {
            Method method = world.getClass().getMethod("setGameRuleValue", String.class, String.class);
            Object result = method.invoke(world, name, value);
            return result instanceof Boolean ? (Boolean) result : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private Object tryGetTypedValue(World world, ResolvedRule resolved) {
        Method method = findWorldMethod(world, "getGameRuleValue", 1);
        if (method == null || resolved.rule == null) return null;

        try {
            return method.invoke(world, resolved.rule);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private String tryGetStringValue(World world, String name) {
        try {
            Method method = world.getClass().getMethod("getGameRuleValue", String.class);
            Object result = method.invoke(world, name);
            return result == null ? null : String.valueOf(result);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private Method findWorldMethod(World world, String name, int parameterCount) {
        for (Method method : world.getClass().getMethods()) {
            if (!Objects.equals(method.getName(), name) || method.getParameterTypes().length != parameterCount)
                continue;

            Class<?>[] parameters = method.getParameterTypes();
            if (!org.bukkit.GameRule.class.isAssignableFrom(parameters[0]))
                continue;

            return method;
        }

        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private org.bukkit.GameRule<?> resolveField(@Nullable Class<?> type, String name) {
        if (type == null || StringUtils.isBlank(name)) return null;

        try {
            Field field = type.getField(name);
            Object value = field.get(null);
            return value instanceof org.bukkit.GameRule ? (org.bukkit.GameRule<?>) value : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private Class<?> loadClass(String name) {
        try {
            return Class.forName(name);
        } catch (Exception ignored) {
            return null;
        }
    }

    @NotNull
    private String toConstantName(String name) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < name.length(); i++) {
            char current = name.charAt(i);
            if (Character.isUpperCase(current) && builder.length() > 0)
                builder.append('_');

            builder.append(Character.toUpperCase(current));
        }

        return builder.toString();
    }

    @NotNull
    private String toSnakeCase(String constantName) {
        return constantName.toLowerCase(Locale.ENGLISH);
    }
}
