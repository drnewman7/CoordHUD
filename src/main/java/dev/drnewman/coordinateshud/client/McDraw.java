package dev.drnewman.coordinateshud.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * THE ONLY FILE THAT TOUCHES GUI DRAWING CALLS (verified to compile against Minecraft 26.3).
 * Colours are ARGB; text with alpha 0 is invisible, so always use 0xFF...... for text.
 */
final class McDraw {
    private GuiGraphicsExtractor g;
    private Font font;

    void bind(GuiGraphicsExtractor graphics, Font font) {
        this.g = graphics;
        this.font = font;
    }

    void fill(int x0, int y0, int x1, int y1, int argb) {
        g.fill(x0, y0, x1, y1, argb);
    }

    void text(String s, int x, int y, int argb) {
        g.text(font, s, x, y, argb);
    }

    /** Everything drawn until pop() is scaled by s around the screen origin. */
    void pushScale(float s) {
        g.pose().pushMatrix();
        g.pose().scale(s, s);
    }

    void pop() {
        g.pose().popMatrix();
    }

    int width(String s) {
        return font.width(s);
    }

    int lineHeight() {
        return font.lineHeight;
    }
}
