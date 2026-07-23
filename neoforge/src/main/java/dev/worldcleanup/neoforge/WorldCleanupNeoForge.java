package dev.worldcleanup.neoforge;

import dev.worldcleanup.WorldCleanup;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@Mod(WorldCleanup.MOD_ID)
public final class WorldCleanupNeoForge {
    public WorldCleanupNeoForge(IEventBus modEventBus, net.neoforged.fml.ModContainer modContainer) {
        WorldCleanup.initialize(FMLPaths.CONFIGDIR.get());
        modContainer.registerConfig(ModConfig.Type.COMMON, WorldCleanupNeoConfig.SPEC, "world-cleanup.toml");
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            registerClientConfigScreen(modContainer);
        }
        modEventBus.addListener(this::onConfigChanged);
        NeoForge.EVENT_BUS.register(this);
    }

    private static void registerClientConfigScreen(net.neoforged.fml.ModContainer modContainer) {
        try {
            Class<?> client = Class.forName("dev.worldcleanup.neoforge.WorldCleanupNeoClient");
            client.getMethod("register", net.neoforged.fml.ModContainer.class).invoke(null, modContainer);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not register the World Cleanup config screen", exception);
        }
    }

    private void onConfigChanged(ModConfigEvent event) {
        if (event.getConfig().getSpec() == WorldCleanupNeoConfig.SPEC) {
            WorldCleanupNeoConfig.apply();
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WorldCleanup.recordPlayerDeath(player);
        }
    }
}
