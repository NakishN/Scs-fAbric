package com.scs.anticheat;

import com.scs.ScS;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Детектор сообщений античита SkyBars
 * Версия 2.2 - Стабильная работа без проблемных зависимостей
 */
public class AntiCheatDetector {
    // Паттерны для разных форматов сообщений античита
    private static final Pattern ANTICHEAT_PATTERN_WITH_COUNT = Pattern.compile(
            ".*Анти-Чит.*?\\]\\s*(\\w+)\\s+(.+?)\\s*\\(([^)]+)\\)\\s*#(\\d+).*",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern ANTICHEAT_PATTERN_NO_COUNT = Pattern.compile(
            ".*Анти-Чит.*?\\]\\s*(\\w+)\\s+(.+?)\\s*\\(([^)]+)\\).*",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    // Хранилище данных
    private static final Map<String, Map<String, Integer>> playerViolations = new ConcurrentHashMap<>();
    private static final List<ViolationRecord> violationHistory = new ArrayList<>();

    // Статистика
    private static int totalViolations = 0;
    private static boolean isListening = false;
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Структура для хранения записей о нарушениях
    public static class ViolationRecord {
        public final String timestamp;
        public final String playerName;
        public final String violationType;
        public final String description;
        public final int count;
        public final String criticalityLevel;

        public ViolationRecord(String playerName, String violationType, String description, int count, String criticalityLevel) {
            this.timestamp = LocalDateTime.now().format(timeFormatter);
            this.playerName = playerName;
            this.violationType = violationType;
            this.description = description;
            this.count = count;
            this.criticalityLevel = criticalityLevel;
        }
    }

    public static void init() {
        ScS.LOGGER.info("🚀 Инициализация AntiCheatDetector v2.2...");

        // Сбрасываем все данные при инициализации
        clearAllData();

        // Тестируем все форматы сообщений
        testAllMessageFormats();

        // Показываем статистику после тестов
        showDetailedStats();

        // Включаем мониторинг
        enableMonitoring();

        ScS.LOGGER.info("✅ AntiCheatDetector v2.2 готов к работе!");
    }

    private static void testAllMessageFormats() {
        ScS.LOGGER.info("🧪 === ТЕСТИРОВАНИЕ ВСЕХ ФОРМАТОВ СООБЩЕНИЙ ===");

        String[] testMessages = {
                // Формат с счетчиком #число
                "[13:35:40] [Render thread/INFO]: [System] [CHAT] [Анти-Чит] nakish_ tried to move abnormally (Move) #1",
                "[14:22:15] [Render thread/INFO]: [System] [CHAT] [Анти-Чит] player123 used speed hacks (KillAura) #5",
                "[15:45:33] [Render thread/INFO]: [System] [CHAT] [Анти-Чит] TestUser broke blocks too quickly (FastPlace) #2",

                // Новый формат БЕЗ счетчика (ваша проблема)
                "[12:08:40] [Render thread/INFO]: [System] [CHAT] [Анти-Чит] vadimSSS01 tried to reach entity outside max reach distance (HitBox)",

                // Дополнительные форматы для тестирования
                "[10:15:22] [Render thread/INFO]: [System] [CHAT] [Анти-Чит] CheaterName used fly hacks (Fly) #12",
                "[09:30:45] [Render thread/INFO]: [System] [CHAT] [Анти-Чит] PlayerXYZ clicked too fast (AutoClick)",
                "[11:12:33] [Render thread/INFO]: [System] [CHAT] [Анти-Чит] BadPlayer ignored velocity changes (Velocity) #3",

                // Повторные нарушения для тестирования подсчета
                "[16:20:10] [Render thread/INFO]: [System] [CHAT] [Анти-Чит] vadimSSS01 tried to reach entity outside max reach distance (HitBox)",
                "[16:25:30] [Render thread/INFO]: [System] [CHAT] [Анти-Чит] vadimSSS01 used combat hacks (KillAura)",
        };

        for (int i = 0; i < testMessages.length; i++) {
            ScS.LOGGER.info("📝 Тест {}: {}", i + 1, testMessages[i]);
            parseAntiCheatMessage(testMessages[i]);
            ScS.LOGGER.info("───────────────────────────────────");
        }

        ScS.LOGGER.info("🧪 === ВСЕ ТЕСТЫ ЗАВЕРШЕНЫ ===");
    }

    // Основной метод для внешнего использования
    public static void handleRealMessage(String message) {
        if (isAntiCheatMessage(message)) {
            ScS.LOGGER.info("🚨 ПОЛУЧЕНО РЕАЛЬНОЕ СООБЩЕНИЕ АНТИЧИТА!");
            parseAntiCheatMessage(message);
        } else {
            ScS.LOGGER.info("ℹ️ Сообщение не является сообщением античита: {}", message);
        }
    }

    public static void parseAntiCheatMessage(String rawMessage) {
        try {
            ScS.LOGGER.info("🔍 Анализируем: {}", rawMessage);

            // Очищаем сообщение
            String cleanMessage = cleanMessage(rawMessage);
            ScS.LOGGER.info("🧹 Очищено: {}", cleanMessage);

            if (!isAntiCheatMessage(cleanMessage)) {
                ScS.LOGGER.warn("❌ Не является сообщением античита");
                return;
            }

            // Пробуем разные методы парсинга
            boolean parsed = tryRegexParsing(cleanMessage);

            if (!parsed) {
                ScS.LOGGER.info("🔄 Regex не сработал, пробуем структурный анализ...");
                parseByStructure(cleanMessage);
            }

        } catch (Exception e) {
            ScS.LOGGER.error("❌ Ошибка парсинга: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean tryRegexParsing(String message) {
        try {
            // Пробуем паттерн с счетчиком
            Matcher matcherWithCount = ANTICHEAT_PATTERN_WITH_COUNT.matcher(message);
            if (matcherWithCount.find()) {
                String playerName = matcherWithCount.group(1);
                String description = matcherWithCount.group(2).trim();
                String violationType = matcherWithCount.group(3);
                int count = Integer.parseInt(matcherWithCount.group(4));

                processViolation(playerName, description, violationType, count, true);
                return true;
            }

            // Пробуем паттерн без счетчика
            Matcher matcherNoCount = ANTICHEAT_PATTERN_NO_COUNT.matcher(message);
            if (matcherNoCount.find()) {
                String playerName = matcherNoCount.group(1);
                String description = matcherNoCount.group(2).trim();
                String violationType = matcherNoCount.group(3);

                int count = incrementPlayerViolation(playerName, violationType);
                processViolation(playerName, description, violationType, count, false);
                return true;
            }

        } catch (Exception e) {
            ScS.LOGGER.debug("Regex парсинг не удался: {}", e.getMessage());
        }

        return false;
    }

    private static void parseByStructure(String message) {
        try {
            ScS.LOGGER.info("🎯 Структурный анализ: {}", message);

            // Находим позицию "[Анти-Чит]"
            int antiCheatIndex = message.toLowerCase().indexOf("анти-чит");
            if (antiCheatIndex == -1) {
                ScS.LOGGER.warn("❌ '[Анти-Чит]' не найден в сообщении");
                return;
            }

            // Берем текст после "[Анти-Чит]"
            String afterAntiCheat = message.substring(antiCheatIndex + 8); // 8 = длина "анти-чит"
            afterAntiCheat = afterAntiCheat.replaceFirst("^[\\]\\s]*", "").trim(); // убираем ] и пробелы

            ScS.LOGGER.info("📍 После '[Анти-Чит]': '{}'", afterAntiCheat);

            // Разбираем части сообщения
            String[] parts = afterAntiCheat.split("\\s+");
            if (parts.length < 3) {
                ScS.LOGGER.warn("❌ Недостаточно частей в сообщении");
                return;
            }

            String playerName = null;
            String description = "";
            String violationType = "Unknown";
            int count = 1;

            // Первая часть должна быть именем игрока
            if (isValidPlayerName(parts[0])) {
                playerName = parts[0];

                // Собираем описание и ищем тип в скобках
                StringBuilder descBuilder = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    String word = parts[i];

                    // Если в скобках - это тип нарушения
                    if (word.startsWith("(") && word.endsWith(")")) {
                        violationType = word.substring(1, word.length() - 1);
                    }
                    // Если начинается с # - это счетчик
                    else if (word.startsWith("#")) {
                        try {
                            count = Integer.parseInt(word.substring(1));
                        } catch (NumberFormatException ignored) {}
                    }
                    // Иначе добавляем к описанию
                    else {
                        descBuilder.append(word).append(" ");
                    }
                }

                description = descBuilder.toString().trim();

                // Если счетчика нет, считаем сами
                boolean hasOriginalCount = count > 1 || message.contains("#");
                if (!hasOriginalCount) {
                    count = incrementPlayerViolation(playerName, violationType);
                }

                processViolation(playerName, description, violationType, count, hasOriginalCount);

            } else {
                ScS.LOGGER.warn("❌ '{}' не является валидным именем игрока", parts[0]);
            }

        } catch (Exception e) {
            ScS.LOGGER.error("❌ Ошибка структурного анализа: {}", e.getMessage());
        }
    }

    private static void processViolation(String playerName, String description, String violationType, int count, boolean hasOriginalCount) {
        // Определяем тип если не указан
        if (violationType.isEmpty() || "Unknown".equals(violationType)) {
            violationType = detectViolationType(description);
        }

        String criticalityLevel = getCriticalityLevel(violationType);
        String countInfo = hasOriginalCount ? count + " (серверный)" : count + " (подсчитано)";

        ScS.LOGGER.info("✅ === УСПЕШНО РАСПАРСЕНО ===");
        ScS.LOGGER.info("  🎯 Игрок: '{}'", playerName);
        ScS.LOGGER.info("  📝 Описание: '{}'", description);
        ScS.LOGGER.info("  ⚠️ Тип нарушения: '{}'", violationType);
        ScS.LOGGER.info("  🔢 Счетчик: {}", countInfo);
        ScS.LOGGER.info("  🚨 Критичность: {}", criticalityLevel);

        // Создаем запись о нарушении
        ViolationRecord record = new ViolationRecord(playerName, violationType, description, count, criticalityLevel);
        violationHistory.add(record);
        totalViolations++;

        // Показываем алерты для критичных случаев
        checkCriticalViolations(playerName, violationType, count, criticalityLevel);

        ScS.LOGGER.info("  📊 Общий счетчик нарушений: {}", totalViolations);
        ScS.LOGGER.info("✅ ===========================");
    }

    private static void checkCriticalViolations(String playerName, String violationType, int count, String criticalityLevel) {
        if (criticalityLevel.contains("КРИТИЧНО")) {
            if (count >= 5) {
                ScS.LOGGER.warn("🚨🚨🚨 КРИТИЧЕСКИЙ АЛЕРТ! 🚨🚨🚨");
                ScS.LOGGER.warn("   Игрок '{}' достиг {} нарушений типа '{}'!", playerName, count, violationType);
                ScS.LOGGER.warn("   Рекомендуется немедленное вмешательство!");
            } else if (count >= 3) {
                ScS.LOGGER.warn("⚠️ ВНИМАНИЕ! Игрок '{}' имеет {} нарушений '{}'", playerName, count, violationType);
            }
        } else if (count >= 10) {
            ScS.LOGGER.warn("⚠️ Игрок '{}' имеет много ({}) нарушений '{}'", playerName, count, violationType);
        }
    }

    private static int incrementPlayerViolation(String playerName, String violationType) {
        playerViolations.putIfAbsent(playerName, new ConcurrentHashMap<>());
        Map<String, Integer> violations = playerViolations.get(playerName);
        return violations.merge(violationType, 1, Integer::sum);
    }

    private static void showDetailedStats() {
        ScS.LOGGER.info("📊 === ПОДРОБНАЯ СТАТИСТИКА ===");

        if (violationHistory.isEmpty()) {
            ScS.LOGGER.info("Нет записей о нарушениях");
            return;
        }

        ScS.LOGGER.info("📈 Всего нарушений: {}", totalViolations);
        ScS.LOGGER.info("👥 Игроков с нарушениями: {}", playerViolations.size());

        ScS.LOGGER.info("📋 Последние нарушения:");
        for (int i = Math.max(0, violationHistory.size() - 5); i < violationHistory.size(); i++) {
            ViolationRecord record = violationHistory.get(i);
            ScS.LOGGER.info("   {} | {} | {} | {} x{} [{}]",
                    record.timestamp, record.playerName, record.violationType,
                    record.description, record.count, record.criticalityLevel);
        }

        if (playerViolations.size() > 0) {
            ScS.LOGGER.info("📊 По игрокам:");
            for (Map.Entry<String, Map<String, Integer>> playerEntry : playerViolations.entrySet()) {
                String playerName = playerEntry.getKey();
                Map<String, Integer> violations = playerEntry.getValue();

                ScS.LOGGER.info("   👤 {}: ", playerName);
                for (Map.Entry<String, Integer> violationEntry : violations.entrySet()) {
                    String type = violationEntry.getKey();
                    int count = violationEntry.getValue();
                    String level = getCriticalityLevel(type);
                    ScS.LOGGER.info("      ├─ {}: {} раз [{}]", type, count, level);
                }
            }
        }

        ScS.LOGGER.info("📊 ===============================");
    }

    // Вспомогательные методы

    private static String cleanMessage(String message) {
        return message
                .replaceAll("\\[\\d{2}:\\d{2}:\\d{2}\\]", "") // время [12:34:56]
                .replaceAll("\\[Render thread/INFO\\]:", "") // поток
                .replaceAll("\\[System\\]", "") // система
                .replaceAll("\\[CHAT\\]", "") // чат
                .replaceAll("&[0-9a-fk-or]", "") // цветовые коды
                .replaceAll("\\s+", " ") // множественные пробелы
                .trim();
    }

    public static boolean isAntiCheatMessage(String message) {
        return message.toLowerCase().contains("анти-чит");
    }

    private static boolean isValidPlayerName(String name) {
        return name != null &&
                name.length() >= 3 &&
                name.length() <= 16 &&
                name.matches("[A-Za-z0-9_]+") &&
                !name.toLowerCase().matches("(анти|чит|система|чат|render|thread|info|system)");
    }

    private static String detectViolationType(String description) {
        String lower = description.toLowerCase();

        if (lower.contains("move") || lower.contains("abnormally")) return "Move";
        if (lower.contains("place") || lower.contains("break") || lower.contains("quickly")) return "FastPlace";
        if (lower.contains("reach") || lower.contains("distance") || lower.contains("hitbox")) return "HitBox";
        if (lower.contains("velocity") || lower.contains("ignore") || lower.contains("knockback")) return "Velocity";
        if (lower.contains("combat") || lower.contains("hacks") || lower.contains("killaura") || lower.contains("aura")) return "KillAura";
        if (lower.contains("speed") || lower.contains("delay")) return "Delay";
        if (lower.contains("robots") || lower.contains("automatic") || lower.contains("bot")) return "AutoBot";
        if (lower.contains("click") || lower.contains("autoclick") || lower.contains("cps")) return "Click";
        if (lower.contains("fly") || lower.contains("flying") || lower.contains("levitation")) return "Fly";
        if (lower.contains("scaffold") || lower.contains("tower")) return "Scaffold";
        if (lower.contains("jesus") || lower.contains("water")) return "Jesus";

        return "Unknown";
    }

    private static String getCriticalityLevel(String violationType) {
        switch (violationType) {
            case "KillAura":
            case "Velocity":
            case "AutoBot":
            case "Fly":
            case "Jesus":
                return "🔴 КРИТИЧНО";
            case "HitBox":
            case "FastPlace":
            case "Click":
            case "Delay":
            case "Scaffold":
                return "🟡 СРЕДНЕ";
            case "Move":
                return "🟢 НИЗКО";
            default:
                return "⚪ НЕИЗВЕСТНО";
        }
    }

    // Публичные методы управления

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

    public static void clearAllData() {
        playerViolations.clear();
        violationHistory.clear();
        totalViolations = 0;
        ScS.LOGGER.info("🗑️ Все данные очищены");
    }

    // Методы для внешнего доступа к статистике

    public static Map<String, Map<String, Integer>> getPlayerViolations() {
        return new ConcurrentHashMap<>(playerViolations);
    }

    public static List<ViolationRecord> getViolationHistory() {
        return new ArrayList<>(violationHistory);
    }

    public static int getTotalViolations() {
        return totalViolations;
    }

    // Метод для ручного добавления сообщения (для GUI в будущем)
    public static void submitMessage(String message) {
        ScS.LOGGER.info("📝 Ручной ввод сообщения: {}", message);
        handleRealMessage(message);
    }
}