package com.supermartijn642.core.item;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.IItemRenderProperties;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public class EditableClientItemExtensions implements IItemRenderProperties {

    protected BlockEntityWithoutLevelRenderer customRenderer;

    public void setCustomRenderer(BlockEntityWithoutLevelRenderer customRenderer){
        this.customRenderer = customRenderer;
    }

    @Override
    public BlockEntityWithoutLevelRenderer getItemStackRenderer(){
        return this.customRenderer == null ? IItemRenderProperties.super.getItemStackRenderer() : this.customRenderer;
    }
}
