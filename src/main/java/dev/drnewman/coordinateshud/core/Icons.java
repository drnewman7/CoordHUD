package dev.drnewman.coordinateshud.core;

import java.util.ArrayList;
import java.util.List;

/** Original 7x7 pixel icons, stored as text bitmaps and turned into a few filled rectangles each. */
public final class Icons {
    public static final int SIZE = 7;

    public enum Icon {
        PIN(0xFFFF8A80, new String[] {
            "..###..", ".#####.", ".##.##.", ".#####.", "..###..", "...#...", "...#..."}),
        COMPASS(0xFF8EC9F0, new String[] {
            "..###..", ".#...#.", "#...#.#", "#..#..#", "#.#...#", ".#...#.", "..###.."}),
        SUN(0xFFFFD166, new String[] {
            "...#...", ".#...#.", "..###..", "#.###.#", "..###..", ".#...#.", "...#..."}),
        PORTAL(0xFFB388FF, new String[] {
            ".#####.", ".#...#.", ".#...#.", ".#...#.", ".#...#.", ".#...#.", ".#####."}),
        SKULL(0xFFD0D0D0, new String[] {
            ".#####.", "#######", "#..#..#", "#######", ".#####.", "..#.#..", "..#.#.."});

        public final int argb;
        private final int[][] rects;

        Icon(int argb, String[] bitmap) {
            this.argb = argb;
            this.rects = toRects(bitmap);
        }

        /** Horizontal runs as {x, y, width, height}, relative to the icon's top-left. */
        public int[][] rects() {
            return rects;
        }
    }

    private Icons() {}

    private static int[][] toRects(String[] bitmap) {
        List<int[]> out = new ArrayList<>();
        for (int y = 0; y < bitmap.length; y++) {
            String row = bitmap[y];
            int x = 0;
            while (x < row.length()) {
                if (row.charAt(x) == '#') {
                    int start = x;
                    while (x < row.length() && row.charAt(x) == '#') x++;
                    out.add(new int[] {start, y, x - start, 1});
                } else {
                    x++;
                }
            }
        }
        return out.toArray(new int[0][]);
    }
}
