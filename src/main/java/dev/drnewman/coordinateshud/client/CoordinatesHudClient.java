package dev.drnewman.coordinateshud.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.drnewman.coordinateshud.core.ConfigIO;
import dev.drnewman.coordinateshud.core.HudConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/** Client entrypoint: registers the HUD element, the two keybinds and the config hot-reload. Dev : Dr. Newman */
public final class CoordinatesHudClient implements ClientModInitializer {
    public static final String MOD_ID = "coordhud";
    private static final int RELOAD_CHECK_TICKS = 40; // ~2 seconds

    private final McDraw draw = new McDraw();
    private final HudRenderer hud = new HudRenderer(draw);
    private Path configFile;
    private HudConfig config = new HudConfig();
    private long configStamp = -1L;
    private int tickCounter;
    private KeyMapping copyKey;
    private KeyMapping settingsKey;

    @Override
    public void onInitializeClient() {
        configFile = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".properties");
        reloadConfig();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
        copyKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key." + MOD_ID + ".copy", InputConstants.Type.KEYBOARD, InputConstants.KEY_K, category));

        settingsKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key." + MOD_ID + ".settings", InputConstants.Type.KEYBOARD, InputConstants.KEY_H, category));

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "coordinates"), (graphics, tickDelta) -> {
            Minecraft mc = Minecraft.getInstance();
            draw.bind(graphics, mc.font);
            hud.render(mc, config);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (copyKey.consumeClick()) {
                if (config.enabled) {
                    CopyAction.perform(client, config, hud.feedback);
                }
            }
            while (settingsKey.consumeClick()) {
                if (client.player != null) {
                    client.setScreenAndShow(new SettingsScreen(config, this::saveConfig));
                }
            }
            if (++tickCounter >= RELOAD_CHECK_TICKS) {
                tickCounter = 0;
                if (currentStamp() != configStamp) {
                    reloadConfig();
                }
            }
        });
    }

    private void saveConfig() {
        ConfigIO.save(configFile, config);
        configStamp = currentStamp(); // our own save must not trigger a reload
    }

    private void reloadConfig() {
        config = ConfigIO.load(configFile);
        configStamp = currentStamp(); // taken after load, so our own rewrite does not trigger another reload
    }

    private long currentStamp() {
        try {
            return Files.getLastModifiedTime(configFile).toMillis();
        } catch (IOException e) {
            return -1L;
        }
    }
}
