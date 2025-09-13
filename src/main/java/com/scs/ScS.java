package com.scs;

import com.scs.anticheat.AntiCheatDetector;
import com.scs.anticheat.AntiCheatGui;
import com.scs.anticheat.KeyHandler;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScS implements ClientModInitializer {
    public static final String MODID = "scs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("=== SCS ENHANCED FABRIC ЗАГРУЖАЕТСЯ ===");
        LOGGER.info("🛡️ ScS Enhanced AntiCheat Monitor для SkyBars");
        LOGGER.info("📋 Версия: 2.0 | Minecraft: 1.20.2 | Fabric");

        // Инициализируем компоненты
        AntiCheatDetector.init();
        AntiCheatGui.init();
        KeyHandler.init();

        LOGGER.info("✅ SCS Enhanced успешно загружен!");
        LOGGER.info("🎮 Горячие клавиши:");
        LOGGER.info("   F8 - переключить HUD");
        LOGGER.info("   F9 - открыть меню");
        LOGGER.info("   F10 - очистить записи");
    }
}