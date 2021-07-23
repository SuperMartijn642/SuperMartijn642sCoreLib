package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class TileEntityBaseScreen<T extends BlockEntity> extends ObjectBaseScreen<T> {

    protected final BlockPos tilePos;

    protected TileEntityBaseScreen(Component title, BlockPos tilePos){
        super(title);
        this.tilePos = tilePos;
    }

    protected TileEntityBaseScreen(Component title){
        this(title, null);
    }

    @Override
    protected T getObject(){
        return this.getTileEntity();
    }

    @SuppressWarnings("unchecked")
    protected T getTileEntity(){
        BlockEntity tile = ClientUtils.getWorld().getBlockEntity(this.tilePos);

        if(tile == null)
            return null;

        try{
            return (T)tile;
        }catch(ClassCastException ignore){}
        return null;
    }
}
