package dev.drnewman.coordinateshud.client;

import dev.drnewman.coordinateshud.core.DimKind;
import dev.drnewman.coordinateshud.core.HudState;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Reads game state into a reusable HudState. Client-only, read-only: it never writes to the world or sends packets.
 */
final class McBridge {
    private McBridge() {}

    static boolean read(Minecraft mc, HudState out) {
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) {
            out.available = false;
            return false;
        }
        out.available = true;
        // The player's own position, not the camera's (third person / spectator camera do not affect it).
        out.x = player.getX();
        out.y = player.getY();
        out.z = player.getZ();
        out.yaw = player.getYRot(); // horizontal rotation only; pitch (getXRot) is never read
        out.hasDayTime = true;
        out.dayTime = level.getOverworldClockTime();
        out.dim = kind(level.dimension());

        Optional<GlobalPos> death = player.getLastDeathLocation();
        if (death.isPresent()) {
            GlobalPos g = death.get();
            BlockPos p = g.pos();
            out.hasDeath = true;
            out.deathX = p.getX();
            out.deathY = p.getY();
            out.deathZ = p.getZ();
            out.deathDim = kind(g.dimension());
        } else {
            out.hasDeath = false;
        }
        return true;
    }

    private static DimKind kind(ResourceKey<Level> key) {
        if (Level.OVERWORLD.equals(key)) return DimKind.OVERWORLD;
        if (Level.NETHER.equals(key)) return DimKind.NETHER;
        if (Level.END.equals(key)) return DimKind.END;
        return DimKind.OTHER;
    }
}
