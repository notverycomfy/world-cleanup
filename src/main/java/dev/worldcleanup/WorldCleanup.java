package dev.worldcleanup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WorldCleanup {
    public static final String MOD_ID = "world_cleanup";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final TagKey<Item> VALUABLE = itemTag("valuable");
    public static final TagKey<Item> COMMON = itemTag("common");
    public static final TagKey<Item> FARM_DROPS = itemTag("farm_drops");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, List<DeathSite>> DEATHS = new HashMap<>();
    private static CleanupConfig config = new CleanupConfig();
    private static Path configFile;

    private WorldCleanup() {
    }

    public static void initialize(Path configDirectory) {
        Path file = configDirectory.resolve("world_cleanup.json");
        configFile = file;
        try {
            Files.createDirectories(configDirectory);
            if (Files.isRegularFile(file)) {
                try (Reader reader = Files.newBufferedReader(file)) {
                    CleanupConfig loaded = GSON.fromJson(reader, CleanupConfig.class);
                    if (loaded != null) config = loaded;
                }
            }
            config.validate();
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Could not load World Cleanup config; using safe defaults", exception);
            config = new CleanupConfig();
        }
        LOGGER.info("World Cleanup is active");
    }

    public static void recordPlayerDeath(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        String dimension = dimensionKey(level);
        synchronized (DEATHS) {
            List<DeathSite> sites = DEATHS.computeIfAbsent(dimension, ignored -> new ArrayList<>());
            prune(sites, level.getGameTime());
            sites.add(new DeathSite(player.getX(), player.getY(), player.getZ(), level.getGameTime()));
        }
    }

    public static int refreshTicks() {
        return secondsToTicks(config.classificationRefreshSeconds);
    }

    public static int lifetimeTicks(ItemEntity item, boolean wasPlayerThrown) {
        if (!(item.level() instanceof ServerLevel level)) return Integer.MAX_VALUE;

        Entity owner = item.getOwner();
        boolean playerThrown = wasPlayerThrown || owner instanceof Player;
        boolean mobDrop = owner instanceof Mob;
        boolean common = matches(item, COMMON, config.commonItems);
        boolean farm = matches(item, FARM_DROPS, config.farmDropItems) && isDenseFarmArea(level, item);
        boolean valuable = matches(item, VALUABLE, config.valuableItems)
            || item.getItem().isEnchanted()
            || item.getItem().isDamageableItem();
        boolean deathProtected = nearRecentDeath(level, item);

        TimerPolicy.ItemContext itemContext = new TimerPolicy.ItemContext(
            playerThrown,
            mobDrop,
            common,
            farm,
            valuable,
            deathProtected
        );
        return secondsToTicks(TimerPolicy.lifetimeSeconds(config, itemContext));
    }

    private static boolean isDenseFarmArea(ServerLevel level, ItemEntity item) {
        int threshold = config.farmItemThreshold;
        return level.getEntitiesOfClass(
            ItemEntity.class,
            item.getBoundingBox().inflate(config.farmDetectionRadius),
            candidate -> candidate.isAlive() && matches(candidate, FARM_DROPS, config.farmDropItems)
        ).size() >= threshold;
    }

    private static boolean nearRecentDeath(ServerLevel level, ItemEntity item) {
        long now = level.getGameTime();
        double radiusSquared = config.deathProtectionRadius * config.deathProtectionRadius;
        synchronized (DEATHS) {
            List<DeathSite> sites = DEATHS.get(dimensionKey(level));
            if (sites == null) return false;
            prune(sites, now);
            for (DeathSite site : sites) {
                double x = item.getX() - site.x;
                double y = item.getY() - site.y;
                double z = item.getZ() - site.z;
                if (x * x + y * y + z * z <= radiusSquared) return true;
            }
            return false;
        }
    }

    private static void prune(List<DeathSite> sites, long now) {
        long memory = secondsToTicks(config.deathMemorySeconds);
        sites.removeIf(site -> now - site.gameTime > memory);
    }

    private static String dimensionKey(ServerLevel level) {
        return level.dimension().identifier().toString();
    }

    private static int secondsToTicks(int seconds) {
        return Math.multiplyExact(seconds, 20);
    }

    public static void applyConfig(CleanupConfig updated) {
        updated.validate();
        config = updated;
    }

    public static CleanupConfig configCopy() {
        return GSON.fromJson(GSON.toJson(config), CleanupConfig.class);
    }

    public static boolean applyAndSave(CleanupConfig updated) {
        updated.validate();
        config = updated;
        if (configFile == null) return false;
        try (Writer writer = Files.newBufferedWriter(configFile)) {
            GSON.toJson(config, writer);
            return true;
        } catch (IOException exception) {
            LOGGER.error("Could not save World Cleanup config", exception);
            return false;
        }
    }

    private static boolean matches(ItemEntity entity, TagKey<Item> tag, List<String> configuredItems) {
        if (config.useDataPackTags && entity.getItem().is(tag)) return true;
        String id = BuiltInRegistries.ITEM.getKey(entity.getItem().getItem()).toString();
        return configuredItems.contains(id);
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, path));
    }

    private record DeathSite(double x, double y, double z, long gameTime) {
    }
}
