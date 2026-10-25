package dev.drnewman.coordinateshud.core;

/**
 * World day number from the world's day-time tick counter (24000 ticks per day).
 * firstDay is 0 or 1: the label shown during the first 24000 ticks of the world.
 */
public final class WorldDay {
    public static final long TICKS_PER_DAY = 24000L;

    private WorldDay() {}

    /** Zero-based day index (same as vanilla "/time query day"). Negative times floor correctly. */
    public static long dayIndex(long dayTimeTicks) {
        return Math.floorDiv(dayTimeTicks, TICKS_PER_DAY);
    }

    public static long displayDay(long dayTimeTicks, int firstDay) {
        return dayIndex(dayTimeTicks) + (firstDay == 0 ? 0 : 1);
    }
}
