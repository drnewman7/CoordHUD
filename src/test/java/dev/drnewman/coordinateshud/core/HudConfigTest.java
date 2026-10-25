package dev.drnewman.coordinateshud.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.drnewman.coordinateshud.core.HudConfig.Anchor;
import dev.drnewman.coordinateshud.core.HudConfig.CoordinateFormat;
import dev.drnewman.coordinateshud.core.HudConfig.DirectionStyle;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class HudConfigTest {
    private static Properties props(String text) throws Exception {
        Properties p = new Properties();
        p.load(new StringReader(text));
        return p;
    }

    @Test
    void defaults() {
        HudConfig c = new HudConfig();
        assertTrue(c.enabled);
        assertTrue(c.showCoordinates && c.showDirection && c.showHeading && c.showDay && c.showCopyIcon);
        assertEquals(Anchor.TOP_LEFT, c.anchor);
        assertEquals(60, c.backgroundOpacity);
        assertEquals(CoordinateFormat.BLOCK, c.coordinateFormat);
        assertEquals(DirectionStyle.EIGHT_WAY, c.directionStyle);
        assertEquals(1, c.firstDay);
        assertEquals(0x99000000, c.backgroundArgb());
    }

    @Test
    void validationClamps() throws Exception {
        HudConfig c = HudConfig.fromProperties(props("offsetX=-5\noffsetY=99999\nbackgroundOpacity=250\nfirstDay=7\n"));
        assertEquals(0, c.offsetX);
        assertEquals(HudConfig.MAX_OFFSET, c.offsetY);
        assertEquals(100, c.backgroundOpacity);
        assertEquals(1, c.firstDay);
    }

    @Test
    void invalidValuesFallBackIndividually() throws Exception {
        HudConfig c = HudConfig.fromProperties(props(
                "showDay=false\nanchor=middle\noffsetX=abc\naccentColor=zzzzzz\nshowHeading=maybe\nbackgroundOpacity=30\n"));
        assertFalse(c.showDay);                       // valid, preserved
        assertEquals(30, c.backgroundOpacity);        // valid, preserved
        assertEquals(Anchor.TOP_LEFT, c.anchor);      // invalid -> default
        assertEquals(4, c.offsetX);                   // invalid -> default
        assertEquals(0x7FC8E8, c.accentColor);        // invalid -> default
        assertTrue(c.showHeading);                    // invalid -> default
    }

    @Test
    void parsesFriendlyValues() throws Exception {
        HudConfig c = HudConfig.fromProperties(props(
                "anchor=bottom-right\naccentColor=#ff8800\ncoordinateFormat=decimal\ndirectionStyle=cardinal\n"));
        assertEquals(Anchor.BOTTOM_RIGHT, c.anchor);
        assertEquals(0xFF8800, c.accentColor);
        assertEquals(CoordinateFormat.DECIMAL, c.coordinateFormat);
        assertEquals(DirectionStyle.CARDINAL, c.directionStyle);
    }

    @Test
    void roundTrip() throws Exception {
        HudConfig a = new HudConfig();
        a.anchor = Anchor.BOTTOM_LEFT;
        a.offsetX = 12;
        a.showDay = false;
        a.accentColor = 0x0A0B0C;
        a.coordinateFormat = CoordinateFormat.DECIMAL;
        HudConfig b = HudConfig.fromProperties(props(a.toFileText()));
        assertEquals(a.toFileText(), b.toFileText());
        assertEquals(0x0A0B0C, b.accentColor);
    }

    @Test
    void fileLifecycle() throws Exception {
        Path dir = Files.createTempDirectory("hudcfg");
        Path file = dir.resolve("sub").resolve("cfg.properties");

        // missing -> defaults written
        HudConfig c = ConfigIO.load(file);
        assertTrue(Files.exists(file));
        assertEquals(new HudConfig().toFileText(), c.toFileText());

        // outdated/partial file keeps valid prefs and gets completed
        Files.writeString(file, "showDay=false\noffsetX=20\n", StandardCharsets.UTF_8);
        c = ConfigIO.load(file);
        assertFalse(c.showDay);
        assertEquals(20, c.offsetX);
        assertTrue(Files.readString(file).contains("backgroundOpacity=60"));

        // malformed (bad unicode escape) -> moved aside, defaults restored
        Files.writeString(file, "enabled=\\uZZZZ\n", StandardCharsets.UTF_8);
        c = ConfigIO.load(file);
        assertTrue(c.enabled);
        assertTrue(Files.exists(file.resolveSibling("cfg.properties.broken")));

        // binary garbage -> still safe
        Files.write(file, new byte[] {(byte) 0xFF, (byte) 0xFE, (byte) 0x80, 0x00, 0x41});
        c = ConfigIO.load(file);
        assertEquals(60, c.backgroundOpacity);

        // unchanged file is not rewritten
        long before = Files.getLastModifiedTime(file).toMillis();
        Thread.sleep(20);
        ConfigIO.load(file);
        assertEquals(before, Files.getLastModifiedTime(file).toMillis());
    }
}
