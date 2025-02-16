package me.croabeast.lib.reflect;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import me.croabeast.lib.util.ServerInfoUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@UtilityClass
public final class Craft {

    @UtilityClass
    public static class Server {

        public final Reflector INSTANCE = Reflector.from(Bukkit::getServer);

        public void loadServerIcon() {
            INSTANCE.call("loadIcon");
        }

        public File fileFrom(String fileName) {
            return Reflector.from(() -> Minecraft.INSTANCE.get("options")).call((Object) null, "valueOf", fileName);
        }

        public File getBukkit() {
            return INSTANCE.call("getConfigFile");
        }

        public File getCommands() {
            return INSTANCE.call("getCommandsConfigFile");
        }

        public FileConfiguration createCommandsConfiguration() {
            return YamlConfiguration.loadConfiguration(getCommands());
        }

        public SimpleCommandMap getCommandMap() {
            return INSTANCE.get("commandMap");
        }

        public Map<String, org.bukkit.command.Command> getKnownCommands() {
            return Reflector.of(SimpleCommandMap.class).get(getCommandMap(), "knownCommands");
        }

        public void updateCommands() {
            if (ServerInfoUtils.SERVER_VERSION >= 13.0)
                Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
        }

        @SneakyThrows
        public void syncCommands(Collection<String> collection) {
            if (ServerInfoUtils.SERVER_VERSION < 13.0) return;

            Collection<?> children = Command.Dispatcher.getRoot().getChildren();
            INSTANCE.call("syncCommands");

            Command.Node root = Command.Dispatcher.getRoot();
            for (Object child : children) {
                String name = new Command.Node(child).getName();
                root.removeCommand(name);

                if (!collection.contains(name)) root.addChild(child);
            }

            updateCommands();
        }

        @SneakyThrows
        public void reloadCommandsFile() {
            if (ServerInfoUtils.PAPER_ENABLED && ServerInfoUtils.SERVER_VERSION >= 12) {
                Server.INSTANCE.call("reloadCommandAliases");
                return;
            }

            Set<String> commands = Bukkit.getCommandAliases().keySet();
            Command.Dispatcher.removeCommands(commands);

            final SimpleCommandMap map = getCommandMap();
            for (String alias : commands) {
                org.bukkit.command.Command command = getKnownCommands().remove(alias);
                if (command != null) command.unregister(map);
            }

            FileConfiguration file = createCommandsConfiguration();

            INSTANCE.set("overrideAllCommandBlockCommands",
                    file.getStringList("command-block-overrides").contains("*"));
            INSTANCE.set("commandsConfiguration", file);

            if (ServerInfoUtils.SERVER_VERSION <= 13)
                INSTANCE.set("ignoreVanillaPermissions",
                        file.getBoolean("ignore-vanilla-permissions"));

            if (ServerInfoUtils.SERVER_VERSION <= 12)
                INSTANCE.set("unrestrictedAdvancements",
                        file.getBoolean("unrestricted-advancements"));

            map.registerServerAliases();
            syncCommands(commands);
        }

        @UtilityClass
        public static class Minecraft {

            public final Reflector INSTANCE = Reflector.ofNms("MinecraftServer");

            static {
                INSTANCE.setInitial(() -> Server.INSTANCE.get("console"));
            }
            @SneakyThrows
            public Object getMojangCommandDispatcher() {
                Reflector dispatcher = Command.Dispatcher.INSTANCE;
                if (dispatcher == null) return null;

                Object bukkit = INSTANCE.get("vanillaCommandDispatcher");
                Class<?> c;
                try {
                    c = Class.forName("com.mojang.brigadier.CommandDispatcher");
                } catch (Exception e) {
                    return null;
                }

                return dispatcher.get(bukkit, c);
            }
        }
    }

    @UtilityClass
    public static class Command {

        @UtilityClass
        public static class Dispatcher {

            @Nullable
            public final Reflector INSTANCE = ((Supplier<Reflector>) () -> {
                if (ServerInfoUtils.SERVER_VERSION < 13.0)
                    return null;

                return ServerInfoUtils.SERVER_VERSION >= 17.0 ?
                        Reflector.of("net.minecraft.commands.CommandDispatcher") :
                        Reflector.ofNms("CommandDispatcher");
            }).get();

            public void removeCommands(Collection<String> collection) {
                if (ServerInfoUtils.SERVER_VERSION >= 13.0)
                    collection.forEach(getRoot()::removeCommand);
            }

            @SneakyThrows
            public Node getRoot() {
                Object dispatcher = Server.Minecraft.getMojangCommandDispatcher();

                return dispatcher != null ?
                        new Node(Reflector.from(() -> dispatcher).call("getRoot")) :
                        null;
            }
        }

        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Node {

            private static final Reflector INSTANCE = Reflector.of("com.mojang.brigadier.tree.CommandNode");

            private final Object node;

            public Collection<?> getChildren() {
                return INSTANCE.call(node, "getChildren");
            }

            public String getName() {
                return INSTANCE.call(node, "getName");
            }

            public void addChild(Object node) {
                INSTANCE.call(this.node, "addChild", INSTANCE.getType().cast(node));
            }

            public void removeCommand(String name) {
                INSTANCE.call(node, "removeCommand", name);
            }
        }
    }
}
