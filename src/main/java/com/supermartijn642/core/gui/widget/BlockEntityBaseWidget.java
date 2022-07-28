package com.supermartijn642.core.gui.widget;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created 23/07/2022 by SuperMartijn642
 */
public abstract class BlockEntityBaseWidget<T extends TileEntity> extends ObjectBaseWidget<T> {

    protected final World blockEntityLevel;
    protected final BlockPos blockEntityPos;

    public BlockEntityBaseWidget(int x, int y, int width, int height, World blockEntityLevel, BlockPos blockEntityPos){
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
