package me.croabeast.takion.format;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
class PlayerHeadUtils {

    private final Pattern TOKEN_PATTERN = Pattern.compile("(?i)(?:\\{player_head(?::([^}]*))?}|<player_head(?::([^>]*))?>)");
    private final String REGEX = TOKEN_PATTERN.pattern();
    private final String DISPLAY_MARKER = "■";
    private final String BASE64_PREFIX = "b64:";

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

        @Override
        public String removeFormat(String string) {
            return stripTokens(string);
        }
    };

    String replace(Player parser, String input) {
        return transform(parser, input, false);
    }

    private String stripTokens(String input) {
        return transform(null, input, true);
    }

    private String transform(Player parser, String input, boolean stripTokens) {
        if (StringUtils.isBlank(input)) return input;

        Matcher matcher = TOKEN_PATTERN.matcher(input);
        if (!matcher.find()) return input;

        StringBuilder result = new StringBuilder(input.length() + 32);
        int lastEnd = 0;

        matcher.reset();
        while (matcher.find()) {
            result.append(input, lastEnd, matcher.start());

            if (!stripTokens) {
                String replacement = buildLegacyHeadComponent(parser, matcher);
                if (replacement == null) result.append(matcher.group());
                else result.append(replacement);
            }

            lastEnd = matcher.end();
        }

        result.append(input, lastEnd, input.length());
        return result.toString();
    }

    private String buildLegacyHeadComponent(Player parser, MatchResult result) {
        HeadArguments args = parseArguments(rawArguments(result));

        if (StringUtils.isBlank(args.target))
            return parser == null ?
                    null :
                    buildMarker(parser.getName(), parser.getUniqueId(), null);

        if (isTextureValue(args.target))
            return buildMarker(null, null, args.target);

        UUID uuid = tryParseUuid(args.target);
        if (uuid != null)
            return buildMarker(resolveName(uuid), uuid, null);

        Player online = findOnlinePlayer(args.target);
        if (online != null)
            return buildMarker(online.getName(), online.getUniqueId(), null);

        @SuppressWarnings("deprecation")
        OfflinePlayer offline = Bukkit.getOfflinePlayer(args.target);
        UUID offlineUuid = offline.getUniqueId();
        String offlineName = offline.getName();

        boolean notBlank = StringUtils.isNotBlank(offlineName);
        return buildMarker(
                notBlank ? offlineName : args.target,
                notBlank ? offlineUuid : null,
                null
        );
    }

    private String buildMarker(String name, UUID uuid, String textureValue) {
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

    private String serializeHoverItem(String itemJson) {
        if (StringUtils.isBlank(itemJson)) return itemJson;

        return BASE64_PREFIX + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(itemJson.getBytes(StandardCharsets.UTF_8));
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

    @RequiredArgsConstructor
    @Getter
    private static final class HeadArguments {
        private final String target;
        private final boolean hat;
    }
}
