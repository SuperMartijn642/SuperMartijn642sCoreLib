package com.supermartijn642.core.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.IntSupplier;

/**
 * Created 05/06/2023 by SuperMartijn642
 */
public final class MutableWidgetRenderContext implements WidgetRenderContext {

    public static MutableWidgetRenderContext create(IntSupplier widgetX, IntSupplier widgetY){
        return new MutableWidgetRenderContext(widgetX, widgetY);
    }

    private final IntSupplier widgetOffsetX, widgetOffsetY;
    private GuiGraphicsExtractor guiGraphics;
    private float partialTicks;
    private Font font;
    private Minecraft minecraft;
    private int windowWidth, windowHeight;

    private MutableWidgetRenderContext(IntSupplier widgetOffsetX, IntSupplier widgetOffsetY){
        this.widgetOffsetX = widgetOffsetX;
        this.widgetOffsetY = widgetOffsetY;
    }

    public void update(GuiGraphicsExtractor guiGraphics, float partialTicks, Font font, Minecraft minecraft, int windowWidth, int windowHeight){
        this.guiGraphics = guiGraphics;
        this.partialTicks = partialTicks;
        this.font = font;
        this.minecraft = minecraft;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
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

    @Override
    public int windowWidgetXOffset(){
        return this.widgetOffsetX.getAsInt();
    }

    @Override
    public int windowWidgetYOffset(){
        return this.widgetOffsetY.getAsInt();
    }

    @Override
    public int windowWidth(){
        return this.windowWidth;
    }

    @Override
    public int windowHeight(){
        return this.windowHeight;
    }
}
