package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Created 27/07/2022 by SuperMartijn642
 */
public interface CustomItemRenderer {

    static BuiltinItemRendererRegistry.DynamicItemRenderer of(CustomItemRenderer customRenderer){
        return customRenderer::render;
    }

    /**
     * Renders the given item stack.
     */
    void render(ItemStack itemStack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay);
}
