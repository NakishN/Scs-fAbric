package com.scs.anticheat;

import com.scs.Scs;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AntiCheatGui {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private static boolean listVisible = true;
    private static boolean menuOpen = false;

    public static void init() {
        Scs.LOGGER.info("=== ИНИЦИАЛИЗАЦИЯ AntiCheatGui ===");

        // Регистрируем рендеринг HUD
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (CLIENT.options.hideGui || CLIENT.getDebugOverlay().showDebugScreen()) {
                return;
            }

            if (listVisible) {
                renderAntiCheatList(drawContext);
            }

            if (menuOpen) {
                renderMenu(drawContext);
            }
        });

        Scs.LOGGER.info("✅ AntiCheatGui зарегистрирован!");
    }

    public static void toggleList() {
        listVisible = !listVisible;
        Scs.LOGGER.info("Список античита: {}", listVisible ? "показан" : "скрыт");
    }

    public static void toggleMenu() {
        menuOpen = !menuOpen;
        Scs.LOGGER.info("Меню античита: {}", menuOpen ? "открыто" : "закрыто");
    }

    public static boolean isListVisible() {
        return listVisible;
    }

    public static boolean isMenuOpen() {
        return menuOpen;
    }

    private static void renderAntiCheatList(DrawContext context) {
        List<AntiCheatDetection> detections = AntiCheatDetector.getDetections();

        if (detections.isEmpty()) {
            return;
        }

        int screenWidth = CLIENT.getWindow().getScaledWidth();
        int screenHeight = CLIENT.getWindow().getScaledHeight();

        // Позиция в правом верхнем углу
        int startX = screenWidth - 300;
        int startY = 10;
        int maxHeight = screenHeight - 50;

        // Фон для списка
        context.fill(startX - 5, startY - 5, startX + 295, Math.min(startY + detections.size() * 12 + 25, maxHeight), 0x80000000);

        // Заголовок
        context.drawText(CLIENT.textRenderer, "§lСписок нарушений", startX, startY, 0xFFFFFF, true);

        int y = startY + 15;
        int maxEntries = (maxHeight - startY - 30) / 12;

        // Показываем последние записи (самые новые сверху)
        List<AntiCheatDetection> recentDetections = detections.size() > maxEntries
                ? detections.subList(detections.size() - maxEntries, detections.size())
                : detections;

        // Переворачиваем список чтобы новые были сверху
        for (int i = recentDetections.size() - 1; i >= 0; i--) {
            AntiCheatDetection detection = recentDetections.get(i);

            String timeStr = TIME_FORMAT.format(new Date(detection.getTimestamp()));
            int color = detection.getType().getColor();

            String text = String.format("§7[%s] §r%s §7(%s)",
                    timeStr,
                    detection.getPlayerName(),
                    detection.getType().name()
            );

            if (detection.getCount() > 1) {
                text += " §c#" + detection.getCount();
            }

            context.drawText(CLIENT.textRenderer, text, startX, y, color, false);
            y += 12;
        }

        // Показываем информацию о горячих клавишах внизу
        String hotkeys = "§7F8 - скрыть/показать | F9 - меню";
        context.drawText(CLIENT.textRenderer, hotkeys, startX,
                Math.min(y + 5, maxHeight - 10), 0x888888, false);
    }

    private static void renderMenu(DrawContext context) {
        int screenWidth = CLIENT.getWindow().getScaledWidth();
        int screenHeight = CLIENT.getWindow().getScaledHeight();

        int menuWidth = 400;
        int menuHeight = 300;
        int menuX = (screenWidth - menuWidth) / 2;
        int menuY = (screenHeight - menuHeight) / 2;

        // Затемнение фона
        context.fill(0, 0, screenWidth, screenHeight, 0x80000000);

        // Фон меню
        context.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, 0xCC000000);

        // Рамка
        context.fill(menuX, menuY, menuX + menuWidth, menuY + 2, 0xFFFFFFFF); // Верх
        context.fill(menuX, menuY + menuHeight - 2, menuX + menuWidth, menuY + menuHeight, 0xFFFFFFFF); // Низ
        context.fill(menuX, menuY, menuX + 2, menuY + menuHeight, 0xFFFFFFFF); // Лево
        context.fill(menuX + menuWidth - 2, menuY, menuX + menuWidth, menuY + menuHeight, 0xFFFFFFFF); // Право

        // Заголовок
        context.drawCenteredTextWithShadow(CLIENT.textRenderer, "§lМеню античита", menuX + menuWidth / 2, menuY + 10, 0xFFFFFF);

        // Статистика
        List<AntiCheatDetection> detections = AntiCheatDetector.getDetections();
        int totalDetections = detections.size();

        int yPos = menuY + 40;

        context.drawText(CLIENT.textRenderer, "Всего обнаружений: " + totalDetections, menuX + 10, yPos, 0xFFFFFF, false);
        yPos += 20;

        // Статистика по типам нарушений
        for (ViolationType type : ViolationType.values()) {
            long count = detections.stream()
                    .filter(d -> d.getType() == type)
                    .count();

            if (count > 0) {
                String text = String.format("%s: %d", type.name(), count);
                context.drawText(CLIENT.textRenderer, text, menuX + 10, yPos, type.getColor(), false);
                yPos += 15;
            }
        }

        // Инструкции
        yPos = menuY + menuHeight - 80;
        context.drawText(CLIENT.textRenderer, "§7Горячие клавиши:", menuX + 10, yPos, 0xAAAAAA, false);
        yPos += 15;

        context.drawText(CLIENT.textRenderer, "§7F8 - Показать/скрыть список", menuX + 10, yPos, 0xAAAAAA, false);
        yPos += 12;

        context.drawText(CLIENT.textRenderer, "§7F9 - Открыть/закрыть меню", menuX + 10, yPos, 0xAAAAAA, false);
        yPos += 12;

        context.drawText(CLIENT.textRenderer, "§7ESC - Закрыть меню", menuX + 10, yPos, 0xAAAAAA, false);
    }
}