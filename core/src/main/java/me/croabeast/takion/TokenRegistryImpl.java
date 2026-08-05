package me.croabeast.takion;

import me.croabeast.takion.token.Token;
import me.croabeast.takion.token.TokenRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The one registry implementation, replacing the three that carried identical logic.
 *
 * <p>Insertion order is preserved because the order tokens resolve in is part of the output: a
 * token registered later must not silently start winning.
 */
class TokenRegistryImpl<K extends Token<?>> implements TokenRegistry<K> {

    private final Map<String, K> tokens = new LinkedHashMap<>();

    @Override
    public boolean load(@Nullable K token) {
        if (token == null) return false;

        String id = key(token.getId());
        return id != null && tokens.putIfAbsent(id, token) == null;
    }

    @Override
    public boolean remove(@Nullable String id) {
        String key = key(id);
        return key != null && tokens.remove(key) != null;
    }

    @Override
    public boolean edit(@Nullable String id, @Nullable K token) {
        String key = key(id);
        if (key == null || token == null || !tokens.containsKey(key)) return false;

        tokens.put(key, token);
        return true;
    }

    @Nullable
    @Override
    public K get(@Nullable String id) {
        String key = key(id);
        return key == null ? null : tokens.get(key);
    }

    @NotNull
    @Override
    public Collection<K> getAll() {
        return Collections.unmodifiableCollection(tokens.values());
    }

    @Override
    public void clear() {
        tokens.clear();
    }

    private static String key(String id) {
        return id == null || id.isEmpty() ? null : id.toLowerCase(Locale.ENGLISH);
    }
}
