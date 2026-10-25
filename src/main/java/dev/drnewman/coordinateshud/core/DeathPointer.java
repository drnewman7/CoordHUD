package dev.drnewman.coordinateshud.core;

/**
 * Distance and compass direction from the player to the last death point (same dimension only).
 * Reusable: update() writes into this object, so there is no allocation per frame.
 * Minecraft axes: +X = east, +Z = south, so north is -Z.
 */
public final class DeathPointer {
    /** Vertical differences smaller than this are not mentioned. */
    public static final int VERTICAL_THRESHOLD = 3;

    public int distance;     // horizontal blocks, rounded
    public Direction dir = Direction.N;
    public int dyShown;      // death height minus player height, or 0 when below the threshold
    public boolean here;

    public void update(double px, double py, double pz, int deathX, int deathY, int deathZ, boolean cardinalOnly) {
        double dx = deathX + 0.5 - px;
        double dz = deathZ + 0.5 - pz;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        distance = (int) Math.round(horizontal);
        int dy = (int) Math.round(deathY - py);
        dyShown = Math.abs(dy) >= VERTICAL_THRESHOLD ? dy : 0;
        here = horizontal < 0.9 && dyShown == 0; // anywhere inside the death block
        double heading = Compass.normalizeHeading(Math.toDegrees(Math.atan2(dx, -dz)));
        dir = cardinalOnly ? Compass.cardinal(heading) : Compass.eight(heading);
    }
}
