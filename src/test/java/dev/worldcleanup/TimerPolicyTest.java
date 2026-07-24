package dev.worldcleanup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimerPolicyTest {
    private final CleanupConfig config = new CleanupConfig();

    @Test
    void defaultsHaveSensibleOrdering() {
        assertTrue(config.farmSeconds < config.commonSeconds);
        assertTrue(config.commonSeconds < config.playerThrownSeconds);
        assertTrue(config.playerThrownSeconds < config.mobDropSeconds);
        assertTrue(config.mobDropSeconds < config.defaultSeconds);
        assertTrue(config.playerThrownSeconds < config.deathProtectionSeconds);
        assertTrue(config.deathProtectionSeconds < config.valuableSeconds);
    }

    @Test
    void eachBaseCategoryUsesItsConfiguredTimer() {
        TimerPolicy.ItemContext uncategorized = TimerPolicy.ItemContext.uncategorized();

        assertEquals(600, resolve(uncategorized));
        assertEquals(120, resolve(uncategorized.withCommonCategory()));
        assertEquals(300, resolve(uncategorized.withMobDrop()));
        assertEquals(60, resolve(uncategorized.withDenseFarmDrop()));
        assertEquals(1200, resolve(uncategorized.withValuableCategory()));
        assertEquals(900, resolve(uncategorized.withRecentDeathProtection()));
        assertEquals(180, resolve(uncategorized.withPlayerThrown()));
    }

    @Test
    void playerThrownSourceIgnoresItemCategories() {
        TimerPolicy.ItemContext thrownValuableFarmDrop = TimerPolicy.ItemContext.uncategorized()
            .withPlayerThrown()
            .withMobDrop()
            .withCommonCategory()
            .withDenseFarmDrop()
            .withValuableCategory();

        assertEquals(180, resolve(thrownValuableFarmDrop));
        assertEquals(900, resolve(thrownValuableFarmDrop.withRecentDeathProtection()));
    }

    @Test
    void protectionsOverrideAggressiveCleanup() {
        TimerPolicy.ItemContext farmMobDrop = TimerPolicy.ItemContext.uncategorized()
            .withMobDrop()
            .withCommonCategory()
            .withDenseFarmDrop();

        assertEquals(1200, resolve(farmMobDrop.withValuableCategory()));
        assertEquals(900, resolve(farmMobDrop.withRecentDeathProtection()));
        assertEquals(1200, resolve(
            farmMobDrop.withValuableCategory().withRecentDeathProtection()
        ));
    }

    @Test
    void sourceAndFarmRulesOverrideOrdinaryCategoryTiming() {
        TimerPolicy.ItemContext commonMobDrop = TimerPolicy.ItemContext.uncategorized()
            .withMobDrop()
            .withCommonCategory();

        assertEquals(300, resolve(commonMobDrop));
        assertEquals(60, resolve(commonMobDrop.withDenseFarmDrop()));
    }

    private int resolve(TimerPolicy.ItemContext itemContext) {
        return TimerPolicy.lifetimeSeconds(config, itemContext);
    }
}
