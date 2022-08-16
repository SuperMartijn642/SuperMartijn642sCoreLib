package com.supermartijn642.core.block;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import java.util.Set;
import java.util.function.Supplier;

/**
 * Created 06/08/2022 by SuperMartijn642
 */
public final class BaseBlockEntityType<T extends TileEntity> extends TileEntityType<T> {

    /**
     * Creates a new block entity type.
     * @param entitySupplier used to create new block entities when a world is loaded
     * @param validBlocks    blocks which may hold the block entity
     */
    public static <T extends BaseBlockEntity> BaseBlockEntityType<T> create(Supplier<T> entitySupplier, Block... validBlocks){
        return new BaseBlockEntityType<>(entitySupplier, ImmutableSet.copyOf(validBlocks));
    }

    private BaseBlockEntityType(Supplier<T> entitySupplier, Set<Block> validBlocks){
        super(entitySupplier, validBlocks, null);
    }
}
