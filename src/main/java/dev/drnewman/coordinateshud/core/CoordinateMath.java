package dev.drnewman.coordinateshud.core;

import dev.drnewman.coordinateshud.core.HudConfig.CoordinateFormat;

/** Pure coordinate helpers. No Minecraft classes. */
public final class CoordinateMath {
    public static final int HUD_DECIMALS = 1;
    public static final int COPY_DECIMALS = 2;

    private CoordinateMath() {}

    /** Mathematical floor (so -0.5 becomes -1, never 0). Non-finite input gives 0; callers check isFinite first. */
    public static long floorBlock(double value) {
        return (long) Math.floor(value);
    }

    /** Fixed-point formatting without locale surprises and without "-0.0". Returns "?" for non-finite input. */
    public static String formatDecimal(double value, int places) {
        if (!Double.isFinite(value) || places < 0 || places > 6) {
            return "?";
        }
        long scale = 1;
        for (int i = 0; i < places; i++) {
            scale *= 10;
        }
        long scaled = Math.round(value * scale);
        boolean negative = scaled < 0;
        long abs = Math.abs(scaled);
        if (places == 0) {
            return (negative ? "-" : "") + abs;
        }
        String frac = Long.toString(abs % scale);
        StringBuilder sb = new StringBuilder(24);
        if (negative) {
            sb.append('-');
        }
        sb.append(abs / scale).append('.');
        for (int i = frac.length(); i < places; i++) {
            sb.append('0');
        }
        return sb.append(frac).toString();
    }

    /** One coordinate as shown on the HUD. */
    public static String hudCoordinate(double value, CoordinateFormat format) {
        if (!Double.isFinite(value)) {
            return "?";
        }
        return format == CoordinateFormat.DECIMAL
                ? formatDecimal(value, HUD_DECIMALS)
                : Long.toString(floorBlock(value));
    }

    /** Clipboard text such as "248, 72, -136". Returns null if any value is not finite (never copy garbage). */
    public static String clipboardString(double x, double y, double z, CoordinateFormat format) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            return null;
        }
        if (format == CoordinateFormat.DECIMAL) {
            return formatDecimal(x, COPY_DECIMALS) + ", " + formatDecimal(y, COPY_DECIMALS) + ", "
                    + formatDecimal(z, COPY_DECIMALS);
        }
        return floorBlock(x) + ", " + floorBlock(y) + ", " + floorBlock(z);
    }
}
