package dev.drnewman.coordinateshud.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DeathPointerTest {
    private static DeathPointer at(double px, double py, double pz, int dx, int dy, int dz) {
        DeathPointer p = new DeathPointer();
        p.update(px, py, pz, dx, dy, dz, false);
        return p;
    }

    /** Player stands at the centre of block (0,64,0); axes: +X east, +Z south. */
    @Test
    void eightDirections() {
        assertEquals(Direction.E, at(0.5, 64, 0.5, 48, 64, 0).dir);
        assertEquals(Direction.W, at(0.5, 64, 0.5, -48, 64, 0).dir);
        assertEquals(Direction.N, at(0.5, 64, 0.5, 0, 64, -48).dir);
        assertEquals(Direction.S, at(0.5, 64, 0.5, 0, 64, 48).dir);
        assertEquals(Direction.NE, at(0.5, 64, 0.5, 30, 64, -30).dir);
        assertEquals(Direction.SE, at(0.5, 64, 0.5, 30, 64, 30).dir);
        assertEquals(Direction.SW, at(0.5, 64, 0.5, -30, 64, 30).dir);
        assertEquals(Direction.NW, at(0.5, 64, 0.5, -30, 64, -30).dir);
    }

    @Test
    void cardinalOnly() {
        DeathPointer p = new DeathPointer();
        p.update(0.5, 64, 0.5, 30, 64, -20, true);
        assertEquals(Direction.E, p.dir);
        p.update(0.5, 64, 0.5, 20, 64, -30, true);
        assertEquals(Direction.N, p.dir);
    }

    @Test
    void distanceIsHorizontalAndRounded() {
        assertEquals(48, at(0.5, 64, 0.5, 48, 64, 0).distance);
        assertEquals(5, at(0.5, 64, 0.5, 3, 64, 4).distance); // 3-4-5 triangle
        assertEquals(48, at(0.5, 10, 0.5, 48, 64, 0).distance); // height ignored
    }

    @Test
    void verticalAndHere() {
        assertEquals(12, at(0.5, 52, 0.5, 48, 64, 0).dyShown);   // death is 12 above
        assertEquals(-5, at(0.5, 69, 0.5, 48, 64, 0).dyShown);   // death is 5 below
        assertEquals(0, at(0.5, 65, 0.5, 48, 64, 0).dyShown);    // 1 is below the threshold
        assertTrue(at(0.5, 64, 0.5, 0, 64, 0).here);
        assertFalse(at(0.5, 64, 0.5, 5, 64, 0).here);
        assertFalse(at(0.5, 40, 0.5, 0, 64, 0).here);            // same column but far above
    }

    @Test
    void pointerText() {
        assertEquals("48 blocks E", Extras.pointerText(at(0.5, 64, 0.5, 48, 64, 0)));
        assertEquals("1 block E", Extras.pointerText(at(0.5, 64, 0.5, 1, 64, 0)));
        assertEquals("48 blocks E, 12 up", Extras.pointerText(at(0.5, 52, 0.5, 48, 64, 0)));
        assertEquals("48 blocks E, 5 down", Extras.pointerText(at(0.5, 69, 0.5, 48, 64, 0)));
        assertEquals("Here", Extras.pointerText(at(0.5, 64, 0.5, 0, 64, 0)));
    }
}
