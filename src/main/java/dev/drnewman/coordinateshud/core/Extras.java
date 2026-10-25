package dev.drnewman.coordinateshud.core;

/** Text helpers for the Nether/Overworld conversion row and the last-death row. Pure Java. */
public final class Extras {
    private Extras() {}

    /** Label for the conversion row, or null when no conversion applies (End, custom dimensions). */
    public static String conversionLabel(DimKind current) {
        return switch (current) {
            case OVERWORLD -> "Nether";
            case NETHER -> "Overworld";
            default -> null;
        };
    }

    /** "x, z" in the other dimension (Overworld to Nether divides by 8, Nether to Overworld multiplies by 8). */
    public static String conversionText(DimKind current, double x, double z) {
        if (!Double.isFinite(x) || !Double.isFinite(z)) {
            return null;
        }
        return switch (current) {
            case OVERWORLD -> pair(CoordinateMath.floorBlock(x / 8.0), CoordinateMath.floorBlock(z / 8.0));
            case NETHER -> pair(CoordinateMath.floorBlock(x * 8.0), CoordinateMath.floorBlock(z * 8.0));
            default -> null;
        };
    }

    /** Values that change whenever the conversion text would change (used as a cheap cache key). */
    public static long conversionKey(DimKind current, double v) {
        if (!Double.isFinite(v)) return 0;
        return switch (current) {
            case OVERWORLD -> CoordinateMath.floorBlock(v / 8.0);
            case NETHER -> CoordinateMath.floorBlock(v * 8.0);
            default -> 0;
        };
    }

    /**
     * Death row text. In another dimension distance is meaningless, so coordinates plus the dimension name are
     * shown whatever the style. In the same dimension: "48 blocks NW[, 12 up]", coordinates, or both.
     */
    public static String deathText(HudConfig.DeathStyle style, DeathPointer p, long x, long y, long z,
                                   DimKind deathDim, DimKind currentDim) {
        String coords = x + ", " + y + ", " + z;
        if (deathDim != currentDim) {
            return coords + " (" + dimName(deathDim) + ")";
        }
        return switch (style) {
            case COORDS -> coords;
            case DISTANCE -> pointerText(p);
            case BOTH -> pointerText(p) + " (" + coords + ")";
        };
    }

    public static String pointerText(DeathPointer p) {
        if (p.here) {
            return "Here";
        }
        StringBuilder sb = new StringBuilder(32);
        sb.append(p.distance).append(p.distance == 1 ? " block " : " blocks ").append(p.dir.abbreviation());
        if (p.dyShown > 0) {
            sb.append(", ").append(p.dyShown).append(" up");
        } else if (p.dyShown < 0) {
            sb.append(", ").append(-p.dyShown).append(" down");
        }
        return sb.toString();
    }

    public static String dimName(DimKind k) {
        return switch (k) {
            case OVERWORLD -> "Overworld";
            case NETHER -> "Nether";
            case END -> "End";
            case OTHER -> "Other";
        };
    }

    private static String pair(long a, long b) {
        return a + ", " + b;
    }
}
