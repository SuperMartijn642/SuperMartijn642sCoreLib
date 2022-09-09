package com.supermartijn642.core.gui;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class BlockEntityBaseContainer<T extends BlockEntity> extends ObjectBaseContainer<T> {

    protected final Level blockEntityLevel;
    protected final BlockPos blockEntityPos;

    public BlockEntityBaseContainer(BaseContainerType<?> type, Player player, Level blockEntityLevel, BlockPos blockEntityPos){
        super(type, player);
        this.blockEntityLevel = blockEntityLevel;
        this.blockEntityPos = blockEntityPos;
    }

    public BlockEntityBaseContainer(BaseContainerType<?> type, Player player, BlockPos blockEntityPos){
        this(type, player, player.level, blockEntityPos);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T getObject(T oldObject){
        BlockEntity entity = this.blockEntityLevel.getBlockEntity(this.blockEntityPos);
        if(entity == null)
            return null;

        try{
            return (T)entity;
        }catch(ClassCastException ignore){}
        return null;
    }

    @Override
    protected boolean validateObject(T object){
        return object != null && !object.isRemoved();
    }
}
