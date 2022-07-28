package com.supermartijn642.core.block;

import net.minecraft.tileentity.ITickableTileEntity;

/**
 * Created 17/07/2022 by SuperMartijn642
 */
public interface TickableBlockEntity extends ITickableTileEntity {

    /**
     * Called once per tick.
     */
    void update();

    @Override
    default void tick(){
        this.update();
    }
}
