package com.supermartijn642.core.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.client.renderer.WorldRenderer;
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
@Mixin(WorldRenderer.class)
public class LevelRendererMixin {

    private MatrixStack poseStack;
    private float partialTicks;

    @ModifyVariable(method = "updateCameraAndRender", at = @At("HEAD"))
    public MatrixStack modifyPoseStack(MatrixStack poseStack){
        this.poseStack = poseStack;
        return poseStack;
    }

    @ModifyVariable(method = "updateCameraAndRender", at = @At("HEAD"))
    public float modifyPartialTicks(float partialTicks){
        this.partialTicks = partialTicks;
        return partialTicks;
    }

    @Inject(method = "updateCameraAndRender",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;pushMatrix()V"),
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockRayTraceResult;getPos()Lnet/minecraft/util/math/BlockPos;"),
            to = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;multMatrix(Lnet/minecraft/util/math/vector/Matrix4f;)V")
        ))
    public void renderLevel(CallbackInfo ci){
        MinecraftForge.EVENT_BUS.post(new RenderWorldEvent(this.poseStack, this.partialTicks));
    }

}
