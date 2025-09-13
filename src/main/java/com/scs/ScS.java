package com.scs;

import com.scs.anticheat.AntiCheatDetector;
import com.scs.anticheat.AntiCheatGui;
import com.scs.anticheat.KeyHandler;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Scs implements ClientModInitializer {
    public static final String MODID = "scs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("=== SCS FABRIC ЗАГРУЖАЕТСЯ ===");

        // Инициализируем компоненты
        AntiCheatDetector.init();
        AntiCheatGui.init();
        KeyHandler.init();

        LOGGER.info("✅ SCS AntiCheat Helper загружен для SkyBars (Fabric 1.20.2)!");
    }
}