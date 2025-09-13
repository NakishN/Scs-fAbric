package com.scs.anticheat;

import com.scs.ScS;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntiCheatDetector {
    private static final List<AntiCheatDetection> detections = new ArrayList<>();
    private static final Pattern ANTICHEAT_PATTERN = Pattern.compile(
            ".*\\[Анти-Чит\\]\\s+(\\w+)\\s+(.*?)(?:\\s*\\(([^)]+)\\))?(?:\\s*#(\\d+))?.*"
    );

    public static void init() {
        ScS.LOGGER.info("=== ИНИЦИАЛИЗАЦИЯ AntiCheatDetector ===");

        try {
            // Регистрируем обработчик сообщений чата
            ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
                if (!overlay) { // Игнорируем overlay сообщения
                    handleChatMessage(message);
                }
            });
            ScS.LOGGER.info("✅ AntiCheatDetector зарегистрирован!");
        } catch (Exception e) {
            ScS.LOGGER.error("❌ Ошибка регистрации AntiCheatDetector: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleChatMessage(Text message) {
        try {
            String messageText = message.getString();

            // Проверяем сообщения античита
            if (messageText.contains("Анти-Чит") || messageText.contains("[Анти-Чит]")) {
                ScS.LOGGER.info("🚨 НАЙДЕНО СООБЩЕНИЕ АНТИЧИТА!");

                String playerName = parsePlayerName(messageText);
                ScS.LOGGER.info("Имя игрока: '{}'", playerName);

                if (playerName != null && !playerName.isEmpty()) {
                    ViolationType type = parseViolationType(messageText);
                    int count = parseCount(messageText);

                    ScS.LOGGER.info("Тип: {}, Счет: {}", type, count);

                    // Создаем кнопки
                    createButtons(playerName, type);

                    // Сохраняем
                    AntiCheatDetection detection = new AntiCheatDetection(playerName, messageText, type, count);
                    synchronized (detections) {
                        detections.add(detection);
                        if (detections.size() > 100) {
                            detections.remove(0);
                        }
                    }

                    ScS.LOGGER.info("✅ Обработано: {} - {}", playerName, type);
                } else {
                    ScS.LOGGER.warn("❌ НЕ УДАЛОСЬ ИЗВЛЕЧЬ ИМЯ ИГРОКА");
                    ScS.LOGGER.warn("Сообщение: '{}'", messageText);
                }
            }
        } catch (Exception e) {
            ScS.LOGGER.error("❌ Ошибка обработки сообщения чата: {}", e.getMessage());
        }
    }

    private static String parsePlayerName(String message) {
        try {
            // Убираем временные метки и системные префиксы
            String cleaned = message.replaceAll("\\[\\d{2}[а-я]{3}\\.\\d{4}\\s+\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\]", "")
                    .replaceAll("\\[Render thread/INFO\\]", "")
                    .replaceAll("\\[net\\.minecraft\\.client\\.gui\\.components\\.ChatComponent/\\]:", "")
                    .replaceAll("\\[System\\]", "")
                    .replaceAll("\\[CHAT\\]", "")
                    .trim();

            // Используем regex для точного извлечения
            Matcher matcher = ANTICHEAT_PATTERN.matcher(cleaned);
            if (matcher.find()) {
                String playerName = matcher.group(1);
                if (playerName != null && playerName.matches("[A-Za-z0-9_]{3,16}")) {
                    return playerName;
                }
            }

            // Альтернативный поиск по словам
            String[] words = cleaned.split("\\s+");
            for (int i = 0; i < words.length; i++) {
                if (words[i].contains("Анти-Чит") && i + 1 < words.length) {
                    String candidate = words[i + 1];
                    if (candidate.matches("[A-Za-z0-9_]{3,16}")) {
                        return candidate;
                    }
                }
            }
        } catch (Exception e) {
            ScS.LOGGER.error("Ошибка парсинга имени игрока: {}", e.getMessage());
        }

        return null;
    }

    private static ViolationType parseViolationType(String message) {
        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("move") || lowerMessage.contains("abnormally")) return ViolationType.MOVE;
        if (lowerMessage.contains("click")) return ViolationType.CLICK;
        if (lowerMessage.contains("fastplace") || lowerMessage.contains("fastbreak")) return ViolationType.FASTPLACE;
        if (lowerMessage.contains("hitbox") || lowerMessage.contains("reach")) return ViolationType.HITBOX;
        if (lowerMessage.contains("velocity")) return ViolationType.VELOCITY;
        if (lowerMessage.contains("killaura") || lowerMessage.contains("combat hacks")) return ViolationType.KILLAURA;
        if (lowerMessage.contains("delay")) return ViolationType.DELAY;
        if (lowerMessage.contains("autobot") || lowerMessage.contains("automatic robots")) return ViolationType.AUTOBOT;

        return ViolationType.MOVE;
    }

    private static int parseCount(String message) {
        try {
            if (message.contains("#")) {
                String[] parts = message.split("#");
                if (parts.length > 1) {
                    String numberPart = parts[1].trim().split("\\s+")[0];
                    return Integer.parseInt(numberPart);
                }
            }
        } catch (Exception e) {
            ScS.LOGGER.warn("Ошибка парсинга счетчика: {}", e.getMessage());
        }
        return 1;
    }

    private static void createButtons(String playerName, ViolationType type) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) {
                ScS.LOGGER.warn("❌ Игрок не найден, пропускаем создание кнопок");
                return;
            }

            // Создаем красивое сообщение с кнопками
            MutableText message = Text.literal("§8§l▬▬▬ §c§lSCS ANTICHEAT §8§l▬▬▬");
            message.append(Text.literal("\n§7Игрок: §f" + playerName));
            message.append(Text.literal("\n§7Нарушение: §e" + type.getDisplayName()));

            // Кнопки действий
            MutableText buttons = Text.literal("\n§8§l▬ §fДействия §8§l▬\n");

            buttons.append(Text.literal("§a[ЗАМОРОЗИТЬ]")
                    .styled(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/freezing " + playerName))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Text.literal("§aЗаморозить игрока для проверки")))));

            buttons.append(Text.literal(" §b[СПЕК]")
                    .styled(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/matrix spectate " + playerName))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Text.literal("§bСледить за игроком")))));

            buttons.append(Text.literal(" §e[АКТИВНОСТЬ]")
                    .styled(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/playeractivity " + playerName))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Text.literal("§eПроверить активность игрока")))));

            buttons.append(Text.literal(" §6[ИСТОРИЯ]")
                    .styled(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/freezinghistory " + playerName))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Text.literal("§6История проверок игрока")))));

            message.append(buttons);
            message.append(Text.literal("\n§8§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));

            client.player.sendMessage(message, false);
            ScS.LOGGER.info("✅ КНОПКИ ОТПРАВЛЕНЫ для игрока: {}", playerName);

        } catch (Exception e) {
            ScS.LOGGER.error("❌ ОШИБКА СОЗДАНИЯ КНОПОК: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    // Геттеры
    public static List<AntiCheatDetection> getDetections() {
        synchronized (detections) {
            return new ArrayList<>(detections);
        }
    }

    public static void clearDetections() {
        synchronized (detections) {
            detections.clear();
            ScS.LOGGER.info("🗑️ Список нарушений очищен");
        }
    }

    public static int getTotalDetections() {
        synchronized (detections) {
            return detections.size();
        }
    }

    public static long getCriticalDetections() {
        synchronized (detections) {
            return detections.stream()
                    .filter(d -> d.getType().isCritical())
                    .count();
        }
    }
}