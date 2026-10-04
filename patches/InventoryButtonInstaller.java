package com.dark.enchantmentprogression.client;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class InventoryButtonInstaller {
    private InventoryButtonInstaller() {}
    public static void install(Screen parent, int width, int height) {
        Button button = Button.builder(Component.literal("✦"), b -> Minecraft.getInstance().gui.setScreen(new ProgressionScreen(parent)))
                .bounds(width / 2 + 80, height / 2 - 82, 22, 20).build();
        button.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("enchantment_progression.open")));
        Screens.getWidgets(parent).add(button);
    }
}
