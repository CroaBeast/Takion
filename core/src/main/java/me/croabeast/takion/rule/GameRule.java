package me.croabeast.takion.rule;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public interface GameRule<T> {

    GameRule<Boolean> COMMAND_BLOCK_OUTPUT = RuleUtils.boolRule("commandBlockOutput", true, 4.2);
    GameRule<Boolean> DO_FIRE_TICK = RuleUtils.boolRule("doFireTick", true, 4.2);
    GameRule<Boolean> DO_MOB_LOOT = RuleUtils.boolRule("doMobLoot", true, 4.2);
    GameRule<Boolean> DO_MOB_SPAWNING = RuleUtils.boolRule("doMobSpawning", true, 4.2);
    GameRule<Boolean> DO_TILE_DROPS = RuleUtils.boolRule("doTileDrops", true, 4.2);
    GameRule<Boolean> KEEP_INVENTORY = RuleUtils.boolRule("keepInventory", false, 4.2);
    GameRule<Boolean> MOB_GRIEFING = RuleUtils.boolRule("mobGriefing", true, 4.2);
    GameRule<Boolean> DO_DAYLIGHT_CYCLE = RuleUtils.boolRule("doDaylightCycle", true, 6.1);
    GameRule<Boolean> NATURAL_REGENERATION = RuleUtils.boolRule("naturalRegeneration", true, 6.1);
    GameRule<Boolean> LOG_ADMIN_COMMANDS = RuleUtils.boolRule("logAdminCommands", true, 8);
    GameRule<Integer> RANDOM_TICK_SPEED = RuleUtils.intRule("randomTickSpeed", 3, 8);
    GameRule<Boolean> REDUCED_DEBUG_INFO = RuleUtils.boolRule("reducedDebugInfo", false, 8);
    GameRule<Boolean> SEND_COMMAND_FEEDBACK = RuleUtils.boolRule("sendCommandFeedback", true, 8);
    GameRule<Boolean> SHOW_DEATH_MESSAGES = RuleUtils.boolRule("showDeathMessages", true, 8);
    GameRule<Boolean> DO_ENTITY_DROPS = RuleUtils.boolRule("doEntityDrops", true, 8.1);
    GameRule<Boolean> DISABLE_ELYTRA_MOVEMENT_CHECK = RuleUtils.boolRule("disableElytraMovementCheck", false, 9);
    GameRule<Integer> SPAWN_RADIUS = RuleUtils.intRule("spawnRadius", 10, 9);
    GameRule<Boolean> SPECTATORS_GENERATE_CHUNKS = RuleUtils.boolRule("spectatorsGenerateChunks", true, 9);
    GameRule<Boolean> DO_WEATHER_CYCLE = RuleUtils.boolRule("doWeatherCycle", true, 11);
    GameRule<Integer> MAX_ENTITY_CRAMMING = RuleUtils.intRule("maxEntityCramming", 24, 11);
    GameRule<Boolean> ANNOUNCE_ADVANCEMENTS = RuleUtils.boolRule("announceAdvancements", true, 12);
    GameRule<Boolean> DO_LIMITED_CRAFTING = RuleUtils.boolRule("doLimitedCrafting", false, 12);
    GameRule<Integer> MAX_COMMAND_CHAIN_LENGTH = RuleUtils.intRule("maxCommandChainLength", 65536, 12);
    GameRule<Integer> GAME_LOOP_FUNCTION = RuleUtils.intRule("gameLoopFunction", 0, 12);
    GameRule<Boolean> DISABLE_RAIDS = RuleUtils.boolRule("disableRaids", false, 14.3);
    GameRule<Boolean> DO_IMMEDIATE_RESPAWN = RuleUtils.boolRule("doImmediateRespawn", false, 15);
    GameRule<Boolean> DO_INSOMNIA = RuleUtils.boolRule("doInsomnia", true, 15);
    GameRule<Boolean> DROWNING_DAMAGE = RuleUtils.boolRule("drowningDamage", true, 15);
    GameRule<Boolean> FALL_DAMAGE = RuleUtils.boolRule("fallDamage", true, 15);
    GameRule<Boolean> FIRE_DAMAGE = RuleUtils.boolRule("fireDamage", true, 15);
    GameRule<Boolean> DO_PATROL_SPAWNING = RuleUtils.boolRule("doPatrolSpawning", true, 15.2);
    GameRule<Boolean> DO_TRADER_SPAWNING = RuleUtils.boolRule("doTraderSpawning", true, 15.2);
    GameRule<Boolean> FORGIVE_DEAD_PLAYERS = RuleUtils.boolRule("forgiveDeadPlayers", true, 16);
    GameRule<Boolean> UNIVERSAL_ANGER = RuleUtils.boolRule("universalAnger", false, 16);
    GameRule<Boolean> FREEZE_DAMAGE = RuleUtils.boolRule("freezeDamage", true, 17);
    GameRule<Integer> PLAYERS_SLEEPING_PERCENTAGE = RuleUtils.intRule("playersSleepingPercentage", 100, 17);
    GameRule<Boolean> DO_WARDEN_SPAWNING = RuleUtils.boolRule("doWardenSpawning", true, 19);
    GameRule<Boolean> BLOCK_EXPLOSION_DROP_DECAY = RuleUtils.boolRule("blockExplosionDropDecay", true, 19.3);
    GameRule<Boolean> GLOBAL_SOUND_EVENTS = RuleUtils.boolRule("globalSoundEvents", true, 19.3);
    GameRule<Boolean> LAVA_SOURCE_CONVERSION = RuleUtils.boolRule("lavaSourceConversion", false, 19.3);
    GameRule<Boolean> MOB_EXPLOSION_DROP_DECAY = RuleUtils.boolRule("mobExplosionDropDecay", true, 19.3);
    GameRule<Integer> SNOW_ACCUMULATION_HEIGHT = RuleUtils.intRule("snowAccumulationHeight", 1, 19.3);
    GameRule<Boolean> TNT_EXPLOSION_DROP_DECAY = RuleUtils.boolRule("tntExplosionDropDecay", false, 19.3);
    GameRule<Boolean> WATER_SOURCE_CONVERSION = RuleUtils.boolRule("waterSourceConversion", true, 19.3);
    GameRule<Integer> COMMAND_MODIFICATION_BLOCK_LIMIT = RuleUtils.intRule("commandModificationBlockLimit", 32768, 19.4);
    GameRule<Boolean> DO_VINES_SPREAD = RuleUtils.boolRule("doVinesSpread", true, 19.4);
    GameRule<Boolean> ENDER_PEARLS_VANISH_ON_DEATH = RuleUtils.boolRule("enderPearlsVanishOnDeath", true, 20.2);
    GameRule<Integer> MAX_COMMAND_FORK_COUNT = RuleUtils.intRule("maxCommandForkCount", 65536, 20.3);
    GameRule<Integer> PLAYERS_NETHER_PORTAL_CREATIVE_DELAY = RuleUtils.intRule("playersNetherPortalCreativeDelay", 1, 20.3);
    GameRule<Integer> PLAYERS_NETHER_PORTAL_DEFAULT_DELAY = RuleUtils.intRule("playersNetherPortalDefaultDelay", 80, 20.3);
    GameRule<Boolean> PROJECTILES_CAN_BREAK_BLOCKS = RuleUtils.boolRule("projectilesCanBreakBlocks", false, 20.3);
    GameRule<Integer> SPAWN_CHUNK_RADIUS = RuleUtils.intRule("spawnChunkRadius", 2, 20.5, 21.9);
    GameRule<Integer> MINECART_MAX_SPEED = RuleUtils.intRule("minecartMaxSpeed", 8, 21.2);
    GameRule<Boolean> DISABLE_PLAYER_MOVEMENT_CHECK = RuleUtils.boolRule("disablePlayerMovementCheck", false, 21.2);
    GameRule<Boolean> ALLOW_FIRE_TICKS_AWAY_FROM_PLAYER = RuleUtils.boolRule("allowFireTicksAwayFromPlayer", false, 21.5);
    GameRule<Boolean> TNT_EXPLODES = RuleUtils.boolRule("tntExplodes", true, 21.5);
    GameRule<Boolean> LOCATOR_BAR = RuleUtils.boolRule("locatorBar", true, 21.6);
    GameRule<Boolean> ALLOW_ENTERING_NETHER_USING_PORTALS = RuleUtils.boolRule("allowEnteringNetherUsingPortals", true, 21.9);
    GameRule<Boolean> SPAWN_MONSTERS = RuleUtils.boolRule("spawnMonsters", true, 21.9);
    GameRule<Boolean> COMMAND_BLOCKS_ENABLED = RuleUtils.boolRule("commandBlocksEnabled", true, 21.9);
    GameRule<Boolean> PVP = RuleUtils.boolRule("pvp", true, 21.9);
    GameRule<Boolean> SPAWNER_BLOCKS_ENABLED = RuleUtils.boolRule("spawnerBlocksEnabled", true, 21.9);

    @NotNull
    String getName();

    @NotNull
    Class<T> getType();

    @NotNull
    T getDefault();

    @NotNull
    T getValue(World world) throws RuntimeException;

    boolean setValue(World world, T value);

    default boolean isDefault(World world) {
        try {
            return Objects.equals(getValue(world), getDefault());
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    default org.bukkit.GameRule<T> asBukkit() {
        return (org.bukkit.GameRule<T>) Objects.requireNonNull(org.bukkit.GameRule.getByName(getName()));
    }

    @NotNull
    static Set<GameRule<?>> getRules() {
        return new HashSet<>(RuleUtils.RULE_MAP.values());
    }
}
