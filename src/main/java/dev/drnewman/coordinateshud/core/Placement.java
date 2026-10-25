package dev.drnewman.coordinateshud.core;

/** Screen placement for the panel, always clamped on-screen so resizing can never lose the HUD. */
public final class Placement {
    private Placement() {}

    public static int x(HudConfig.Anchor anchor, int panelWidth, int screenWidth, int offset) {
        int v = anchor.isRight() ? screenWidth - panelWidth - offset : offset;
        return clamp(v, 0, Math.max(0, screenWidth - panelWidth));
    }

    public static int y(HudConfig.Anchor anchor, int panelHeight, int screenHeight, int offset) {
        int v = anchor.isBottom() ? screenHeight - panelHeight - offset : offset;
        return clamp(v, 0, Math.max(0, screenHeight - panelHeight));
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
