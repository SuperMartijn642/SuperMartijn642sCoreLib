package com.supermartijn642.core.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Created 05/06/2023 by SuperMartijn642
 */
public final class MutableWidgetRenderContext implements WidgetRenderContext {

    public static MutableWidgetRenderContext create(){
        return new MutableWidgetRenderContext();
    }

    private GuiGraphicsExtractor guiGraphics;
    private float partialTicks;
    private Font font;
    private Minecraft minecraft;

    private MutableWidgetRenderContext(){
    }

    public void update(GuiGraphicsExtractor guiGraphics, float partialTicks, Font font, Minecraft minecraft){
        this.guiGraphics = guiGraphics;
        this.partialTicks = partialTicks;
        this.font = font;
        this.minecraft = minecraft;
    }

    @Override
    public GuiGraphicsExtractor guiGraphics(){
        return this.guiGraphics;
    }

    @Override
    public float partialTicks(){
        return this.partialTicks;
    }

    @Override
    public Font font(){
        return this.font;
    }

    @Override
    public Minecraft minecraft(){
        return this.minecraft;
    }
}
