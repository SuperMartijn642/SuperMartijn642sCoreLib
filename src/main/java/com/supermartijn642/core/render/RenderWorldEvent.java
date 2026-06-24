package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraftforge.eventbus.api.bus.EventBus;
import net.minecraftforge.eventbus.api.event.MutableEvent;

/**
 * Created 17/11/2021 by SuperMartijn642
 * <p>
 * Fired right after blocks are rendered and the {@link net.minecraftforge.client.event.RenderHighlightEvent.Block} is fired.
 */
public class RenderWorldEvent extends MutableEvent {

    @SuppressWarnings("NullableProblems")
    public static final EventBus<RenderWorldEvent> EVENT_BUS = EventBus.create(RenderWorldEvent.class);

    private final PoseStack poseStack;
    private final float partialTicks;
    private final SubmitNodeCollector submitter;

    public RenderWorldEvent(PoseStack poseStack, float partialTicks, SubmitNodeCollector submitter){
        this.poseStack = poseStack;
        this.partialTicks = partialTicks;
        this.submitter = submitter;
    }

    public PoseStack getPoseStack(){
        return this.poseStack;
    }

    public float getPartialTicks(){
        return this.partialTicks;
    }

    public SubmitNodeCollector getSubmitter(){
        return this.submitter;
    }
}
