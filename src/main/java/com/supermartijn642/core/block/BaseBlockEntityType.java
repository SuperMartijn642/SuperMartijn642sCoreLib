package com.supermartijn642.core.block;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.function.BiFunction;

/**
 * Created 06/08/2022 by SuperMartijn642
 */
public final class BaseBlockEntityType<T extends BlockEntity> extends BlockEntityType<T> {

    /**
     * Creates a new block entity type.
     * @param entitySupplier used to create new block entities when a world is loaded
     * @param validBlocks    blocks which may hold the block entity
     */
    public static <T extends BaseBlockEntity> BaseBlockEntityType<T> create(BiFunction<BlockPos,BlockState,T> entitySupplier, Block... validBlocks){
        return new BaseBlockEntityType<>(entitySupplier, ImmutableSet.copyOf(validBlocks));
    }

    private BaseBlockEntityType(BiFunction<BlockPos,BlockState,T> entitySupplier, Set<Block> validBlocks){
        super(entitySupplier::apply, validBlocks);
    }
}
