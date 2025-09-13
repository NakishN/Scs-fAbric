package com.scs;

import com.scs.anticheat.AntiCheatDetector;
import com.scs.anticheat.AntiCheatGui;
import com.scs.anticheat.KeyHandler;
import com.scs.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScS implements ClientModInitializer {
    public static final String MODID = "scs-enhanced";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("=== SCS ENHANCED FABRIC ЗАГРУЖАЕТСЯ ===");
        LOGGER.info("🛡️ ScS Enhanced AntiCheat Monitor для SkyBars");
        LOGGER.info("📋 Версия: 2.0 | Minecraft: 1.20.2 | Fabric");
        LOGGER.info("👤 Автор: nakish_ | Лицензия: All Rights Reserved");

        try {
            // Загружаем конфигурацию первой
            LOGGER.info("📝 Загрузка конфигурации...");
            ModConfig.load();

            // Инициализируем компоненты в правильном порядке
            LOGGER.info("🔧 Инициализация компонентов...");

            // 1. Детектор античита (основная логика)
            AntiCheatDetector.init();

            // 2. GUI интерфейс
            AntiCheatGui.init();

            // 3. Обработчик клавиш
            KeyHandler.init();

            LOGGER.info("✅ SCS Enhanced успешно загружен!");
            LOGGER.info("🎮 Горячие клавиши:");
            LOGGER.info("   F8  - переключить HUD мониторинг");
            LOGGER.info("   F9  - открыть меню статистики");
            LOGGER.info("   F10 - очистить все записи");
            LOGGER.info("═══════════════════════════════════════");

        } catch (Exception e) {
            LOGGER.error("❌ КРИТИЧЕСКАЯ ОШИБКА при загрузке SCS Enhanced:", e);
            throw new RuntimeException("Не удалось загрузить SCS Enhanced", e);
        }
    }

    /**
     * Метод для безопасного завершения работы мода
     */
    public static void shutdown() {
        try {
            LOGGER.info("🔄 Завершение работы SCS Enhanced...");

            // Сохраняем конфигурацию
            ModConfig.save();

            // Очищаем данные
            AntiCheatDetector.clearDetections();

            LOGGER.info("✅ SCS Enhanced корректно завершен");

        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при завершении работы:", e);
        }
    }

    /**
     * Получает текущую версию мода
     */
    public static String getVersion() {
        return "2.0.0";
    }

    /**
     * Получает информацию о моде
     */
    public static String getModInfo() {
        return String.format("SCS Enhanced v%s для Minecraft 1.20.2 Fabric", getVersion());
    }
}