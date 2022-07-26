package com.supermartijn642.core.gui.widget;

import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Created 23/07/2022 by SuperMartijn642
 */
public abstract class BlockEntityBaseContainerWidget<T extends BlockEntity, C extends AbstractContainerMenu> extends ObjectBaseContainerWidget<T,C> {

    protected final Level blockEntityLevel;
    protected final BlockPos blockEntityPos;

    public BlockEntityBaseContainerWidget(int x, int y, int width, int height, Level blockEntityLevel, BlockPos blockEntityPos){
        super(x, y, width, height);
        this.blockEntityLevel = blockEntityLevel;
        this.blockEntityPos = blockEntityPos;
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
