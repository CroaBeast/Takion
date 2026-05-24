package me.croabeast.takion.message;

import lombok.experimental.UtilityClass;
import me.croabeast.common.function.TriFunction;
import me.croabeast.vnc.VNC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Function;

@UtilityClass
class ReflectUtils {

    private static final String BUKKIT_API_VERSION;

    static {
        String pkg = Bukkit.getServer().getClass().getPackage().getName();
        String ver = pkg.substring(pkg.lastIndexOf('.') + 1);
        BUKKIT_API_VERSION = ver.matches("v\\d+_\\d+_R\\d+") ? ver : "";
    }

    final double VERSION = VNC.SERVER_VERSION;
    final boolean IS_LEGACY = VNC.isBefore("1.17");

    final Class<?> BASE_COMP_CLASS = from(
            IS_LEGACY ? null : "network.chat.", "IChatBaseComponent");

    private Class<?> firstNonNull(Class<?>... classes) {
        if (classes == null) return null;

        for (Class<?> clazz : classes)
            if (clazz != null)
                return clazz;

        return null;
    }

    Class<?> from(String prefix, String name) {
        StringBuilder builder = new StringBuilder("net.minecraft.");

        if (IS_LEGACY)
            builder.append("server.")
                    .append(BUKKIT_API_VERSION)
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
            Class<?> serializer = firstNonNull(
                    from(
                            IS_LEGACY ? "IChatBaseComponent$" : "network.chat.IChatBaseComponent$",
                            "ChatSerializer"
                    ),
                    from(null, "ChatSerializer")
            );

            if (serializer == null) return null;

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
        Object connect = readFirstField(
                handler,
                IS_LEGACY ? new String[] {"playerConnection"} : new String[] {"playerConnection", "b", "c"}
        );

        if (connect == null || clazz == null)
            throw new IllegalStateException("Could not resolve the player connection packet bridge.");

        Method method = getFirstMethod(
                connect.getClass(),
                clazz,
                VERSION < 18.0 ? new String[] {"sendPacket", "a"} : new String[] {"a", "sendPacket"}
        );

        if (method == null)
            throw new NoSuchMethodException("Could not resolve the packet sending method.");

        method.invoke(connect, o);
    }

    private Object readFirstField(Object instance, String... names) throws IllegalAccessException {
        for (String name : names) {
            Field field = getField(instance.getClass(), name);
            if (field != null)
                return field.get(instance);
        }
        return null;
    }

    private Field getField(Class<?> type, String name) {
        try {
            return type.getField(name);
        } catch (NoSuchFieldException ignored) {}

        try {
            Field field = type.getDeclaredField(name);
            if (!field.isAccessible())
                field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException ignored) {
            return null;
        }
    }

    private Method getFirstMethod(Class<?> type, Class<?> parameter, String... names) {
        for (String name : names) {
            try {
                return type.getMethod(name, parameter);
            } catch (NoSuchMethodException ignored) {}

            try {
                Method method = type.getDeclaredMethod(name, parameter);
                if (!method.isAccessible())
                    method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {}
        }
        return null;
    }

    int round(int i) {
        return Math.round((float) i / 20);
    }

    private enum TitleType {
        TITLE, SUBTITLE
    }

    final TriFunction<Integer, Integer, Integer, ?> TIMES_PACKET_INSTANCE = (in, stay, out) -> {
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
            Class<?> oldEnum = firstNonNull(
                    from("PacketPlayOutTitle$", "EnumTitleAction"),
                    from(null, "EnumTitleAction")
            );

            Class<?> packetClass = from(null, "PacketPlayOutTitle");

            if (packetClass == null || oldEnum == null || BASE_COMP_CLASS == null || component == null)
                return null;

            return packetClass
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
