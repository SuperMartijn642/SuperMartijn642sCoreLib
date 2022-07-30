package com.supermartijn642.core.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.render.CustomItemRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 29/07/2022 by SuperMartijn642
 */
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "net/minecraft/client/renderer/tileentity/ItemStackTileEntityRenderer.renderByItem (Lnet/minecraft/item/ItemStack;Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;II)V",
            shift = At.Shift.BEFORE
        ),
        cancellable = true
    )
    private void render(ItemStack stack, ItemCameraTransforms.TransformType transformType, boolean leftHand, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay, IBakedModel model, CallbackInfo ci){
        if(model.isCustomRenderer()){
            ItemStackTileEntityRenderer renderer = stack.getItem().getItemStackTileEntityRenderer();
            if(renderer instanceof CustomItemRenderer.CustomItemRendererHolder){
                ((CustomItemRenderer.CustomItemRendererHolder)renderer).getCustomItemRenderer().render(stack, transformType, poseStack, bufferSource, combinedLight, combinedOverlay);
                poseStack.popPose();
                ci.cancel();
            }
        }
    }
}
