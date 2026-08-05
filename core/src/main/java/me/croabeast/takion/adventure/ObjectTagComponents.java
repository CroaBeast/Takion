package me.croabeast.takion.adventure;

import me.croabeast.takion.token.Token;
import lombok.experimental.UtilityClass;
import me.croabeast.takion.tag.Tag;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.object.SpriteObjectContents;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

@UtilityClass
class ObjectTagComponents {

    @Nullable
    Component playerHead(Token.Rendered result) {
        Map<String, Object> data = result.getData();
        if (data.isEmpty()) return null;

        PlayerHeadObjectContents.Builder builder = ObjectContents.playerHead()
                .hat(getBoolean(data, "hat", true));

        String texture = getString(data, "texture");
        if (StringUtils.isNotBlank(texture)) {
            builder.profileProperty(PlayerHeadObjectContents.property("textures", texture));
            return Component.object().contents(builder.build()).build();
        }

        String name = getString(data, "name");
        UUID uuid = getUuid(data, "uuid");

        if (StringUtils.isBlank(name) && uuid == null) return null;
        if (StringUtils.isNotBlank(name)) builder.name(name);
        if (uuid != null) builder.id(uuid);

        return Component.object().contents(builder.build()).build();
    }

    @Nullable
    Component sprite(Token.Rendered result) {
        Map<String, Object> data = result.getData();
        String spriteValue = getString(data, "sprite");
        if (StringUtils.isBlank(spriteValue)) return null;

        Key sprite = toKey(spriteValue);
        if (sprite == null) return null;

        String atlasValue = getString(data, "atlas");
        SpriteObjectContents contents;

        if (StringUtils.isBlank(atlasValue)) {
            contents = ObjectContents.sprite(sprite);
        } else {
            Key atlas = toKey(atlasValue);
            if (atlas == null) return null;
            contents = ObjectContents.sprite(atlas, sprite);
        }

        return Component.object().contents(contents).build();
    }

    @Nullable
    private Key toKey(String value) {
        return StringUtils.isNotBlank(value) && Key.parseable(value) ? Key.key(value) : null;
    }

    @Nullable
    private String getString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null ? null : String.valueOf(value);
    }

    @Nullable
    private UUID getUuid(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof UUID) return (UUID) value;

        if (value instanceof String) {
            try {
                return UUID.fromString((String) value);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        return null;
    }

    private boolean getBoolean(Map<String, Object> data, String key, boolean fallback) {
        Object value = data.get(key);
        return value instanceof Boolean ? (Boolean) value : fallback;
    }
}
