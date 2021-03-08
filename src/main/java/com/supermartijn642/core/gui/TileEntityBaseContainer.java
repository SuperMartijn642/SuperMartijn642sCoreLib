package com.supermartijn642.core.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class TileEntityBaseContainer<T extends TileEntity> extends ObjectBaseContainer<T> {

    protected final World tileWorld;
    protected final BlockPos tilePos;

    public TileEntityBaseContainer(EntityPlayer player, World tileWorld, BlockPos tilePos){
        super(player);
        this.tileWorld = tileWorld;
        this.tilePos = tilePos;
    }

    public TileEntityBaseContainer(EntityPlayer player, BlockPos tilePos){
        this(player, player.world, tilePos);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn){
        return super.canInteractWith(playerIn);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T getObject(){
        TileEntity tile = this.tileWorld.getTileEntity(this.tilePos);

        if(tile == null)
            return null;

        try{
            return (T)tile;
        }catch(ClassCastException ignore){}
        return null;
    }
}
