package dev.drnewman.coordinateshud.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CompassTest {
    private static Direction dir(double yaw) {
        return Compass.fromYaw(yaw, false);
    }

    /** Minecraft: yaw 0 = south, 90 = west, 180/-180 = north, -90 = east. */
    @Test
    void vanillaYawConvention() {
        assertEquals(Direction.S, dir(0));
        assertEquals(Direction.W, dir(90));
        assertEquals(Direction.N, dir(180));
        assertEquals(Direction.N, dir(-180));
        assertEquals(Direction.E, dir(-90));
        assertEquals(Direction.E, dir(270));
    }

    @Test
    void diagonals() {
        assertEquals(Direction.SW, dir(45));
        assertEquals(Direction.NW, dir(135));
        assertEquals(Direction.NE, dir(-135));
        assertEquals(Direction.SE, dir(-45));
    }

    @Test
    void eastIsNeverWestAndNorthIsNeverSouth() {
        for (double yaw = -720; yaw <= 720; yaw += 0.5) {
            Direction d = dir(yaw);
            double h = Compass.heading(yaw);
            if (h > 67.5 && h < 112.5) assertEquals(Direction.E, d);
            if (h > 247.5 && h < 292.5) assertEquals(Direction.W, d);
            if (h > 337.5 || h < 22.5) assertEquals(Direction.N, d);
            if (h > 157.5 && h < 202.5) assertEquals(Direction.S, d);
        }
    }

    @Test
    void sectorBoundariesAreCentredOnHeadings() {
        assertEquals(Direction.N, Compass.eight(22.4999));
        assertEquals(Direction.NE, Compass.eight(22.5));
        assertEquals(Direction.NE, Compass.eight(67.4999));
        assertEquals(Direction.E, Compass.eight(67.5));
        assertEquals(Direction.E, Compass.eight(112.4999));
        assertEquals(Direction.SE, Compass.eight(112.5));
        assertEquals(Direction.SE, Compass.eight(157.4999));
        assertEquals(Direction.S, Compass.eight(157.5));
        assertEquals(Direction.S, Compass.eight(202.4999));
        assertEquals(Direction.SW, Compass.eight(202.5));
        assertEquals(Direction.SW, Compass.eight(247.4999));
        assertEquals(Direction.W, Compass.eight(247.5));
        assertEquals(Direction.W, Compass.eight(292.4999));
        assertEquals(Direction.NW, Compass.eight(292.5));
        assertEquals(Direction.NW, Compass.eight(337.4999));
        assertEquals(Direction.N, Compass.eight(337.5));
        assertEquals(Direction.N, Compass.eight(359.9999));
        assertEquals(Direction.N, Compass.eight(0));
    }

    @Test
    void cardinalOnly() {
        assertEquals(Direction.N, Compass.cardinal(44.9999));
        assertEquals(Direction.E, Compass.cardinal(45));
        assertEquals(Direction.E, Compass.cardinal(134.9999));
        assertEquals(Direction.S, Compass.cardinal(135));
        assertEquals(Direction.W, Compass.cardinal(270));
        assertEquals(Direction.N, Compass.cardinal(315));
        assertEquals(Direction.N, Compass.cardinal(359));
        assertEquals(Direction.S, Compass.fromYaw(0, true));
        assertEquals(Direction.W, Compass.fromYaw(90, true));
    }

    @Test
    void yawNormalization() {
        assertEquals(0.0, Compass.normalizeYaw(0), 1e-9);
        assertEquals(-180.0, Compass.normalizeYaw(180), 1e-9);
        assertEquals(-180.0, Compass.normalizeYaw(-180), 1e-9);
        assertEquals(-170.0, Compass.normalizeYaw(190), 1e-9);
        assertEquals(170.0, Compass.normalizeYaw(-190), 1e-9);
        assertEquals(0.0, Compass.normalizeYaw(360), 1e-9);
        assertEquals(-180.0, Compass.normalizeYaw(540), 1e-9);
        assertEquals(10.0, Compass.normalizeYaw(-710), 1e-9);
        assertEquals(0.0, Compass.normalizeYaw(Double.NaN), 1e-9);
    }

    @Test
    void headingNormalization() {
        assertEquals(180.0, Compass.heading(0), 1e-9);
        assertEquals(270.0, Compass.heading(90), 1e-9);
        assertEquals(0.0, Compass.heading(180), 1e-9);
        assertEquals(0.0, Compass.heading(-180), 1e-9);
        assertEquals(90.0, Compass.heading(-90), 1e-9);
        assertEquals(359.0, Compass.heading(179), 1e-9);
        assertEquals(1.0, Compass.heading(-179), 1e-9);
        assertEquals(0.0, Compass.heading(540), 1e-9);
        assertEquals(0.0, Compass.normalizeHeading(-1e-20), 1e-9);
        for (double y = -1000; y <= 1000; y += 0.37) {
            double h = Compass.heading(y);
            if (h < 0.0 || h >= 360.0) throw new AssertionError("heading out of range for yaw " + y + ": " + h);
        }
    }

    @Test
    void headingDegreesRoundAndWrap() {
        assertEquals(180, Compass.headingDegrees(0));
        assertEquals(0, Compass.headingDegrees(180));
        assertEquals(0, Compass.headingDegrees(179.6)); // heading 359.6 rounds to 360 -> 0
        assertEquals(23, Compass.headingDegrees(-157.5)); // heading 22.5 -> NE sector, shows 23
        assertEquals(22, Compass.headingDegrees(-157.6));
        assertEquals(90, Compass.headingDegrees(-90));
    }
}
