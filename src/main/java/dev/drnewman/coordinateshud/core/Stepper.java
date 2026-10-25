package dev.drnewman.coordinateshud.core;

/** Maps a 0..1 slider fraction to a stepped integer range and back. */
public final class Stepper {
    private Stepper() {}

    public static int fromFraction(double fraction, int min, int max, int step) {
        double f = Math.max(0.0, Math.min(1.0, fraction));
        long steps = Math.round((f * (max - min)) / step);
        int v = min + (int) steps * step;
        return Math.max(min, Math.min(max, v));
    }

    public static double toFraction(int value, int min, int max) {
        if (max <= min) return 0.0;
        return Math.max(0.0, Math.min(1.0, (double) (value - min) / (max - min)));
    }
}
