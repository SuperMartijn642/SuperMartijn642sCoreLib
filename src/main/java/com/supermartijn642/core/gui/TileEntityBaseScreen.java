package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class TileEntityBaseScreen<T extends TileEntity> extends ObjectBaseScreen<T> {

    protected final BlockPos tilePos;

    protected TileEntityBaseScreen(ITextComponent title, BlockPos tilePos){
        super(title);
        this.tilePos = tilePos;
    }

    protected TileEntityBaseScreen(ITextComponent title){
        this(title, null);
    }

    @Override
    protected T getObject(){
        return this.getTileEntity();
    }

    @SuppressWarnings("unchecked")
    protected T getTileEntity(){
        TileEntity tile = ClientUtils.getWorld().getTileEntity(this.tilePos);

        if(tile == null)
            return null;

        try{
            return (T)tile;
        }catch(ClassCastException ignore){}
        return null;
    }
}
