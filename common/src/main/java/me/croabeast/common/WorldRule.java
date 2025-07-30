package me.croabeast.common;

import lombok.SneakyThrows;
import me.croabeast.common.util.ServerInfoUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents a game rule for a Minecraft world.
 * <p>
 * {@code WorldRule} is an abstraction over Bukkit's game rules, allowing you to get and set specific
 * rules by name, while also providing default values and minimum server version requirements.
 * Each {@code WorldRule} is associated with a rule name, a type ({@link Boolean} or {@link Integer}),
 * a default value, and a minimum version number from which the rule is available.
 * </p>
 * This class also maintains a registry of all defined game rules within a static map.
 * <p>
 * Example usage:
 * <pre><code>
 * // Retrieve the "doFireTick" game rule value from a world
 * WorldRule<Boolean> fireTickRule = WorldRule.DO_FIRE_TICK;
 * Boolean currentValue = fireTickRule.getValue(world);
 *
 * // Set a new value for the game rule if allowed
 * boolean success = fireTickRule.setValue(world, false);
 * </code></pre>
 * </p>
 *
 * @param <T> the type of the game rule value (e.g., {@code Boolean} or {@code Integer})
 */
@SuppressWarnings({"deprecation", "unchecked"})
public abstract class WorldRule<T> {

    private static final Map<String, WorldRule<?>> RULE_MAP = new LinkedHashMap<>();

    /**
     * Game rule: commandBlockOutput. Indicates whether command blocks should output their command results.
     */
    public static final WorldRule<Boolean> COMMAND_BLOCK_OUTPUT;

    /**
     * Game rule: doFireTick. Indicates whether fire should naturally progress (tick) in the world.
     */
    public static final WorldRule<Boolean> DO_FIRE_TICK;

    /**
     * Game rule: doMobLoot. Indicates whether mobs should drop loot when killed.
     */
    public static final WorldRule<Boolean> DO_MOB_LOOT;

    /**
     * Game rule: doMobSpawning. Indicates whether mobs should spawn naturally.
     */
    public static final WorldRule<Boolean> DO_MOB_SPAWNING;

    /**
     * Game rule: doTileDrops. Indicates whether blocks should drop items when broken.
     */
    public static final WorldRule<Boolean> DO_TILE_DROPS;

    /**
     * Game rule: keepInventory. Indicates whether players keep their inventory upon death.
     */
    public static final WorldRule<Boolean> KEEP_INVENTORY;

    /**
     * Game rule: mobGriefing. Indicates whether mobs can modify blocks (e.g., creepers destroying blocks).
     */
    public static final WorldRule<Boolean> MOB_GRIEFING;

    /**
     * Game rule: doDaylightCycle. Indicates whether the day-night cycle should progress.
     */
    public static final WorldRule<Boolean> DO_DAYLIGHT_CYCLE;

    /**
     * Game rule: naturalRegeneration. Indicates whether players naturally regenerate health.
     */
    public static final WorldRule<Boolean> NATURAL_REGENERATION;

    /**
     * Game rule: logAdminCommands. Indicates whether admin command executions should be logged.
     */
    public static final WorldRule<Boolean> LOG_ADMIN_COMMANDS;

    /**
     * Game rule: randomTickSpeed. Controls the rate at which random block ticks occur (affecting crop growth, etc.).
     */
    public static final WorldRule<Integer> RANDOM_TICK_SPEED;

    /**
     * Game rule: reducedDebugInfo. Indicates whether the debug information should be reduced.
     */
    public static final WorldRule<Boolean> REDUCED_DEBUG_INFO;

    /**
     * Game rule: sendCommandFeedback. Indicates whether command feedback should be sent to the command sender.
     */
    public static final WorldRule<Boolean> SEND_COMMAND_FEEDBACK;

    /**
     * Game rule: showDeathMessages. Indicates whether death messages should be broadcast.
     */
    public static final WorldRule<Boolean> SHOW_DEATH_MESSAGES;

    /**
     * Game rule: doEntityDrops. Indicates whether entities drop items when killed.
     */
    public static final WorldRule<Boolean> DO_ENTITY_DROPS;

    /**
     * Game rule: disableElytraMovementCheck. Disables the elytra movement check to allow for freer flight.
     */
    public static final WorldRule<Boolean> DISABLE_ELYTRA_MOVEMENT_CHECK;

    /**
     * Game rule: spawnRadius. Controls the radius around the world spawn where players will spawn.
     */
    public static final WorldRule<Integer> SPAWN_RADIUS;

    /**
     * Game rule: spectatorsGenerateChunks. Determines whether spectators can generate chunks by moving.
     */
    public static final WorldRule<Boolean> SPECTATORS_GENERATE_CHUNKS;

    /**
     * Game rule: doWeatherCycle. Indicates whether weather should change naturally.
     */
    public static final WorldRule<Boolean> DO_WEATHER_CYCLE;

    /**
     * Game rule: maxEntityCramming. Determines the maximum number of entities allowed in a single block space.
     */
    public static final WorldRule<Integer> MAX_ENTITY_CRAMMING;

    /**
     * Game rule: announceAdvancements. Indicates whether advancements should be announced to chat.
     */
    public static final WorldRule<Boolean> ANNOUNCE_ADVANCEMENTS;

    /**
     * Game rule: doLimitedCrafting. Limits the crafting recipes available to players.
     */
    public static final WorldRule<Boolean> DO_LIMITED_CRAFTING;

    /**
     * Game rule: maxCommandChainLength. Sets the maximum allowed length of command chains.
     */
    public static final WorldRule<Integer> MAX_COMMAND_CHAIN_LENGTH;

    /**
     * Game rule: disableRaids. Disables raid spawning in the world.
     */
    public static final WorldRule<Boolean> DISABLE_RAIDS;

    /**
     * Game rule: doImmediateRespawn. Controls whether players respawn immediately upon death.
     */
    public static final WorldRule<Boolean> DO_IMMEDIATE_RESPAWN;

    /**
     * Game rule: doInsomnia. Controls whether players experience the insomnia effect (related to phantoms).
     */
    public static final WorldRule<Boolean> DO_INSOMNIA;

    /**
     * Game rule: drowningDamage. Indicates whether players take damage from drowning.
     */
    public static final WorldRule<Boolean> DROWNING_DAMAGE;

    /**
     * Game rule: fallDamage. Indicates whether players take damage from falling.
     */
    public static final WorldRule<Boolean> FALL_DAMAGE;

    /**
     * Game rule: fireDamage. Indicates whether players take damage from fire.
     */
    public static final WorldRule<Boolean> FIRE_DAMAGE;

    /**
     * Game rule: doPatrolSpawning. Controls whether patrols are allowed to spawn.
     */
    public static final WorldRule<Boolean> DO_PATROL_SPAWNING;

    /**
     * Game rule: doTraderSpawning. Controls whether trader villagers can spawn.
     */
    public static final WorldRule<Boolean> DO_TRADER_SPAWNING;

    /**
     * Game rule: forgiveDeadPlayers. Indicates whether mobs will forgive players after they die.
     */
    public static final WorldRule<Boolean> FORGIVE_DEAD_PLAYERS;

    /**
     * Game rule: universalAnger. Determines whether all hostile mobs become angry when a player is attacked.
     */
    public static final WorldRule<Boolean> UNIVERSAL_ANGER;

    /**
     * Game rule: freezeDamage. Indicates whether players take damage from freezing.
     */
    public static final WorldRule<Boolean> FREEZE_DAMAGE;

    /**
     * Game rule: playersSleepingPercentage. The percentage of players that must sleep to change the time.
     */
    public static final WorldRule<Integer> PLAYERS_SLEEPING_PERCENTAGE;

    /**
     * Game rule: doWardenSpawning. Controls whether the Warden can spawn.
     */
    public static final WorldRule<Boolean> DO_WARDEN_SPAWNING;

    /**
     * Game rule: blockExplosionDropDecay. Determines if block explosion drops are decayed.
     */
    public static final WorldRule<Boolean> BLOCK_EXPLOSION_DROP_DECAY;

    /**
     * Game rule: globalSoundEvents. Controls whether sound events are global.
     */
    public static final WorldRule<Boolean> GLOBAL_SOUND_EVENTS;

    /**
     * Game rule: lavaSourceConversion. Controls whether lava converts into a source block.
     */
    public static final WorldRule<Boolean> LAVA_SOURCE_CONVERSION;

    /**
     * Game rule: mobExplosionDropDecay. Determines if mob explosion drops are decayed.
     */
    public static final WorldRule<Boolean> MOB_EXPLOSION_DROP_DECAY;

    /**
     * Game rule: snowAccumulationHeight. Determines the height at which snow can accumulate.
     */
    public static final WorldRule<Integer> SNOW_ACCUMULATION_HEIGHT;

    /**
     * Game rule: tntExplosionDropDecay. Determines if TNT explosion drops are decayed.
     */
    public static final WorldRule<Boolean> TNT_EXPLOSION_DROP_DECAY;

    /**
     * Game rule: waterSourceConversion. Controls whether water converts into a source block.
     */
    public static final WorldRule<Boolean> WATER_SOURCE_CONVERSION;

    /**
     * Game rule: commandModificationBlockLimit. Sets a limit for command modifications via blocks.
     */
    public static final WorldRule<Integer> COMMAND_MODIFICATION_BLOCK_LIMIT;

    /**
     * Game rule: doVinesSpread. Indicates whether vines should spread naturally.
     */
    public static final WorldRule<Boolean> DO_VINES_SPREAD;

    /**
     * Game rule: enderPearlsVanishOnDeath. Indicates whether ender pearls vanish upon player death.
     */
    public static final WorldRule<Boolean> ENDER_PEARLS_VANISH_ON_DEATH;

    /**
     * Game rule: maxCommandForkCount. Sets the maximum number of forks in a command chain.
     */
    public static final WorldRule<Integer> MAX_COMMAND_FORK_COUNT;

    /**
     * Game rule: playersNetherPortalCreativeDelay. The creative mode delay for nether portal usage.
     */
    public static final WorldRule<Integer> PLAYERS_NETHER_PORTAL_CREATIVE_DELAY;

    /**
     * Game rule: playersNetherPortalDefaultDelay. The default delay for nether portal usage.
     */
    public static final WorldRule<Integer> PLAYERS_NETHER_PORTAL_DEFAULT_DELAY;

    /**
     * Game rule: projectilesCanBreakBlocks. Indicates whether projectiles are allowed to break blocks.
     */
    public static final WorldRule<Boolean> PROJECTILES_CAN_BREAK_BLOCKS;

    /**
     * Game rule: spawnChunkRadius. Determines the radius of spawn chunks.
     */
    public static final WorldRule<Integer> SPAWN_CHUNK_RADIUS;

    /**
     * Game rule: minecartMaxSpeed. Sets the maximum speed for mine carts.
     */
    public static final WorldRule<Integer> MINECART_MAX_SPEED;

    /**
     * Game rule: disablePlayerMovementCheck. Disables movement checks for players.
     */
    public static final WorldRule<Boolean> DISABLE_PLAYER_MOVEMENT_CHECK;

    /**
     * Game rule: allowFireTicksAwayFromPlayer. Controls fire tick behavior away from players.
     */
    public static final WorldRule<Boolean> ALLOW_FIRE_TICKS_AWAY_FROM_PLAYER;

    /**
     * Game rule: tntExplodes. Indicates whether TNT should explode.
     */
    public static final WorldRule<Boolean> TNT_EXPLODES;

    /**
     * Game rule: useLocatorBar. Enables the locator bar feature.
     */
    public static final WorldRule<Boolean> USE_LOCATOR_BAR;

    static {
        COMMAND_BLOCK_OUTPUT = boolRule("commandBlockOutput", true, 4.2);
        DO_FIRE_TICK = boolRule("doFireTick", true, 4.2);
        DO_MOB_LOOT = boolRule("doMobLoot", true, 4.2);
        DO_MOB_SPAWNING = boolRule("doMobSpawning", true, 4.2);
        DO_TILE_DROPS = boolRule("doTileDrops", true, 4.2);
        KEEP_INVENTORY = boolRule("keepInventory", false, 4.2);
        MOB_GRIEFING = boolRule("mobGriefing", true, 4.2);
        DO_DAYLIGHT_CYCLE = boolRule("doDaylightCycle", true, 6.1);
        NATURAL_REGENERATION = boolRule("naturalRegeneration", true, 6.1);
        LOG_ADMIN_COMMANDS = boolRule("logAdminCommands", true, 8);
        RANDOM_TICK_SPEED = intRule("randomTickSpeed", 3, 8);
        REDUCED_DEBUG_INFO = boolRule("reducedDebugInfo", false, 8);
        SEND_COMMAND_FEEDBACK = boolRule("sendCommandFeedback", true, 8);
        SHOW_DEATH_MESSAGES = boolRule("showDeathMessages", true, 8);
        DO_ENTITY_DROPS = boolRule("doEntityDrops", true, 8.1);
        DISABLE_ELYTRA_MOVEMENT_CHECK = boolRule("disableElytraMovementCheck", false, 9);
        SPAWN_RADIUS = intRule("spawnRadius", 10, 9);
        SPECTATORS_GENERATE_CHUNKS = boolRule("spectatorsGenerateChunks", true, 9);
        DO_WEATHER_CYCLE = boolRule("doWeatherCycle", true, 11);
        MAX_ENTITY_CRAMMING = intRule("maxEntityCramming", 24, 11);
        ANNOUNCE_ADVANCEMENTS = boolRule("announceAdvancements", true, 12);
        DO_LIMITED_CRAFTING = boolRule("doLimitedCrafting", false, 12);
        MAX_COMMAND_CHAIN_LENGTH = intRule("maxCommandChainLength", 65536, 12);
        DISABLE_RAIDS = boolRule("disableRaids", false, 14.3);
        DO_IMMEDIATE_RESPAWN = boolRule("doImmediateRespawn", false, 15);
        DO_INSOMNIA = boolRule("doInsomnia", true, 15);
        DROWNING_DAMAGE = boolRule("drowningDamage", true, 15);
        FALL_DAMAGE = boolRule("fallDamage", true, 15);
        FIRE_DAMAGE = boolRule("fireDamage", true, 15);
        DO_PATROL_SPAWNING = boolRule("doPatrolSpawning", true, 15.2);
        DO_TRADER_SPAWNING = boolRule("doTraderSpawning", true, 15.2);
        FORGIVE_DEAD_PLAYERS = boolRule("forgiveDeadPlayers", true, 16);
        UNIVERSAL_ANGER = boolRule("universalAnger", false, 16);
        FREEZE_DAMAGE = boolRule("freezeDamage", true, 17);
        PLAYERS_SLEEPING_PERCENTAGE = intRule("playersSleepingPercentage", 100, 17);
        DO_WARDEN_SPAWNING = boolRule("doWardenSpawning", true, 19);
        BLOCK_EXPLOSION_DROP_DECAY = boolRule("blockExplosionDropDecay", true, 19.3);
        GLOBAL_SOUND_EVENTS = boolRule("globalSoundEvents", true, 19.3);
        LAVA_SOURCE_CONVERSION = boolRule("lavaSourceConversion", false, 19.3);
        MOB_EXPLOSION_DROP_DECAY = boolRule("mobExplosionDropDecay", true, 19.3);
        SNOW_ACCUMULATION_HEIGHT = intRule("snowAccumulationHeight", 1, 19.3);
        TNT_EXPLOSION_DROP_DECAY = boolRule("tntExplosionDropDecay", false, 19.3);
        WATER_SOURCE_CONVERSION = boolRule("waterSourceConversion", true, 19.3);
        COMMAND_MODIFICATION_BLOCK_LIMIT = intRule("commandModificationBlockLimit", 32768, 19.4);
        DO_VINES_SPREAD = boolRule("doVinesSpread", true, 19.4);
        ENDER_PEARLS_VANISH_ON_DEATH = boolRule("enderPearlsVanishOnDeath", true, 20.2);
        MAX_COMMAND_FORK_COUNT = intRule("maxCommandForkCount", 65536, 20.3);
        PLAYERS_NETHER_PORTAL_CREATIVE_DELAY = intRule("playersNetherPortalCreativeDelay", 1, 20.3);
        PLAYERS_NETHER_PORTAL_DEFAULT_DELAY = intRule("playersNetherPortalDefaultDelay", 80, 20.3);
        PROJECTILES_CAN_BREAK_BLOCKS = boolRule("projectilesCanBreakBlocks", false, 20.3);
        SPAWN_CHUNK_RADIUS = intRule("spawnChunkRadius", 2, 20.5);
        MINECART_MAX_SPEED = intRule("minecartMaxSpeed", 8, 21.2);
        DISABLE_PLAYER_MOVEMENT_CHECK = boolRule("disablePlayerMovementCheck", false, 21.2);
        ALLOW_FIRE_TICKS_AWAY_FROM_PLAYER = boolRule("allowFireTicksAwayFromPlayer", false, 21.5);
        TNT_EXPLODES = boolRule("tntExplodes", true, 21.5);
        USE_LOCATOR_BAR = boolRule("useLocatorBar", true, 21.6);
    }

    private final String rule;
    private final Class<T> clazz;
    private final T def;

    /**
     * The minimum server version required for this game rule to be applicable.
     */
    private final double minVersion;

    /**
     * Constructs a new {@code WorldRule} with the specified rule name, type, default value, and minimum server version.
     *
     * @param rule       the name of the game rule
     * @param clazz      the class type representing the rule's value
     * @param def        the default value for the rule
     * @param minVersion the minimum server version required for this rule
     */
    private WorldRule(String rule, Class<T> clazz, T def, double minVersion) {
        this.rule = rule;
        this.clazz = clazz;
        this.def = def;
        this.minVersion = minVersion;
        RULE_MAP.put(this.rule, this);
    }

    /**
     * Returns the name of this world rule.
     *
     * @return the rule name
     */
    @NotNull
    public String getName() {
        return rule;
    }

    /**
     * Returns the type of this world rule (e.g., {@code Boolean.class} or {@code Integer.class}).
     *
     * @return the class representing the type of the rule's value
     */
    @NotNull
    public Class<T> getType() {
        return clazz;
    }

    /**
     * Retrieves the current value of this game rule from the specified world.
     *
     * @param world the world from which to get the game rule value
     * @return the current value of the rule, or {@code null} if unavailable
     * @throws Exception if the value cannot be retrieved
     */
    @Nullable
    public abstract T getValue(World world) throws Exception;

    /**
     * Returns the default value for this game rule.
     *
     * @return the default value
     */
    public T getDefault() {
        return def;
    }

    /**
     * Sets a new value for this game rule in the given world.
     *
     * @param world the world where the rule should be set
     * @param value the new value to assign
     * @return {@code true} if the rule was successfully updated; {@code false} otherwise
     */
    public boolean setValue(World world, T value) {
        return ServerInfoUtils.SERVER_VERSION >= minVersion && world.setGameRuleValue(rule, String.valueOf(value));
    }

    /**
     * Determines if the game rule in the given world is set to its default value.
     *
     * @param world the world to check
     * @return {@code true} if the current value equals the default; {@code false} otherwise
     * @throws Exception if the value cannot be retrieved
     */
    public boolean isDefault(World world) throws Exception {
        return Objects.equals(getValue(world), getDefault());
    }

    /**
     * Retrieves the corresponding Bukkit {@link GameRule} object for this world rule.
     *
     * @return the Bukkit {@link GameRule} for this rule
     * @throws NullPointerException if no matching GameRule is found
     */
    @NotNull
    public GameRule<T> asBukkit() {
        return (GameRule<T>) Objects.requireNonNull(GameRule.getByName(rule));
    }

    /**
     * Creates a boolean-type world rule with the specified parameters.
     *
     * @param rule the name of the game rule
     * @param def  the default boolean value
     * @param min  the minimum server version for this rule
     * @return a new {@code WorldRule<Boolean>}
     */
    private static WorldRule<Boolean> boolRule(String rule, boolean def, double min) {
        return new WorldRule<Boolean>(rule, Boolean.class, def, min) {
            @SneakyThrows
            @Override
            public Boolean getValue(World world) {
                if (ServerInfoUtils.SERVER_VERSION < min)
                    return null;

                String value = world.getGameRuleValue(rule);

                return StringUtils.isBlank(value) ||
                        !value.matches("(?i)true|false")
                        ? null :
                        Boolean.parseBoolean(value);
            }
        };
    }

    /**
     * Creates an integer-type world rule with the specified parameters.
     *
     * @param rule the name of the game rule
     * @param def  the default integer value
     * @param min  the minimum server version for this rule
     * @return a new {@code WorldRule<Integer>}
     */
    private static WorldRule<Integer> intRule(String rule, int def, double min) {
        return new WorldRule<Integer>(rule, Integer.class, def, min) {
            @SneakyThrows
            @Override
            public Integer getValue(World world) {
                if (ServerInfoUtils.SERVER_VERSION < min)
                    return null;

                String value = world.getGameRuleValue(rule);
                if (StringUtils.isBlank(value))
                    return null;

                try {
                    return Integer.parseInt(value);
                } catch (Exception e) {
                    return null;
                }
            }
        };
    }

    /**
     * Returns a set of all registered world rules.
     *
     * @return a set of {@code WorldRule} objects
     */
    public static Set<WorldRule<?>> values() {
        return new LinkedHashSet<>(RULE_MAP.values());
    }
}
