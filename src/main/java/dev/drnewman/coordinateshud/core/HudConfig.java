package dev.drnewman.coordinateshud.core;

import java.util.Locale;
import java.util.Properties;

/**
 * User settings. Pure Java (java.util.Properties) so it can be unit-tested and needs no library.
 * Invalid or missing values fall back to defaults one key at a time, so valid preferences survive.
 *
 */
public final class HudConfig {
    public static final int FORMAT_VERSION = 1;
    public static final int MAX_OFFSET = 1000;

    public enum Anchor {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;

        public boolean isRight() {
            return this == TOP_RIGHT || this == BOTTOM_RIGHT;
        }

        public boolean isBottom() {
            return this == BOTTOM_LEFT || this == BOTTOM_RIGHT;
        }
    }

    public enum CoordinateFormat { BLOCK, DECIMAL }

    /** CARDINAL = N/E/S/W only; EIGHT_WAY = N/NE/E/SE/S/SW/W/NW. Degrees are controlled by showHeading. */
    public enum DirectionStyle { CARDINAL, EIGHT_WAY }

    /** GLASS = rounded translucent glass; MINECRAFT = vanilla tooltip look (notched corners, purple frame); CLASSIC = flat rectangle. */
    public enum Style { GLASS, MINECRAFT, CLASSIC }

    /** How the death row reads: DISTANCE ("48 blocks NW"), COORDS ("12, 64, -30") or BOTH. */
    public enum DeathStyle { DISTANCE, COORDS, BOTH }

    public boolean enabled = true;
    public boolean showCoordinates = true;
    public boolean showDirection = true;
    public boolean showHeading = true;
    public boolean showDay = true;
    public boolean showCopyIcon = true;
    public boolean hideWhenScreenOpen = false;
    public Anchor anchor = Anchor.TOP_LEFT;
    public int offsetX = 4;
    public int offsetY = 4;
    public int backgroundOpacity = 60;
    public int accentColor = 0x7FC8E8;
    public CoordinateFormat coordinateFormat = CoordinateFormat.BLOCK;
    public DirectionStyle directionStyle = DirectionStyle.EIGHT_WAY;
    public int firstDay = 1;
    public Style style = Style.GLASS;
    public int cornerRadius = 4;
    public boolean showIcons = true;
    public boolean coloredAxes = true;
    public int scale = 100;
    public boolean hideWithDebugScreen = true;
    public boolean showDimensionConversion = true;
    public boolean showLastDeath = true;
    public DeathStyle deathStyle = DeathStyle.DISTANCE;

    /** Bumped by changed(); lets the renderer notice in-place edits made by the settings screen. */
    public int revision;

    /** Call after editing fields in place (e.g. from the settings screen). */
    public void changed() {
        sanitize();
        revision++;
    }

    /** Clamp everything into a valid range. Returns this for chaining. */
    public HudConfig sanitize() {
        if (anchor == null) anchor = Anchor.TOP_LEFT;
        if (coordinateFormat == null) coordinateFormat = CoordinateFormat.BLOCK;
        if (directionStyle == null) directionStyle = DirectionStyle.EIGHT_WAY;
        offsetX = Math.max(0, Math.min(MAX_OFFSET, offsetX));
        offsetY = Math.max(0, Math.min(MAX_OFFSET, offsetY));
        backgroundOpacity = Math.max(0, Math.min(100, backgroundOpacity));
        accentColor &= 0xFFFFFF;
        firstDay = firstDay == 0 ? 0 : 1;
        if (style == null) style = Style.GLASS;
        if (deathStyle == null) deathStyle = DeathStyle.DISTANCE;
        cornerRadius = Math.max(0, Math.min(8, cornerRadius));
        scale = Math.max(50, Math.min(300, scale));
        return this;
    }

    /** Background colour as ARGB (black with the configured opacity). */
    public int backgroundArgb() {
        return Math.round(backgroundOpacity * 255f / 100f) << 24;
    }

    public int accentArgb() {
        return 0xFF000000 | accentColor;
    }

    public static HudConfig fromProperties(Properties p) {
        HudConfig d = new HudConfig();
        HudConfig c = new HudConfig();
        c.enabled = bool(p, "enabled", d.enabled);
        c.showCoordinates = bool(p, "showCoordinates", d.showCoordinates);
        c.showDirection = bool(p, "showDirection", d.showDirection);
        c.showHeading = bool(p, "showHeading", d.showHeading);
        c.showDay = bool(p, "showDay", d.showDay);
        c.showCopyIcon = bool(p, "showCopyIcon", d.showCopyIcon);
        c.hideWhenScreenOpen = bool(p, "hideWhenScreenOpen", d.hideWhenScreenOpen);
        c.anchor = enumValue(p, "anchor", Anchor.class, d.anchor);
        c.offsetX = integer(p, "offsetX", d.offsetX);
        c.offsetY = integer(p, "offsetY", d.offsetY);
        c.backgroundOpacity = integer(p, "backgroundOpacity", d.backgroundOpacity);
        c.accentColor = color(p, "accentColor", d.accentColor);
        c.coordinateFormat = enumValue(p, "coordinateFormat", CoordinateFormat.class, d.coordinateFormat);
        c.directionStyle = enumValue(p, "directionStyle", DirectionStyle.class, d.directionStyle);
        c.firstDay = integer(p, "firstDay", d.firstDay);
        c.style = enumValue(p, "style", Style.class, d.style);
        c.cornerRadius = integer(p, "cornerRadius", d.cornerRadius);
        c.showIcons = bool(p, "showIcons", d.showIcons);
        c.coloredAxes = bool(p, "coloredAxes", d.coloredAxes);
        c.scale = integer(p, "scale", d.scale);
        c.hideWithDebugScreen = bool(p, "hideWithDebugScreen", d.hideWithDebugScreen);
        c.showDimensionConversion = bool(p, "showDimensionConversion", d.showDimensionConversion);
        c.showLastDeath = bool(p, "showLastDeath", d.showLastDeath);
        c.deathStyle = enumValue(p, "deathStyle", DeathStyle.class, d.deathStyle);
        return c.sanitize();
    }

    /** Full, commented file text. Deterministic so unchanged configs are not rewritten on every load. */
    public String toFileText() {
        return "# CoordHUD - configuration\n"
                + "# Dev : Dr. Newman\n"
                + "# Edit while the game runs: changes are picked up within a few seconds.\n"
                + "# Invalid values are replaced by defaults individually; other settings are kept.\n"
                + "configVersion=" + FORMAT_VERSION + "\n\n"
                + "# Master switch for the whole HUD (true/false)\n"
                + "enabled=" + enabled + "\n"
                + "# Individual rows / elements (true/false)\n"
                + "showCoordinates=" + showCoordinates + "\n"
                + "showDirection=" + showDirection + "\n"
                + "showHeading=" + showHeading + "\n"
                + "showDay=" + showDay + "\n"
                + "showCopyIcon=" + showCopyIcon + "\n"
                + "# Hide the HUD while a screen other than chat is open (true/false)\n"
                + "hideWhenScreenOpen=" + hideWhenScreenOpen + "\n\n"
                + "# TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT\n"
                + "anchor=" + anchor + "\n"
                + "# Distance from the anchored corner in GUI pixels (0-" + MAX_OFFSET + ")\n"
                + "offsetX=" + offsetX + "\n"
                + "offsetY=" + offsetY + "\n"
                + "# Panel background opacity in percent (0-100)\n"
                + "backgroundOpacity=" + backgroundOpacity + "\n"
                + "# Accent colour as RRGGBB hex (used for the direction and the copy icon highlight)\n"
                + "accentColor=" + String.format(Locale.ROOT, "%06X", accentColor) + "\n\n"
                + "# BLOCK (whole blocks, floored) or DECIMAL\n"
                + "coordinateFormat=" + coordinateFormat + "\n"
                + "# CARDINAL (N/E/S/W) or EIGHT_WAY (N/NE/E/SE/S/SW/W/NW). EIGHT_WAY + showHeading=true = direction plus degrees\n"
                + "directionStyle=" + directionStyle + "\n"
                + "# Label of the first in-game day: 0 (like /time query day) or 1\n"
                + "firstDay=" + firstDay + "\n\n"
                + "# GLASS (rounded translucent), MINECRAFT (vanilla tooltip look) or CLASSIC (flat rectangle)\n"
                + "style=" + style + "\n"
                + "# Corner radius in GUI pixels, 0-8 (GLASS only)\n"
                + "cornerRadius=" + cornerRadius + "\n"
                + "# Tiny row icons and coloured X/Y/Z letters (true/false)\n"
                + "showIcons=" + showIcons + "\n"
                + "coloredAxes=" + coloredAxes + "\n"
                + "# HUD size in percent, 50-300\n"
                + "scale=" + scale + "\n"
                + "# Hide while the F3 debug screen is open (true/false)\n"
                + "hideWithDebugScreen=" + hideWithDebugScreen + "\n"
                + "# Extra rows: Nether<->Overworld coordinates, and your last death location (true/false)\n"
                + "showDimensionConversion=" + showDimensionConversion + "\n"
                + "showLastDeath=" + showLastDeath + "\n"
                + "# Death row text: DISTANCE (48 blocks NW), COORDS (12, 64, -30) or BOTH\n"
                + "deathStyle=" + deathStyle + "\n";
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        v = v.trim();
        if (v.equalsIgnoreCase("true")) return true;
        if (v.equalsIgnoreCase("false")) return false;
        return def;
    }

    private static int integer(Properties p, String key, int def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static int color(Properties p, String key, int def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        v = v.trim();
        if (v.startsWith("#")) v = v.substring(1);
        else if (v.startsWith("0x") || v.startsWith("0X")) v = v.substring(2);
        if (v.length() != 6) return def;
        try {
            return Integer.parseInt(v, 16);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static <E extends Enum<E>> E enumValue(Properties p, String key, Class<E> type, E def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Enum.valueOf(type, v.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_'));
        } catch (IllegalArgumentException e) {
            return def;
        }
    }
}
