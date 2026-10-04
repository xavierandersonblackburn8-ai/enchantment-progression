package com.dark.enchantmentprogression.client;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class InventoryButtonInstaller {
    private InventoryButtonInstaller() {}
    public static void install(Screen parent, int width, int height) {
        Button button = Button.builder(Component.translatable("enchantment_progression.open"), b -> Minecraft.getInstance().gui.setScreen(new ProgressionScreen(parent)))
                .bounds(width / 2 + 92, height / 2 - 82, 96, 20).build();
        Screens.getWidgets(parent).add(button);
    }
}
