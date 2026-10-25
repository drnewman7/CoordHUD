package dev.drnewman.coordinateshud.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.drnewman.coordinateshud.core.HudConfig.Anchor;
import dev.drnewman.coordinateshud.core.HudLayout.Content;
import dev.drnewman.coordinateshud.core.HudLayout.Rect;
import dev.drnewman.coordinateshud.core.HudLayout.TextRun;
import java.util.function.ToIntFunction;
import org.junit.jupiter.api.Test;

class HudLayoutTest {
    private static final ToIntFunction<String> W = s -> s.length() * 6;

    private static HudLayout build(HudConfig c, Content content) {
        return HudLayout.build(c, content, W, 9);
    }

    private static final Content FULL = new Content("248", "72", "-136", "NE", "45\u00B0", "24", null, null, null);

    @Test
    void defaultPanelIsCompact() {
        HudLayout l = build(new HudConfig(), FULL);
        assertTrue(l.width >= 140 && l.width <= 180, "width " + l.width);
        assertEquals(2 * 4 + 3 * 9 + 2 * 2, l.height);
        assertEquals(new Rect(l.width - 4 - 9, 4, 9, 9), l.button);
    }

    @Test
    void buttonNeverOverlapsCoordinates() {
        Content wide = new Content("-29999999", "-64", "-29999999", "N", "0\u00B0", "1", null, null, null);
        HudLayout l = build(new HudConfig(), wide);
        int rightmost = 0;
        for (TextRun r : l.runs) {
            if (r.y() == 4) rightmost = Math.max(rightmost, r.x() + W.applyAsInt(r.text()));
        }
        assertTrue(l.button.x() - rightmost >= HudLayout.COLUMN_GAP, "gap " + (l.button.x() - rightmost));
        assertTrue(l.button.x() + l.button.width() + HudLayout.PADDING <= l.width);
    }

    @Test
    void runsStayInsidePanel() {
        HudLayout l = build(new HudConfig(), FULL);
        for (TextRun r : l.runs) {
            assertTrue(r.x() >= 0 && r.x() + W.applyAsInt(r.text()) <= l.width, r.text());
            assertTrue(r.y() >= 0 && r.y() + 9 <= l.height, r.text());
        }
    }

    @Test
    void togglesRemoveRows() {
        HudConfig c = new HudConfig();
        c.showDay = false;
        c.showDirection = false;
        c.showHeading = false;
        HudLayout l = build(c, FULL);
        assertEquals(2 * 4 + 9, l.height);

        c.showCoordinates = false;
        assertTrue(build(c, FULL).isEmpty());
    }

    @Test
    void copyIconToggleAndNoIconWithoutCoordinates() {
        HudConfig c = new HudConfig();
        c.showCopyIcon = false;
        assertNull(build(c, FULL).button);
        c.showCopyIcon = true;
        c.showCoordinates = false;
        assertNull(build(c, FULL).button);
    }

    @Test
    void headingAloneGetsItsOwnLabel() {
        HudConfig c = new HudConfig();
        c.showDirection = false;
        HudLayout l = build(c, FULL);
        boolean found = false;
        for (TextRun r : l.runs) found |= r.text().equals("Heading:");
        assertTrue(found);
    }

    @Test
    void hitTest() {
        Rect r = new Rect(10, 20, 9, 9);
        assertTrue(r.contains(10, 20));
        assertTrue(r.contains(18, 28));
        assertFalse(r.contains(19, 28));
        assertFalse(r.contains(9, 20));
        assertFalse(r.contains(10, 29));
    }

    @Test
    void placementAnchorsAndClamps() {
        assertEquals(4, Placement.x(Anchor.TOP_LEFT, 150, 400, 4));
        assertEquals(400 - 150 - 4, Placement.x(Anchor.TOP_RIGHT, 150, 400, 4));
        assertEquals(240 - 39 - 4, Placement.y(Anchor.BOTTOM_LEFT, 39, 240, 4));
        assertEquals(4, Placement.y(Anchor.TOP_RIGHT, 39, 240, 4));
        assertEquals(0, Placement.x(Anchor.TOP_RIGHT, 150, 100, 4));      // narrower than panel
        assertEquals(250, Placement.x(Anchor.TOP_LEFT, 150, 400, 5000));  // offset clamped on-screen
    }

    @Test
    void clickLatchFiresOncePerPress() {
        ClickLatch l = new ClickLatch();
        assertFalse(l.pressedEdge(false));
        assertTrue(l.pressedEdge(true));
        assertFalse(l.pressedEdge(true));
        assertFalse(l.pressedEdge(true));
        assertFalse(l.pressedEdge(false));
        assertTrue(l.pressedEdge(true));
    }

    @Test
    void feedbackExpires() {
        FeedbackTimer t = new FeedbackTimer();
        assertEquals(FeedbackTimer.Kind.NONE, t.current(0));
        t.show(FeedbackTimer.Kind.SUCCESS, 1_000L);
        assertEquals(FeedbackTimer.Kind.SUCCESS, t.current(1_000L + FeedbackTimer.DURATION_NANOS - 1));
        assertEquals(FeedbackTimer.Kind.NONE, t.current(1_000L + FeedbackTimer.DURATION_NANOS));
        t.show(FeedbackTimer.Kind.FAILURE, 5L);
        assertEquals(FeedbackTimer.Kind.FAILURE, t.current(6L));
    }
}
