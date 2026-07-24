package dev.worldcleanup.fabric.client;

import dev.worldcleanup.CleanupConfig;
import dev.worldcleanup.WorldCleanup;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

final class WorldCleanupConfigScreen extends Screen {
    private final Screen parent;
    private CleanupConfig draft = WorldCleanup.configCopy();
    private final List<EditBox> boxes = new ArrayList<>();
    private List<Field> fields = List.of();
    private int page;
    private String status = "";

    WorldCleanupConfigScreen(Screen parent) {
        super(Component.literal("World Cleanup Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        boxes.clear();
        fields = pages().get(page);
        int formWidth = Math.min(420, width - 40);
        int x = (width - formWidth) / 2;
        int y = 52;

        for (Field field : fields) {
            EditBox box = new EditBox(font, x, y + 11, formWidth, 20, Component.literal(field.label));
            box.setMaxLength(field.itemList ? 8192 : 32);
            box.setValue(field.read.apply(draft));
            boxes.add(addRenderableWidget(box));
            y += 43;
        }

        int bottom = height - 28;
        addRenderableWidget(Button.builder(Component.literal("<"), button -> changePage(-1))
            .bounds(width / 2 - 175, bottom, 40, 20).build()).active = page > 0;
        addRenderableWidget(Button.builder(Component.literal("Save"), button -> save())
            .bounds(width / 2 - 127, bottom, 62, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Reset defaults"), button -> resetDefaults())
            .bounds(width / 2 - 57, bottom, 96, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> onClose())
            .bounds(width / 2 + 47, bottom, 62, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> changePage(1))
            .bounds(width / 2 + 117, bottom, 40, 20).build()).active = page < pages().size() - 1;
    }

    private void changePage(int direction) {
        if (commitPage()) {
            page += direction;
            status = "";
            rebuildWidgets();
        }
    }

    private void save() {
        if (!commitPage()) return;
        if (WorldCleanup.applyAndSave(draft)) {
            status = "Saved. New item checks use these settings immediately.";
        } else {
            status = "Could not write the configuration file.";
        }
    }

    private void resetDefaults() {
        draft = new CleanupConfig();
        status = "Defaults loaded. Press Save to apply them.";
        rebuildWidgets();
    }

    private boolean commitPage() {
        try {
            for (int index = 0; index < fields.size(); index++) {
                fields.get(index).write.accept(draft, boxes.get(index).getValue().trim());
            }
            status = "";
            return true;
        } catch (IllegalArgumentException exception) {
            status = exception.getMessage();
            return false;
        }
    }

    @Override
    public void onClose() {
        try {
            try {
                minecraft.getClass().getMethod("setScreenAndShow", Screen.class).invoke(minecraft, parent);
            } catch (NoSuchMethodException ignored) {
                minecraft.getClass().getMethod("setScreen", Screen.class).invoke(minecraft, parent);
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not return to Mod Menu", exception);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title.getString(), width / 2, 16, 0xFFFFFFFF);
        graphics.centeredText(font, "Page " + (page + 1) + " / " + pages().size(), width / 2, 30, 0xFFA0A0A0);
        int y = 52;
        for (Field field : fields) {
            graphics.text(font, field.label, (width - Math.min(420, width - 40)) / 2, y, 0xFFFFFFFF);
            y += 43;
        }
        if (!status.isEmpty()) {
            graphics.centeredText(font, status, width / 2, height - 42, status.startsWith("Saved") ? 0xFF55FF55 : 0xFFFF5555);
        }
    }

    private List<List<Field>> pages() {
        return List.of(
            List.of(
                integer("Default lifetime (seconds)", c -> c.defaultSeconds, (c, v) -> c.defaultSeconds = v),
                integer("Valuable lifetime (seconds)", c -> c.valuableSeconds, (c, v) -> c.valuableSeconds = v),
                integer("Common/junk lifetime (seconds)", c -> c.commonSeconds, (c, v) -> c.commonSeconds = v),
                integer("Player-thrown lifetime (seconds)", c -> c.playerThrownSeconds, (c, v) -> c.playerThrownSeconds = v)
            ),
            List.of(
                integer("Mob-drop lifetime (seconds)", c -> c.mobDropSeconds, (c, v) -> c.mobDropSeconds = v),
                integer("Dense-farm lifetime (seconds)", c -> c.farmSeconds, (c, v) -> c.farmSeconds = v),
                integer("Death protection (seconds)", c -> c.deathProtectionSeconds, (c, v) -> c.deathProtectionSeconds = v),
                integer("Death-site memory (seconds)", c -> c.deathMemorySeconds, (c, v) -> c.deathMemorySeconds = v)
            ),
            List.of(
                decimal("Death protection radius", c -> c.deathProtectionRadius, (c, v) -> c.deathProtectionRadius = v),
                decimal("Farm detection radius", c -> c.farmDetectionRadius, (c, v) -> c.farmDetectionRadius = v),
                integer("Farm item threshold", c -> c.farmItemThreshold, (c, v) -> c.farmItemThreshold = v),
                integer("Refresh interval (seconds)", c -> c.classificationRefreshSeconds, (c, v) -> c.classificationRefreshSeconds = v)
            ),
            List.of(
                bool("Use data-pack category tags (true/false)", c -> c.useDataPackTags, (c, v) -> c.useDataPackTags = v),
                items("Valuable item IDs (comma-separated)", c -> c.valuableItems, (c, v) -> c.valuableItems = v),
                items("Common/junk item IDs (comma-separated)", c -> c.commonItems, (c, v) -> c.commonItems = v),
                items("Farm-drop item IDs (comma-separated)", c -> c.farmDropItems, (c, v) -> c.farmDropItems = v)
            )
        );
    }

    private static Field integer(String label, Function<CleanupConfig, Integer> read, BiConsumer<CleanupConfig, Integer> write) {
        return new Field(label, config -> Integer.toString(read.apply(config)), (config, text) -> {
            try {
                int value = Integer.parseInt(text);
                if (value <= 0) throw new NumberFormatException();
                write.accept(config, value);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(label + " must be a positive whole number.");
            }
        }, false);
    }

    private static Field decimal(String label, Function<CleanupConfig, Double> read, BiConsumer<CleanupConfig, Double> write) {
        return new Field(label, config -> Double.toString(read.apply(config)), (config, text) -> {
            try {
                double value = Double.parseDouble(text);
                if (!Double.isFinite(value) || value <= 0) throw new NumberFormatException();
                write.accept(config, value);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(label + " must be a positive number.");
            }
        }, false);
    }

    private static Field items(String label, Function<CleanupConfig, List<String>> read, BiConsumer<CleanupConfig, List<String>> write) {
        return new Field(label, config -> String.join(", ", read.apply(config)), (config, text) -> {
            List<String> values = text.isBlank() ? new ArrayList<>() : Arrays.stream(text.split(","))
                .map(String::trim).filter(value -> !value.isEmpty()).distinct().toList();
            String invalid = values.stream()
                .filter(value -> !value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
                .findFirst().orElse(null);
            if (invalid != null) throw new IllegalArgumentException("Invalid item ID: " + invalid);
            write.accept(config, new ArrayList<>(values));
        }, true);
    }

    private static Field bool(String label, Function<CleanupConfig, Boolean> read, BiConsumer<CleanupConfig, Boolean> write) {
        return new Field(label, config -> Boolean.toString(read.apply(config)), (config, text) -> {
            if (!text.equalsIgnoreCase("true") && !text.equalsIgnoreCase("false")) {
                throw new IllegalArgumentException(label + " must be true or false.");
            }
            write.accept(config, Boolean.parseBoolean(text));
        }, false);
    }

    private record Field(
        String label,
        Function<CleanupConfig, String> read,
        BiConsumer<CleanupConfig, String> write,
        boolean itemList
    ) {
    }
}
