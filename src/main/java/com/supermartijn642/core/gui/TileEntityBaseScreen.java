package com.supermartijn642.core.gui;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class TileEntityBaseScreen<T extends TileEntity> extends ObjectBaseScreen<T> {
    protected TileEntityBaseScreen(ITextComponent title){
        super(title);
    }

    @Override
    protected T getObject(){
        return this.getTileEntity();
    }

    protected abstract T getTileEntity();
}
