package com.supermartijn642.core.gui.widget;

import net.minecraft.inventory.container.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created 23/07/2022 by SuperMartijn642
 */
public abstract class BlockEntityBaseContainerWidget<T extends TileEntity, C extends Container> extends ObjectBaseContainerWidget<T,C> {

    protected final World blockEntityLevel;
    protected final BlockPos blockEntityPos;

    public BlockEntityBaseContainerWidget(int x, int y, int width, int height, World blockEntityLevel, BlockPos blockEntityPos){
        super(x, y, width, height);
        this.blockEntityLevel = blockEntityLevel;
        this.blockEntityPos = blockEntityPos;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T getObject(T oldObject){
        TileEntity entity = this.blockEntityLevel.getBlockEntity(this.blockEntityPos);
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
