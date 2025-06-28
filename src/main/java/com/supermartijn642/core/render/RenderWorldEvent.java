package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.eventbus.api.bus.EventBus;
import net.minecraftforge.eventbus.api.event.RecordEvent;

/**
 * Created 17/11/2021 by SuperMartijn642
 * <p>
 * Fired right after blocks are rendered and the {@link net.minecraftforge.client.event.RenderHighlightEvent.Block} is fired.
 */
public class RenderWorldEvent implements RecordEvent {

    @SuppressWarnings("NullableProblems")
    public static final EventBus<RenderWorldEvent> EVENT_BUS = EventBus.create(RenderWorldEvent.class);

    private final PoseStack poseStack;
    private final float partialTicks;

    public RenderWorldEvent(PoseStack poseStack, float partialTicks){
        this.poseStack = poseStack;
        this.partialTicks = partialTicks;
    }

    public PoseStack getPoseStack(){
        return this.poseStack;
    }

    public float getPartialTicks(){
        return this.partialTicks;
    }
}
