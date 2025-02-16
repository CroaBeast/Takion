package me.croabeast.takion.message;

import lombok.experimental.UtilityClass;
import me.croabeast.lib.util.ServerInfoUtils;
import org.bukkit.entity.Player;

import java.util.function.BiFunction;
import java.util.function.Function;

@UtilityClass
class ReflectUtils {

    final double VERSION = ServerInfoUtils.SERVER_VERSION;
    final boolean IS_LEGACY = VERSION < 17.0;

    final Class<?> BASE_COMP_CLASS = from(
            IS_LEGACY ? null : "network.chat.", "IChatBaseComponent");

    Class<?> from(String prefix, String name) {
        StringBuilder builder = new StringBuilder("net.minecraft.");

        if (IS_LEGACY)
            builder.append("server.")
                    .append(ServerInfoUtils.BUKKIT_API_VERSION)
                    .append(".");

        if (prefix != null) builder.append(prefix);

        try {
            return Class.forName(builder.append(name) + "");
        } catch (Exception e) {
            return null;
        }
    }

    final Function<String, Object> COMPONENT_SERIALIZER = message -> {
        try {
            Class<?> serializer = from(
                    IS_LEGACY ? null : ("network.chat.IChatBaseComponent$"),
                    "ChatSerializer");

            return serializer.getDeclaredMethod("a", String.class)
                    .invoke(null, "{\"text\":\"" + message + "\"}");
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    };

    void sendPacket(Player p, Object o) throws Exception {
        Class<?> clazz = from(
                IS_LEGACY ? null : "network.protocol.",
                "Packet"
        );

        Object handler = p.getClass().getMethod("getHandle").invoke(p);
        String co = VERSION >= 20.0 ? "c" : "b";

        Object connect = handler.getClass()
                .getField(IS_LEGACY ? "playerConnection" : co)
                .get(handler);

        connect.getClass()
                .getMethod(VERSION < 18.0 ? "sendPacket" : "a", clazz)
                .invoke(connect, o);
    }

    int round(int i) {
        return Math.round((float) i / 20);
    }

    interface TimesInitialize {
        Object from(int in, int stay, int out);
    }

    private enum TitleType {
        TITLE, SUBTITLE
    }

    final TimesInitialize TIMES_PACKET_INSTANCE = (in, stay, out) -> {
        try {
            return from(null, "PacketPlayOutTitle")
                    .getDeclaredConstructor(int.class, int.class, int.class)
                    .newInstance(round(in), round(stay), round(out));
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    };

    final BiFunction<Boolean, String, Object> LEGACY_PACKET_INSTANCE = (b, s) -> {
        TitleType type = b ? TitleType.TITLE : TitleType.SUBTITLE;
        Object component = COMPONENT_SERIALIZER.apply(s);

        try {
            Class<?> oldEnum = from(
                    VERSION < 8.3 ? "PacketPlayOutTitle$" : null,
                    "EnumTitleAction"
            );

            return from(null, "PacketPlayOutTitle")
                    .getDeclaredConstructor(oldEnum, BASE_COMP_CLASS)
                    .newInstance(
                            oldEnum.getField(type + "").get(null),
                            component
                    );
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    };
}
