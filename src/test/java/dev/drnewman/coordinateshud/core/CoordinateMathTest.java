package dev.drnewman.coordinateshud.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.drnewman.coordinateshud.core.HudConfig.CoordinateFormat;
import org.junit.jupiter.api.Test;

class CoordinateMathTest {
    @Test
    void floorsPositive() {
        assertEquals(248L, CoordinateMath.floorBlock(248.9));
        assertEquals(0L, CoordinateMath.floorBlock(0.99));
    }

    @Test
    void floorsNegativeTowardMinusInfinity() {
        assertEquals(-1L, CoordinateMath.floorBlock(-0.5));
        assertEquals(-137L, CoordinateMath.floorBlock(-136.1));
        assertEquals(-136L, CoordinateMath.floorBlock(-136.0));
    }

    @Test
    void blockString() {
        assertEquals("248, 72, -136", CoordinateMath.clipboardString(248.4, 72.0, -135.2, CoordinateFormat.BLOCK));
        assertEquals("-1, -1, -1", CoordinateMath.clipboardString(-0.1, -0.9, -0.5, CoordinateFormat.BLOCK));
    }

    @Test
    void decimalString() {
        assertEquals("248.50, 72.00, -136.25",
                CoordinateMath.clipboardString(248.5, 72.0, -136.25, CoordinateFormat.DECIMAL));
        assertEquals("0.0", CoordinateMath.formatDecimal(-0.04, 1));
        assertEquals("-0.1", CoordinateMath.formatDecimal(-0.06, 1));
        assertEquals("12.3", CoordinateMath.formatDecimal(12.34, 1));
        assertEquals("5", CoordinateMath.formatDecimal(4.6, 0));
    }

    @Test
    void hudCoordinate() {
        assertEquals("-136", CoordinateMath.hudCoordinate(-135.5, CoordinateFormat.BLOCK));
        assertEquals("-135.5", CoordinateMath.hudCoordinate(-135.5, CoordinateFormat.DECIMAL));
    }

    @Test
    void nonFiniteIsNeverFabricated() {
        assertNull(CoordinateMath.clipboardString(Double.NaN, 0, 0, CoordinateFormat.BLOCK));
        assertNull(CoordinateMath.clipboardString(0, Double.POSITIVE_INFINITY, 0, CoordinateFormat.DECIMAL));
        assertEquals("?", CoordinateMath.hudCoordinate(Double.NaN, CoordinateFormat.BLOCK));
    }
}
