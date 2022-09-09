package com.supermartijn642.core.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class BlockEntityBaseContainer<T extends TileEntity> extends ObjectBaseContainer<T> {

    protected final World blockEntityLevel;
    protected final BlockPos blockEntityPos;

    public BlockEntityBaseContainer(BaseContainerType<?> type, EntityPlayer player, World blockEntityLevel, BlockPos blockEntityPos){
        super(type, player);
        this.blockEntityLevel = blockEntityLevel;
        this.blockEntityPos = blockEntityPos;
    }

    public BlockEntityBaseContainer(BaseContainerType<?> type, EntityPlayer player, BlockPos blockEntityPos){
        this(type, player, player.world, blockEntityPos);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T getObject(T oldObject){
        TileEntity entity = this.blockEntityLevel.getTileEntity(this.blockEntityPos);
        if(entity == null)
            return null;

        try{
            return (T)entity;
        }catch(ClassCastException ignore){}
        return null;
    }

    @Override
    protected boolean validateObject(T object){
        return object != null && !object.isInvalid() && object.getWorld() != null;
    }
}
