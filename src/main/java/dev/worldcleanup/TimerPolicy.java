package dev.worldcleanup;

public final class TimerPolicy {
    private TimerPolicy() {
    }

    public static int lifetimeSeconds(
        CleanupConfig config,
        ItemContext item
    ) {
        if (item.playerThrown()) {
            return item.nearRecentDeath()
                ? Math.max(config.playerThrownSeconds, config.deathProtectionSeconds)
                : config.playerThrownSeconds;
        }

        int lifetime = item.common() ? config.commonSeconds : config.defaultSeconds;
        if (item.mobDrop()) lifetime = config.mobDropSeconds;
        if (item.denseFarmDrop()) lifetime = Math.min(lifetime, config.farmSeconds);
        if (item.valuable()) lifetime = Math.max(lifetime, config.valuableSeconds);
        if (item.nearRecentDeath()) lifetime = Math.max(lifetime, config.deathProtectionSeconds);
        return lifetime;
    }

    public record ItemContext(
        boolean playerThrown,
        boolean mobDrop,
        boolean common,
        boolean denseFarmDrop,
        boolean valuable,
        boolean nearRecentDeath
    ) {
        public static ItemContext uncategorized() {
            return new ItemContext(false, false, false, false, false, false);
        }

        public ItemContext withPlayerThrown() {
            return new ItemContext(true, mobDrop, common, denseFarmDrop, valuable, nearRecentDeath);
        }

        public ItemContext withMobDrop() {
            return new ItemContext(playerThrown, true, common, denseFarmDrop, valuable, nearRecentDeath);
        }

        public ItemContext withCommonCategory() {
            return new ItemContext(playerThrown, mobDrop, true, denseFarmDrop, valuable, nearRecentDeath);
        }

        public ItemContext withDenseFarmDrop() {
            return new ItemContext(playerThrown, mobDrop, common, true, valuable, nearRecentDeath);
        }

        public ItemContext withValuableCategory() {
            return new ItemContext(playerThrown, mobDrop, common, denseFarmDrop, true, nearRecentDeath);
        }

        public ItemContext withRecentDeathProtection() {
            return new ItemContext(playerThrown, mobDrop, common, denseFarmDrop, valuable, true);
        }
    }
}
