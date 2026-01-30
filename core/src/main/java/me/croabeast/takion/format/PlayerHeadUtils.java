package me.croabeast.takion.format;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.Proxy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

@UtilityClass
class PlayerHeadUtils {

    private final String REGEX = "(?i)\\{player_head(?::([^}]+))?}";
    private final String DISPLAY_MARKER = "■";
    private final Map<String, String> TEXTURE_CACHE = new ConcurrentHashMap<>();
    private final Map<String, UUID> UUID_CACHE = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry> LOOKUP_CACHE = new ConcurrentHashMap<>();
    private final long CACHE_TTL_MILLIS = TimeUnit.MINUTES.toMillis(10);

    private volatile YggdrasilAuthenticationService authService;
    private volatile MinecraftSessionService sessionService;
    private volatile GameProfileRepository profileRepository;

    private enum CacheStatus {
        PENDING,
        READY,
        FAILED
    }

    private final class CacheEntry {
        private final String texture;
        private final long timestamp;
        private final CacheStatus status;

        private CacheEntry(String texture, long timestamp, CacheStatus status) {
            this.texture = texture;
            this.timestamp = timestamp;
            this.status = status;
        }
    }

    final StringFormat FORMAT = new StringFormat() {
        @NotNull
        public String getRegex() {
            return REGEX;
        }

        @NotNull
        public String accept(String string) {
            return accept(null, string);
        }

        @NotNull
        public String accept(Player player, String string) {
            return replace(player, string);
        }
    };

    public String replace(Player parser, String input) {
        if (StringUtils.isBlank(input)) return input;

        Matcher matcher = FORMAT.matcher(input);
        if (!matcher.find()) return input;

        StringBuilder result = new StringBuilder();
        int lastEnd = 0;

        matcher.reset();
        while (matcher.find()) {
            result.append(input, lastEnd, matcher.start());

            String name = matcher.group(1);
            String replacement = buildHeadComponent(parser, name);
            result.append(replacement == null ? matcher.group() : replacement);

            lastEnd = matcher.end();
        }

        result.append(input.substring(lastEnd));
        return result.toString();
    }

    @SuppressWarnings("deprecation")
    private String buildHeadComponent(Player parser, String name) {
        Player target = parser;

        if (StringUtils.isNotBlank(name)) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
            target = offline.getPlayer();
            if (target == null && offline.hasPlayedBefore())
                return buildFromOfflineProfile(offline);
        }

        if (target == null && StringUtils.isBlank(name)) return null;
        if (target != null) {
            String texture = resolveTexture(target.getName(), target.getUniqueId());
            return buildMarker(target.getName(), target.getUniqueId(), texture);
        }

        return StringUtils.isNotBlank(name) ? buildFromMojangProfile(name) : null;
    }

    private String buildFromOfflineProfile(OfflinePlayer offline) {
        String name = offline.getName();
        UUID uuid = offline.getUniqueId();
        String texture = resolveTexture(name, uuid);
        return buildMarker(name, uuid, texture);
    }

    private String buildFromMojangProfile(String name) {
        String texture = resolveTexture(name, UUID_CACHE.get(name.toLowerCase()));
        return buildMarker(name, UUID_CACHE.get(name.toLowerCase()), texture);
    }

    private String buildMarker(String name, UUID uuid, String textureValue) {
        if (StringUtils.isBlank(textureValue)) return null;

        StringBuilder json = new StringBuilder()
                .append("{\"id\":\"minecraft:player_head\",\"Count\":1,\"tag\":{")
                .append("\"SkullOwner\":{");

        if (uuid != null)
            json.append("\"Id\":\"").append(uuid).append("\",");

        if (StringUtils.isNotBlank(name))
            json.append("\"Name\":\"").append(escapeJson(name)).append("\",");

        json.append("\"Properties\":{\"textures\":[{\"Value\":\"")
                .append(textureValue)
                .append("\"}]}}}").append("}");

        String escaped = json.toString().replace("\"", "\\\"");
        return "<hover_item:\"" + escaped + "\">" + DISPLAY_MARKER + "</text>";
    }

    private String resolveTexture(String name, UUID uuid) {
        if (StringUtils.isBlank(name) && uuid == null) return null;

        String cacheKey = StringUtils.isNotBlank(name) ? name.toLowerCase() : uuid.toString();

        String cached = TEXTURE_CACHE.get(cacheKey);
        if (StringUtils.isNotBlank(cached)) return cached;

        CacheEntry entry = LOOKUP_CACHE.get(cacheKey);
        if (entry != null && isRunning(entry))
            return entry.status == CacheStatus.READY ? entry.texture : null;

        enqueueLookup(name, uuid, cacheKey);
        return null;
    }

    private void enqueueLookup(String name, UUID uuid, String cacheKey) {
        LOOKUP_CACHE.compute(cacheKey, (key, entry) ->
                entry != null && isRunning(entry) ?
                        entry :
                        new CacheEntry(null, System.currentTimeMillis(), CacheStatus.PENDING)
        );

        runAsync(() -> {
            if (uuid != null || StringUtils.isBlank(name)) {
                String value = fetchTextureValue(new GameProfile(uuid, name), name, uuid);
                writeCache(cacheKey, value);
                return;
            }

            GameProfileRepository repository = getProfileRepository();
            try {
                repository.findProfilesByNames(new String[] {name}, Agent.MINECRAFT, new ProfileLookupCallback() {
                    @Override
                    public void onProfileLookupSucceeded(GameProfile profile) {
                        if (profile != null) {
                            UUID_CACHE.put(name.toLowerCase(), profile.getId());
                            String value = fetchTextureValue(profile, name, profile.getId());
                            writeCache(cacheKey, value);
                            return;
                        }

                        writeCache(cacheKey, null);
                    }

                    @Override
                    public void onProfileLookupFailed(GameProfile profile, Exception exception) {
                        writeCache(cacheKey, null);
                    }
                });
            } catch (Exception e) {
                writeCache(cacheKey, null);
            }
        });
    }

    private boolean isRunning(CacheEntry entry) {
        return System.currentTimeMillis() - entry.timestamp < CACHE_TTL_MILLIS;
    }

    private String fetchTextureValue(GameProfile profile, String name, UUID uuid) {
        GameProfile filled;
        try {
            MinecraftSessionService session = getSessionService();
            filled = session.fillProfileProperties(profile, true);
        } catch (Exception e) {
            return null;
        }

        Property texture = filled.getProperties().get("textures").stream().findFirst().orElse(null);
        if (texture == null) return null;

        String value = texture.getValue();
        if (StringUtils.isBlank(value)) return null;

        if (StringUtils.isNotBlank(name))
            TEXTURE_CACHE.put(name.toLowerCase(), value);
        if (uuid != null)
            TEXTURE_CACHE.put(uuid.toString(), value);

        return value;
    }

    private void writeCache(String cacheKey, String value) {
        CacheEntry result = new CacheEntry(
                value,
                System.currentTimeMillis(),
                StringUtils.isNotBlank(value) ? CacheStatus.READY : CacheStatus.FAILED
        );
        LOOKUP_CACHE.put(cacheKey, result);
    }

    private void runAsync(Runnable task) {
        CompletableFuture.runAsync(task);
    }

    private MinecraftSessionService getSessionService() {
        if (sessionService != null) return sessionService;
        synchronized (PlayerHeadUtils.class) {
            if (sessionService == null)
                sessionService = getAuthService().createMinecraftSessionService();
        }
        return sessionService;
    }

    private GameProfileRepository getProfileRepository() {
        if (profileRepository != null) return profileRepository;
        synchronized (PlayerHeadUtils.class) {
            if (profileRepository == null)
                profileRepository = getAuthService().createProfileRepository();
        }
        return profileRepository;
    }

    private YggdrasilAuthenticationService getAuthService() {
        if (authService != null) return authService;
        synchronized (PlayerHeadUtils.class) {
            if (authService == null)
                authService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
        }
        return authService;
    }

    private String escapeJson(String value) {
        return StringUtils.isBlank(value) ? value : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
