package me.croabeast.takion;

import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * Installs a stub {@link Server} so library code that touches {@link Bukkit} can run headless.
 *
 * <p>Takion builds its {@code NO_PLUGIN} instance in a static initializer, and that path reaches
 * {@code Bukkit.getLogger()}. Without a server the class fails to initialize and nothing can be
 * tested at all.
 */
public final class TestServer {

    private static final Logger LOGGER = Logger.getLogger("TakionTest");

    private static boolean installed;

    private TestServer() {}

    public static synchronized void install() {
        if (installed) return;
        installed = true;

        if (Bukkit.getServer() != null) return;

        Bukkit.setServer((Server) Proxy.newProxyInstance(
                TestServer.class.getClassLoader(),
                new Class<?>[] {Server.class},
                new StubHandler()
        ));
    }

    /** No plugin is ever enabled, so optional integrations such as PlaceholderAPI stay out. */
    private static Object pluginManager() {
        return Proxy.newProxyInstance(
                TestServer.class.getClassLoader(),
                new Class<?>[] {org.bukkit.plugin.PluginManager.class},
                (proxy, method, args) -> "getPlugins".equals(method.getName()) ?
                        new org.bukkit.plugin.Plugin[0] :
                        new StubHandler().invoke(proxy, method, args)
        );
    }

    private static final class StubHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "getLogger":
                    return LOGGER;
                case "getName":
                    return "TakionTest";
                case "getVersion":
                case "getBukkitVersion":
                    return "1.16.5-R0.1-SNAPSHOT";
                case "isPrimaryThread":
                    return true;
                case "getOnlinePlayers":
                    return Collections.emptyList();
                case "getPluginManager":
                    return pluginManager();
                case "toString":
                    return "TestServer";
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "equals":
                    return proxy == args[0];
                default:
                    return defaultValue(method.getReturnType());
            }
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
}
