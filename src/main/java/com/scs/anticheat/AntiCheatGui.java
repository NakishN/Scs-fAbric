package com.scs.anticheat;

import com.scs.ScS;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AntiCheatGui {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private static boolean listVisible = true;
    private static boolean menuOpen = false;

    public static void init() {
        ScS.LOGGER.info("=== ИНИЦИАЛИЗАЦИЯ AntiCheatGui ===");

        try {
            // Регистрируем рендеринг HUD
            HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
                try {
                    if (CLIENT.options.hideGui || CLIENT.getDebugOverlay().showDebugScreen()) {
                        return;
                    }

                    if (listVisible) {
                        renderAntiCheatList(drawContext);
                    }

                    if (menuOpen) {
                        renderMenu(drawContext);
                    }
                } catch (Exception e) {
                    ScS.LOGGER.error("Ошибка рендеринга HUD: {}", e.getMessage());
                }
            });
            ScS.LOGGER.info("✅ AntiCheatGui зарегистрирован!");
        } catch (Exception e) {
            ScS.LOGGER.error("❌ Ошибка регистрации AntiCheatGui: {}", e.getMessage());
        }
    }

    public static void toggleList() {
        listVisible = !listVisible;
        ScS.LOGGER.info("Список античита: {}", listVisible ? "показан" : "скрыт");
    }

    public static void toggleMenu() {
        menuOpen = !menuOpen;
        if (!menuOpen && CLIENT.currentScreen != null) {
            CLIENT.setScreen(null);
        }
        ScS.LOGGER.info("Меню античита: {}", menuOpen ? "открыто" : "закрыто");
    }

    public static boolean isListVisible() {
        return listVisible;
    }

    public static boolean isMenuOpen() {
        return menuOpen;
    }

    private static void renderAntiCheatList(DrawContext context) {
        try {
            List<AntiCheatDetection> detections = AntiCheatDetector.getDetections();

            if (detections.isEmpty()) {
                return;
            }

            int screenWidth = CLIENT.getWindow().getScaledWidth();
            int screenHeight = CLIENT.getWindow().getScaledHeight();

            // Позиция в правом верхнем углу
            int startX = screenWidth - 320;
            int startY = 10;
            int maxHeight = screenHeight - 100;

            // Рассчитываем количество записей
            int maxEntries = Math.min(15, (maxHeight - 60) / 12);
            int actualEntries = Math.min(detections.size(), maxEntries);

            // Размер фона
            int bgHeight = actualEntries * 12 + 55;

            // Фон для списка с прозрачностью
            context.fill(startX - 5, startY - 5, startX + 315, startY + bgHeight, 0x88000000);

            // Заголовок
            context.drawTextWithShadow(CLIENT.textRenderer,
                    "§l🛡 ScS Enhanced Monitor",
                    startX, startY, 0xFFFFFF);

            // Статистика
            int totalDetections = AntiCheatDetector.getTotalDetections();
            long criticalDetections = AntiCheatDetector.getCriticalDetections();

            String stats = String.format("Всего: %d | Критичных: %d", totalDetections, criticalDetections);
            context.drawText(CLIENT.textRenderer, stats, startX, startY + 12, 0xCCCCCC, false);

            // Разделитель
            context.drawText(CLIENT.textRenderer, "─────────────────────────", startX, startY + 22, 0x666666, false);

            int y = startY + 32;

            // Показываем последние записи (самые новые сверху)
            List<AntiCheatDetection> recentDetections = detections.size() > maxEntries
                    ? detections.subList(detections.size() - maxEntries, detections.size())
                    : detections;

            // Переворачиваем список чтобы новые были сверху
            for (int i = recentDetections.size() - 1; i >= 0; i--) {
                AntiCheatDetection detection = recentDetections.get(i);

                String timeStr = TIME_FORMAT.format(new Date(detection.getTimestamp()));
                int color = detection.getType().getColor();

                String text = String.format("[%s] %s - %s",
                        timeStr,
                        detection.getPlayerName(),
                        detection.getType().getDisplayName()
                );

                if (detection.getCount() > 1) {
                    text += " #" + detection.getCount();
                }

                // Обрезаем текст если он слишком длинный
                if (CLIENT.textRenderer.getWidth(text) > 300) {
                    text = CLIENT.textRenderer.trimToWidth(text, 280) + "...";
                }

                context.drawText(CLIENT.textRenderer, text, startX, y, color, false);
                y += 12;
            }

            // Разделитель
            context.drawText(CLIENT.textRenderer, "─────────────────────────", startX, y + 2, 0x666666, false);

            // Показываем информацию о горячих клавишах внизу
            String hotkeys = "F8 - HUD | F9 - Menu | F10 - Clear";
            context.drawText(CLIENT.textRenderer, hotkeys, startX, y + 12, 0x888888, false);
        } catch (Exception e) {
            ScS.LOGGER.error("Ошибка рендеринга списка античита: {}", e.getMessage());
        }
    }

    private static void renderMenu(DrawContext context) {
        try {
            int screenWidth = CLIENT.getWindow().getScaledWidth();
            int screenHeight = CLIENT.getWindow().getScaledHeight();

            int menuWidth = 450;
            int menuHeight = 350;
            int menuX = (screenWidth - menuWidth) / 2;
            int menuY = (screenHeight - menuHeight) / 2;

            // Затемнение фона
            context.fill(0, 0, screenWidth, screenHeight, 0x80000000);

            // Фон меню
            context.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, 0xEE000000);

            // Заголовок
            context.drawCenteredTextWithShadow(CLIENT.textRenderer,
                    "§l📊 SCS Enhanced - Статистика",
                    menuX + menuWidth / 2, menuY + 10, 0xFFFFFF);

            List<AntiCheatDetection> detections = AntiCheatDetector.getDetections();
            int totalDetections = detections.size();

            int yPos = menuY + 35;
            int leftCol = menuX + 15;
            int rightCol = menuX + 240;

            // Общая статистика
            context.drawText(CLIENT.textRenderer, "§eОбщая статистика:", leftCol, yPos, 0xFFFFFF, false);
            yPos += 15;

            context.drawText(CLIENT.textRenderer, "• Всего обнаружений: " + totalDetections, leftCol, yPos, 0xFFFFFF, false);
            yPos += 12;

            // Последний час
            long oneHourAgo = System.currentTimeMillis() - 3600000;
            long recentDetections = detections.stream()
                    .filter(d -> d.getTimestamp() > oneHourAgo)
                    .count();

            context.drawText(CLIENT.textRenderer, "• За последний час: " + recentDetections, leftCol, yPos, 0xFFFFFF, false);
            yPos += 12;

            // Критичных нарушений
            long criticalCount = detections.stream()
                    .filter(d -> d.getType().isCritical())
                    .count();

            context.drawText(CLIENT.textRenderer, "• Критичных нарушений: " + criticalCount, leftCol, yPos, 0xFF5555, false);
            yPos += 12;

            // Уникальных игроков
            long uniquePlayers = detections.stream()
                    .map(AntiCheatDetection::getPlayerName)
                    .distinct()
                    .count();

            context.drawText(CLIENT.textRenderer, "• Уникальных игроков: " + uniquePlayers, leftCol, yPos, 0xFFFFFF, false);
            yPos += 20;

            // Топ нарушители
            context.drawText(CLIENT.textRenderer, "§c🎯 Топ нарушители:", leftCol, yPos, 0xFFFFFF, false);
            yPos += 15;

            Map<String, Long> playerCounts = detections.stream()
                    .collect(Collectors.groupingBy(AntiCheatDetection::getPlayerName, Collectors.counting()));

            int topYPos = yPos;
            playerCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .forEach(entry -> {
                        String text = String.format("• %s - %d нарушений", entry.getKey(), entry.getValue());
                        context.drawText(CLIENT.textRenderer, text, leftCol, topYPos +
                                (int)(playerCounts.entrySet().stream()
                                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                                        .limit(5)
                                        .collect(Collectors.toList())
                                        .indexOf(entry)) * 12, 0xFFFF55, false);
                    });

            // Статистика по типам (правая колонка)
            context.drawText(CLIENT.textRenderer, "§a📈 По типам нарушений:", rightCol, menuY + 50, 0xFFFFFF, false);
            int rightYPos = menuY + 70;

            Map<ViolationType, Long> typeCounts = detections.stream()
                    .collect(Collectors.groupingBy(AntiCheatDetection::getType, Collectors.counting()));

            for (ViolationType type : ViolationType.values()) {
                long count = typeCounts.getOrDefault(type, 0L);
                if (count > 0) {
                    double percentage = totalDetections > 0 ? (count * 100.0 / totalDetections) : 0;
                    String text = String.format("• %s: %d (%.1f%%)", type.getDisplayName(), count, percentage);
                    context.drawText(CLIENT.textRenderer, text, rightCol, rightYPos, type.getColor(), false);
                    rightYPos += 12;
                }
            }

            // Настройки и инструкции внизу
            yPos = menuY + menuHeight - 80;
            context.drawText(CLIENT.textRenderer, "§7⚙ Горячие клавиши:", leftCol, yPos, 0xFFFFFF, false);
            yPos += 15;

            context.drawText(CLIENT.textRenderer, "• F8 - Показать/скрыть HUD список", leftCol, yPos, 0xCCCCCC, false);
            yPos += 12;

            context.drawText(CLIENT.textRenderer, "• F9 - Открыть/закрыть это меню", leftCol, yPos, 0xCCCCCC, false);
            yPos += 12;

            context.drawText(CLIENT.textRenderer, "• F10 - Очистить всю историю", leftCol, yPos, 0xCCCCCC, false);
            yPos += 12;

            context.drawText(CLIENT.textRenderer, "• ESC - Закрыть меню", leftCol, yPos, 0xCCCCCC, false);
        } catch (Exception e) {
            ScS.LOGGER.error("Ошибка рендеринга меню: {}", e.getMessage());
        }
    }
}