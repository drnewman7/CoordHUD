package dev.drnewman.coordinateshud.core;

/** Ready-made accent colours for the settings screen cycle button. */
public enum AccentPreset {
    SKY("Sky", 0x7FC8E8), MINT("Mint", 0x7BE0B0), AMBER("Amber", 0xFFC857),
    ROSE("Rose", 0xFF8FA3), VIOLET("Violet", 0xB79CFF), WHITE("White", 0xFFFFFF);

    public final String label;
    public final int rgb;

    AccentPreset(String label, int rgb) {
        this.label = label;
        this.rgb = rgb;
    }

    /** The preset nearest to a colour (so a hand-edited colour still shows something sensible). */
    public static AccentPreset closest(int rgb) {
        AccentPreset best = SKY;
        long bestDist = Long.MAX_VALUE;
        for (AccentPreset p : values()) {
            long dr = ((p.rgb >> 16) & 255) - ((rgb >> 16) & 255);
            long dg = ((p.rgb >> 8) & 255) - ((rgb >> 8) & 255);
            long db = (p.rgb & 255) - (rgb & 255);
            long d = dr * dr + dg * dg + db * db;
            if (d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }
}
