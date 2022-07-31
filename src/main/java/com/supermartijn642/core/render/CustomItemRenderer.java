package com.supermartijn642.core.render;

import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;

/**
 * Created 27/07/2022 by SuperMartijn642
 */
public interface CustomItemRenderer {

    static ItemStackTileEntityRenderer of(CustomItemRenderer customRenderer){
        return new ItemStackTileEntityRenderer() {
            @Override
            public void renderByItem(ItemStack itemStack){
                customRenderer.render(itemStack);
            }
        };
    }

    /**
     * Renders the given item stack.
     */
    void render(ItemStack itemStack);
}
