package ru.wars333.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import ru.wars333.client.module.ModuleManager;
import ru.wars333.client.util.AccountManager;

public class Wars333Client implements ClientModInitializer {
    public static final String NAME = "wars333";

    @Override
    public void onInitializeClient() {
        AccountManager.load();
        ModuleManager.init();
        ClientTickEvents.END_CLIENT_TICK.register(ModuleManager::tick);
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> ModuleManager.hud(ctx));
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ModuleManager.shutdown());
    }
}
