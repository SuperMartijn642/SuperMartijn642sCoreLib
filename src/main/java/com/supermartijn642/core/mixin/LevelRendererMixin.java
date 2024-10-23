package com.supermartijn642.core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 17/11/2021 by SuperMartijn642
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Unique
    private static final PoseStack POSE_STACK = new PoseStack();

    @Inject(
        method = "method_62213(Lnet/minecraft/client/renderer/FogParameters;Lcom/mojang/blaze3d/resource/ResourceHandle;Lcom/mojang/blaze3d/resource/ResourceHandle;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V",
        at = @At("TAIL")
    )
    private void renderLevel(CallbackInfo ci){
        RenderWorldEvent.EVENT.invoker().accept(new RenderWorldEvent(POSE_STACK, ClientUtils.getPartialTicks()));
        if(!POSE_STACK.clear())
            throw new IllegalStateException("Pose stack was not cleared properly during RenderWorldEvent!");
    }
}
