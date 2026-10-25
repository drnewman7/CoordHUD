package dev.drnewman.coordinateshud.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SettingsLogicTest {
    @Test
    void stepperSnapsAndClamps() {
        assertEquals(50, Stepper.fromFraction(0.0, 50, 300, 5));
        assertEquals(300, Stepper.fromFraction(1.0, 50, 300, 5));
        assertEquals(175, Stepper.fromFraction(0.5, 50, 300, 5));
        assertEquals(100, Stepper.fromFraction(0.2, 50, 300, 5));
        assertEquals(0, Stepper.fromFraction(-3.0, 0, 100, 5));
        assertEquals(100, Stepper.fromFraction(9.0, 0, 100, 5));
        assertEquals(4, Stepper.fromFraction(0.5, 0, 8, 1));
    }

    @Test
    void stepperRoundTrip() {
        for (int v = 50; v <= 300; v += 5) {
            assertEquals(v, Stepper.fromFraction(Stepper.toFraction(v, 50, 300), 50, 300, 5));
        }
        assertEquals(0.0, Stepper.toFraction(5, 5, 5), 1e-9);
    }

    @Test
    void accentPresets() {
        assertEquals(AccentPreset.SKY, AccentPreset.closest(0x7FC8E8));
        assertEquals(AccentPreset.WHITE, AccentPreset.closest(0xFEFEFE));
        assertEquals(AccentPreset.ROSE, AccentPreset.closest(0xFF8FA0));
    }

    @Test
    void changedBumpsRevisionAndSanitizes() {
        HudConfig c = new HudConfig();
        int r = c.revision;
        c.scale = 9999;
        c.changed();
        assertEquals(300, c.scale);
        assertTrue(c.revision > r);
        assertEquals(HudConfig.DeathStyle.DISTANCE, new HudConfig().deathStyle);
    }
}
