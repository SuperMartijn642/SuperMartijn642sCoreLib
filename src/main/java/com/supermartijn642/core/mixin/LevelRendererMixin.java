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
        // For some reason this method has one fewer parameters outside of dev, so specify only the method name and not the signature
        method = "method_62213",
        at = @At("TAIL"),
        require = 1
    )
    private void renderLevel(CallbackInfo ci){
        RenderWorldEvent.EVENT.invoker().accept(new RenderWorldEvent(POSE_STACK, ClientUtils.getPartialTicks()));
        if(!POSE_STACK.isEmpty())
            throw new IllegalStateException("Pose stack was not cleared properly during RenderWorldEvent!");
    }
}
