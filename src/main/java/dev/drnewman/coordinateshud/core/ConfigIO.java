package dev.drnewman.coordinateshud.core;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/** Reads and writes the config file. Never throws: a broken file is moved aside and defaults are used. */
public final class ConfigIO {
    private ConfigIO() {}

    public static HudConfig load(Path file) {
        if (!Files.exists(file)) {
            HudConfig defaults = new HudConfig();
            save(file, defaults);
            return defaults;
        }
        try {
            String existing = Files.readString(file, StandardCharsets.UTF_8);
            Properties props = new Properties();
            try (Reader r = new java.io.StringReader(existing)) {
                props.load(r);
            }
            HudConfig config = HudConfig.fromProperties(props);
            // Rewrite only when something was missing/invalid/outdated, so the file is not touched needlessly.
            if (!existing.equals(config.toFileText())) {
                save(file, config);
            }
            return config;
        } catch (IOException | IllegalArgumentException e) {
            moveAside(file);
            HudConfig defaults = new HudConfig();
            save(file, defaults);
            return defaults;
        }
    }

    /** @return true if the file was written. */
    public static boolean save(Path file, HudConfig config) {
        try {
            Path dir = file.toAbsolutePath().getParent();
            if (dir != null) {
                Files.createDirectories(dir);
            }
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(tmp, config.toFileText(), StandardCharsets.UTF_8);
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    private static void moveAside(Path file) {
        try {
            Files.move(file, file.resolveSibling(file.getFileName() + ".broken"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            // Best effort; save() will overwrite anyway.
        }
    }
}
