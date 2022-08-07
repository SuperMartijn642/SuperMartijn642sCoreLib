package com.supermartijn642.core.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

/**
 * Created 27/07/2022 by SuperMartijn642
 */
public interface CustomBlockEntityRenderer<T extends TileEntity> {

    static <T extends TileEntity> TileEntitySpecialRenderer<T> of(CustomBlockEntityRenderer<T> customRenderer){
        return new TileEntitySpecialRenderer<T>() {

            @Override
            public void render(T entity, double x, double y, double z, float partialTicks, int combinedOverlay, float alpha){
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                customRenderer.render(entity, partialTicks, combinedOverlay, alpha);
                GlStateManager.popMatrix();
            }
        };
    }

    /**
     * Renders the given block entity.
     */
    void render(T entity, float partialTicks, int combinedOverlay, float alpha);
}
