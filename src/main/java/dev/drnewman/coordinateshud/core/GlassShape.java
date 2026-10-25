package dev.drnewman.coordinateshud.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Pre-computed list of coloured rectangles that draws a rounded "glass" panel using only fill calls:
 * anti-aliased corners, a translucent body with a soft vertical gradient, a bright rim that fades
 * toward the bottom (tinted with the accent colour) and a thin sheen under the top edge.
 * Built when the layout changes, never per frame. Pure Java, no Minecraft classes.
 */
public final class GlassShape {
    public final int width;
    public final int height;
    public final int count;
    public final int[] x0;
    public final int[] y0;
    public final int[] x1;
    public final int[] y1;
    public final int[] argb;

    private GlassShape(int width, int height, List<int[]> strips) {
        this.width = width;
        this.height = height;
        this.count = strips.size();
        x0 = new int[count];
        y0 = new int[count];
        x1 = new int[count];
        y1 = new int[count];
        argb = new int[count];
        for (int i = 0; i < count; i++) {
            int[] s = strips.get(i);
            x0[i] = s[0];
            y0[i] = s[1];
            x1[i] = s[2];
            y1[i] = s[3];
            argb[i] = s[4];
        }
    }

    /** Classic flat rectangle: one strip. */
    public static GlassShape flat(int w, int h, int opacityPct) {
        List<int[]> s = new ArrayList<>(1);
        int a = alphaOf(opacityPct);
        if (a > 0) {
            s.add(new int[] {0, 0, w, h, a << 24});
        }
        return new GlassShape(w, h, s);
    }

    /**
     * Vanilla-tooltip look: near-black purple body with notched corners and a 1px purple frame inset by one
     * pixel whose colour fades from blue-violet at the top to deep violet at the bottom.
     */
    public static GlassShape minecraft(int w, int h, int opacityPct) {
        List<int[]> s = new ArrayList<>(64);
        int pct = Math.max(0, Math.min(100, opacityPct));
        int a = (int) Math.round(0xF0 * pct / 100.0);
        if (a > 0 && w >= 4 && h >= 4) {
            add(s, 1, 0, w - 1, 1, a, 0x100010);
            add(s, 0, 1, w, h - 1, a, 0x100010);
            add(s, 1, h - 1, w - 1, h, a, 0x100010);
            int ba = (int) Math.round(0x60 + 0x60 * pct / 100.0);
            for (int y = 1; y <= h - 2; y++) {
                double t = (double) (y - 1) / Math.max(1, h - 3);
                int rgb = lerpRgb(0x5000FF, 0x28007F, t);
                if (y == 1 || y == h - 2) {
                    add(s, 1, y, w - 1, y + 1, ba, rgb);
                } else {
                    add(s, 1, y, 2, y + 1, ba, rgb);
                    add(s, w - 2, y, w - 1, y + 1, ba, rgb);
                }
            }
        }
        return new GlassShape(w, h, s);
    }

    public static GlassShape glass(int w, int h, int radius, int opacityPct, int accentRgb) {
        int r = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
        int baseAlpha = alphaOf(opacityPct);
        List<int[]> s = new ArrayList<>(128);

        // ---- body (inside the 1px rim) ----
        for (int y = 1; y < h - 1; y++) {
            double t = h > 1 ? (double) y / (h - 1) : 0;
            int rgb = lerpRgb(0x26323F, 0x0A0E14, t);
            int a = (int) Math.round(baseAlpha * (1.0 - 0.10 * t));
            boolean cornerRow = y < r || y >= h - r;
            if (!cornerRow) {
                add(s, 1, y, w - 1, y + 1, a, rgb);
            } else {
                add(s, r, y, w - r, y + 1, a, rgb);
                int cy = y < r ? y : h - 1 - y;
                for (int cx = 1; cx < r; cx++) {
                    double cov = coverage(cx, cy, r, r - 1);
                    if (cov > 0) {
                        int pa = (int) Math.round(a * cov);
                        add(s, cx, y, cx + 1, y + 1, pa, rgb);
                        add(s, w - 1 - cx, y, w - cx, y + 1, pa, rgb);
                    }
                }
            }
        }

        // ---- rim: 1px, bright on top, fading and accent-tinted toward the bottom ----
        for (int y = 0; y < h; y++) {
            double t = h > 1 ? (double) y / (h - 1) : 0;
            int a = (int) Math.round(lerp(0x80, 0x30, t));
            int rgb = lerpRgb(0xFFFFFF, accentRgb, 0.55 * t);
            if (y == 0 || y == h - 1) {
                add(s, r, y, w - r, y + 1, a, rgb);
            } else if (y >= r && y < h - r) {
                add(s, 0, y, 1, y + 1, a, rgb);
                add(s, w - 1, y, w, y + 1, a, rgb);
            }
            if (y < r || y >= h - r) {
                int cy = y < r ? y : h - 1 - y;
                for (int cx = 0; cx < r; cx++) {
                    double ring = coverage(cx, cy, r, r) - coverage(cx, cy, r, r - 1);
                    if (ring > 0) {
                        int pa = (int) Math.round(a * ring);
                        add(s, cx, y, cx + 1, y + 1, pa, rgb);
                        add(s, w - 1 - cx, y, w - cx, y + 1, pa, rgb);
                    }
                }
            }
        }

        // ---- sheen just under the top rim ----
        int inset = Math.max(r, 2);
        if (w > 2 * inset && h > 4) {
            add(s, inset, 1, w - inset, 2, 0x34, 0xFFFFFF);
            add(s, inset + 1, 2, w - inset - 1, 3, 0x12, 0xFFFFFF);
        }
        return new GlassShape(w, h, s);
    }

    /**
     * Fraction (0..1) of the pixel at (px,py) of the top-left corner square that lies inside a circle of
     * the given radius centred at (cr, cr). 4x4 supersampling; a radius <= 0 covers nothing.
     */
    static double coverage(int px, int py, int cr, int radius) {
        if (radius <= 0) {
            return 0;
        }
        int hits = 0;
        double r2 = (double) radius * radius;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                double sx = px + (i + 0.5) / 4.0 - cr;
                double sy = py + (j + 0.5) / 4.0 - cr;
                if (sx * sx + sy * sy <= r2) {
                    hits++;
                }
            }
        }
        return hits / 16.0;
    }

    private static void add(List<int[]> list, int x0, int y0, int x1, int y1, int alpha, int rgb) {
        int a = Math.max(0, Math.min(255, alpha));
        if (a == 0 || x1 <= x0 || y1 <= y0) {
            return;
        }
        list.add(new int[] {x0, y0, x1, y1, (a << 24) | (rgb & 0xFFFFFF)});
    }

    private static int alphaOf(int opacityPct) {
        return Math.round(Math.max(0, Math.min(100, opacityPct)) * 255f / 100f);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static int lerpRgb(int a, int b, double t) {
        int r = (int) Math.round(lerp((a >> 16) & 255, (b >> 16) & 255, t));
        int g = (int) Math.round(lerp((a >> 8) & 255, (b >> 8) & 255, t));
        int bl = (int) Math.round(lerp(a & 255, b & 255, t));
        return (r << 16) | (g << 8) | bl;
    }
}
