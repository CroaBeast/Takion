package me.croabeast.common.discord;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Minimal Discord webhook client with support for placeholder replacement and embeds.
 *
 * <p>The placeholder token is usually {@code {message}} and gets replaced with the provided message
 * in all configurable text fields.
 */
@Accessors(chain = true, fluent = true)
@Setter
public final class Webhook {

    private static final String DEFAULT_MESSAGE_TOKEN = "{message}";
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 15_000;

    private final String url;
    private final String token;
    private final String message;

    private boolean enabled = true;

    private String content;
    private String username;
    private String avatarUrl;

    private boolean tts;

    private final List<Embed> embeds = new ArrayList<>();

    /**
     * Creates a webhook sender.
     *
     * @param url target Discord webhook URL
     * @param token placeholder token to replace (for example {@code {message}})
     * @param message replacement value used for the token
     */
    public Webhook(String url, String token, String message) {
        this.url = url == null ? "" : url;
        this.token = token;
        this.message = message;
    }

    /**
     * Builds a {@link Webhook} from configuration using {@code {message}} as token.
     *
     * @param sec configuration section
     * @return configured webhook instance
     */
    public static Webhook fromConfig(ConfigurationSection sec) {
        return fromConfig(sec, DEFAULT_MESSAGE_TOKEN, null);
    }

    /**
     * Builds a webhook from a configuration section.
     *
     * @param sec configuration section
     * @param token placeholder token used in configured strings
     * @param message replacement for the token
     * @return configured webhook instance
     * @throws IllegalArgumentException if {@code sec} is {@code null}
     */
    public static Webhook fromConfig(ConfigurationSection sec, String token, String message) {
        if (sec == null) {
            throw new IllegalArgumentException("ConfigurationSection cannot be null");
        }

        Webhook hook = new Webhook(sec.getString("url"), token, message);
        hook.enabled = sec.getBoolean("enabled", true);
        hook.content = hook.applyTemplate(sec.getString("content"));
        hook.tts = sec.getBoolean("tts", false);
        hook.username = sec.getString("username");
        hook.avatarUrl = sec.getString("avatar-url");
        hook.loadEmbeds(sec.getConfigurationSection("embeds"));
        return hook;
    }

    private void loadEmbeds(ConfigurationSection embedsSection) {
        if (embedsSection == null) return;

        for (String key : embedsSection.getKeys(false)) {
            ConfigurationSection embedSection = embedsSection.getConfigurationSection(key);
            if (embedSection != null) addEmbed(buildEmbed(embedSection));
        }
    }

    private Embed buildEmbed(ConfigurationSection section) {
        Embed embed = new Embed(token, message)
                .title(section.getString("title"))
                .description(section.getString("description"))
                .url(section.getString("url"))
                .footer(section.getString("footer.text"), section.getString("footer.icon-url"))
                .thumbnail(section.getString("thumbnail-url"))
                .image(section.getString("image-url"))
                .author(section.getString("author.name"),
                        section.getString("author.url"),
                        section.getString("author.icon-url"))
                .color(section.getString("color"));

        ConfigurationSection fieldsSection = section.getConfigurationSection("fields");
        if (fieldsSection == null) return embed;

        for (String fieldKey : fieldsSection.getKeys(false)) {
            ConfigurationSection fieldSection = fieldsSection.getConfigurationSection(fieldKey);
            if (fieldSection != null)
                embed.field(
                        fieldSection.getString("name"),
                        fieldSection.getString("value"),
                        fieldSection.getBoolean("inline", false)
                );
        }

        return embed;
    }

    /**
     * Sets webhook content after applying token replacement.
     *
     * @param content message content
     * @return current instance for chaining
     */
    public Webhook content(String content) {
        this.content = applyTemplate(content);
        return this;
    }

    /**
     * Adds an embed to this webhook.
     *
     * @param embed embed to add
     * @return current instance for chaining
     */
    public Webhook addEmbed(Embed embed) {
        if (embed != null) this.embeds.add(embed);
        return this;
    }

    /**
     * Sends the webhook payload synchronously.
     *
     * @return {@code true} if Discord responded with a 2xx status code
     */
    public boolean send() {
        if (!enabled || StringUtils.isBlank(url)) return false;

        try {
            byte[] body = toJson().getBytes(StandardCharsets.UTF_8);
            HttpsURLConnection connection = openConnection();

            try (OutputStream output = connection.getOutputStream()) {
                output.write(body);
            }

            return connection.getResponseCode() / 100 == 2;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private HttpsURLConnection openConnection() throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("User-Agent", "DiscordWebhook/2.0");
        return connection;
    }

    /**
     * Overrides content and sends the webhook synchronously.
     *
     * @param overrideMessage message to send as content
     * @return {@code true} if Discord responded with a 2xx status code
     */
    public boolean send(String overrideMessage) {
        if (overrideMessage != null)
            this.content(applyTemplate(overrideMessage));
        return send();
    }

    /**
     * Sends the webhook asynchronously.
     */
    public void sendAsync() {
        CompletableFuture.runAsync(this::send);
    }

    /**
     * Overrides content and sends the webhook asynchronously.
     *
     * @param overrideMessage message to send as content
     */
    public void sendAsync(String overrideMessage) {
        CompletableFuture.runAsync(() -> send(overrideMessage));
    }

    private String toJson() {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfPresent(payload, "content", content);
        putIfPresent(payload, "username", username);
        putIfPresent(payload, "avatar_url", avatarUrl);
        payload.put("tts", tts);

        if (!embeds.isEmpty()) {
            List<Object> embedsArray = new ArrayList<>();
            for (Embed embed : embeds) {
                embedsArray.add(embed.toMap());
            }
            payload.put("embeds", embedsArray);
        }

        return Json.write(payload);
    }

    private String applyTemplate(String value) {
        if (value == null) return null;
        return StringUtils.isNotBlank(token) && message != null ? value.replace(token, message) : value;
    }

    private static void putIfPresent(Map<String, Object> map, String key, Object value) {
        if (value == null) return;
        if (value instanceof String && ((String) value).isEmpty())
            return;

        map.put(key, value);
    }

    private static Integer parseColor(String s) {
        if (s == null) return null;

        s = s.trim();

        try {
            if (s.startsWith("#"))
                return Integer.parseInt(s.substring(1), 16);

            if (s.startsWith("0x") || s.startsWith("0X"))
                return Integer.parseInt(s.substring(2), 16);

            return Integer.parseInt(s);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    /**
     * Builder for Discord embed payloads.
     */
    @Setter(AccessLevel.NONE)
    public static final class Embed {

        private final String token;
        private final String message;

        private String title;
        private String description;
        private String url;
        private String thumbnail;
        private String image;
        private String footerText;
        private String footerIcon;
        private String authorName;
        private String authorUrl;
        private String authorIcon;

        private Integer color;

        private final List<Field> fields = new ArrayList<>();

        /**
         * Creates an embed without placeholder replacement.
         */
        public Embed() {
            this(null, null);
        }

        private Embed(String token, String message) {
            this.token = token;
            this.message = message;
        }

        private String applyTemplate(String value) {
            if (value == null) return null;

            if (token != null && message != null)
                return value.replace(token, message);

            return value;
        }

        public Embed title(String s) {
            this.title = applyTemplate(s);
            return this;
        }

        public Embed description(String s) {
            this.description = applyTemplate(s);
            return this;
        }

        public Embed url(String s) {
            this.url = applyTemplate(s);
            return this;
        }

        public Embed thumbnail(String s) {
            this.thumbnail = applyTemplate(s);
            return this;
        }

        public Embed image(String s) {
            this.image = applyTemplate(s);
            return this;
        }

        public Embed footer(String text, String iconUrl) {
            this.footerText = applyTemplate(text);
            this.footerIcon = applyTemplate(iconUrl);
            return this;
        }

        public Embed author(String name, String url, String iconUrl) {
            this.authorName = applyTemplate(name);
            this.authorUrl = applyTemplate(url);
            this.authorIcon = applyTemplate(iconUrl);
            return this;
        }

        public Embed color(String color) {
            this.color = parseColor(applyTemplate(color));
            return this;
        }

        public Embed field(String name, String value, boolean inline) {
            fields.add(new Field(applyTemplate(name), applyTemplate(value), inline));
            return this;
        }

        private Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            putIfPresent(map, "title", title);
            putIfPresent(map, "description", description);
            putIfPresent(map, "url", url);
            if (color != null) map.put("color", color);

            if (footerText != null || footerIcon != null) {
                Map<String, Object> footerMap = new LinkedHashMap<>();
                putIfPresent(footerMap, "text", footerText);
                putIfPresent(footerMap, "icon_url", footerIcon);
                map.put("footer", footerMap);
            }

            if (thumbnail != null)
                map.put("thumbnail", Collections.singletonMap("url", thumbnail));

            if (image != null)
                map.put("image", Collections.singletonMap("url", image));

            if (authorName != null || authorUrl != null || authorIcon != null) {
                Map<String, Object> authorMap = new LinkedHashMap<>();
                putIfPresent(authorMap, "name", authorName);
                putIfPresent(authorMap, "url", authorUrl);
                putIfPresent(authorMap, "icon_url", authorIcon);
                map.put("author", authorMap);
            }

            if (!fields.isEmpty()) {
                List<Object> fieldArray = new ArrayList<>();
                for (Field field : fields)
                    fieldArray.add(field.toMap());
                map.put("fields", fieldArray);
            }

            return map;
        }

        /**
         * Single embed field.
         */
        public static final class Field {
            private final String name;
            private final String value;
            private final boolean inline;

            Field(String name, String value, boolean inline) {
                this.name = name;
                this.value = value;
                this.inline = inline;
            }

            private Map<String, Object> toMap() {
                Map<String, Object> map = new LinkedHashMap<>();
                putIfPresent(map, "name", name);
                putIfPresent(map, "value", value);
                map.put("inline", inline);
                return map;
            }
        }
    }

    private static final class Json {

        static String write(Object o) {
            StringBuilder sb = new StringBuilder();
            writeVal(sb, o);
            return sb.toString();
        }

        @SuppressWarnings("unchecked")
        private static void writeVal(StringBuilder sb, Object v) {
            if (v == null) {
                sb.append("null");
                return;
            }

            if (v instanceof String) {
                sb.append('"').append(escape((String) v)).append('"');
                return;
            }

            if (v instanceof Number || v instanceof Boolean) {
                sb.append(v);
                return;
            }

            if (v instanceof Map) {
                Map<String, Object> m = (Map<String, Object>) v;
                sb.append('{');
                boolean first = true;
                for (Map.Entry<String, Object> e : m.entrySet()) {
                    if (!first) {
                        sb.append(',');
                    }
                    first = false;
                    sb.append('"').append(escape(e.getKey())).append("\":");
                    writeVal(sb, e.getValue());
                }
                sb.append('}');
                return;
            }

            if (v instanceof Iterable) {
                sb.append('[');
                boolean first = true;
                for (Object it : (Iterable<?>) v) {
                    if (!first) {
                        sb.append(',');
                    }
                    first = false;
                    writeVal(sb, it);
                }
                sb.append(']');
                return;
            }

            sb.append('"').append(escape(String.valueOf(v))).append('"');
        }

        private static String escape(String s) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '\\':
                    case '"':
                        sb.append('\\').append(c);
                        break;
                    case '\b':
                        sb.append("\\b");
                        break;
                    case '\f':
                        sb.append("\\f");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\r':
                        sb.append("\\r");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    default:
                        if (c < 32) {
                            sb.append(String.format("\\u%04x", (int) c));
                        } else {
                            sb.append(c);
                        }
                }
            }
            return sb.toString();
        }
    }
}
