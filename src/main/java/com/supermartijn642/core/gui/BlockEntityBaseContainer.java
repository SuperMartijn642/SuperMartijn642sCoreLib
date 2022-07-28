package com.supermartijn642.core.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class BlockEntityBaseContainer<T extends TileEntity> extends ObjectBaseContainer<T> {

    protected final World tileWorld;
    protected final BlockPos tilePos;

    public BlockEntityBaseContainer(ContainerType<?> type, int id, PlayerEntity player, World tileWorld, BlockPos tilePos){
        super(type, id, player);
        this.tileWorld = tileWorld;
        this.tilePos = tilePos;
    }

    public BlockEntityBaseContainer(ContainerType<?> type, int id, PlayerEntity player, BlockPos tilePos){
        this(type, id, player, player.level, tilePos);
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn){
        return super.stillValid(playerIn);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T getObject(){
        TileEntity tile = this.tileWorld.getBlockEntity(this.tilePos);

        if(tile == null)
            return null;

        try{
            return (T)tile;
        }catch(ClassCastException ignore){}
        return null;
    }
}
