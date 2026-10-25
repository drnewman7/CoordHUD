package dev.drnewman.coordinateshud.core;

/** Compass directions. */
public enum Direction {
    N("N", "North"), NE("NE", "Northeast"), E("E", "East"), SE("SE", "Southeast"),
    S("S", "South"), SW("SW", "Southwest"), W("W", "West"), NW("NW", "Northwest");

    private final String abbreviation;
    private final String fullName;

    Direction(String abbreviation, String fullName) {
        this.abbreviation = abbreviation;
        this.fullName = fullName;
    }

    public String abbreviation() {
        return abbreviation;
    }

    public String fullName() {
        return fullName;
    }
}
