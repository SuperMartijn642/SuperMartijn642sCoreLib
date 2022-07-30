package com.supermartijn642.core.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;

/**
 * Created 27/07/2022 by SuperMartijn642
 */
public interface CustomItemRenderer {

    static ItemStackTileEntityRenderer of(CustomItemRenderer customRenderer){
        return new CustomItemRendererHolder() {

            @Override
            public CustomItemRenderer getCustomItemRenderer(){
                return customRenderer;
            }

            @Override
            public void renderByItem(ItemStack itemStack, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay){
                customRenderer.render(itemStack, ItemCameraTransforms.TransformType.GUI, poseStack, bufferSource, combinedLight, combinedOverlay);
            }
        };
    }

    /**
     * Renders the given item stack.
     */
    void render(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay);


    /**
     * Do not use!
     * Used to get the {@link com.supermartijn642.core.render.CustomItemRenderer} from the item stack tile entity renderer instances.
     */
    @Deprecated
    abstract class CustomItemRendererHolder extends ItemStackTileEntityRenderer {

        public abstract CustomItemRenderer getCustomItemRenderer();
    }
}
