package com.scs.anticheat;

import com.scs.Scs;
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
        Scs.LOGGER.info("=== ИНИЦИАЛИЗАЦИЯ KeyHandler ===");

        // Регистрируем горячие клавиши
        toggleListKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.scs.toggle_list",
                InputUtil.Type.KEYBOARD,
                GLFW.GLFW_KEY_F8,
                "category.scs.general"
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
            // Обработка F8 - показать/скрыть список
            while (toggleListKey.wasPressed()) {
                AntiCheatGui.toggleList();
            }

            // Обработка F9 - открыть/закрыть меню
            while (toggleMenuKey.wasPressed()) {
                AntiCheatGui.toggleMenu();
            }

            // Обработка F10 - очистить записи
            while (clearEntriesKey.wasPressed()) {
                AntiCheatDetector.clearDetections();
            }
        });

        Scs.LOGGER.info("✅ KeyHandler зарегистрирован!");
    }
}