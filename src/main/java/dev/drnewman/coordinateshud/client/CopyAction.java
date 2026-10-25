package dev.drnewman.coordinateshud.client;

import dev.drnewman.coordinateshud.core.CoordinateMath;
import dev.drnewman.coordinateshud.core.FeedbackTimer;
import dev.drnewman.coordinateshud.core.HudConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** Copies the player's coordinates, read at the moment of the action, to the system clipboard. */
final class CopyAction {
    private CopyAction() {}

    static void perform(Minecraft mc, HudConfig cfg, FeedbackTimer feedback) {
        long now = System.nanoTime();
        LocalPlayer player = mc.player;
        if (player == null) {
            return; // nothing valid to copy, and nothing to claim
        }
        String text = CoordinateMath.clipboardString(player.getX(), player.getY(), player.getZ(), cfg.coordinateFormat);
        if (text == null) {
            feedback.show(FeedbackTimer.Kind.FAILURE, now);
            return;
        }
        try {
            mc.keyboardHandler.setClipboard(text);
            feedback.show(FeedbackTimer.Kind.SUCCESS, now);
        } catch (RuntimeException e) {
            feedback.show(FeedbackTimer.Kind.FAILURE, now);
        }
    }
}
