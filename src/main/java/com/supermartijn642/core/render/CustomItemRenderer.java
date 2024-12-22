package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Created 27/07/2022 by SuperMartijn642
 */
public interface CustomItemRenderer {

    static SpecialModelRenderer<?> toSpecialModelRenderer(CustomItemRenderer customRenderer){
        return new SpecialModelRenderer<ItemStack>() {
            @Override
            public void render(@Nullable ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay, boolean hasFoil){
                customRenderer.render(stack, transformType, poseStack, bufferSource, combinedLight, combinedOverlay);
            }

            @Override
            public @Nullable ItemStack extractArgument(ItemStack stack){
                return stack;
            }
        };
    }

    /**
     * Renders the given item stack.
     */
    void render(ItemStack itemStack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay);
}
