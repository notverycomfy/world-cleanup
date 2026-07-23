package dev.worldcleanup.neoforge;

import dev.worldcleanup.CleanupConfig;
import dev.worldcleanup.WorldCleanup;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

final class WorldCleanupNeoConfig {
    static final ModConfigSpec SPEC;
    private static final ModConfigSpec.IntValue DEFAULT_SECONDS;
    private static final ModConfigSpec.IntValue VALUABLE_SECONDS;
    private static final ModConfigSpec.IntValue COMMON_SECONDS;
    private static final ModConfigSpec.IntValue PLAYER_THROWN_SECONDS;
    private static final ModConfigSpec.IntValue MOB_DROP_SECONDS;
    private static final ModConfigSpec.IntValue FARM_SECONDS;
    private static final ModConfigSpec.IntValue DEATH_PROTECTION_SECONDS;
    private static final ModConfigSpec.IntValue DEATH_MEMORY_SECONDS;
    private static final ModConfigSpec.DoubleValue DEATH_RADIUS;
    private static final ModConfigSpec.DoubleValue FARM_RADIUS;
    private static final ModConfigSpec.IntValue FARM_THRESHOLD;
    private static final ModConfigSpec.IntValue REFRESH_SECONDS;
    private static final ModConfigSpec.BooleanValue USE_DATA_PACK_TAGS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> VALUABLE_ITEMS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> COMMON_ITEMS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> FARM_ITEMS;

    static {
        CleanupConfig defaults = new CleanupConfig();
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Dropped-item lifetimes, in seconds. Protective rules take priority.").push("timers");
        DEFAULT_SECONDS = time(builder, "defaultSeconds", defaults.defaultSeconds, "Lifetime for uncategorized items.");
        VALUABLE_SECONDS = time(builder, "valuableSeconds", defaults.valuableSeconds, "Minimum lifetime for valuable items.");
        COMMON_SECONDS = time(builder, "commonSeconds", defaults.commonSeconds, "Lifetime for common items.");
        PLAYER_THROWN_SECONDS = time(builder, "playerThrownSeconds", defaults.playerThrownSeconds, "Minimum lifetime for player-thrown items.");
        MOB_DROP_SECONDS = time(builder, "mobDropSeconds", defaults.mobDropSeconds, "Lifetime for mob drops.");
        FARM_SECONDS = time(builder, "farmSeconds", defaults.farmSeconds, "Lifetime for farm items in a dense farm.");
        DEATH_PROTECTION_SECONDS = time(builder, "deathProtectionSeconds", defaults.deathProtectionSeconds, "Minimum lifetime near a recent player death.");
        DEATH_MEMORY_SECONDS = time(builder, "deathMemorySeconds", defaults.deathMemorySeconds, "How long a player death location is remembered.");
        builder.pop();

        builder.comment("Detection distances and performance controls.").push("detection");
        DEATH_RADIUS = builder.comment("Protection radius around a recent player death.")
            .defineInRange("deathProtectionRadius", defaults.deathProtectionRadius, 1.0, 128.0);
        FARM_RADIUS = builder.comment("Radius used to count nearby farm drops.")
            .defineInRange("farmDetectionRadius", defaults.farmDetectionRadius, 1.0, 32.0);
        FARM_THRESHOLD = builder.comment("Tagged farm drops required nearby before the short farm timer activates.")
            .defineInRange("farmItemThreshold", defaults.farmItemThreshold, 2, 1000);
        REFRESH_SECONDS = builder.comment("Seconds between item reclassification checks.")
            .defineInRange("classificationRefreshSeconds", defaults.classificationRefreshSeconds, 1, 60);
        builder.pop();

        builder.comment("Item IDs can be added, edited, or removed in-game. Data-pack tags still work too.").push("items");
        USE_DATA_PACK_TAGS = builder.comment("Use world_cleanup data-pack tags in addition to the editable lists. Disable for list-only classification.")
            .define("useDataPackTags", defaults.useDataPackTags);
        VALUABLE_ITEMS = builder.comment("Items receiving the valuable timer.")
            .defineListAllowEmpty("valuableItems", defaults.valuableItems, WorldCleanupNeoConfig::validItemId);
        COMMON_ITEMS = builder.comment("Items receiving the common timer.")
            .defineListAllowEmpty("commonItems", defaults.commonItems, WorldCleanupNeoConfig::validItemId);
        FARM_ITEMS = builder.comment("Items eligible for dense-farm cleanup.")
            .defineListAllowEmpty("farmDropItems", defaults.farmDropItems, WorldCleanupNeoConfig::validItemId);
        builder.pop();
        SPEC = builder.build();
    }

    private WorldCleanupNeoConfig() {
    }

    static void apply() {
        CleanupConfig config = new CleanupConfig();
        config.defaultSeconds = DEFAULT_SECONDS.getAsInt();
        config.valuableSeconds = VALUABLE_SECONDS.getAsInt();
        config.commonSeconds = COMMON_SECONDS.getAsInt();
        config.playerThrownSeconds = PLAYER_THROWN_SECONDS.getAsInt();
        config.mobDropSeconds = MOB_DROP_SECONDS.getAsInt();
        config.farmSeconds = FARM_SECONDS.getAsInt();
        config.deathProtectionSeconds = DEATH_PROTECTION_SECONDS.getAsInt();
        config.deathMemorySeconds = DEATH_MEMORY_SECONDS.getAsInt();
        config.deathProtectionRadius = DEATH_RADIUS.getAsDouble();
        config.farmDetectionRadius = FARM_RADIUS.getAsDouble();
        config.farmItemThreshold = FARM_THRESHOLD.getAsInt();
        config.classificationRefreshSeconds = REFRESH_SECONDS.getAsInt();
        config.useDataPackTags = USE_DATA_PACK_TAGS.getAsBoolean();
        config.valuableItems = new ArrayList<>(VALUABLE_ITEMS.get());
        config.commonItems = new ArrayList<>(COMMON_ITEMS.get());
        config.farmDropItems = new ArrayList<>(FARM_ITEMS.get());
        WorldCleanup.applyConfig(config);
    }

    static void resetDefaults() {
        DEFAULT_SECONDS.set(DEFAULT_SECONDS.getDefault());
        VALUABLE_SECONDS.set(VALUABLE_SECONDS.getDefault());
        COMMON_SECONDS.set(COMMON_SECONDS.getDefault());
        PLAYER_THROWN_SECONDS.set(PLAYER_THROWN_SECONDS.getDefault());
        MOB_DROP_SECONDS.set(MOB_DROP_SECONDS.getDefault());
        FARM_SECONDS.set(FARM_SECONDS.getDefault());
        DEATH_PROTECTION_SECONDS.set(DEATH_PROTECTION_SECONDS.getDefault());
        DEATH_MEMORY_SECONDS.set(DEATH_MEMORY_SECONDS.getDefault());
        DEATH_RADIUS.set(DEATH_RADIUS.getDefault());
        FARM_RADIUS.set(FARM_RADIUS.getDefault());
        FARM_THRESHOLD.set(FARM_THRESHOLD.getDefault());
        REFRESH_SECONDS.set(REFRESH_SECONDS.getDefault());
        USE_DATA_PACK_TAGS.set(USE_DATA_PACK_TAGS.getDefault());
        VALUABLE_ITEMS.set(new ArrayList<>(VALUABLE_ITEMS.getDefault()));
        COMMON_ITEMS.set(new ArrayList<>(COMMON_ITEMS.getDefault()));
        FARM_ITEMS.set(new ArrayList<>(FARM_ITEMS.getDefault()));
        SPEC.save();
        apply();
    }

    private static ModConfigSpec.IntValue time(ModConfigSpec.Builder builder, String name, int value, String comment) {
        return builder.comment(comment).defineInRange(name, value, 1, 86400);
    }

    private static boolean validItemId(Object value) {
        return value instanceof String id && id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+");
    }
}
