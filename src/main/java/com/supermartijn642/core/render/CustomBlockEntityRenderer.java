package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Created 27/07/2022 by SuperMartijn642
 */
public interface CustomBlockEntityRenderer<T extends BlockEntity> {

    static <T extends BlockEntity> BlockEntityRenderer<T> of(CustomBlockEntityRenderer<T> customRenderer){
        return customRenderer::render;
    }

    /**
     * Renders the given block entity.
     */
    void render(T entity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay);
}
