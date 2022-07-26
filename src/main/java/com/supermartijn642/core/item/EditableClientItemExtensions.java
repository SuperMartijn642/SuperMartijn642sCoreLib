package com.supermartijn642.core.item;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public class EditableClientItemExtensions implements IClientItemExtensions {

    protected BlockEntityWithoutLevelRenderer customRenderer;

    public void setCustomRenderer(BlockEntityWithoutLevelRenderer customRenderer){
        this.customRenderer = customRenderer;
    }

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer(){
        return this.customRenderer == null ? IClientItemExtensions.super.getCustomRenderer() : this.customRenderer;
    }
}
