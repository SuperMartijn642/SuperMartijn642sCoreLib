package com.supermartijn642.core.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.tileentity.TileEntity;

/**
 * Created 27/07/2022 by SuperMartijn642
 */
public interface CustomBlockEntityRenderer<T extends TileEntity> {

    static <T extends TileEntity> TileEntityRenderer<T> of(CustomBlockEntityRenderer<T> customRenderer){
        return new TileEntityRenderer<T>() {

            @Override
            public void render(T entity, double x, double y, double z, float partialTicks, int combinedOverlay){
                GlStateManager.pushMatrix();
                GlStateManager.translated(x, y, z);
                customRenderer.render(entity, partialTicks, combinedOverlay);
                GlStateManager.popMatrix();
            }
        };
    }

    /**
     * Renders the given block entity.
     */
    void render(T entity, float partialTicks, int combinedOverlay);
}
