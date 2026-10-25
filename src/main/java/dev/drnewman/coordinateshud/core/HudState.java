package dev.drnewman.coordinateshud.core;

/** Reusable snapshot of the game values the HUD needs (filled by the client bridge, no allocation per frame). */
public final class HudState {
    public boolean available;
    public double x;
    public double y;
    public double z;
    public double yaw;
    public boolean hasDayTime;
    public long dayTime;
    public DimKind dim = DimKind.OTHER;
    public boolean hasDeath;
    public int deathX;
    public int deathY;
    public int deathZ;
    public DimKind deathDim = DimKind.OTHER;
}
