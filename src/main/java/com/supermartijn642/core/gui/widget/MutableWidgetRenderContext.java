package com.supermartijn642.core.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Created 05/06/2023 by SuperMartijn642
 */
public final class MutableWidgetRenderContext implements WidgetRenderContext {

    public static MutableWidgetRenderContext create(){
        return new MutableWidgetRenderContext();
    }

    private GuiGraphics guiGraphics;
    private float partialTicks;

    private MutableWidgetRenderContext(){
    }

    public void update(GuiGraphics guiGraphics, float partialTicks){
        this.guiGraphics = guiGraphics;
        this.partialTicks = partialTicks;
    }

    public void update(GuiGraphics guiGraphics){
        this.update(guiGraphics, ClientUtils.getPartialTicks());
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
}
