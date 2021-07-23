package com.supermartijn642.core.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class TileEntityBaseContainerScreen<T extends BlockEntity, X extends TileEntityBaseContainer<T>> extends ObjectBaseContainerScreen<T,X> {

    public TileEntityBaseContainerScreen(X screenContainer, Component title){
        super(screenContainer, title);
    }
}
