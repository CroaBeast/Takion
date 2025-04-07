package me.croabeast.common.reflect;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import me.croabeast.common.util.ServerInfoUtils;
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

/**
 * The {@code Craft} class provides reflection-based helper methods to interact with
 * internal server classes and configuration files in a CraftBukkit/Spigot environment.
 * <p>
 * It offers utilities for working with server icons, configuration files, command maps, and command
 * dispatchers, leveraging reflection to access methods and fields that are not normally exposed.
 * The class is organized into nested utility classes that group functionality:
 * </p>
 * <ul>
 *   <li>{@link Craft.Server}: Contains methods for server-related tasks such as loading icons,
 *       retrieving configuration files, and updating commands.</li>
 *   <li>{@link Craft.Command}: Provides access to command dispatching and manipulation,
 *       including removal and synchronization of commands.</li>
 * </ul>
 */
@UtilityClass
public class Craft {

    /**
     * Contains server-related utility methods using reflection.
     */
    @UtilityClass
    public static class Server {

        /**
         * A singleton {@link Reflector} instance wrapping the current Bukkit server.
         */
        public final Reflector INSTANCE = Reflector.from(Bukkit::getServer);

        /**
         * Invokes the internal method to load the server icon.
         */
        public void loadServerIcon() {
            INSTANCE.call("loadIcon");
        }

        /**
         * Retrieves a {@link File} based on a provided file name from the server options.
         *
         * @param fileName the name of the file to retrieve
         * @return the corresponding {@link File}
         */
        public File fileFrom(String fileName) {
            return Reflector.from(() -> Minecraft.INSTANCE.get("options")).call((Object) null, "valueOf", fileName);
        }

        /**
         * Retrieves the Bukkit server's configuration file.
         *
         * @return the configuration {@link File}
         */
        public File getBukkit() {
            return INSTANCE.call("getConfigFile");
        }

        /**
         * Retrieves the server's commands configuration file.
         *
         * @return the commands configuration {@link File}
         */
        public File getCommands() {
            return INSTANCE.call("getCommandsConfigFile");
        }

        /**
         * Loads the commands configuration file into a {@link FileConfiguration}.
         *
         * @return the {@link FileConfiguration} representing the commands configuration
         */
        public FileConfiguration createCommandsConfiguration() {
            return YamlConfiguration.loadConfiguration(getCommands());
        }

        /**
         * Retrieves the server's {@link SimpleCommandMap} via reflection.
         *
         * @return the server's command map
         */
        public SimpleCommandMap getCommandMap() {
            return INSTANCE.get("commandMap");
        }

        /**
         * Retrieves a map of known commands from the command map.
         *
         * @return a {@link Map} of command names to {@link org.bukkit.command.Command} objects
         */
        public Map<String, org.bukkit.command.Command> getKnownCommands() {
            return Reflector.of(SimpleCommandMap.class).get(getCommandMap(), "knownCommands");
        }

        /**
         * Updates command information for all online players.
         * <p>
         * For server versions 13.0 or higher, this method calls {@code updateCommands()} on each player.
         * </p>
         */
        public void updateCommands() {
            if (ServerInfoUtils.SERVER_VERSION >= 13.0)
                Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
        }

        /**
         * Synchronizes the server commands with a given collection of command names.
         * <p>
         * This method removes any commands from the command dispatcher that are not in the provided collection,
         * and then updates the commands for all online players.
         * </p>
         *
         * @param collection a collection of command names to synchronize
         */
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

        /**
         * Reloads the server's commands configuration file and updates command registrations.
         * <p>
         * For Paper servers (version >= 12), it invokes the method to reload command aliases.
         * Otherwise, it removes and unregisters existing command aliases, reloads the configuration,
         * updates internal settings, and re-registers commands.
         * </p>
         */
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

        /**
         * Provides access to internal Minecraft server functions.
         */
        @UtilityClass
        public static class Minecraft {

            /**
             * A singleton {@link Reflector} instance wrapping the NMS MinecraftServer class.
             */
            public final Reflector INSTANCE = Reflector.ofNms("MinecraftServer");

            static {
                INSTANCE.setInitial(() -> Server.INSTANCE.get("console"));
            }

            /**
             * Retrieves the Mojang command dispatcher from the Minecraft server.
             *
             * @return the command dispatcher object, or {@code null} if unavailable
             */
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

    /**
     * Contains utilities for manipulating server commands via reflection.
     */
    @UtilityClass
    public static class Command {

        /**
         * Contains methods for interacting with the command dispatcher.
         */
        @UtilityClass
        public static class Dispatcher {

            /**
             * A singleton {@link Reflector} instance for the command dispatcher.
             * <p>
             * Depending on the server version, it loads either the modern or legacy command dispatcher.
             * For server versions below 13.0, this instance is {@code null}.
             * </p>
             */
            @Nullable
            public final Reflector INSTANCE = ((Supplier<Reflector>) () -> {
                if (ServerInfoUtils.SERVER_VERSION < 13.0)
                    return null;
                return ServerInfoUtils.SERVER_VERSION >= 17.0 ?
                        Reflector.of("net.minecraft.commands.CommandDispatcher") :
                        Reflector.ofNms("CommandDispatcher");
            }).get();

            /**
             * Removes commands from the root command node that match the provided collection of names.
             *
             * @param collection a collection of command names to remove
             */
            public void removeCommands(Collection<String> collection) {
                if (ServerInfoUtils.SERVER_VERSION >= 13.0)
                    collection.forEach(getRoot()::removeCommand);
            }

            /**
             * Retrieves the root node of the command dispatcher.
             *
             * @return the root {@link Node} of the command dispatcher, or {@code null} if unavailable
             * @throws Exception if an error occurs during reflection
             */
            @SneakyThrows
            public Node getRoot() {
                Object dispatcher = Server.Minecraft.getMojangCommandDispatcher();
                return dispatcher != null ?
                        new Node(Reflector.from(() -> dispatcher).call("getRoot")) :
                        null;
            }
        }

        /**
         * Represents a node in the command tree.
         * <p>
         * Provides methods to retrieve child nodes, obtain the command name, add new child nodes,
         * and remove commands.
         * </p>
         */
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Node {

            private static final Reflector INSTANCE = Reflector.of("com.mojang.brigadier.tree.CommandNode");

            private final Object node;

            /**
             * Retrieves the collection of child nodes of this command node.
             *
             * @return a {@link Collection} of child nodes
             */
            public Collection<?> getChildren() {
                return INSTANCE.call(node, "getChildren");
            }

            /**
             * Retrieves the name of this command node.
             *
             * @return the command node's name as a {@link String}
             */
            public String getName() {
                return INSTANCE.call(node, "getName");
            }

            /**
             * Adds a child node to this command node.
             *
             * @param node the child node to add
             */
            public void addChild(Object node) {
                INSTANCE.call(this.node, "addChild", INSTANCE.getType().cast(node));
            }

            /**
             * Removes a command from this node by its name.
             *
             * @param name the name of the command to remove
             */
            public void removeCommand(String name) {
                INSTANCE.call(node, "removeCommand", name);
            }
        }
    }
}
