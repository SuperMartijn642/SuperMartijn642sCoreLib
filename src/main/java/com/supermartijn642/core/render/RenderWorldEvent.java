package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.SubmitNodeCollector;

import java.util.function.Consumer;

/**
 * Created 17/11/2021 by SuperMartijn642
 * <p>
 * Fired right after blocks are rendered and the block highlight is drawn.
 */
public class RenderWorldEvent {

    public static Event<Consumer<RenderWorldEvent>> EVENT = EventFactory.createArrayBacked(
        Consumer.class,
        listeners -> event -> {
            for(Consumer<RenderWorldEvent> listener : listeners)
                listener.accept(event);
        }
    );

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
