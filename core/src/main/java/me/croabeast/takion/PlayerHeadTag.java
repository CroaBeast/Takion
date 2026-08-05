package me.croabeast.takion;

import me.croabeast.takion.token.Token;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.croabeast.takion.tag.Tag;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

final class PlayerHeadTag implements Tag {

    private static final Pattern PATTERN =
            Pattern.compile("(?i)(?:\\{player_head(?::([^}]*))?}|<player_head(?::([^>]*))?>)");
    private static final String DISPLAY_MARKER = "\u25A0";
    private static final String BASE64_PREFIX = "b64:";

    @NotNull
    public String getId() {
        return "player_head";
    }

    @NotNull
    public Pattern getPattern() {
        return PATTERN;
    }

    @NotNull
    public Token.Rendered resolve(@NotNull Token.Context context, @NotNull MatchResult match) {
        HeadArguments args = parseArguments(rawArguments(match));
        Player parser = context.getParser();
        Map<String, Object> data = new HashMap<>();
        data.put("hat", args.isHat());

        String legacy;
        if (StringUtils.isBlank(args.getTarget())) {
            if (parser == null) return unresolved(match);

            data.put("name", parser.getName());
            data.put("uuid", parser.getUniqueId());
            legacy = buildLegacyMarker(parser.getName(), parser.getUniqueId(), null);
        } else if (isTextureValue(args.getTarget())) {
            data.put("texture", args.getTarget());
            legacy = buildLegacyMarker(null, null, args.getTarget());
        } else {
            UUID uuid = tryParseUuid(args.getTarget());
            if (uuid != null) {
                String name = resolveName(uuid);
                data.put("uuid", uuid);
                if (StringUtils.isNotBlank(name)) data.put("name", name);
                legacy = buildLegacyMarker(name, uuid, null);
            } else {
                Player online = findOnlinePlayer(args.getTarget());
                if (online != null) {
                    data.put("name", online.getName());
                    data.put("uuid", online.getUniqueId());
                    legacy = buildLegacyMarker(online.getName(), online.getUniqueId(), null);
                } else {
                    @SuppressWarnings("deprecation")
                    OfflinePlayer offline = Bukkit.getOfflinePlayer(args.getTarget());
                    UUID offlineUuid = offline.getUniqueId();
                    String offlineName = offline.getName();
                    boolean notBlank = StringUtils.isNotBlank(offlineName);

                    data.put("name", notBlank ? offlineName : args.getTarget());
                    if (notBlank) data.put("uuid", offlineUuid);

                    legacy = buildLegacyMarker(
                            notBlank ? offlineName : args.getTarget(),
                            notBlank ? offlineUuid : null,
                            null
                    );
                }
            }
        }

        return new Token.SimpleRendered()
                .setLegacy(legacy)
                .setMeasure(DISPLAY_MARKER)
                .setData(data);
    }

    @NotNull
    private Token.Rendered unresolved(@NotNull MatchResult match) {
        String value = match.group();
        return new Token.SimpleRendered()
                .setLegacy(value)
                .setMeasure(value);
    }

    private String buildLegacyMarker(String name, UUID uuid, String textureValue) {
        StringBuilder json = new StringBuilder()
                .append("{\"id\":\"minecraft:player_head\",\"Count\":1");

        if (uuid != null || StringUtils.isNotBlank(name) || StringUtils.isNotBlank(textureValue)) {
            json.append(",\"tag\":{\"SkullOwner\":{");

            boolean needsComma = false;

            if (uuid != null) {
                json.append("\"Id\":\"").append(uuid).append("\"");
                needsComma = true;
            }

            if (StringUtils.isNotBlank(name)) {
                if (needsComma) json.append(',');
                json.append("\"Name\":\"").append(escapeJson(name)).append("\"");
                needsComma = true;
            }

            if (StringUtils.isNotBlank(textureValue)) {
                if (needsComma) json.append(',');
                json.append("\"Properties\":{\"textures\":[{\"Value\":\"")
                        .append(textureValue)
                        .append("\"}]}");
            }

            json.append("}}");
        }

        json.append('}');

        return "<hover_item:\""
                + serializeHoverItem(json.toString())
                + "\">"
                + DISPLAY_MARKER
                + "</text>";
    }

    private String rawArguments(MatchResult result) {
        for (int i = 1; i <= result.groupCount(); i++) {
            String group = result.group(i);
            if (group != null) return group;
        }
        return null;
    }

    private HeadArguments parseArguments(String rawArguments) {
        if (StringUtils.isBlank(rawArguments))
            return new HeadArguments(null, true);

        String[] parts = rawArguments.split(":", 2);
        String first = sanitize(parts[0]);

        if (parts.length == 1 && isBooleanToken(first))
            return new HeadArguments(null, Boolean.parseBoolean(first));

        boolean hat = parts.length != 2 || !isBooleanToken(parts[1]) ||
                Boolean.parseBoolean(parts[1]);

        return new HeadArguments(first, hat);
    }

    private boolean isTextureValue(String value) {
        return StringUtils.isNotBlank(value) && value.length() > 16 && tryParseUuid(value) == null;
    }

    private UUID tryParseUuid(String value) {
        if (StringUtils.isBlank(value)) return null;

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String resolveName(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);
        return online != null ? online.getName() : Bukkit.getOfflinePlayer(uuid).getName();
    }

    private Player findOnlinePlayer(String name) {
        Player player = Bukkit.getPlayerExact(name);
        if (player != null) return player;

        for (Player online : Bukkit.getOnlinePlayers())
            if (online.getName().equalsIgnoreCase(name))
                return online;

        return null;
    }

    private boolean isBooleanToken(String value) {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }

    private String sanitize(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private String escapeJson(String value) {
        return StringUtils.isBlank(value) ? value : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String serializeHoverItem(String itemJson) {
        if (StringUtils.isBlank(itemJson)) return itemJson;

        return BASE64_PREFIX + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(itemJson.getBytes(StandardCharsets.UTF_8));
    }

    @RequiredArgsConstructor
    @Getter
    private static final class HeadArguments {
        private final String target;
        private final boolean hat;
    }
}
