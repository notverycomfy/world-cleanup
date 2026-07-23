package dev.worldcleanup.neoforge;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class WorldCleanupNeoClient {
    private WorldCleanupNeoClient() {
    }

    public static void register(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, WorldCleanupNeoConfigScreen::new);
    }
}
