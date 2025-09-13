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
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("scs-config.json");

    private static ModConfig INSTANCE = new ModConfig();

    // HUD настройки
    public boolean enableHud = true;
    public int hudX = -320;
    public int hudY = 10;
    public int maxMessages = 50;
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

    // Фильтры нарушений
    public List<String> violationKeywords = Arrays.asList(
            "tried to move abnormally",
            "tried to reach entity outside",
            "might be using combat hacks",
            "suspected use of automatic robots",
            "tried to interact",
            "invalid movement",
            "speed hacks",
            "fly hacks"
    );

    // Команды для кнопок
    public String freezeCommand = "/freezing";
    public String spectateCommand = "/matrix spectate";
    public String activityCommand = "/playeractivity";
    public String historyCommand = "/freezinghistory";

    public static ModConfig getInstance() {
        return INSTANCE;
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                INSTANCE = GSON.fromJson(json, ModConfig.class);
                ScS.LOGGER.info("✅ Конфигурация загружена из {}", CONFIG_PATH);
            } else {
                save(); // Создаем дефолтную конфигурацию
                ScS.LOGGER.info("📝 Создана новая конфигурация в {}", CONFIG_PATH);
            }
        } catch (IOException e) {
            ScS.LOGGER.error("❌ Ошибка загрузки конфигурации: {}", e.getMessage());
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
}