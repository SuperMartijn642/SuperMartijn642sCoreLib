package com.supermartijn642.core.render;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created 17/11/2021 by SuperMartijn642
 * <p>
 * Fired right after blocks are rendered and the {@link net.minecraftforge.client.event.DrawBlockHighlightEvent} is fired.
 */
public class RenderWorldEvent extends Event {

    private final float partialTicks;

    public RenderWorldEvent(float partialTicks){
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks(){
        return this.partialTicks;
    }
}
