package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.bus.EventBus;
import net.minecraftforge.eventbus.api.event.MutableEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created 17/11/2021 by SuperMartijn642
 * <p>
 * Fired right after blocks are rendered and the {@link net.minecraftforge.client.event.RenderHighlightEvent.Block} is fired.
 */
public class RenderWorldEvent extends MutableEvent {

    @SuppressWarnings("NullableProblems")
    public static final EventBus<RenderWorldEvent> EVENT_BUS = EventBus.create(RenderWorldEvent.class);

    private final float partialTicks;
    private final Camera camera;
    private final LevelRenderState levelRenderState;
    private final Consumer<BiConsumer<PoseStack,SubmitNodeCollector>> collector;

    public RenderWorldEvent(float partialTicks, Camera camera, LevelRenderState levelRenderState, Consumer<BiConsumer<PoseStack,SubmitNodeCollector>> collector){
        this.partialTicks = partialTicks;
        this.camera = camera;
        this.levelRenderState = levelRenderState;
        this.collector = collector;
    }

    public float getPartialTicks(){
        return this.partialTicks;
    }

    public Camera getCamera(){
        return this.camera;
    }

    public Vec3 getCameraPos(){
        return this.camera.position();
    }

    public LevelRenderState getLevelRenderState(){
        return this.levelRenderState;
    }

    public void submitFeatures(BiConsumer<PoseStack,SubmitNodeCollector> submitter){
        this.collector.accept(submitter);
    }
}
