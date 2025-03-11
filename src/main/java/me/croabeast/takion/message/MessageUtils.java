package me.croabeast.takion.message;

import lombok.experimental.UtilityClass;
import me.croabeast.lib.reflect.Reflector;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.function.UnaryOperator;

@UtilityClass
public class MessageUtils {

    boolean test(Player player, String message) {
        if (player == null) return false;

        if (ReflectUtils.VERSION < 11.0) {
            try {
                Class<?> clazz = ReflectUtils.from(null, "IChatBaseComponent").getDeclaredClasses()[0];
                final Object result = "{\"text\":\"" + message + "\"}";

                Reflector.from(() -> player).callAsReflector("getHandle")
                        .callAsReflector("playerConnection")
                        .call("sendPacket", Reflector.of(clazz).call("a", result));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        return true;
    }

    public boolean sendActionBar(Player player, String message) {
        if (player == null) return false;

        if (ReflectUtils.VERSION < 11.0) {
            try {
                Method method = player.getClass().getMethod("getHandle");
                Object handle = method.invoke(player);

                method = handle.getClass().getMethod("playerConnection");
                handle = method.invoke(handle);

                method = handle.getClass().getMethod(
                        "sendPacket",
                        ReflectUtils.from(null, "PacketPlayOutChat")
                );

                method.invoke(handle, ((UnaryOperator<Object>) (s) -> {
                    Class<?> legacyClass = ReflectUtils.from(null, "IChatBaseComponent");
                    legacyClass = legacyClass.getDeclaredClasses()[0];

                    try {
                        Method m = legacyClass.getMethod("a", String.class);
                        return m.invoke(null, "{\"text\":\"" + s + "\"}");
                    } catch (Exception e) {
                        return null;
                    }
                }).apply(message));
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        return true;
    }
}
