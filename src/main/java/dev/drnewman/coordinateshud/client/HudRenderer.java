package dev.drnewman.coordinateshud.client;

import dev.drnewman.coordinateshud.core.ClickLatch;
import dev.drnewman.coordinateshud.core.Compass;
import dev.drnewman.coordinateshud.core.CoordinateMath;
import dev.drnewman.coordinateshud.core.DeathPointer;
import dev.drnewman.coordinateshud.core.DimKind;
import dev.drnewman.coordinateshud.core.Direction;
import dev.drnewman.coordinateshud.core.Extras;
import dev.drnewman.coordinateshud.core.FeedbackTimer;
import dev.drnewman.coordinateshud.core.GlassShape;
import dev.drnewman.coordinateshud.core.HudConfig;
import dev.drnewman.coordinateshud.core.HudLayout;
import dev.drnewman.coordinateshud.core.HudState;
import dev.drnewman.coordinateshud.core.Placement;
import dev.drnewman.coordinateshud.core.WorldDay;
import java.util.List;
import java.util.function.ToIntFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;

/**
 * Draws the compact panel and handles the copy button. All maths lives in the core package.
 *
 * Performance: the layout and the glass shape are rebuilt only when a displayed value or the config
 * changes; a normal frame allocates nothing.
 *
 * Copy button: the cursor is only free while a screen is open, so the button is clickable while the chat
 * screen is open (press T). During normal gameplay use the keybind. The click is detected by polling
 * the mouse state (no event hooks, no mixins); the click is not consumed.
 */
final class HudRenderer {
    private static final int COLOR_LABEL = 0xFFAAAAAA;
    private static final int COLOR_VALUE = 0xFFFFFFFF;
    private static final int COLOR_X = 0xFFFF7B7B;
    private static final int COLOR_Y = 0xFF7BE27B;
    private static final int COLOR_Z = 0xFF72A8FF;
    private static final int ICON_IDLE = 0xFFC8C8C8;
    private static final int FAIL = 0xFFFF5555;
    private static final int HOVER_BOX = 0x40FFFFFF;

    private final McDraw draw;
    private final ToIntFunction<String> measure;
    private final HudState state = new HudState();
    private final ClickLatch latch = new ClickLatch();
    final FeedbackTimer feedback = new FeedbackTimer();

    private HudLayout layout = HudLayout.empty();
    private GlassShape shape = GlassShape.flat(0, 0, 0);
    private HudConfig layoutConfig;
    private boolean keyValid;
    private long kx;
    private long ky;
    private long kz;
    private long kDay;
    private long kConvX;
    private long kConvZ;
    private int kHeading;
    private Direction kDir;
    private DimKind kDim;
    private boolean kHasDay;
    private boolean kHasDeath;
    private int kDeathX;
    private int kDeathY;
    private int kDeathZ;
    private DimKind kDeathDim;
    private int kRev;
    private int kPDist;
    private Direction kPDir;
    private int kPDy;
    private final DeathPointer pointer = new DeathPointer();

    private GlassShape pillShape;
    private int pillW;
    private int pillH;
    private HudConfig pillConfig;
    private int pillRev;

    HudRenderer(McDraw draw) {
        this.draw = draw;
        this.measure = draw::width;
    }

    void render(Minecraft mc, HudConfig cfg) {
        boolean down = mc.mouseHandler.isLeftPressed();
        boolean edge = latch.pressedEdge(down);

        if (!cfg.enabled) {
            return;
        }
        if (!McBridge.read(mc, state)) {
            return; // no world/player: show nothing rather than stale or invented values
        }
        Screen screen = mc.gui.screen();
        boolean chatOpen = screen instanceof ChatScreen;
        if (cfg.hideWhenScreenOpen && screen != null && !chatOpen) {
            return;
        }
        if (cfg.hideWithDebugScreen && DebugScreenProbe.isOpen(mc)) {
            return;
        }

        refreshLayout(cfg);
        HudLayout l = layout;
        if (l.isEmpty()) {
            return;
        }

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        boolean scaled = cfg.scale != 100;
        float s = cfg.scale / 100f;
        // Placement happens in "scaled space": the screen is sw/s wide as far as the panel is concerned.
        int vw = scaled ? Math.round(sw / s) : sw;
        int vh = scaled ? Math.round(sh / s) : sh;
        int ox = scaled ? Math.round(cfg.offsetX / s) : cfg.offsetX;
        int oy = scaled ? Math.round(cfg.offsetY / s) : cfg.offsetY;
        int px = Placement.x(cfg.anchor, l.width, vw, ox);
        int py = Placement.y(cfg.anchor, l.height, vh, oy);

        if (scaled) {
            draw.pushScale(s);
        }
        try {
            paint(mc, cfg, l, px, py, vh, sw, sh, s, chatOpen, edge);
        } finally {
            if (scaled) {
                draw.pop();
            }
        }
    }

    private void paint(Minecraft mc, HudConfig cfg, HudLayout l, int px, int py, int viewHeight,
                       int sw, int sh, float s, boolean chatOpen, boolean edge) {
        GlassShape bg = shape;
        for (int i = 0; i < bg.count; i++) {
            draw.fill(px + bg.x0[i], py + bg.y0[i], px + bg.x1[i], py + bg.y1[i], bg.argb[i]);
        }

        List<HudLayout.IconRun> icons = l.icons;
        for (int i = 0; i < icons.size(); i++) {
            HudLayout.IconRun ic = icons.get(i);
            int[][] rects = ic.icon().rects();
            for (int[] r : rects) {
                draw.fill(px + ic.x() + r[0], py + ic.y() + r[1],
                        px + ic.x() + r[0] + r[2], py + ic.y() + r[1] + r[3], ic.icon().argb);
            }
        }

        int accent = cfg.accentArgb();
        List<HudLayout.TextRun> runs = l.runs;
        for (int i = 0; i < runs.size(); i++) {
            HudLayout.TextRun r = runs.get(i);
            int color = switch (r.role()) {
                case LABEL -> COLOR_LABEL;
                case VALUE -> COLOR_VALUE;
                case ACCENT -> accent;
                case AXIS_X -> cfg.coloredAxes ? COLOR_X : COLOR_LABEL;
                case AXIS_Y -> cfg.coloredAxes ? COLOR_Y : COLOR_LABEL;
                case AXIS_Z -> cfg.coloredAxes ? COLOR_Z : COLOR_LABEL;
            };
            draw.text(r.text(), px + r.x(), py + r.y(), color);
        }

        long now = System.nanoTime();
        FeedbackTimer.Kind kind = feedback.current(now);

        HudLayout.Rect b = l.button;
        if (b != null) {
            int bx = px + b.x();
            int by = py + b.y();
            boolean hover = false;
            if (chatOpen) {
                // Raw cursor position is in window pixels; convert to GUI-scaled, then to scaled-space coordinates.
                double mx = mc.mouseHandler.xpos() * sw / mc.getWindow().getScreenWidth() / s;
                double my = mc.mouseHandler.ypos() * sh / mc.getWindow().getScreenHeight() / s;
                hover = b.contains((int) Math.floor(mx) - px, (int) Math.floor(my) - py);
                if (hover && edge) {
                    CopyAction.perform(mc, cfg, feedback);
                    kind = feedback.current(System.nanoTime());
                }
            }
            if (hover) {
                draw.fill(bx - 1, by - 1, bx + b.width() + 1, by + b.height() + 1, HOVER_BOX);
            }
            if (kind == FeedbackTimer.Kind.SUCCESS) {
                drawCheck(bx, by, accent);
            } else if (kind == FeedbackTimer.Kind.FAILURE) {
                drawCross(bx, by, FAIL);
            } else {
                drawCopyIcon(bx, by, hover ? accent : ICON_IDLE);
            }
        }

        if (kind != FeedbackTimer.Kind.NONE) {
            drawPill(cfg, viewHeight, px, py, l, kind == FeedbackTimer.Kind.SUCCESS ? "Copied!" : "Copy failed",
                    kind == FeedbackTimer.Kind.SUCCESS ? accent : FAIL);
        }
    }

    private void refreshLayout(HudConfig cfg) {
        boolean dec = cfg.coordinateFormat == HudConfig.CoordinateFormat.DECIMAL;
        long x = dec ? Math.round(state.x * 10.0) : CoordinateMath.floorBlock(state.x);
        long y = dec ? Math.round(state.y * 10.0) : CoordinateMath.floorBlock(state.y);
        long z = dec ? Math.round(state.z * 10.0) : CoordinateMath.floorBlock(state.z);
        int heading = Compass.headingDegrees(state.yaw);
        Direction dir = Compass.fromYaw(state.yaw, cfg.directionStyle == HudConfig.DirectionStyle.CARDINAL);
        long day = state.hasDayTime ? WorldDay.displayDay(state.dayTime, cfg.firstDay) : 0L;
        boolean sameDimDeath = state.hasDeath && state.deathDim == state.dim;
        if (sameDimDeath) {
            pointer.update(state.x, state.y, state.z, state.deathX, state.deathY, state.deathZ,
                    cfg.directionStyle == HudConfig.DirectionStyle.CARDINAL);
        }
        int pDist = sameDimDeath ? pointer.distance : 0;
        Direction pDir = sameDimDeath ? pointer.dir : null;
        int pDy = sameDimDeath ? pointer.dyShown : 0;
        long convX = Extras.conversionKey(state.dim, state.x);
        long convZ = Extras.conversionKey(state.dim, state.z);

        if (keyValid && layoutConfig == cfg && x == kx && y == ky && z == kz && heading == kHeading
                && dir == kDir && day == kDay && state.hasDayTime == kHasDay
                && state.dim == kDim && convX == kConvX && convZ == kConvZ
                && state.hasDeath == kHasDeath && state.deathX == kDeathX && state.deathY == kDeathY
                && state.deathZ == kDeathZ && state.deathDim == kDeathDim
                && cfg.revision == kRev && pDist == kPDist && pDir == kPDir && pDy == kPDy) {
            return;
        }
        keyValid = true;
        layoutConfig = cfg;
        kx = x;
        ky = y;
        kz = z;
        kHeading = heading;
        kDir = dir;
        kDay = day;
        kHasDay = state.hasDayTime;
        kDim = state.dim;
        kConvX = convX;
        kConvZ = convZ;
        kHasDeath = state.hasDeath;
        kDeathX = state.deathX;
        kDeathY = state.deathY;
        kDeathZ = state.deathZ;
        kDeathDim = state.deathDim;
        kRev = cfg.revision;
        kPDist = pDist;
        kPDir = pDir;
        kPDy = pDy;

        HudLayout.Content content = new HudLayout.Content(
                CoordinateMath.hudCoordinate(state.x, cfg.coordinateFormat),
                CoordinateMath.hudCoordinate(state.y, cfg.coordinateFormat),
                CoordinateMath.hudCoordinate(state.z, cfg.coordinateFormat),
                dir.abbreviation(),
                heading + "\u00B0",
                state.hasDayTime ? Long.toString(day) : null,
                Extras.conversionLabel(state.dim),
                Extras.conversionText(state.dim, state.x, state.z),
                state.hasDeath
                        ? Extras.deathText(cfg.deathStyle, pointer, state.deathX, state.deathY, state.deathZ, state.deathDim, state.dim)
                        : null);
        layout = HudLayout.build(cfg, content, measure, draw.lineHeight());
        shape = makeShape(cfg, layout.width, layout.height, cfg.cornerRadius);
    }

    private static GlassShape makeShape(HudConfig cfg, int w, int h, int radius) {
        return switch (cfg.style) {
            case GLASS -> GlassShape.glass(w, h, radius, cfg.backgroundOpacity, cfg.accentColor);
            case MINECRAFT -> GlassShape.minecraft(w, h, cfg.backgroundOpacity);
            case CLASSIC -> GlassShape.flat(w, h, cfg.backgroundOpacity);
        };
    }

    // --- tiny icons made of filled rectangles (no textures, no emoji glyphs) ---

    /** Two overlapping squares, 9x9. */
    private void drawCopyIcon(int x, int y, int c) {
        draw.fill(x, y, x + 6, y + 1, c);          // back square: top
        draw.fill(x, y, x + 1, y + 6, c);          //              left
        draw.fill(x + 5, y, x + 6, y + 3, c);      //              right (upper part)
        draw.fill(x, y + 5, x + 3, y + 6, c);      //              bottom (left part)
        draw.fill(x + 3, y + 3, x + 9, y + 4, c);  // front square: top
        draw.fill(x + 3, y + 8, x + 9, y + 9, c);  //               bottom
        draw.fill(x + 3, y + 3, x + 4, y + 9, c);  //               left
        draw.fill(x + 8, y + 3, x + 9, y + 9, c);  //               right
    }

    private void drawCheck(int x, int y, int c) {
        int[][] p = {{1, 4}, {2, 5}, {3, 6}, {4, 5}, {5, 4}, {6, 3}, {7, 2}};
        for (int[] q : p) {
            draw.fill(x + q[0], y + q[1], x + q[0] + 1, y + q[1] + 1, c);
        }
    }

    private void drawCross(int x, int y, int c) {
        for (int i = 1; i <= 7; i++) {
            draw.fill(x + i, y + i, x + i + 1, y + i + 1, c);
            draw.fill(x + 8 - i, y + i, x + 9 - i, y + i + 1, c);
        }
    }

    /** Small label just outside the panel so the coordinate row is never cramped. */
    private void drawPill(HudConfig cfg, int viewHeight, int px, int py, HudLayout l, String text, int color) {
        int w = draw.width(text) + 8;
        int h = draw.lineHeight() + 4;
        if (pillShape == null || pillConfig != cfg || pillRev != cfg.revision || pillW != w || pillH != h) {
            pillShape = makeShape(cfg, w, h, Math.min(cfg.cornerRadius, 4));
            pillConfig = cfg;
            pillRev = cfg.revision;
            pillW = w;
            pillH = h;
        }
        int y = py + l.height + 2;
        if (y + h > viewHeight) {
            y = py - h - 2; // no room below: put it above
        }
        int x = px + l.width - w;
        GlassShape p = pillShape;
        for (int i = 0; i < p.count; i++) {
            draw.fill(x + p.x0[i], y + p.y0[i], x + p.x1[i], y + p.y1[i], p.argb[i]);
        }
        draw.text(text, x + 4, y + 2, color);
    }
}
