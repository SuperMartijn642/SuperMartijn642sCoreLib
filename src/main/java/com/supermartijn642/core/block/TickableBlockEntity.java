package com.supermartijn642.core.block;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created 17/07/2022 by SuperMartijn642
 */
public interface TickableBlockEntity {

    /**
     * Called once per tick. Should be registered in {@link EntityBlock#getTicker(Level, BlockState, BlockEntityType)}
     */
    void update();
}
