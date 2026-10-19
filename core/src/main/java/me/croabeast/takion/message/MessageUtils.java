package me.croabeast.takion.message;

import java.util.logging.Level;
import org.bukkit.Bukkit;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

@UtilityClass
public class MessageUtils {

    public boolean sendActionBar(Player player, String message) {
        if (player == null) return false;

        if (ReflectUtils.BEFORE_1_11) {
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
                Bukkit.getLogger().log(Level.WARNING, "Could not send the action bar packet", e);
                return false;
            }
        }

        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        return true;
    }

    // Before 1.13 the packet carries both texts, so the one not being changed is kept here.
    private final Map<Player, String[]> TAB_LISTS = new WeakHashMap<>();

    public boolean sendTabList(Player player, String message, boolean header) {
        if (player == null) return false;

        if (!ReflectUtils.BEFORE_1_13) {
            if (header) player.setPlayerListHeader(message);
            else player.setPlayerListFooter(message);
            return true;
        }

        String[] texts = TAB_LISTS.computeIfAbsent(player, p -> new String[] {"", ""});
        texts[header ? 0 : 1] = message;

        try {
            Class<?> packetClass = ReflectUtils.from(null, "PacketPlayOutPlayerListHeaderFooter");
            if (packetClass == null) return false;

            Object packet = packetClass.getDeclaredConstructor().newInstance();
            for (int i = 0; i < 2; i++) {
                Field field = packetClass.getDeclaredField(i == 0 ? "a" : "b");
                field.setAccessible(true);
                field.set(packet, ReflectUtils.COMPONENT_SERIALIZER.apply(texts[i]));
            }

            ReflectUtils.sendPacket(player, packet);
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Could not send the tab list packet", e);
            return false;
        }
    }

    // The client only gets plugin messages on channels it registered, and it never registers
    // the brand one, so it is added to the player's channels by hand.
    @SuppressWarnings("unchecked")
    public boolean sendBrand(Plugin plugin, Player player, String brand) {
        if (player == null) return false;

        String channel = !ReflectUtils.BEFORE_1_13 ? "minecraft:brand" : "MC|Brand";
        Messenger messenger = Bukkit.getMessenger();
        if (!messenger.isOutgoingChannelRegistered(plugin, channel))
            messenger.registerOutgoingPluginChannel(plugin, channel);

        if (!player.getListeningPluginChannels().contains(channel)) {
            try {
                player.getClass().getMethod("addChannel", String.class).invoke(player, channel);
            } catch (NoSuchMethodException e) {
                try {
                    ((Set<String>) player.getClass().getMethod("channels").invoke(player)).add(channel);
                } catch (Exception ex) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        byte[] text = brand.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream payload = new ByteArrayOutputStream();

        int length = text.length;
        while ((length & ~0x7F) != 0) {
            payload.write((length & 0x7F) | 0x80);
            length >>>= 7;
        }
        payload.write(length);
        payload.write(text, 0, text.length);

        player.sendPluginMessage(plugin, channel, payload.toByteArray());
        return true;
    }

    // Toasts are advancements, so they exist since 1.12, and they are sent with PacketEvents.
    public boolean sendToast(Player player, String title, String icon, String style) {
        if (player == null || ReflectUtils.BEFORE_1_12) return false;
        if (!Bukkit.getPluginManager().isPluginEnabled("packetevents")) return false;

        PacketToast.send(player, title, icon, style);
        return true;
    }
}
