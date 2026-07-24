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
        assertEquals(600, resolve(false, false, false, false, false, false));
        assertEquals(120, resolve(false, false, true, false, false, false));
        assertEquals(300, resolve(false, true, false, false, false, false));
        assertEquals(60, resolve(false, false, false, true, false, false));
        assertEquals(1200, resolve(false, false, false, false, true, false));
        assertEquals(900, resolve(false, false, false, false, false, true));
        assertEquals(180, resolve(true, false, false, false, false, false));
    }

    @Test
    void playerThrownSourceIgnoresItemCategories() {
        assertEquals(180, resolve(true, true, true, true, true, false));
        assertEquals(900, resolve(true, true, true, true, true, true));
    }

    @Test
    void protectionsOverrideAggressiveCleanup() {
        assertEquals(1200, resolve(false, true, true, true, true, false));
        assertEquals(900, resolve(false, true, true, true, false, true));
        assertEquals(1200, resolve(false, true, true, true, true, true));
    }

    @Test
    void sourceAndFarmRulesOverrideOrdinaryCategoryTiming() {
        assertEquals(300, resolve(false, true, true, false, false, false));
        assertEquals(60, resolve(false, true, true, true, false, false));
    }

    private int resolve(boolean player, boolean mob, boolean common, boolean farm, boolean valuable, boolean death) {
        return TimerPolicy.lifetimeSeconds(config, player, mob, common, farm, valuable, death);
    }
}
