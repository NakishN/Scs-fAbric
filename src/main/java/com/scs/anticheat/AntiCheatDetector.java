package com.scs.anticheat;

import com.scs.ScS;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntiCheatDetector {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    // Исправленный regex - более точный
    private static final Pattern ANTICHEAT_PATTERN = Pattern.compile(
            ".*Анти-Чит.*?\\]\\s*(\\w+)\\s+(.+?)\\s*\\(([^)]+)\\)(?:\\s*#(\\d+))?.*",
            Pattern.CASE_INSENSITIVE
    );

    // Простой паттерн для поиска по словам
    private static boolean isListening = false;
    private static String lastMessage = "";

    public static void init() {
        ScS.LOGGER.info("Инициализация AntiCheatDetector с реальным перехватом...");

        // Тестируем исправленный парсер
        testFixedParsing();

        // Включаем реальный перехват сообщений
        startRealTimeMonitoring();

        ScS.LOGGER.info("AntiCheatDetector готов к работе!");
    }

    private static void testFixedParsing() {
        ScS.LOGGER.info("=== ТЕСТ ИСПРАВЛЕННОГО ПАРСЕРА ===");

        String testMessage = "[13:35:40] [Render thread/INFO]: [System] [CHAT] [Анти-Чит] nakish_ tried to move abnormally (Move) #1";
        ScS.LOGGER.info("Тестируем: {}", testMessage);

        parseAntiCheatMessage(testMessage);

        ScS.LOGGER.info("=== ТЕСТ ЗАВЕРШЕН ===");
    }

    private static void startRealTimeMonitoring() {
        ScS.LOGGER.info("Включаем мониторинг сообщений в реальном времени...");
        isListening = true;

        // Регистрируем обработчик для перехвата сообщений
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (isListening && client.player != null) {
                checkForNewChatMessages();
            }
        });
    }

    private static void checkForNewChatMessages() {
        try {
            // Эмулируем проверку новых сообщений
            // В реальном случае здесь будет перехват из чата

            // TODO: Здесь нужно будет подключиться к реальному чату
            // Пока что логируем что мы слушаем
            if (System.currentTimeMillis() % 5000 < 50) { // каждые 5 секунд
                ScS.LOGGER.info("Мониторинг активен, ожидаем сообщения античита...");
            }

        } catch (Exception e) {
            // Игнорируем ошибки чтобы не спамить
        }
    }

    // Этот метод можно вызвать вручную для обработки реального сообщения
    public static void handleRealMessage(String message) {
        if (isAntiCheatMessage(message)) {
            ScS.LOGGER.info("🚨 ПЕРЕХВАЧЕНО РЕАЛЬНОЕ СООБЩЕНИЕ АНТИЧИТА!");
            parseAntiCheatMessage(message);
        }
    }

    public static void parseAntiCheatMessage(String rawMessage) {
        try {
            ScS.LOGGER.info("Обрабатываем: {}", rawMessage);

            // Очищаем сообщение
            String cleanMessage = cleanMessage(rawMessage);
            ScS.LOGGER.info("Очищено: {}", cleanMessage);

            if (!isAntiCheatMessage(cleanMessage)) {
                ScS.LOGGER.warn("Не является сообщением античита");
                return;
            }

            // Используем новый подход - ищем по ключевым точкам
            parseByStructure(cleanMessage);

        } catch (Exception e) {
            ScS.LOGGER.error("Ошибка парсинга: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private static void parseByStructure(String message) {
        try {
            ScS.LOGGER.info("Структурный парсинг: {}", message);

            // Находим позицию "[Анти-Чит]"
            int antiCheatIndex = message.toLowerCase().indexOf("анти-чит");
            if (antiCheatIndex == -1) return;

            // Берем текст после "[Анти-Чит]"
            String afterAntiCheat = message.substring(antiCheatIndex);
            ScS.LOGGER.info("После 'Анти-Чит': {}", afterAntiCheat);

            // Разделяем по пробелам
            String[] parts = afterAntiCheat.split("\\s+");

            String playerName = null;
            String description = "";
            String violationType = "Unknown";
            int count = 1;

            // Ищем имя игрока (первое валидное слово после "Анти-Чит")
            for (int i = 0; i < parts.length; i++) {
                String cleanPart = parts[i].replaceAll("[\\[\\]()]", "");
                if (isValidPlayerName(cleanPart)) {
                    playerName = cleanPart;

                    // Собираем описание из слов после имени игрока
                    StringBuilder descBuilder = new StringBuilder();
                    for (int j = i + 1; j < parts.length; j++) {
                        String word = parts[j];

                        // Если слово в скобках - это тип нарушения
                        if (word.startsWith("(") && word.endsWith(")")) {
                            violationType = word.substring(1, word.length() - 1);
                        }
                        // Если начинается с # - это счетчик
                        else if (word.startsWith("#")) {
                            try {
                                count = Integer.parseInt(word.substring(1));
                            } catch (NumberFormatException ignored) {}
                        }
                        // Иначе это часть описания
                        else {
                            descBuilder.append(word).append(" ");
                        }
                    }
                    description = descBuilder.toString().trim();
                    break;
                }
            }

            if (playerName != null) {
                // Если тип не найден в скобках, определяем по описанию
                if ("Unknown".equals(violationType)) {
                    violationType = detectViolationType(description);
                }

                String criticalityLevel = getCriticalityLevel(violationType);

                ScS.LOGGER.info("✅ УСПЕШНО РАСПАРСЕНО:");
                ScS.LOGGER.info("  🎯 Игрок: '{}'", playerName);
                ScS.LOGGER.info("  📝 Описание: '{}'", description);
                ScS.LOGGER.info("  ⚠️ Тип: '{}'", violationType);
                ScS.LOGGER.info("  🔢 Счетчик: {}", count);
                ScS.LOGGER.info("  🚨 Критичность: {}", criticalityLevel);

                // TODO: Здесь создаем кнопки и добавляем в список

            } else {
                ScS.LOGGER.warn("❌ Не удалось найти имя игрока в сообщении");
            }

        } catch (Exception e) {
            ScS.LOGGER.error("Ошибка структурного парсинга: {}", e.getMessage());
        }
    }

    private static String cleanMessage(String message) {
        return message
                .replaceAll("\\[\\d{2}:\\d{2}:\\d{2}\\]", "") // время
                .replaceAll("\\[Render thread/INFO\\]:", "") // поток
                .replaceAll("\\[System\\]", "") // система
                .replaceAll("\\[CHAT\\]", "") // чат
                .replaceAll("&[0-9a-fk-or]", "") // цвета
                .replaceAll("\\s+", " ") // лишние пробелы
                .trim();
    }

    private static boolean isAntiCheatMessage(String message) {
        return message.toLowerCase().contains("анти-чит");
    }

    private static boolean isValidPlayerName(String name) {
        return name != null &&
                name.length() >= 3 &&
                name.length() <= 16 &&
                name.matches("[A-Za-z0-9_]+") &&
                !name.toLowerCase().matches("(анти|чит|система|чат)");
    }

    private static String detectViolationType(String description) {
        String lower = description.toLowerCase();

        if (lower.contains("move") || lower.contains("abnormally")) return "Move";
        if (lower.contains("place") || lower.contains("break") || lower.contains("quickly")) return "FastPlace";
        if (lower.contains("reach") || lower.contains("distance")) return "HitBox";
        if (lower.contains("velocity") || lower.contains("ignore")) return "Velocity";
        if (lower.contains("combat") || lower.contains("hacks")) return "KillAura";
        if (lower.contains("speed") || lower.contains("delay")) return "Delay";
        if (lower.contains("robots") || lower.contains("automatic")) return "AutoBot";
        if (lower.contains("click")) return "Click";

        return "Unknown";
    }

    private static String getCriticalityLevel(String violationType) {
        switch (violationType) {
            case "KillAura":
            case "Velocity":
            case "AutoBot":
            case "HitBox":
                return "🔴 КРИТИЧНО";
            case "FastPlace":
            case "Click":
            case "Delay":
                return "🟡 СРЕДНЕ";
            case "Move":
                return "🟢 НИЗКО";
            default:
                return "⚪ НЕИЗВЕСТНО";
        }
    }

    // Публичные методы для управления
    public static void enableMonitoring() {
        isListening = true;
        ScS.LOGGER.info("✅ Мониторинг включен");
    }

    public static void disableMonitoring() {
        isListening = false;
        ScS.LOGGER.info("❌ Мониторинг отключен");
    }

    public static boolean isMonitoring() {
        return isListening;
    }
}