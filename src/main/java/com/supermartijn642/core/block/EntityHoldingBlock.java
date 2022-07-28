package com.supermartijn642.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created 17/07/2022 by SuperMartijn642
 */
public interface EntityHoldingBlock extends EntityBlock {

    BlockEntity createNewBlockEntity(BlockPos pos, BlockState state);

    @Override
    default BlockEntity newBlockEntity(BlockPos pos, BlockState state){
        return this.createNewBlockEntity(pos, state);
    }

    @Override
    default <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType){
        return (level2, pos, state2, entity) -> {
            if(entity instanceof TickableBlockEntity)
                ((TickableBlockEntity)entity).update();
        };
    }
}
