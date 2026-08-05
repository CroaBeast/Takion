package me.croabeast.takion;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A stub {@link Player} that records everything sent to it.
 *
 * <p>Without this the channel pipeline cannot be exercised at all: everything below
 * {@code TakionLib.colorize} needs a real receiver, so the interesting behaviour, which is what a
 * given player actually ends up seeing, was untestable.
 */
public final class TestPlayer {

    private final List<String> plain = new ArrayList<>();
    private final List<BaseComponent[]> components = new ArrayList<>();
    private final List<String> typed = new ArrayList<>();

    private final UUID uuid = UUID.randomUUID();
    private final String name;
    private final String displayName;
    private final Player handle;

    public TestPlayer(String name) {
        this(name, name);
    }

    public TestPlayer(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
        this.handle = (Player) Proxy.newProxyInstance(
                TestPlayer.class.getClassLoader(),
                new Class<?>[] {Player.class},
                new Handler()
        );
    }

    public Player handle() {
        return handle;
    }

    /** Plain strings received through {@code sendMessage(String)}. */
    public List<String> plain() {
        return Collections.unmodifiableList(plain);
    }

    /** Component arrays received through {@code spigot().sendMessage(...)}. */
    public List<BaseComponent[]> components() {
        return Collections.unmodifiableList(components);
    }

    /** Messages received through a positioned send, such as the action bar. */
    public List<String> typed() {
        return Collections.unmodifiableList(typed);
    }

    public void reset() {
        plain.clear();
        components.clear();
        typed.clear();
    }

    private final class Handler implements InvocationHandler {

        private final CapturingSpigot spigot = new CapturingSpigot();

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "getUniqueId":
                    return uuid;
                case "getName":
                    return name;
                case "getDisplayName":
                    return displayName;
                case "spigot":
                    return spigot;
                case "isOnline":
                    return true;
                case "sendMessage":
                    capture(args);
                    return null;
                case "toString":
                    return "TestPlayer(" + name + ")";
                case "hashCode":
                    return uuid.hashCode();
                case "equals":
                    return proxy == args[0];
                default:
                    return defaultValue(method.getReturnType());
            }
        }

        private void capture(Object[] args) {
            if (args == null || args.length == 0) return;

            Object first = args[0];
            if (first instanceof String) {
                plain.add((String) first);
                return;
            }

            if (first instanceof String[]) Collections.addAll(plain, (String[]) first);
        }

        private Object defaultValue(Class<?> type) {
            if (!type.isPrimitive()) return null;
            if (type == boolean.class) return false;
            if (type == void.class) return null;
            if (type == long.class) return 0L;
            if (type == double.class) return 0D;
            if (type == float.class) return 0F;
            return 0;
        }
    }

    private final class CapturingSpigot extends Player.Spigot {

        @Override
        public void sendMessage(BaseComponent... parts) {
            components.add(parts);
        }

        @Override
        public void sendMessage(BaseComponent part) {
            components.add(new BaseComponent[] {part});
        }

        @Override
        public void sendMessage(ChatMessageType position, BaseComponent... parts) {
            typed.add(position + " " + TextComponent.toLegacyText(parts));
        }

        @Override
        public void sendMessage(ChatMessageType position, BaseComponent part) {
            sendMessage(position, new BaseComponent[] {part});
        }
    }
}
