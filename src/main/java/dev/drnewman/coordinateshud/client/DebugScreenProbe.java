package dev.drnewman.coordinateshud.client;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tells whether the F3 debug screen is open.
 *
 * Minecraft 26.x reworked the debug screen and does not expose a single stable accessor for "is it showing",
 * so the method is looked up once by name on first use and the choice is logged. If none of the known names
 * exists, the HUD simply stays visible.
 */
final class DebugScreenProbe {
    private static final Logger LOG = LoggerFactory.getLogger("coordhud");

    private static boolean resolved;
    private static Method method;
    private static boolean onOverlay; // true: call on getDebugOverlay(); false: call on debugEntries

    private DebugScreenProbe() {}

    static boolean isOpen(Minecraft mc) {
        if (!resolved) {
            resolve(mc);
        }
        if (method == null) {
            return false;
        }
        try {
            Object target = onOverlay ? mc.getDebugOverlay() : mc.debugEntries;
            return target != null && Boolean.TRUE.equals(method.invoke(target));
        } catch (ReflectiveOperationException | RuntimeException e) {
            method = null; // stop trying after a failure
            LOG.warn("F3 detection disabled after an error: {}", e.toString());
            return false;
        }
    }

    private static void resolve(Minecraft mc) {
        resolved = true;
        Object entries = mc.debugEntries;
        Object overlay = mc.getDebugOverlay();
        // Candidates in order of likelihood.
        if (tryMethod(entries, "isOverlayVisible", false)
                || tryMethod(overlay, "showDebugScreen", true)
                || tryMethod(entries, "isOverlayEnabled", false)) {
            LOG.info("F3 detection using {}#{}", method.getDeclaringClass().getSimpleName(), method.getName());
            return;
        }
        LOG.warn("F3 detection unavailable; boolean no-arg methods seen: entries={} overlay={}",
                booleanMethods(entries), booleanMethods(overlay));
    }

    private static boolean tryMethod(Object target, String name, boolean overlay) {
        if (target == null) return false;
        try {
            Method m = target.getClass().getMethod(name);
            if (m.getReturnType() == boolean.class && !Modifier.isStatic(m.getModifiers())) {
                method = m;
                onOverlay = overlay;
                return true;
            }
        } catch (NoSuchMethodException ignored) {
            // try the next candidate
        }
        return false;
    }

    private static String booleanMethods(Object target) {
        if (target == null) return "[null]";
        StringBuilder sb = new StringBuilder("[");
        for (Method m : target.getClass().getMethods()) {
            if (m.getParameterCount() == 0 && m.getReturnType() == boolean.class && m.getDeclaringClass() != Object.class) {
                sb.append(m.getName()).append(' ');
            }
        }
        return sb.append(']').toString();
    }
}
