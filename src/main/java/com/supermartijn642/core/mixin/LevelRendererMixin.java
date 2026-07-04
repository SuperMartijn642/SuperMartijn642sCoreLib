package com.supermartijn642.core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Created 17/11/2021 by SuperMartijn642
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Unique
    private static final PoseStack POSE_STACK = new PoseStack();

    @Inject(
        method = "submitFeatures(Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeCollector;Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;finalizeGizmoCollection()V",
            shift = At.Shift.BEFORE
        )
    )
    private void renderLevel(LevelRenderState levelRenderState, SubmitNodeCollector output, boolean renderOutline, CallbackInfo ci){
        List<BiConsumer<PoseStack,SubmitNodeCollector>> submitters = levelRenderState.getRenderDataOrDefault(RenderWorldEvent.DATA_KEY, List.of());
        try{
            for(BiConsumer<PoseStack,SubmitNodeCollector> submitter : submitters){
                submitter.accept(POSE_STACK, output);
                if(!POSE_STACK.isEmpty())
                    throw new IllegalStateException("Pose stack was not cleared properly during RenderWorldEvent!");
            }
        }catch(Exception e){
            throw new RuntimeException("Encountered an exception from RenderWorldEvent submitters!", e);
        }
    }
}
