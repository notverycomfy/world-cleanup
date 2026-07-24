package dev.worldcleanup;

import java.util.ArrayList;
import java.util.List;

public final class CleanupConfig {
    public int defaultSeconds = 600;
    public int valuableSeconds = 1200;
    public int commonSeconds = 120;
    public int playerThrownSeconds = 180;
    public int mobDropSeconds = 300;
    public int farmSeconds = 60;
    public int deathProtectionSeconds = 900;
    public int deathMemorySeconds = 300;
    public double deathProtectionRadius = 16.0;
    public double farmDetectionRadius = 6.0;
    public int farmItemThreshold = 24;
    public int classificationRefreshSeconds = 5;
    public boolean useDataPackTags = true;
    public List<String> valuableItems = new ArrayList<>(List.of(
        "minecraft:nether_star", "minecraft:elytra", "minecraft:diamond", "minecraft:diamond_block",
        "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore",
        "minecraft:gold_ingot", "minecraft:gold_block", "minecraft:raw_gold", "minecraft:raw_gold_block",
        "minecraft:gold_ore", "minecraft:deepslate_gold_ore", "minecraft:nether_gold_ore",
        "minecraft:emerald", "minecraft:emerald_block", "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore",
        "minecraft:iron_ingot", "minecraft:iron_block", "minecraft:raw_iron_block",
        "minecraft:netherite_ingot", "minecraft:netherite_block", "minecraft:netherite_scrap",
        "minecraft:ancient_debris", "minecraft:enchanted_golden_apple", "minecraft:golden_apple",
        "minecraft:dragon_egg", "minecraft:beacon", "minecraft:shulker_box", "minecraft:ender_chest",
        "minecraft:totem_of_undying", "minecraft:enchanted_book", "minecraft:experience_bottle",
        "minecraft:heart_of_the_sea", "minecraft:nautilus_shell", "minecraft:echo_shard",
        "minecraft:recovery_compass", "minecraft:trident", "minecraft:mace", "minecraft:heavy_core",
        "minecraft:netherite_upgrade_smithing_template", "minecraft:dragon_head", "minecraft:wither_skeleton_skull"
    ));
    public List<String> commonItems = new ArrayList<>(List.of(
        "minecraft:cobblestone", "minecraft:cobbled_deepslate", "minecraft:dirt", "minecraft:grass_block",
        "minecraft:coarse_dirt", "minecraft:rooted_dirt", "minecraft:podzol", "minecraft:mycelium",
        "minecraft:stone", "minecraft:netherrack", "minecraft:end_stone", "minecraft:tuff",
        "minecraft:calcite", "minecraft:blackstone", "minecraft:basalt", "minecraft:sand",
        "minecraft:red_sand", "minecraft:gravel", "minecraft:clay", "minecraft:soul_sand",
        "minecraft:soul_soil", "minecraft:mud", "minecraft:moss_block"
    ));
    public List<String> farmDropItems = new ArrayList<>(List.of(
        "minecraft:rotten_flesh", "minecraft:bone", "minecraft:string", "minecraft:gunpowder",
        "minecraft:spider_eye", "minecraft:feather", "minecraft:egg", "minecraft:wheat",
        "minecraft:carrot", "minecraft:potato", "minecraft:sugar_cane", "minecraft:bamboo", "minecraft:kelp"
    ));

    void validate() {
        defaultSeconds = positive(defaultSeconds, 600);
        valuableSeconds = positive(valuableSeconds, 1200);
        commonSeconds = positive(commonSeconds, 120);
        playerThrownSeconds = positive(playerThrownSeconds, 180);
        mobDropSeconds = positive(mobDropSeconds, 300);
        farmSeconds = positive(farmSeconds, 60);
        deathProtectionSeconds = positive(deathProtectionSeconds, 900);
        deathMemorySeconds = positive(deathMemorySeconds, 300);
        deathProtectionRadius = positive(deathProtectionRadius, 16.0);
        farmDetectionRadius = positive(farmDetectionRadius, 6.0);
        farmItemThreshold = positive(farmItemThreshold, 24);
        classificationRefreshSeconds = positive(classificationRefreshSeconds, 5);
        valuableItems = safeList(valuableItems);
        commonItems = safeList(commonItems);
        farmDropItems = safeList(farmDropItems);
    }

    private static int positive(int value, int fallback) {
        return value > 0 ? value : fallback;
    }

    private static double positive(double value, double fallback) {
        return Double.isFinite(value) && value > 0.0 ? value : fallback;
    }

    private static List<String> safeList(List<String> values) {
        if (values == null) return new ArrayList<>();
        return values.stream()
            .filter(value -> value != null && value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
            .distinct()
            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }
}
