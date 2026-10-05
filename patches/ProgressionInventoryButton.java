package com.dark.enchantmentprogression.client;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Crimson enchanted-book control positioned from the vanilla recipe-book widget every frame. */
public final class ProgressionInventoryButton extends AbstractWidget {
    private static final Identifier ICON=Identifier.fromNamespaceAndPath("enchantment_progression","textures/gui/progression_book.png");
    private static final Identifier HOVER_ICON=Identifier.fromNamespaceAndPath("enchantment_progression","textures/gui/progression_book_highlighted.png");
    private final Screen parent;
    private final int fallbackRecipeX;
    private final int fallbackRecipeY;

    public ProgressionInventoryButton(Screen parent,int guiLeft,int guiTop){
        super(guiLeft+124,guiTop+61,20,18,Component.translatable("enchantment_progression.open"));
        this.parent=parent;
        this.fallbackRecipeX=guiLeft+104;
        this.fallbackRecipeY=guiTop+61;
    }

    private void followRecipeButton(){
        AbstractWidget best=null; int bestScore=Integer.MAX_VALUE;
        for(var listener: Screens.getWidgets(parent)){
            if(listener==this || !(listener instanceof AbstractWidget w)) continue;
            int ww=w.getWidth(),hh=w.getHeight();
            if(ww<18||ww>22||hh<18||hh>22) continue;
            int score=Math.abs(w.getY()-fallbackRecipeY)*5+Math.abs(w.getX()-fallbackRecipeX);
            String n=w.getClass().getSimpleName().toLowerCase();
            if(n.contains("recipe"))score-=1000;
            if(score<bestScore){best=w;bestScore=score;}
        }
        if(best!=null){setX(best.getX()+20);setY(best.getY());}
    }

    @Override protected void extractWidgetRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){
        followRecipeButton();
        // Vanilla recipe-book frame and silhouette, recolored crimson.
        int x=getX(),y=getY();
        g.blit(RenderPipelines.GUI_TEXTURED,isHoveredOrFocused()?HOVER_ICON:ICON,x,y,0,0,20,18,20,18);
    }

    @Override public void onClick(MouseButtonEvent event,boolean doubleClick){
        Minecraft.getInstance().gui.setScreen(new ProgressionScreen(parent));
    }

    @Override protected void updateWidgetNarration(NarrationElementOutput output){
        defaultButtonNarrationText(output);
    }
}
