package dev.drnewman.coordinateshud.core;

/** Short-lived "Copied!" / "Copy failed" state. Time is passed in (System.nanoTime) so it is testable. */
public final class FeedbackTimer {
    public enum Kind { NONE, SUCCESS, FAILURE }

    public static final long DURATION_NANOS = 1_500_000_000L;

    private Kind kind = Kind.NONE;
    private long untilNanos;

    public void show(Kind kind, long nowNanos) {
        this.kind = kind;
        this.untilNanos = nowNanos + DURATION_NANOS;
    }

    public Kind current(long nowNanos) {
        if (kind != Kind.NONE && nowNanos - untilNanos >= 0) {
            kind = Kind.NONE;
        }
        return kind;
    }
}
