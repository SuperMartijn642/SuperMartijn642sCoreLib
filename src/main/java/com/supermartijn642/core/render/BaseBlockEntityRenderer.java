package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public class BaseBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {

    @Override
    public void render(T entity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay){

    }
}
