package dev.worldcleanup;

public final class TimerPolicy {
    private TimerPolicy() {
    }

    public static int lifetimeSeconds(
        CleanupConfig config,
        boolean playerThrown,
        boolean mobDrop,
        boolean common,
        boolean denseFarmDrop,
        boolean valuable,
        boolean nearRecentDeath
    ) {
        if (playerThrown) {
            return nearRecentDeath
                ? Math.max(config.playerThrownSeconds, config.deathProtectionSeconds)
                : config.playerThrownSeconds;
        }

        int lifetime = common ? config.commonSeconds : config.defaultSeconds;
        if (mobDrop) lifetime = config.mobDropSeconds;
        if (denseFarmDrop) lifetime = Math.min(lifetime, config.farmSeconds);
        if (valuable) lifetime = Math.max(lifetime, config.valuableSeconds);
        if (nearRecentDeath) lifetime = Math.max(lifetime, config.deathProtectionSeconds);
        return lifetime;
    }
}
