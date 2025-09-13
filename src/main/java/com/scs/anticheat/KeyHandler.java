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
        });

        Scs.LOGGER.info("✅ KeyHandler зарегистрирован!");
    }
}