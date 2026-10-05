package com.dark.enchantmentprogression.client;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class InventoryButtonInstaller {
    private InventoryButtonInstaller() {}

    public static void install(Screen parent, int width, int height) {
        int guiLeft=(width-176)/2;
        int guiTop=(height-166)/2;
        ProgressionInventoryButton button=new ProgressionInventoryButton(parent,guiLeft,guiTop);
        button.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("enchantment_progression.open")));
        Screens.getWidgets(parent).add(button);
    }
}
