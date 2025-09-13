package com.scs.anticheat;

import com.scs.ScS;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyHandler {
    private static KeyBinding toggleListKey;
    private static KeyBinding toggleMenuKey;
    private static KeyBinding clearEntriesKey;

    public static void init() {
        ScS.LOGGER.info("=== ИНИЦИАЛИЗАЦИЯ KeyHandler ===");

        try {
            // Регистрируем горячие клавиши
            toggleListKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.scs.toggle_list", // ID ключа для локализации
                    InputUtil.Type.KEYBOARD,
                    GLFW.GLFW_KEY_F8,
                    "category.scs.general" // Категория для группировки в настройках
            ));

            toggleMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.scs.toggle_menu",
                    InputUtil.Type.KEYBOARD,
                    GLFW.GLFW_KEY_F9,
                    "category.scs.general"
            ));

            clearEntriesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.scs.clear_entries",
                    InputUtil.Type.KEYBOARD,
                    GLFW.GLFW_KEY_F10,
                    "category.scs.general"
            ));

            // Регистрируем обработчик тиков клиента
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                try {
                    // Проверяем что клиент не на паузе и игрок существует
                    if (client.isPaused() || client.player == null) {
                        return;
                    }

                    // Обработка F8 - показать/скрыть список
                    while (toggleListKey.wasPressed()) {
                        AntiCheatGui.toggleList();
                        ScS.LOGGER.info("🎮 F8 нажато - переключение HUD списка");
                    }

                    // Обработка F9 - открыть/закрыть меню
                    while (toggleMenuKey.wasPressed()) {
                        AntiCheatGui.toggleMenu();
                        ScS.LOGGER.info("🎮 F9 нажато - переключение меню");
                    }

                    // Обработка F10 - очистить записи
                    while (clearEntriesKey.wasPressed()) {
                        int previousCount = AntiCheatDetector.getTotalDetections();
                        AntiCheatDetector.clearDetections();
                        ScS.LOGGER.info("🎮 F10 нажато - очищено {} записей", previousCount);

                        // Уведомляем игрока
                        if (client.player != null) {
                            client.player.sendMessage(
                                    net.minecraft.text.Text.literal("§8[§cSCS§8] §7Очищено §e" + previousCount + "§7 записей античита"),
                                    true
                            );
                        }
                    }
                } catch (Exception e) {
                    ScS.LOGGER.error("Ошибка обработки горячих клавиш: {}", e.getMessage());
                }
            });

            ScS.LOGGER.info("✅ KeyHandler зарегистрирован!");
            ScS.LOGGER.info("🎮 Горячие клавиши активны:");
            ScS.LOGGER.info("   F8  - показать/скрыть HUD список");
            ScS.LOGGER.info("   F9  - открыть/закрыть меню статистики");
            ScS.LOGGER.info("   F10 - очистить все записи");
        } catch (Exception e) {
            ScS.LOGGER.error("❌ Ошибка регистрации KeyHandler: {}", e.getMessage());
        }
    }

    // Геттеры для получения состояния клавиш (для других компонентов)
    public static KeyBinding getToggleListKey() {
        return toggleListKey;
    }

    public static KeyBinding getToggleMenuKey() {
        return toggleMenuKey;
    }

    public static KeyBinding getClearEntriesKey() {
        return clearEntriesKey;
    }
}