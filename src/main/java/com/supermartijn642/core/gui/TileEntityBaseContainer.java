package com.supermartijn642.core.gui;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class TileEntityBaseContainer<T extends BlockEntity> extends ObjectBaseContainer<T> {

    protected final Level tileWorld;
    protected final BlockPos tilePos;

    public TileEntityBaseContainer(MenuType<?> type, int id, Player player, Level tileWorld, BlockPos tilePos){
        super(type, id, player);
        this.tileWorld = tileWorld;
        this.tilePos = tilePos;
    }

    public TileEntityBaseContainer(MenuType<?> type, int id, Player player, BlockPos tilePos){
        this(type, id, player, player.level, tilePos);
    }

    @Override
    public boolean stillValid(Player playerIn){
        return super.stillValid(playerIn);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T getObject(){
        BlockEntity tile = this.tileWorld.getBlockEntity(this.tilePos);

        if(tile == null)
            return null;

        try{
            return (T)tile;
        }catch(ClassCastException ignore){}
        return null;
    }
}
