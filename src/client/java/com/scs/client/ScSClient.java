package com.scs.client;

import com.scs.ScS;
import net.fabricmc.api.ClientModInitializer;

public class ScSClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ScS.LOGGER.info("📦 ScSClient инициализирован");
    }
}