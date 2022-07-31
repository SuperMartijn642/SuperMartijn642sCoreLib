package com.supermartijn642.core.mixin;

import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 17/11/2021 by SuperMartijn642
 */
@Mixin(GameRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "render(FJ)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;enableBlend()V"),
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;shouldRender()Z"),
            to = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;disableBlend()V")
        ))
    public void renderLevel(float partialTicks, long currentTime, CallbackInfo ci){
        MinecraftForge.EVENT_BUS.post(new RenderWorldEvent(partialTicks));
    }
}
