package com.dark.enchantmentprogression.client;

import com.dark.enchantmentprogression.network.UpgradeResult;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

public final class EnchantmentProgressionClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof InventoryScreen) InventoryButtonInstaller.install(screen, width, height);
        });
        ClientPlayNetworking.registerGlobalReceiver(UpgradeResult.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().gui.screen() instanceof ProgressionScreen screen) screen.acceptResult(payload);
            });
        });
    }
}
