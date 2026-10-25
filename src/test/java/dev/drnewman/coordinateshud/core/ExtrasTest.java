package dev.drnewman.coordinateshud.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ExtrasTest {
    @Test
    void overworldToNetherDividesAndFloors() {
        assertEquals("Nether", Extras.conversionLabel(DimKind.OVERWORLD));
        assertEquals("32, -2", Extras.conversionText(DimKind.OVERWORLD, 256.9, -9.0));  // -9/8 floors to -2
        assertEquals("-1, 0", Extras.conversionText(DimKind.OVERWORLD, -0.5, 7.9));
    }

    @Test
    void netherToOverworldMultiplies() {
        assertEquals("Overworld", Extras.conversionLabel(DimKind.NETHER));
        assertEquals("-2048, 704", Extras.conversionText(DimKind.NETHER, -256.0, 88.0));
        assertEquals("-4, 0", Extras.conversionText(DimKind.NETHER, -0.5, 0.1)); // -0.5*8 = -4
    }

    @Test
    void noConversionElsewhere() {
        assertNull(Extras.conversionLabel(DimKind.END));
        assertNull(Extras.conversionText(DimKind.END, 1, 1));
        assertNull(Extras.conversionLabel(DimKind.OTHER));
        assertNull(Extras.conversionText(DimKind.OVERWORLD, Double.NaN, 0));
    }

    @Test
    void conversionKeyTracksVisibleChange() {
        assertEquals(Extras.conversionKey(DimKind.NETHER, 1.0), 8L);
        assertEquals(Extras.conversionKey(DimKind.NETHER, 1.125), 9L);
        assertEquals(Extras.conversionKey(DimKind.OVERWORLD, 15.9), 1L);
        assertEquals(Extras.conversionKey(DimKind.OVERWORLD, 16.0), 2L);
    }

    @Test
    void deathText() {
        DeathPointer p = new DeathPointer();
        p.update(0, 64, 0, 30, 64, -40, false); // 30 east, 40 north -> 50 blocks, NE
        assertEquals("50 blocks NE", Extras.deathText(HudConfig.DeathStyle.DISTANCE, p, 30, 64, -40, DimKind.OVERWORLD, DimKind.OVERWORLD));
        assertEquals("30, 64, -40", Extras.deathText(HudConfig.DeathStyle.COORDS, p, 30, 64, -40, DimKind.OVERWORLD, DimKind.OVERWORLD));
        assertEquals("50 blocks NE (30, 64, -40)", Extras.deathText(HudConfig.DeathStyle.BOTH, p, 30, 64, -40, DimKind.OVERWORLD, DimKind.OVERWORLD));
        // other dimension: always coordinates + dimension name
        assertEquals("30, 64, -40 (Nether)", Extras.deathText(HudConfig.DeathStyle.DISTANCE, p, 30, 64, -40, DimKind.NETHER, DimKind.OVERWORLD));
    }
}
