package dev.drnewman.coordinateshud.client;

import dev.drnewman.coordinateshud.core.AccentPreset;
import dev.drnewman.coordinateshud.core.HudConfig;
import dev.drnewman.coordinateshud.core.Stepper;
import java.util.function.IntConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * In-game settings (default key: H). Edits the live config object, so the HUD behind the screen
 * changes as you drag; everything is saved when the screen closes.
 * Two pages: "Look" (size, opacity, ...) and "Show" (which rows appear).
 */
final class SettingsScreen extends Screen {
    private static final int COL_W = 124;
    private static final int GAP = 6;
    private static final int ROW_H = 24;
    private static final int TOP = 26;
    private static final int H = 20;

    private final HudConfig cfg;
    private final Runnable save;
    private int page;

    SettingsScreen(HudConfig cfg, Runnable save) {
        super(Component.literal("CoordHUD"));
        this.cfg = cfg;
        this.save = save;
    }

    @Override
    protected void init() {
        if (page == 0) {
            buildLookPage();
        } else {
            buildShowPage();
        }
    }

    // ---------------------------------------------------------------- pages

    private void buildLookPage() {
        int i = 0;
        add(slider(i++, "Size", 50, 300, 5, "%", cfg.scale, v -> cfg.scale = v));
        add(slider(i++, "Opacity", 0, 100, 5, "%", cfg.backgroundOpacity, v -> cfg.backgroundOpacity = v));
        add(slider(i++, "Corners", 0, 8, 1, " px", cfg.cornerRadius, v -> cfg.cornerRadius = v));
        add(CycleButton.<HudConfig.Style>builder(v -> Component.literal(styleName(v)), cfg.style)
                .withValues(HudConfig.Style.values())
                .create(x(i), y(i++), COL_W, H, Component.literal("Style"), (b, v) -> { cfg.style = v; cfg.changed(); }));
        add(CycleButton.<HudConfig.Anchor>builder(v -> Component.literal(pretty(v.name())), cfg.anchor)
                .withValues(HudConfig.Anchor.values())
                .create(x(i), y(i++), COL_W, H, Component.literal("Corner"), (b, v) -> { cfg.anchor = v; cfg.changed(); }));
        add(CycleButton.<AccentPreset>builder(v -> Component.literal(v.label), AccentPreset.closest(cfg.accentColor))
                .withValues(AccentPreset.values())
                .create(x(i), y(i++), COL_W, H, Component.literal("Accent"), (b, v) -> { cfg.accentColor = v.rgb; cfg.changed(); }));
        add(slider(i++, "Offset X", 0, 200, 1, " px", cfg.offsetX, v -> cfg.offsetX = v));
        add(slider(i++, "Offset Y", 0, 200, 1, " px", cfg.offsetY, v -> cfg.offsetY = v));
        add(CycleButton.<HudConfig.CoordinateFormat>builder(
                        v -> Component.literal(v == HudConfig.CoordinateFormat.BLOCK ? "Blocks" : "Decimal"), cfg.coordinateFormat)
                .withValues(HudConfig.CoordinateFormat.values())
                .create(x(i), y(i++), COL_W, H, Component.literal("Coords"), (b, v) -> { cfg.coordinateFormat = v; cfg.changed(); }));
        add(CycleButton.<HudConfig.DirectionStyle>builder(
                        v -> Component.literal(v == HudConfig.DirectionStyle.CARDINAL ? "4-way" : "8-way"), cfg.directionStyle)
                .withValues(HudConfig.DirectionStyle.values())
                .create(x(i), y(i++), COL_W, H, Component.literal("Compass"), (b, v) -> { cfg.directionStyle = v; cfg.changed(); }));
        add(CycleButton.<HudConfig.DeathStyle>builder(v -> Component.literal(pretty(v.name())), cfg.deathStyle)
                .withValues(HudConfig.DeathStyle.values())
                .create(x(i), y(i++), COL_W, H, Component.literal("Death"), (b, v) -> { cfg.deathStyle = v; cfg.changed(); }));
        add(CycleButton.<Integer>builder(v -> Component.literal("Day " + v), cfg.firstDay)
                .withValues(1, 0)
                .create(x(i), y(i++), COL_W, H, Component.literal("Starts at"), (b, v) -> { cfg.firstDay = v; cfg.changed(); }));
        buildFooter(i, "Show options >");
    }

    private void buildShowPage() {
        int i = 0;
        add(toggle(i++, "HUD", cfg.enabled, v -> cfg.enabled = v));
        add(toggle(i++, "Coordinates", cfg.showCoordinates, v -> cfg.showCoordinates = v));
        add(toggle(i++, "Direction", cfg.showDirection, v -> cfg.showDirection = v));
        add(toggle(i++, "Degrees", cfg.showHeading, v -> cfg.showHeading = v));
        add(toggle(i++, "Day", cfg.showDay, v -> cfg.showDay = v));
        add(toggle(i++, "Copy icon", cfg.showCopyIcon, v -> cfg.showCopyIcon = v));
        add(toggle(i++, "Row icons", cfg.showIcons, v -> cfg.showIcons = v));
        add(toggle(i++, "Colored XYZ", cfg.coloredAxes, v -> cfg.coloredAxes = v));
        add(toggle(i++, "Nether row", cfg.showDimensionConversion, v -> cfg.showDimensionConversion = v));
        add(toggle(i++, "Death row", cfg.showLastDeath, v -> cfg.showLastDeath = v));
        add(toggle(i++, "Hide on F3", cfg.hideWithDebugScreen, v -> cfg.hideWithDebugScreen = v));
        add(toggle(i++, "Hide in menus", cfg.hideWhenScreenOpen, v -> cfg.hideWhenScreenOpen = v));
        buildFooter(i, "< Look options");
    }

    private void buildFooter(int widgetCount, String switchLabel) {
        int footerY = TOP + ((widgetCount + 1) / 2) * ROW_H + 4;
        add(Button.builder(Component.literal(switchLabel), b -> {
            page = 1 - page;
            init(this.width, this.height); // rebuilds the widgets for the other page
        }).bounds(left(), footerY, COL_W, H).build());
        add(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(left() + COL_W + GAP, footerY, COL_W, H).build());
    }

    // ---------------------------------------------------------------- helpers

    private void add(net.minecraft.client.gui.components.AbstractWidget w) {
        addRenderableWidget(w);
    }

    private int left() {
        return this.width - (2 * COL_W + GAP) - 12;
    }

    private int x(int index) {
        return left() + (index % 2) * (COL_W + GAP);
    }

    private int y(int index) {
        return TOP + (index / 2) * ROW_H;
    }

    private IntSlider slider(int index, String label, int min, int max, int step, String suffix, int current, IntConsumer apply) {
        return new IntSlider(x(index), y(index), COL_W, H, label, min, max, step, suffix, current, v -> {
            apply.accept(v);
            cfg.changed();
        });
    }

    private CycleButton<Boolean> toggle(int index, String label, boolean current, java.util.function.Consumer<Boolean> apply) {
        return CycleButton.onOffBuilder(current)
                .create(x(index), y(index), COL_W, H, Component.literal(label), (b, v) -> {
                    apply.accept(v);
                    cfg.changed();
                });
    }

    private static String styleName(HudConfig.Style v) {
        return switch (v) {
            case GLASS -> "Glass";
            case MINECRAFT -> "Minecraft";
            case CLASSIC -> "Flat";
        };
    }

    private static String pretty(String enumName) {
        String[] parts = enumName.toLowerCase(java.util.Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------- screen behaviour

    @Override
    public boolean isPauseScreen() {
        return false; // keep the world running so the live preview behind the screen stays alive
    }

    /** No blur or dimming, so the HUD behind the screen is shown exactly as it will look in game. */
    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        int panelLeft = left() - 8;
        int panelRight = left() + 2 * COL_W + GAP + 8;
        int rows = page == 0 ? 6 : 6;
        int panelBottom = TOP + rows * ROW_H + 4 + H + 8;
        g.fill(panelLeft, 4, panelRight, panelBottom, 0xA0000000);
        g.centeredText(Minecraft.getInstance().font, "CoordHUD  -  " + (page == 0 ? "Look" : "Show"),
                (panelLeft + panelRight) / 2, 12, 0xFFFFFFFF);
        super.extractRenderState(g, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        cfg.changed();
        save.run();
    }

    // ---------------------------------------------------------------- slider

    private static final class IntSlider extends AbstractSliderButton {
        private final String label;
        private final int min;
        private final int max;
        private final int step;
        private final String suffix;
        private final IntConsumer onChange;

        IntSlider(int x, int y, int w, int h, String label, int min, int max, int step, String suffix,
                  int current, IntConsumer onChange) {
            super(x, y, w, h, Component.empty(), Stepper.toFraction(current, min, max));
            this.label = label;
            this.min = min;
            this.max = max;
            this.step = step;
            this.suffix = suffix;
            this.onChange = onChange;
            updateMessage();
        }

        private int current() {
            return Stepper.fromFraction(this.value, min, max, step);
        }

        @Override
        protected void updateMessage() {
            if (label == null) {
                return; // called by the superclass constructor before our fields exist
            }
            setMessage(Component.literal(label + ": " + current() + suffix));
        }

        @Override
        protected void applyValue() {
            onChange.accept(current());
        }
    }
}
