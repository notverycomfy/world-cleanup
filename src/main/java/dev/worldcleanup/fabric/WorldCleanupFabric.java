package dev.worldcleanup.fabric;

import dev.worldcleanup.WorldCleanup;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

public final class WorldCleanupFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        WorldCleanup.initialize(FabricLoader.getInstance().getConfigDir());
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayer player) {
                WorldCleanup.recordPlayerDeath(player);
            }
        });
    }
}
