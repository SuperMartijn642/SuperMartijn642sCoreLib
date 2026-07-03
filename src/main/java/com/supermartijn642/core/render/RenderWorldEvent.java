package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created 17/11/2021 by SuperMartijn642
 * <p>
 * Fired right after blocks are rendered and the block highlight is drawn.
 */
public class RenderWorldEvent {

    @ApiStatus.Internal
    public static RenderStateDataKey<List<BiConsumer<PoseStack,SubmitNodeCollector>>> DATA_KEY = RenderStateDataKey.create();

    public static Event<Consumer<RenderWorldEvent>> EVENT = EventFactory.createArrayBacked(
        Consumer.class,
        listeners -> event -> {
            for(Consumer<RenderWorldEvent> listener : listeners)
                listener.accept(event);
        }
    );

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
