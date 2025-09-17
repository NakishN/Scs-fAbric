package com.scs.client;

import com.scs.ScS;
import net.fabricmc.api.ClientModInitializer;

/**
 * Дополнительная точка входа для клиентских компонентов
 * Этот класс НЕ должен быть указан в fabric.mod.json как entrypoint,
 * так как основная инициализация происходит в ScS.java
 */
public class ScSClient {

    /**
     * Инициализация клиентских компонентов
     * Вызывается из основного класса ScS
     */
    public static void initializeClient() {
        ScS.LOGGER.info("📦 ScSClient дополнительные компоненты инициализированы");

        // Здесь можно добавить дополнительную клиентскую логику
        // которая должна выполняться после основной инициализации

        registerClientEvents();
        initializeClientResources();
    }

    private static void registerClientEvents() {
        // Дополнительные клиентские события
        ScS.LOGGER.debug("🔧 Регистрация дополнительных клиентских событий");
    }

    private static void initializeClientResources() {
        // Инициализация клиентских ресурсов
        ScS.LOGGER.debug("📁 Загрузка клиентских ресурсов");
    }
}