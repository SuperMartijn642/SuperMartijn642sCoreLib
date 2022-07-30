package com.supermartijn642.core.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.tileentity.TileEntity;

/**
 * Created 27/07/2022 by SuperMartijn642
 */
public interface CustomBlockEntityRenderer<T extends TileEntity> {

    static <T extends TileEntity> TileEntityRenderer<T> of(CustomBlockEntityRenderer<T> customRenderer){
        return new TileEntityRenderer<T>(null) {
            @Override
            public void render(T entity, float partialTicks, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay){
                customRenderer.render(entity, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);
            }
        };
    }

    /**
     * Renders the given block entity.
     */
    void render(T entity, float partialTicks, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay);
}
