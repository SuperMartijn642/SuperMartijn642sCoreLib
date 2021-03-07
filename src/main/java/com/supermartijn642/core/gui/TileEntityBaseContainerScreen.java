package com.supermartijn642.core.gui;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class TileEntityBaseContainerScreen<T extends TileEntity, X extends TileEntityBaseContainer<T>> extends ObjectBaseContainerScreen<T,X> {
    public TileEntityBaseContainerScreen(X screenContainer, ITextComponent title){
        super(screenContainer, title);
    }
}
