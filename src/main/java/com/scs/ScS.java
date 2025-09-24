package com.scs;

import com.scs.anticheat.AntiCheatDetector;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScS implements ClientModInitializer {
    public static final String MODID = "scs-enhanced";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("===== SCS ENHANCED v2.2 ЗАГРУЖАЕТСЯ =====");
        LOGGER.info("Мод для администраторов SkyBars - мониторинг античита");
        LOGGER.info("🛠️ Стабильная версия без проблемных зависимостей:");
        LOGGER.info("  • Улучшенный парсер всех форматов сообщений");
        LOGGER.info("  • Ручной ввод сообщений для тестирования");
        LOGGER.info("  • Автоматический подсчет нарушений");
        LOGGER.info("  • Система критичности нарушений");

        try {
            // Инициализируем детектор античита
            AntiCheatDetector.init();

            // Демонстрируем как добавить реальное сообщение вручную
            demonstrateManualUsage();

            LOGGER.info("===== SCS ENHANCED v2.2 УСПЕШНО ЗАГРУЖЕН =====");
            LOGGER.info("✅ Все системы готовы к работе!");
            LOGGER.info("");
            LOGGER.info("📝 Для обработки реального сообщения используйте:");
            LOGGER.info("   AntiCheatDetector.handleRealMessage(\"[реальное сообщение]\");");

        } catch (Exception e) {
            LOGGER.error("❌ КРИТИЧЕСКАЯ ОШИБКА при загрузке SCS Enhanced: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private void demonstrateManualUsage() {
        LOGGER.info("");
        LOGGER.info("🎯 === ДЕМОНСТРАЦИЯ РУЧНОГО ВВОДА ===");

        // Симулируем получение нового сообщения
        String newMessage = "[12:08:40] [Render thread/INFO]: [System] [CHAT] [Анти-Чит] vadimSSS01 tried to reach entity outside max reach distance (HitBox)";
        LOGGER.info("📥 Симулируем получение: {}", newMessage);

        // Обрабатываем его
        AntiCheatDetector.handleRealMessage(newMessage);

        LOGGER.info("🎯 === ДЕМОНСТРАЦИЯ ЗАВЕРШЕНА ===");
        LOGGER.info("");
    }
}