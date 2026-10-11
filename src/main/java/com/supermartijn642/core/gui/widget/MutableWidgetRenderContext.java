package com.supermartijn642.core.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.function.IntSupplier;

/**
 * Created 05/06/2023 by SuperMartijn642
 */
public final class MutableWidgetRenderContext implements WidgetRenderContext {

    public static MutableWidgetRenderContext create(IntSupplier widgetX, IntSupplier widgetY){
        return new MutableWidgetRenderContext(widgetX, widgetY);
    }

    private final IntSupplier widgetOffsetX, widgetOffsetY;
    private GuiGraphics guiGraphics;
    private float partialTicks;
    private int windowWidth, windowHeight;

    private MutableWidgetRenderContext(IntSupplier widgetOffsetX, IntSupplier widgetOffsetY){
        this.widgetOffsetX = widgetOffsetX;
        this.widgetOffsetY = widgetOffsetY;
    }

    public void update(GuiGraphics guiGraphics, float partialTicks, int windowWidth, int windowHeight){
        this.guiGraphics = guiGraphics;
        this.partialTicks = partialTicks;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

    public void update(GuiGraphics guiGraphics, int windowWidth, int windowHeight){
        this.update(guiGraphics, ClientUtils.getPartialTicks(), windowWidth, windowHeight);
    }

    public GuiGraphics guiGraphics(){
        return this.guiGraphics;
    }

    @Override
    public PoseStack poseStack(){
        return this.guiGraphics.pose;
    }

    @Override
    public MultiBufferSource.BufferSource buffers(){
        return this.guiGraphics.bufferSource;
    }

    @Override
    public float partialTicks(){
        return this.partialTicks;
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
