package com.supermartijn642.core.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraftforge.eventbus.api.Event;

/**
 * Created 17/11/2021 by SuperMartijn642
 *
 * Fired right after blocks are rendered and the {@link net.minecraftforge.client.event.DrawHighlightEvent} is fired.
 */
public class RenderWorldEvent extends Event {

    private final MatrixStack poseStack;
    private final float partialTicks;

    public RenderWorldEvent(MatrixStack poseStack, float partialTicks){
        this.poseStack = poseStack;
        this.partialTicks = partialTicks;
    }

    public MatrixStack getPoseStack(){
        return this.poseStack;
    }

    public float getPartialTicks(){
        return this.partialTicks;
    }
}
