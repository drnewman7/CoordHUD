package dev.drnewman.coordinateshud.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Pure layout of the compact panel: where every text run, icon and the copy button sit, relative to the
 * panel's top-left corner. Text width is injected, so this is testable without Minecraft.
 * The layout is rebuilt only when displayed values change, not every frame.
 */
public final class HudLayout {
    public enum Role { LABEL, VALUE, ACCENT, AXIS_X, AXIS_Y, AXIS_Z }

    public record TextRun(String text, int x, int y, Role role) {}

    public record IconRun(Icons.Icon icon, int x, int y) {}

    public record Rect(int x, int y, int width, int height) {
        public boolean contains(int px, int py) {
            return px >= x && px < x + width && py >= y && py < y + height;
        }
    }

    /** Strings to show. A null field means "do not show that element". */
    public record Content(String x, String y, String z, String direction, String heading, String day,
                          String convLabel, String convText, String deathText) {}

    public static final int PADDING = 4;
    public static final int LINE_GAP = 2;
    public static final int MIN_WIDTH = 140;
    public static final int COLUMN_GAP = 8;
    public static final int ICON_SIZE = 9;       // copy button
    public static final int ROW_ICON_GAP = 3;    // gap between a row icon and its text

    private static final HudLayout EMPTY = new HudLayout(0, 0, List.of(), List.of(), null);

    public final int width;
    public final int height;
    public final List<TextRun> runs;
    public final List<IconRun> icons;
    /** Copy button rectangle relative to the panel, or null when not shown. */
    public final Rect button;

    private HudLayout(int width, int height, List<TextRun> runs, List<IconRun> icons, Rect button) {
        this.width = width;
        this.height = height;
        this.runs = runs;
        this.icons = icons;
        this.button = button;
    }

    public static HudLayout empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return runs.isEmpty();
    }

    public static HudLayout build(HudConfig cfg, Content c, ToIntFunction<String> w, int lineHeight) {
        boolean coords = cfg.showCoordinates && c.x() != null && c.y() != null && c.z() != null;
        boolean dir = cfg.showDirection && c.direction() != null;
        boolean head = cfg.showHeading && c.heading() != null;
        boolean day = cfg.showDay && c.day() != null;
        boolean conv = cfg.showDimensionConversion && c.convLabel() != null && c.convText() != null;
        boolean death = cfg.showLastDeath && c.deathText() != null;
        boolean button = coords && cfg.showCopyIcon;
        int rows = (coords ? 1 : 0) + (dir || head ? 1 : 0) + (day ? 1 : 0) + (conv ? 1 : 0) + (death ? 1 : 0);
        if (rows == 0) {
            return EMPTY;
        }

        int space = w.applyAsInt(" ");
        int left = PADDING + (cfg.showIcons ? Icons.SIZE + ROW_ICON_GAP : 0);
        String dirLabel = dir ? "Facing:" : "Heading:";
        String dirValue = dir ? c.direction() : c.heading();
        String convLabel = conv ? c.convLabel() + ":" : null;

        // Pass 1: required width.
        int width = MIN_WIDTH;
        if (coords) {
            int end = left + pair(w, "X:", c.x(), space) + COLUMN_GAP
                    + pair(w, "Y:", c.y(), space) + COLUMN_GAP + pair(w, "Z:", c.z(), space);
            width = Math.max(width, end + (button ? COLUMN_GAP + ICON_SIZE : 0) + PADDING);
        }
        if (dir || head) {
            int end = left + pair(w, dirLabel, dirValue, space);
            if (dir && head) {
                end += COLUMN_GAP + w.applyAsInt(c.heading());
            }
            width = Math.max(width, end + PADDING);
        }
        if (day) {
            width = Math.max(width, left + pair(w, "Day:", c.day(), space) + PADDING);
        }
        if (conv) {
            width = Math.max(width, left + pair(w, convLabel, c.convText(), space) + PADDING);
        }
        if (death) {
            width = Math.max(width, left + pair(w, "Death:", c.deathText(), space) + PADDING);
        }

        // Pass 2: place everything.
        List<TextRun> runs = new ArrayList<>(16);
        List<IconRun> icons = new ArrayList<>(5);
        Rect buttonRect = null;
        int rowStep = lineHeight + LINE_GAP;
        int row = 0;
        int iconDy = (lineHeight - Icons.SIZE) / 2;

        if (coords) {
            int y = PADDING;
            if (cfg.showIcons) icons.add(new IconRun(Icons.Icon.PIN, PADDING, y + iconDy));
            int x = left;
            x = place(runs, w, "X:", c.x(), x, y, space, Role.AXIS_X, Role.VALUE) + COLUMN_GAP;
            x = place(runs, w, "Y:", c.y(), x, y, space, Role.AXIS_Y, Role.VALUE) + COLUMN_GAP;
            place(runs, w, "Z:", c.z(), x, y, space, Role.AXIS_Z, Role.VALUE);
            if (button) {
                buttonRect = new Rect(width - PADDING - ICON_SIZE, y + (lineHeight - ICON_SIZE) / 2, ICON_SIZE, ICON_SIZE);
            }
            row++;
        }
        if (dir || head) {
            int y = PADDING + row * rowStep;
            if (cfg.showIcons) icons.add(new IconRun(Icons.Icon.COMPASS, PADDING, y + iconDy));
            place(runs, w, dirLabel, dirValue, left, y, space, Role.LABEL, dir ? Role.ACCENT : Role.VALUE);
            if (dir && head) {
                runs.add(new TextRun(c.heading(), width - PADDING - w.applyAsInt(c.heading()), y, Role.VALUE));
            }
            row++;
        }
        if (day) {
            int y = PADDING + row * rowStep;
            if (cfg.showIcons) icons.add(new IconRun(Icons.Icon.SUN, PADDING, y + iconDy));
            place(runs, w, "Day:", c.day(), left, y, space, Role.LABEL, Role.VALUE);
            row++;
        }
        if (conv) {
            int y = PADDING + row * rowStep;
            if (cfg.showIcons) icons.add(new IconRun(Icons.Icon.PORTAL, PADDING, y + iconDy));
            place(runs, w, convLabel, c.convText(), left, y, space, Role.LABEL, Role.VALUE);
            row++;
        }
        if (death) {
            int y = PADDING + row * rowStep;
            if (cfg.showIcons) icons.add(new IconRun(Icons.Icon.SKULL, PADDING, y + iconDy));
            place(runs, w, "Death:", c.deathText(), left, y, space, Role.LABEL, Role.VALUE);
            row++;
        }
        int height = 2 * PADDING + rows * lineHeight + (rows - 1) * LINE_GAP;
        return new HudLayout(width, height, List.copyOf(runs), List.copyOf(icons), buttonRect);
    }

    private static int pair(ToIntFunction<String> w, String label, String value, int space) {
        return w.applyAsInt(label) + space + w.applyAsInt(value);
    }

    /** Adds "label" then "value"; returns the x where the value ends. */
    private static int place(List<TextRun> runs, ToIntFunction<String> w, String label, String value,
                             int x, int y, int space, Role labelRole, Role valueRole) {
        runs.add(new TextRun(label, x, y, labelRole));
        int vx = x + w.applyAsInt(label) + space;
        runs.add(new TextRun(value, vx, y, valueRole));
        return vx + w.applyAsInt(value);
    }
}
