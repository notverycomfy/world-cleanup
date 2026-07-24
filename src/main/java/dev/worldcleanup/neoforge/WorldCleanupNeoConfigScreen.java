package dev.worldcleanup.neoforge;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

final class WorldCleanupNeoConfigScreen extends Screen {
    private final ModContainer mod;
    private final Screen parent;
    private String status = "";

    WorldCleanupNeoConfigScreen(ModContainer mod, Screen parent) {
        super(Component.literal("World Cleanup Configuration"));
        this.mod = mod;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = width / 2 - 100;
        int y = height / 2 - 34;
        addRenderableWidget(Button.builder(Component.literal("Edit settings"), button ->
            openScreen(new ConfigurationScreen(mod, this))
        ).bounds(x, y, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Reset defaults"), button -> {
            WorldCleanupNeoConfig.resetDefaults();
            status = "Defaults restored and saved.";
        }).bounds(x, y + 28, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
            .bounds(x, y + 56, 200, 20).build());
    }

    @Override
    public void onClose() {
        openScreen(parent);
    }

    private void openScreen(Screen screen) {
        try {
            try {
                minecraft.getClass().getMethod("setScreenAndShow", Screen.class).invoke(minecraft, screen);
            } catch (NoSuchMethodException ignored) {
                minecraft.getClass().getMethod("setScreen", Screen.class).invoke(minecraft, screen);
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not open the World Cleanup config screen", exception);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title.getString(), width / 2, height / 2 - 62, 0xFFFFFFFF);
        if (!status.isEmpty()) {
            graphics.centeredText(font, status, width / 2, height / 2 + 52, 0xFF55FF55);
        }
    }
}
