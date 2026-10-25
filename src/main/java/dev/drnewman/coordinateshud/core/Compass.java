package dev.drnewman.coordinateshud.core;

/**
 * Yaw/heading maths.
 *
 * Minecraft yaw convention (Java Edition, entity yRot): 0 = south (+Z), 90 = west (-X),
 * 180 or -180 = north (-Z), -90 (= 270) = east (+X). Yaw grows toward the west, so the compass
 * heading (clockwise from north) is yaw + 180, wrapped into [0, 360). Pitch is never used here.
 */
public final class Compass {
    private static final Direction[] EIGHT = {
        Direction.N, Direction.NE, Direction.E, Direction.SE,
        Direction.S, Direction.SW, Direction.W, Direction.NW};
    private static final Direction[] FOUR = {Direction.N, Direction.E, Direction.S, Direction.W};

    private Compass() {}

    /** Yaw normalised to [-180, 180). Note 180 maps to -180 (both are north). NaN/infinity give 0. */
    public static double normalizeYaw(double yaw) {
        if (!Double.isFinite(yaw)) {
            return 0.0;
        }
        return ((yaw + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
    }

    /** Compass heading in [0, 360): 0 = north, 90 = east, 180 = south, 270 = west. */
    public static double heading(double yaw) {
        return normalizeHeading(yaw + 180.0);
    }

    /** Any heading value wrapped into [0, 360). */
    public static double normalizeHeading(double heading) {
        if (!Double.isFinite(heading)) {
            return 0.0;
        }
        double h = heading % 360.0;
        if (h < 0) {
            h += 360.0;
        }
        return h >= 360.0 ? h - 360.0 : h;
    }

    /** Eight 45-degree sectors, each centred on its heading (N = 337.5..22.5, lower bound inclusive). */
    public static Direction eight(double heading) {
        int sector = (int) Math.floor((normalizeHeading(heading) + 22.5) / 45.0) % 8;
        return EIGHT[sector];
    }

    /** Four 90-degree sectors (N, E, S, W), each centred on its heading. */
    public static Direction cardinal(double heading) {
        int sector = (int) Math.floor((normalizeHeading(heading) + 45.0) / 90.0) % 4;
        return FOUR[sector];
    }

    public static Direction fromYaw(double yaw, boolean cardinalOnly) {
        double h = heading(yaw);
        return cardinalOnly ? cardinal(h) : eight(h);
    }

    /** Heading rounded to a whole degree, always 0..359. */
    public static int headingDegrees(double yaw) {
        return (int) Math.floorMod(Math.round(heading(yaw)), 360L);
    }
}
