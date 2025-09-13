package com.scs.anticheat;

import com.scs.Scs;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Text;

public class AntiCheatDetector {
    private static final List<AntiCheatDetection> detections = new ArrayList<>();

    public static void init() {
        Scs.LOGGER.info("=== ИНИЦИАЛИЗАЦИЯ AntiCheatDetector ===");

        // Регистрируем обработчик сообщений чата
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) { // Игнорируем overlay сообщения
                handleChatMessage(message);
            }
        });

        Scs.LOGGER.info("✅ AntiCheatDetector зарегистрирован!");
    }

    private static void handleChatMessage(Text message) {
        String messageText = message.getString();

        // МАКСИМАЛЬНОЕ ЛОГИРОВАНИЕ
        Scs.LOGGER.info("=== ПОЛУЧЕНО СООБЩЕНИЕ ЧАТА ===");
        Scs.LOGGER.info("Текст: '{}'", messageText);
        Scs.LOGGER.info("Содержит 'Анти-Чит': {}", messageText.contains("Анти-Чит"));

        // Проверяем сообщения античита
        if (messageText.contains("Анти-Чит") || messageText.contains("[Анти-Чит]")) {
            Scs.LOGGER.info("🚨 НАЙДЕНО СООБЩЕНИЕ АНТИЧИТА!");

            String playerName = parsePlayerName(messageText);
            Scs.LOGGER.info("Имя игрока: '{}'", playerName);

            if (playerName != null && !playerName.isEmpty()) {
                ViolationType type = parseViolationType(messageText);
                int count = parseCount(messageText);

                Scs.LOGGER.info("Тип: {}, Счет: {}", type, count);

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

                Scs.LOGGER.info("✅ Обработано: {} - {}", playerName, type);
            } else {
                Scs.LOGGER.error("❌ НЕ УДАЛОСЬ ИЗВЛЕЧЬ ИМЯ ИГРОКА");
                Scs.LOGGER.error("Сообщение: '{}'", messageText);
            }
        }
    }

    private static String parsePlayerName(String message) {
        Scs.LOGGER.info("Парсинг имени из: '{}'", message);

        try {
            // Убираем временные метки
            String cleaned = message.replaceAll("\\[\\d{2}:\\d{2}:\\d{2}\\]", "")
                    .replaceAll("\\[System\\]", "")
                    .replaceAll("\\[CHAT\\]", "")
                    .trim();

            Scs.LOGGER.info("Очищено: '{}'", cleaned);

            String[] words = cleaned.split("\\s+");
            Scs.LOGGER.info("Слова: {}", java.util.Arrays.toString(words));

            // Ищем после [Анти-Чит]
            for (int i = 0; i < words.length; i++) {
                if (words[i].contains("Анти-Чит") && i + 1 < words.length) {
                    String candidate = words[i + 1];
                    if (candidate.matches("[A-Za-z0-9_]{3,16}")) {
                        Scs.LOGGER.info("✅ Найден ник: '{}'", candidate);
                        return candidate;
                    }
                }
            }

            // Альтернативный поиск
            for (String word : words) {
                if (word.matches("[A-Za-z0-9_]{3,16}") &&
                        !word.equals("tried") &&
                        !word.equals("move") &&
                        !word.contains("[") &&
                        !word.contains("]")) {
                    Scs.LOGGER.info("🎯 Альтернативный ник: '{}'", word);
                    return word;
                }
            }

        } catch (Exception e) {
            Scs.LOGGER.error("Ошибка парсинга: {}", e.getMessage());
        }

        return null;
    }

    private static ViolationType parseViolationType(String message) {
        if (message.contains("Move") || message.contains("move")) return ViolationType.MOVE;
        if (message.contains("Click") || message.contains("click")) return ViolationType.CLICK;
        if (message.contains("FastPlace") || message.contains("FastBreak")) return ViolationType.FASTPLACE;
        if (message.contains("HitBox") || message.contains("hitbox")) return ViolationType.HITBOX;
        if (message.contains("Velocity") || message.contains("velocity")) return ViolationType.VELOCITY;
        if (message.contains("KillAura") || message.contains("killaura")) return ViolationType.KILLAURA;
        if (message.contains("Delay") || message.contains("delay")) return ViolationType.DELAY;
        if (message.contains("AutoBot") || message.contains("autobot") || message.contains("robots")) return ViolationType.AUTOBOT;

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
            Scs.LOGGER.warn("Ошибка парсинга счетчика: {}", e.getMessage());
        }
        return 1;
    }

    private static void createButtons(String playerName, ViolationType type) {
        Scs.LOGGER.info("🛠️ СОЗДАЕМ КНОПКИ ДЛЯ: '{}'", playerName);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            Scs.LOGGER.error("❌ Игрок не найден");
            return;
        }

        try {
            // Сначала тестовое сообщение
            client.player.sendMessage(
                    Text.literal("§c§lTEST: Обнаружен " + playerName + " - " + type.name()),
                    false
            );
            Scs.LOGGER.info("✅ Тестовое сообщение отправлено");

            // Затем кнопки
            MutableText message = Text.literal("§8§l▬▬▬ SCS ANTICHEAT ▬▬▬");
            message.append(Text.literal("\n§c§lИгрок: " + playerName));
            message.append(Text.literal("\n§7Нарушение: §e" + type.name()));

            // Кнопки
            MutableText buttons = Text.literal("\n");

            buttons.append(Text.literal("§a[ЗАМОРОЗИТЬ]")
                    .styled(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/freezing " + playerName))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Text.literal("Заморозить для проверки")))));

            buttons.append(Text.literal(" §b[СПЕК]")
                    .styled(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/matrix spectate " + playerName))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Text.literal("Следить за игроком")))));

            buttons.append(Text.literal(" §e[АКТИВНОСТЬ]")
                    .styled(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/playeractivity " + playerName))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Text.literal("Проверить активность")))));

            buttons.append(Text.literal(" §6[ИСТОРИЯ]")
                    .styled(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/freezinghistory " + playerName))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Text.literal("История проверок")))));

            message.append(buttons);
            message.append(Text.literal("\n§8§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));

            client.player.sendMessage(message, false);
            Scs.LOGGER.info("✅ КНОПКИ ОТПРАВЛЕНЫ!");

        } catch (Exception e) {
            Scs.LOGGER.error("❌ ОШИБКА СОЗДАНИЯ КНОПОК: {}", e.getMessage());
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
        }
    }
}