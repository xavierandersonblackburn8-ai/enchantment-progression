package com.dark.enchantmentprogression.client;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class InventoryButtonInstaller {
    private InventoryButtonInstaller() {}

    public static void install(Screen parent, int width, int height) {
        // Vanilla inventory is 176x166 logical pixels.  The recipe-book control sits at
        // guiLeft+104, guiTop+61; progression belongs immediately to its right.
        // These coordinates intentionally follow the inventory origin rather than the
        // old top-right screen anchor, matching the placement approved from the screenshot.
        int guiLeft = (width - 176) / 2;
        int guiTop = (height - 166) / 2;
        Button button = Button.builder(Component.literal("▣"), b ->
                Minecraft.getInstance().gui.setScreen(new ProgressionScreen(parent)))
                .bounds(guiLeft + 124, guiTop + 61, 20, 20)
                .build();
        button.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.translatable("enchantment_progression.open")));
        Screens.getWidgets(parent).add(button);
    }
}
