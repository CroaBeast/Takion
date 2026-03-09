package me.croabeast.takion.message;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

@UtilityClass
public class MessageUtils {

    public boolean sendActionBar(Player player, String message) {
        if (player == null) return false;

        if (ReflectUtils.VERSION < 11.0) {
            try {
                Object component = ReflectUtils.COMPONENT_SERIALIZER.apply(message);
                Class<?> baseComponent = ReflectUtils.BASE_COMP_CLASS;
                Class<?> packetClass = ReflectUtils.from(null, "PacketPlayOutChat");

                if (component == null || baseComponent == null || packetClass == null)
                    return false;

                Object packet;

                try {
                    packet = packetClass
                            .getDeclaredConstructor(baseComponent, byte.class)
                            .newInstance(component, (byte) 2);
                } catch (NoSuchMethodException ignored) {
                    Class<?> messageTypeClass = ReflectUtils.from(null, "ChatMessageType");
                    if (messageTypeClass == null) return false;

                    packet = packetClass
                            .getDeclaredConstructor(baseComponent, messageTypeClass)
                            .newInstance(component, messageTypeClass.getField("GAME_INFO").get(null));
                }

                ReflectUtils.sendPacket(player, packet);
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
