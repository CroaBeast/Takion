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

@Accessors(chain = true, fluent = true)
@Setter
public final class Webhook {

    private final String url, token, message;
    private boolean enabled = true;

    private String content, username, avatarUrl;
    private boolean tts;

    private final List<Embed> embeds = new ArrayList<>();

    public Webhook(String url, String token, String message) {
        this.url = url == null ? "" : url;;
        this.token = token;
        this.message = message;
    }

    public static Webhook fromConfig(ConfigurationSection sec) {
        return fromConfig(sec, "{message}", null);
    }

    public static Webhook fromConfig(ConfigurationSection sec, String token, String message) {
        if (sec == null)
            throw new IllegalArgumentException("ConfigurationSection cannot be null");

        final String url = sec.getString("url");

        Webhook hook = new Webhook(url, token, message);
        hook.enabled = sec.getBoolean("enabled", true);
        hook.content = hook.apply(sec.getString("content"));
        hook.tts = sec.getBoolean("tts", false);
        hook.username = sec.getString("username");
        hook.avatarUrl = sec.getString("avatar-url");

        ConfigurationSection embedsSec = sec.getConfigurationSection("embeds");
        if (embedsSec != null)
            for (String k : embedsSec.getKeys(false)) {
                ConfigurationSection em = embedsSec.getConfigurationSection(k);
                if (em == null) continue;

                Embed e = new Embed(hook.token, hook.message)
                        .title(em.getString("title"))
                        .description(em.getString("description"))
                        .url(em.getString("url"))
                        .footer(em.getString("footer.text"), em.getString("footer.icon-url"))
                        .thumbnail(em.getString("thumbnail-url"))
                        .image(em.getString("image-url"))
                        .author(em.getString("author.name"),
                                em.getString("author.url"),
                                em.getString("author.icon-url"))
                        .color(em.getString("color"));

                ConfigurationSection fieldsSec = em.getConfigurationSection("fields");
                if (fieldsSec != null)
                    for (String fk : fieldsSec.getKeys(false)) {
                        ConfigurationSection fs = fieldsSec.getConfigurationSection(fk);
                        if (fs == null) continue;
                        e.field(fs.getString("name"),
                                fs.getString("value"),
                                fs.getBoolean("inline", false));
                    }

                hook.addEmbed(e);
            }

        return hook;
    }

    public Webhook content(String content) {
        this.content = apply(content);
        return this;
    }

    public Webhook addEmbed(Embed embed) {
        if (embed != null) this.embeds.add(embed);
        return this;
    }

    public boolean send() {
        if (!enabled || StringUtils.isBlank(url)) return false;
        try {
            String json = toJson();
            byte[] body = json.getBytes(StandardCharsets.UTF_8);

            HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(15_000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("User-Agent", "DiscordWebhook/2.0");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body);
            }

            return conn.getResponseCode() / 100 == 2; // 2xx OK
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean send(String overrideMessage) {
        if (overrideMessage != null)
            this.content(apply(overrideMessage));
        return send();
    }

    public void sendAsync() {
        CompletableFuture.runAsync(this::send);
    }

    public void sendAsync(String overrideMessage) {
        CompletableFuture.runAsync(() -> send(overrideMessage));
    }

    private String toJson() {
        Map<String, Object> root = new LinkedHashMap<>();
        putIf(root, "content", content);
        putIf(root, "username", username);
        putIf(root, "avatar_url", avatarUrl);
        root.put("tts", tts);

        if (!embeds.isEmpty()) {
            List<Object> es = new ArrayList<>();
            for (Embed e : embeds) es.add(e.toMap());
            root.put("embeds", es);
        }
        return Json.write(root);
    }

    private String apply(String s) {
        if (s == null) return null;
        return StringUtils.isNotBlank(token) && message != null ? s.replace(token, message) : s;
    }

    private static void putIf(Map<String, Object> map, String k, Object v) {
        if (v == null) return;
        if (v instanceof String && ((String) v).isEmpty()) return;
        map.put(k, v);
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
        }
        catch (Exception ignored) {
            return null;
        }
    }

    // ----------------- Embed builder -----------------

    @Setter(AccessLevel.NONE)
    public static final class Embed {

        private final String token, message;

        private String title, description, url;
        private String thumbnail, image;
        private String footerText, footerIcon;
        private String authorName, authorUrl, authorIcon;
        private Integer color;

        private final List<Field> fields = new ArrayList<>();

        public Embed() {
            this(null, null);
        }

        private Embed(String token, String message) {
            this.token = token; this.message = message;
        }

        private String apply(String s) {
            if (s == null) return null;
            if (token != null && message != null) return s.replace(token, message);
            return s;
        }

        public Embed title(String s) { this.title = apply(s); return this; }
        public Embed description(String s) { this.description = apply(s); return this; }
        public Embed url(String s) { this.url = apply(s); return this; }
        public Embed thumbnail(String s) { this.thumbnail = apply(s); return this; }
        public Embed image(String s) { this.image = apply(s); return this; }
        public Embed footer(String text, String iconUrl) { this.footerText = apply(text); this.footerIcon = apply(iconUrl); return this; }
        public Embed author(String name, String url, String iconUrl) {
            this.authorName = apply(name); this.authorUrl = apply(url); this.authorIcon = apply(iconUrl); return this;
        }
        public Embed color(String color) { this.color = parseColor(apply(color)); return this; }
        public Embed field(String name, String value, boolean inline) {
            fields.add(new Field(apply(name), apply(value), inline)); return this;
        }

        private Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            putIf(m, "title", title);
            putIf(m, "description", description);
            putIf(m, "url", url);
            if (color != null) m.put("color", color);

            if (footerText != null || footerIcon != null) {
                Map<String, Object> fm = new LinkedHashMap<>();
                putIf(fm, "text", footerText);
                putIf(fm, "icon_url", footerIcon);
                m.put("footer", fm);
            }
            if (thumbnail != null) m.put("thumbnail", Collections.singletonMap("url", thumbnail));
            if (image != null) m.put("image", Collections.singletonMap("url", image));

            if (authorName != null || authorUrl != null || authorIcon != null) {
                Map<String, Object> am = new LinkedHashMap<>();
                putIf(am, "name", authorName);
                putIf(am, "url", authorUrl);
                putIf(am, "icon_url", authorIcon);
                m.put("author", am);
            }
            if (!fields.isEmpty()) {
                List<Object> arr = new ArrayList<>();
                for (Field f : fields) arr.add(f.toMap());
                m.put("fields", arr);
            }
            return m;
        }

        public static final class Field {
            private final String name, value;
            private final boolean inline;

            Field(String name, String value, boolean inline) {
                this.name = name; this.value = value; this.inline = inline;
            }

            private Map<String, Object> toMap() {
                Map<String, Object> m = new LinkedHashMap<>();
                putIf(m, "name", name);
                putIf(m, "value", value);
                m.put("inline", inline);
                return m;
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
                    if (!first) sb.append(',');
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
                    if (!first) sb.append(',');
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
                    case '\\': case '"': sb.append('\\').append(c); break;
                    case '\b': sb.append("\\b"); break;
                    case '\f': sb.append("\\f"); break;
                    case '\n': sb.append("\\n"); break;
                    case '\r': sb.append("\\r"); break;
                    case '\t': sb.append("\\t"); break;
                    default:
                        if (c < 32) sb.append(String.format("\\u%04x", (int) c));
                        else sb.append(c);
                }
            }
            return sb.toString();
        }
    }
}
