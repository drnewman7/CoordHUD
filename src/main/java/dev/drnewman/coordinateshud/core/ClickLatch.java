package dev.drnewman.coordinateshud.core;

/** Turns a held-button signal into one event per press, so one click never copies repeatedly. */
public final class ClickLatch {
    private boolean wasDown;

    /** Call every frame with the raw button state; true only on the frame the button goes down. */
    public boolean pressedEdge(boolean down) {
        boolean edge = down && !wasDown;
        wasDown = down;
        return edge;
    }
}
