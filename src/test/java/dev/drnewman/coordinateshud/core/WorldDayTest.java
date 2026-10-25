package dev.drnewman.coordinateshud.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WorldDayTest {
    @Test
    void firstDayDefaultsToOne() {
        assertEquals(1L, WorldDay.displayDay(0, 1));
        assertEquals(1L, WorldDay.displayDay(1000, 1));
        assertEquals(1L, WorldDay.displayDay(23999, 1));
        assertEquals(2L, WorldDay.displayDay(24000, 1));
    }

    @Test
    void zeroBased() {
        assertEquals(0L, WorldDay.displayDay(23999, 0));
        assertEquals(1L, WorldDay.displayDay(24000, 0));
        assertEquals(24L, WorldDay.displayDay(24L * 24000 + 5, 0));
    }

    @Test
    void timeOfDayIsNotTheDay() {
        // Same day, different times of day.
        assertEquals(WorldDay.dayIndex(24000L * 7 + 1), WorldDay.dayIndex(24000L * 7 + 23999));
    }

    @Test
    void negativeTimeFloors() {
        assertEquals(-1L, WorldDay.dayIndex(-1));
        assertEquals(-1L, WorldDay.dayIndex(-24000));
        assertEquals(-2L, WorldDay.dayIndex(-24001));
    }
}
