package com.scs.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scs.ScS;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("scs-enhanced-config.json");

    private static ModConfig INSTANCE = new ModConfig();

    // HUD настройки
    public boolean enableHud = true;
    public int hudX = -320;
    public int hudY = 10;
    public int maxMessages = 100;
    public int showLast = 15;

    // Цвета (в формате 0xRRGGBB)
    public int checkColor = 0x00FF7F;
    public int acColor = 0xFF4444;
    public int violationColor = 0xFFA500;
    public int criticalColor = 0xFF0000;

    // Интерактивные кнопки
    public boolean enableChatButtons = true;
    public boolean autoCommands = false;

    // Звуки и уведомления
    public boolean soundAlerts = true;
    public String alertSound = "minecraft:block.note_block.bell";

    // Логирование
    public boolean enableLogging = true;
    public boolean logAllChat = false;
    public boolean debugMode = false;

    // Фильтры нарушений
    public List<String> violationKeywords = Arrays.asList(
            "tried to move abnormally",
            "tried to reach entity outside",
            "might be using combat hacks",
            "suspected use of automatic robots",
            "tried to interact",
            "invalid movement",
            "speed hacks",
            "fly hacks",
            "reach entity outside max reach distance"
    );

    // Команды для кнопок (настраиваемые)
    public String freezeCommand = "/freezing %player%";
    public String spectateCommand = "/matrix spectate %player%";
    public String activityCommand = "/playeractivity %player%";
    public String historyCommand = "/freezinghistory %player%";

    // Дополнительные настройки
    public boolean showTimestamps = true;
    public boolean showPlayerCount = true;
    public boolean highlightCritical = true;
    public int menuTransparency = 80; // 0-100%

    public static ModConfig getInstance() {
        return INSTANCE;
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                INSTANCE = GSON.fromJson(json, ModConfig.class);

                // Валидация загруженных данных
                validate();

                ScS.LOGGER.info("✅ Конфигурация загружена из {}", CONFIG_PATH);
            } else {
                save(); // Создаем дефолтную конфигурацию
                ScS.LOGGER.info("📝 Создана новая конфигурация в {}", CONFIG_PATH);
            }
        } catch (Exception e) {
            ScS.LOGGER.error("❌ Ошибка загрузки конфигурации: {}", e.getMessage());
            INSTANCE = new ModConfig(); // Возвращаемся к дефолту при ошибке
            save(); // Сохраняем исправленную версию
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(INSTANCE);
            Files.writeString(CONFIG_PATH, json);
            ScS.LOGGER.info("💾 Конфигурация сохранена");
        } catch (IOException e) {
            ScS.LOGGER.error("❌ Ошибка сохранения конфигурации: {}", e.getMessage());
        }
    }

    /**
     * Валидация настроек после загрузки
     */
    private static void validate() {
        // Ограничиваем значения в разумных пределах
        INSTANCE.maxMessages = Math.max(10, Math.min(INSTANCE.maxMessages, 500));
        INSTANCE.showLast = Math.max(5, Math.min(INSTANCE.showLast, 50));
        INSTANCE.menuTransparency = Math.max(0, Math.min(INSTANCE.menuTransparency, 100));

        // Проверяем существование команд
        if (INSTANCE.violationKeywords == null || INSTANCE.violationKeywords.isEmpty()) {
            INSTANCE.violationKeywords = Arrays.asList(
                    "tried to move abnormally",
                    "might be using combat hacks",
                    "suspected use of automatic robots"
            );
        }

        // Исправляем команды если нужно
        if (INSTANCE.freezeCommand == null || !INSTANCE.freezeCommand.contains("%player%")) {
            INSTANCE.freezeCommand = "/freezing %player%";
        }
        if (INSTANCE.spectateCommand == null || !INSTANCE.spectateCommand.contains("%player%")) {
            INSTANCE.spectateCommand = "/matrix spectate %player%";
        }
        if (INSTANCE.activityCommand == null || !INSTANCE.activityCommand.contains("%player%")) {
            INSTANCE.activityCommand = "/playeractivity %player%";
        }
        if (INSTANCE.historyCommand == null || !INSTANCE.historyCommand.contains("%player%")) {
            INSTANCE.historyCommand = "/freezinghistory %player%";
        }
    }

    // Утилиты для работы с командами
    public String getFormattedCommand(String command, String playerName) {
        return command.replace("%player%", playerName);
    }

    // Утилиты для цветов
    public static int parseColor(String hex, int fallback) {
        try {
            if (hex.startsWith("#")) hex = hex.substring(1);
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            ScS.LOGGER.warn("Неверный формат цвета: {}, используется fallback", hex);
            return fallback;
        }
    }

    public static String colorToHex(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
    }

    // Методы для обновления настроек в рантайме
    public void toggleHud() {
        this.enableHud = !this.enableHud;
        save();
    }

    public void toggleSoundAlerts() {
        this.soundAlerts = !this.soundAlerts;
        save();
    }

    public void toggleDebugMode() {
        this.debugMode = !this.debugMode;
        save();
        ScS.LOGGER.info("Режим отладки: {}", this.debugMode ? "включен" : "выключен");
    }
}