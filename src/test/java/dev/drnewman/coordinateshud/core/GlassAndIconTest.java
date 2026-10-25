package dev.drnewman.coordinateshud.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.drnewman.coordinateshud.core.HudLayout.Content;
import java.util.function.ToIntFunction;
import org.junit.jupiter.api.Test;

class GlassAndIconTest {
    private static final ToIntFunction<String> W = s -> s.length() * 6;

    @Test
    void stripsStayInsideBoundsAndAreVisible() {
        for (int r = 0; r <= 8; r++) {
            GlassShape g = GlassShape.glass(150, 40, r, 60, 0x7FC8E8);
            assertTrue(g.count > 0);
            for (int i = 0; i < g.count; i++) {
                assertTrue(g.x0[i] >= 0 && g.y0[i] >= 0 && g.x1[i] <= 150 && g.y1[i] <= 40, "r=" + r + " strip " + i);
                assertTrue(g.x1[i] > g.x0[i] && g.y1[i] > g.y0[i]);
                assertTrue((g.argb[i] >>> 24) > 0);
            }
        }
    }

    /** Alpha at a pixel, summed over strips (rough, enough to compare). */
    private static int alphaAt(GlassShape g, int x, int y) {
        int sum = 0;
        for (int i = 0; i < g.count; i++) {
            if (x >= g.x0[i] && x < g.x1[i] && y >= g.y0[i] && y < g.y1[i]) sum += g.argb[i] >>> 24;
        }
        return sum;
    }

    @Test
    void cornersAreRoundedAndSymmetric() {
        GlassShape g = GlassShape.glass(150, 40, 5, 60, 0x7FC8E8);
        assertEquals(0, alphaAt(g, 0, 0));            // outermost corner pixel is empty
        assertEquals(0, alphaAt(g, 149, 0));
        assertEquals(0, alphaAt(g, 0, 39));
        assertEquals(0, alphaAt(g, 149, 39));
        assertTrue(alphaAt(g, 75, 0) > 0);            // top rim present
        assertTrue(alphaAt(g, 0, 20) > 0);            // left rim present
        assertTrue(alphaAt(g, 75, 20) > 0);           // body present
        for (int y = 0; y < 40; y++) {
            for (int x = 0; x < 150; x++) {
                assertEquals(alphaAt(g, x, y), alphaAt(g, 149 - x, y), "mirror at " + x + "," + y);
            }
        }
    }

    @Test
    void radiusZeroIsAPlainRectangleWithRim() {
        GlassShape g = GlassShape.glass(50, 20, 0, 60, 0xFFFFFF);
        assertTrue(alphaAt(g, 0, 0) > 0);
        assertTrue(alphaAt(g, 49, 19) > 0);
    }

    @Test
    void flatIsOneStripAndZeroOpacityIsEmpty() {
        assertEquals(1, GlassShape.flat(10, 10, 60).count);
        assertEquals(0, GlassShape.flat(10, 10, 0).count);
    }

    @Test
    void minecraftStyleHasNotchedCornersAndFrame() {
        GlassShape g = GlassShape.minecraft(150, 40, 80);
        assertTrue(g.count > 0);
        for (int i = 0; i < g.count; i++) {
            assertTrue(g.x0[i] >= 0 && g.y0[i] >= 0 && g.x1[i] <= 150 && g.y1[i] <= 40);
        }
        assertEquals(0, alphaAt(g, 0, 0));             // notched corner
        assertEquals(0, alphaAt(g, 149, 39));
        assertTrue(alphaAt(g, 1, 0) > 0);              // edge pixel next to the notch
        assertTrue(alphaAt(g, 75, 20) > 0);            // body
        assertTrue(alphaAt(g, 75, 1) > alphaAt(g, 75, 20)); // frame adds to the body alpha
        for (int y = 0; y < 40; y++) {
            for (int x = 0; x < 150; x++) {
                assertEquals(alphaAt(g, x, y), alphaAt(g, 149 - x, y), "mirror " + x + "," + y);
            }
        }
        assertEquals(0, GlassShape.minecraft(150, 40, 0).count);
    }

    @Test
    void iconsFitTheirBox() {
        for (Icons.Icon icon : Icons.Icon.values()) {
            assertTrue(icon.rects().length > 0);
            for (int[] r : icon.rects()) {
                assertTrue(r[0] >= 0 && r[0] + r[2] <= Icons.SIZE && r[1] >= 0 && r[1] + r[3] <= Icons.SIZE, icon.name());
            }
        }
    }

    @Test
    void extraRowsAndIconsLayOut() {
        HudConfig c = new HudConfig();
        Content full = new Content("-256", "70", "88", "SW", "230\u00B0", "5", "Overworld", "-2048, 704", "12, 64, -30");
        HudLayout l = HudLayout.build(c, full, W, 9);
        assertEquals(2 * 4 + 5 * 9 + 4 * 2, l.height);
        assertEquals(5, l.icons.size());
        for (HudLayout.IconRun ic : l.icons) {
            assertEquals(4, ic.x());
            assertTrue(ic.y() >= 0 && ic.y() + Icons.SIZE <= l.height);
        }
        // text starts to the right of the icon column
        for (HudLayout.TextRun r : l.runs) {
            if (r.role() == HudLayout.Role.LABEL || r.role() == HudLayout.Role.AXIS_X) {
                assertTrue(r.x() >= 4 + Icons.SIZE + HudLayout.ROW_ICON_GAP);
            }
        }
        for (HudLayout.TextRun r : l.runs) {
            assertTrue(r.x() + W.applyAsInt(r.text()) <= l.width, r.text());
        }
    }

    @Test
    void togglesHideExtrasAndIcons() {
        HudConfig c = new HudConfig();
        c.showIcons = false;
        c.showDimensionConversion = false;
        c.showLastDeath = false;
        Content full = new Content("1", "2", "3", "N", "0\u00B0", "1", "Nether", "1, 2", "1, 2, 3");
        HudLayout l = HudLayout.build(c, full, W, 9);
        assertTrue(l.icons.isEmpty());
        assertEquals(2 * 4 + 3 * 9 + 2 * 2, l.height);
        assertFalse(l.runs.stream().anyMatch(r -> r.text().equals("Death:")));
        assertFalse(l.runs.stream().anyMatch(r -> r.text().equals("Nether:")));
    }

    @Test
    void newConfigOptionsValidate() throws Exception {
        java.util.Properties p = new java.util.Properties();
        p.load(new java.io.StringReader("scale=9999\ncornerRadius=-3\nstyle=classic\nshowIcons=false\n"));
        HudConfig c = HudConfig.fromProperties(p);
        assertEquals(300, c.scale);
        assertEquals(0, c.cornerRadius);
        assertEquals(HudConfig.Style.CLASSIC, c.style);
        java.util.Properties q = new java.util.Properties();
        q.setProperty("style", "Minecraft");
        assertEquals(HudConfig.Style.MINECRAFT, HudConfig.fromProperties(q).style);
        assertFalse(c.showIcons);
        assertEquals(HudConfig.Style.GLASS, new HudConfig().style);
        assertEquals(4, new HudConfig().cornerRadius);
    }
}
