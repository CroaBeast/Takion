package me.croabeast.takion.rule;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a Minecraft game rule with a typed value that can be read and modified per {@link World}.
 * <p>
 * Each constant in this interface corresponds to a known Minecraft game rule, providing
 * a version-aware abstraction over the native Bukkit {@link org.bukkit.GameRule} API.
 * Rules may have a Bukkit-enum equivalent name and a minimum server version at which they apply.
 * </p>
 * <p>
 * Use {@link #getValue(World)} and {@link #setValue(World, Object)} to interact with a rule's
 * current value in a specific world, or {@link #getRules()} to retrieve all registered rules.
 * </p>
 *
 * @param <T> the type of value this game rule holds (e.g., {@link Boolean} or {@link Integer})
 */
public interface GameRule<T> {

    /** Whether command blocks output their result to the chat. */
    GameRule<Boolean> COMMAND_BLOCK_OUTPUT = RuleUtils.boolRule("commandBlockOutput", true, 4.2);
    /** Whether fire spreads and extinguishes naturally. */
    GameRule<Boolean> DO_FIRE_TICK = RuleUtils.boolRule("doFireTick", true, 4.2);
    /** Whether mobs drop loot when killed. */
    GameRule<Boolean> DO_MOB_LOOT = RuleUtils.boolRule("doMobLoot", "MOB_DROPS", false, true, 4.2);
    /** Whether mobs spawn naturally in the world. */
    GameRule<Boolean> DO_MOB_SPAWNING = RuleUtils.boolRule("doMobSpawning", "SPAWN_MOBS", false, true, 4.2);
    /** Whether blocks drop their items when broken. */
    GameRule<Boolean> DO_TILE_DROPS = RuleUtils.boolRule("doTileDrops", "BLOCK_DROPS", false, true, 4.2);
    /** Whether players keep their inventory on death. */
    GameRule<Boolean> KEEP_INVENTORY = RuleUtils.boolRule("keepInventory", false, 4.2);
    /** Whether mobs can destroy or modify blocks (e.g., creepers, endermen). */
    GameRule<Boolean> MOB_GRIEFING = RuleUtils.boolRule("mobGriefing", true, 4.2);
    /** Whether the daylight cycle advances over time. */
    GameRule<Boolean> DO_DAYLIGHT_CYCLE = RuleUtils.boolRule("doDaylightCycle", "ADVANCE_TIME", false, true, 6.1);
    /** Whether players regenerate health naturally. */
    GameRule<Boolean> NATURAL_REGENERATION = RuleUtils.boolRule("naturalRegeneration", "NATURAL_HEALTH_REGENERATION", false, true, 6.1);
    /** Whether the server logs admin commands to the console. */
    GameRule<Boolean> LOG_ADMIN_COMMANDS = RuleUtils.boolRule("logAdminCommands", true, 8);
    /** The rate at which random ticks occur for each chunk section per game tick. */
    GameRule<Integer> RANDOM_TICK_SPEED = RuleUtils.intRule("randomTickSpeed", 3, 8);
    /** Whether clients are shown reduced debug info in the F3 screen. */
    GameRule<Boolean> REDUCED_DEBUG_INFO = RuleUtils.boolRule("reducedDebugInfo", false, 8);
    /** Whether the server sends feedback messages to the player after running a command. */
    GameRule<Boolean> SEND_COMMAND_FEEDBACK = RuleUtils.boolRule("sendCommandFeedback", true, 8);
    /** Whether death messages are shown in chat when a player dies. */
    GameRule<Boolean> SHOW_DEATH_MESSAGES = RuleUtils.boolRule("showDeathMessages", true, 8);
    /** Whether entities (other than mobs) drop items when killed. */
    GameRule<Boolean> DO_ENTITY_DROPS = RuleUtils.boolRule("doEntityDrops", "ENTITY_DROPS", false, true, 8.1);
    /** Whether elytra movement is checked server-side (disable for smoother flight). */
    GameRule<Boolean> DISABLE_ELYTRA_MOVEMENT_CHECK = RuleUtils.boolRule("disableElytraMovementCheck", "ELYTRA_MOVEMENT_CHECK", true, false, 9);
    /** The radius in blocks around the world spawn that is protected from modification. */
    GameRule<Integer> SPAWN_RADIUS = RuleUtils.intRule("spawnRadius", "RESPAWN_RADIUS", 10, 9);
    /** Whether spectators can generate new chunks by moving through the world. */
    GameRule<Boolean> SPECTATORS_GENERATE_CHUNKS = RuleUtils.boolRule("spectatorsGenerateChunks", true, 9);
    /** Whether the weather cycle changes over time. */
    GameRule<Boolean> DO_WEATHER_CYCLE = RuleUtils.boolRule("doWeatherCycle", "ADVANCE_WEATHER", false, true, 11);
    /** The maximum number of entities that can be crammed into the same block space before taking suffocation damage. */
    GameRule<Integer> MAX_ENTITY_CRAMMING = RuleUtils.intRule("maxEntityCramming", 24, 11);
    /** Whether advancement completion messages are announced in chat. */
    GameRule<Boolean> ANNOUNCE_ADVANCEMENTS = RuleUtils.boolRule("announceAdvancements", "SHOW_ADVANCEMENT_MESSAGES", false, true, 12);
    /** Whether players can craft recipes that have not been unlocked yet. */
    GameRule<Boolean> DO_LIMITED_CRAFTING = RuleUtils.boolRule("doLimitedCrafting", "LIMITED_CRAFTING", false, false, 12);
    /** The maximum length of a chain of commands that can be executed in one tick. */
    GameRule<Integer> MAX_COMMAND_CHAIN_LENGTH = RuleUtils.intRule("maxCommandChainLength", "MAX_COMMAND_SEQUENCE_LENGTH", 65536, 12);
    /** The number of function commands run per game tick (0 disables the game loop function). */
    GameRule<Integer> GAME_LOOP_FUNCTION = RuleUtils.intRule("gameLoopFunction", 0, 12);
    /** Whether raids are disabled (illager raids triggered by bad omen). */
    GameRule<Boolean> DISABLE_RAIDS = RuleUtils.boolRule("disableRaids", "RAIDS", true, false, 14.3);
    /** Whether players respawn immediately without the respawn screen. */
    GameRule<Boolean> DO_IMMEDIATE_RESPAWN = RuleUtils.boolRule("doImmediateRespawn", "IMMEDIATE_RESPAWN", false, false, 15);
    /** Whether phantoms spawn if players have not slept. */
    GameRule<Boolean> DO_INSOMNIA = RuleUtils.boolRule("doInsomnia", "SPAWN_PHANTOMS", false, true, 15);
    /** Whether players take damage from drowning. */
    GameRule<Boolean> DROWNING_DAMAGE = RuleUtils.boolRule("drowningDamage", true, 15);
    /** Whether players take fall damage. */
    GameRule<Boolean> FALL_DAMAGE = RuleUtils.boolRule("fallDamage", true, 15);
    /** Whether players take fire damage. */
    GameRule<Boolean> FIRE_DAMAGE = RuleUtils.boolRule("fireDamage", true, 15);
    /** Whether pillager patrols spawn. */
    GameRule<Boolean> DO_PATROL_SPAWNING = RuleUtils.boolRule("doPatrolSpawning", "SPAWN_PATROLS", false, true, 15.2);
    /** Whether wandering traders spawn. */
    GameRule<Boolean> DO_TRADER_SPAWNING = RuleUtils.boolRule("doTraderSpawning", "SPAWN_WANDERING_TRADERS", false, true, 15.2);
    /** Whether angered neutral mobs stop being angry when the targeted player dies. */
    GameRule<Boolean> FORGIVE_DEAD_PLAYERS = RuleUtils.boolRule("forgiveDeadPlayers", true, 16);
    /** Whether angered neutral mobs attack any nearby player rather than only the one who angered them. */
    GameRule<Boolean> UNIVERSAL_ANGER = RuleUtils.boolRule("universalAnger", false, 16);
    /** Whether players take freeze damage while in powdered snow. */
    GameRule<Boolean> FREEZE_DAMAGE = RuleUtils.boolRule("freezeDamage", true, 17);
    /** The percentage of players that must sleep for the night to advance to day. */
    GameRule<Integer> PLAYERS_SLEEPING_PERCENTAGE = RuleUtils.intRule("playersSleepingPercentage", 100, 17);
    /** Whether the warden can spawn from sculk shriekers. */
    GameRule<Boolean> DO_WARDEN_SPAWNING = RuleUtils.boolRule("doWardenSpawning", "SPAWN_WARDENS", false, true, 19);
    /** Whether block explosions cause dropped items to decay over time. */
    GameRule<Boolean> BLOCK_EXPLOSION_DROP_DECAY = RuleUtils.boolRule("blockExplosionDropDecay", true, 19.3);
    /** Whether sounds from events like raids and beacons are heard by all players regardless of distance. */
    GameRule<Boolean> GLOBAL_SOUND_EVENTS = RuleUtils.boolRule("globalSoundEvents", true, 19.3);
    /** Whether lava can generate as a source block in specific situations. */
    GameRule<Boolean> LAVA_SOURCE_CONVERSION = RuleUtils.boolRule("lavaSourceConversion", false, 19.3);
    /** Whether mob explosions cause dropped items to decay over time. */
    GameRule<Boolean> MOB_EXPLOSION_DROP_DECAY = RuleUtils.boolRule("mobExplosionDropDecay", true, 19.3);
    /** The maximum number of layers of snow that can accumulate on the ground. */
    GameRule<Integer> SNOW_ACCUMULATION_HEIGHT = RuleUtils.intRule("snowAccumulationHeight", 1, 19.3);
    /** Whether TNT explosions cause dropped items to decay over time. */
    GameRule<Boolean> TNT_EXPLOSION_DROP_DECAY = RuleUtils.boolRule("tntExplosionDropDecay", false, 19.3);
    /** Whether water can generate as a source block in specific situations. */
    GameRule<Boolean> WATER_SOURCE_CONVERSION = RuleUtils.boolRule("waterSourceConversion", true, 19.3);
    /** The maximum number of blocks a command can modify in a single operation. */
    GameRule<Integer> COMMAND_MODIFICATION_BLOCK_LIMIT = RuleUtils.intRule("commandModificationBlockLimit", "MAX_BLOCK_MODIFICATIONS", 32768, 19.4);
    /** Whether vines spread to adjacent blocks. */
    GameRule<Boolean> DO_VINES_SPREAD = RuleUtils.boolRule("doVinesSpread", "SPREAD_VINES", false, true, 19.4);
    /** Whether ender pearls thrown by players vanish on the player's death. */
    GameRule<Boolean> ENDER_PEARLS_VANISH_ON_DEATH = RuleUtils.boolRule("enderPearlsVanishOnDeath", true, 20.2);
    /** The maximum number of forks allowed in a single command execution. */
    GameRule<Integer> MAX_COMMAND_FORK_COUNT = RuleUtils.intRule("maxCommandForkCount", "MAX_COMMAND_FORKS", 65536, 20.3);
    /** The delay (in ticks) before a creative-mode player is teleported through a nether portal. */
    GameRule<Integer> PLAYERS_NETHER_PORTAL_CREATIVE_DELAY = RuleUtils.intRule("playersNetherPortalCreativeDelay", 1, 20.3);
    /** The delay (in ticks) before a survival-mode player is teleported through a nether portal. */
    GameRule<Integer> PLAYERS_NETHER_PORTAL_DEFAULT_DELAY = RuleUtils.intRule("playersNetherPortalDefaultDelay", 80, 20.3);
    /** Whether projectiles (arrows, tridents, etc.) can break certain blocks such as glass. */
    GameRule<Boolean> PROJECTILES_CAN_BREAK_BLOCKS = RuleUtils.boolRule("projectilesCanBreakBlocks", false, 20.3);
    /** The radius (in chunks) of spawn chunks that remain loaded around the world spawn. */
    GameRule<Integer> SPAWN_CHUNK_RADIUS = RuleUtils.intRule("spawnChunkRadius", 2, 20.5, 21.9);
    /** The maximum speed (in blocks per tick) at which minecarts can travel. */
    GameRule<Integer> MINECART_MAX_SPEED = RuleUtils.intRule("minecartMaxSpeed", "MAX_MINECART_SPEED", 8, 21.2);
    /** Whether the server checks player movement for irregularities (disable to reduce false kicks). */
    GameRule<Boolean> DISABLE_PLAYER_MOVEMENT_CHECK = RuleUtils.boolRule("disablePlayerMovementCheck", "PLAYER_MOVEMENT_CHECK", true, false, 21.2);
    /** Whether fire can tick and spread even in chunks where no player is present. */
    GameRule<Boolean> ALLOW_FIRE_TICKS_AWAY_FROM_PLAYER = RuleUtils.boolRule("allowFireTicksAwayFromPlayer", false, 21.5);
    /** Whether TNT blocks explode when ignited. */
    GameRule<Boolean> TNT_EXPLODES = RuleUtils.boolRule("tntExplodes", true, 21.5);
    /** Whether the locator bar is shown to players. */
    GameRule<Boolean> LOCATOR_BAR = RuleUtils.boolRule("locatorBar", true, 21.6);
    /** Whether players can travel to the Nether through portals. */
    GameRule<Boolean> ALLOW_ENTERING_NETHER_USING_PORTALS = RuleUtils.boolRule("allowEnteringNetherUsingPortals", "ALLOW_ENTERING_NETHER_USING_PORTALS", false, true, 21.9);
    /** Whether monsters (hostile mobs) spawn naturally. */
    GameRule<Boolean> SPAWN_MONSTERS = RuleUtils.boolRule("spawnMonsters", "SPAWN_MONSTERS", false, true, 21.9);
    /** Whether command blocks can execute commands. */
    GameRule<Boolean> COMMAND_BLOCKS_ENABLED = RuleUtils.boolRule("commandBlocksEnabled", "COMMAND_BLOCKS_WORK", false, true, 21.9);
    /** Whether player versus player combat is allowed. */
    GameRule<Boolean> PVP = RuleUtils.boolRule("pvp", true, 21.9);
    /** Whether spawner blocks produce mobs. */
    GameRule<Boolean> SPAWNER_BLOCKS_ENABLED = RuleUtils.boolRule("spawnerBlocksEnabled", "SPAWNER_BLOCKS_WORK", false, true, 21.9);

    /**
     * Returns the Minecraft name of this game rule (e.g., {@code "keepInventory"}).
     *
     * @return the rule's name; never {@code null}
     */
    @NotNull
    String getName();

    /**
     * Returns the class representing the value type of this game rule.
     *
     * @return the value type class (e.g., {@code Boolean.class} or {@code Integer.class}); never {@code null}
     */
    @NotNull
    Class<T> getType();

    /**
     * Returns the default value for this game rule as defined by Minecraft.
     *
     * @return the default value; never {@code null}
     */
    @NotNull
    T getDefault();

    /**
     * Returns the current value of this game rule in the given world.
     *
     * @param world the world in which to read the rule's value
     * @return the current value; never {@code null}
     * @throws RuntimeException if the rule is not supported in the current server version
     */
    @NotNull
    T getValue(World world) throws RuntimeException;

    /**
     * Sets the value of this game rule in the given world.
     *
     * @param world the world in which to apply the new value
     * @param value the new value for the game rule
     * @return {@code true} if the value was applied successfully; {@code false} otherwise
     */
    boolean setValue(World world, T value);

    /**
     * Returns {@code true} if this game rule's current value in the given world equals its default value.
     *
     * @param world the world to check
     * @return {@code true} if the rule is at its default value; {@code false} otherwise
     */
    default boolean isDefault(World world) {
        try {
            return Objects.equals(getValue(world), getDefault());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the native Bukkit {@link org.bukkit.GameRule} equivalent of this game rule.
     *
     * @return the corresponding Bukkit game rule; never {@code null}
     */
    @SuppressWarnings("unchecked")
    @NotNull
    default org.bukkit.GameRule<T> asBukkit() {
        return (org.bukkit.GameRule<T>) RuleUtils.asBukkit(this);
    }

    /**
     * Returns an unmodifiable set of all registered {@link GameRule} instances.
     *
     * @return a set containing every known game rule; never {@code null}
     */
    @NotNull
    static Set<GameRule<?>> getRules() {
        return new HashSet<>(RuleUtils.RULE_MAP.values());
    }
}
