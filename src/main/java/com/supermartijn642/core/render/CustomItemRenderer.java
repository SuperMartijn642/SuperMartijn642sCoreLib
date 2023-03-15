package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Created 27/07/2022 by SuperMartijn642
 */
public interface CustomItemRenderer {

    static BlockEntityWithoutLevelRenderer of(CustomItemRenderer customRenderer){
        return new BlockEntityWithoutLevelRenderer(null, null) {
            @Override
            public void renderByItem(ItemStack itemStack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay){
                customRenderer.render(itemStack, transformType, poseStack, bufferSource, combinedLight, combinedOverlay);
            }
        };
    }

    /**
     * Renders the given item stack.
     */
    void render(ItemStack itemStack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay);
}
