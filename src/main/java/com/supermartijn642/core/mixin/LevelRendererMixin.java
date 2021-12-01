package com.supermartijn642.core.mixin;

import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 17/11/2021 by SuperMartijn642
 */
@Mixin(EntityRenderer.class)
public class LevelRendererMixin {

    private float partialTicks;

    @ModifyVariable(method = "updateCameraAndRender", at = @At("HEAD"))
    public float modifyPartialTicks(float partialTicks){
        this.partialTicks = partialTicks;
        return partialTicks;
    }

    @Inject(method = "renderWorldPass(IFJ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GlStateManager;enableBlend()V"),
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;shouldRender()Z"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;disableBlend()V")
        ))
    public void renderLevel(CallbackInfo ci){
        MinecraftForge.EVENT_BUS.post(new RenderWorldEvent(this.partialTicks));
    }

}
