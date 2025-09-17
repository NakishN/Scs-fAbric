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
        LOGGER.info("===== SCS ENHANCED ЗАГРУЖАЕТСЯ =====");
        LOGGER.info("Мод для мониторинга античита SkyBars");

        try {
            // Инициализируем детектор сообщений античита (тестовая версия)
            AntiCheatDetector.init();

            LOGGER.info("===== SCS ENHANCED ЗАГРУЖЕН =====");
            LOGGER.info("Тестирование парсинга сообщений античита завершено!");
            LOGGER.info("Проверь логи выше для результатов тестирования.");

        } catch (Exception e) {
            LOGGER.error("ОШИБКА при загрузке SCS Enhanced: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}