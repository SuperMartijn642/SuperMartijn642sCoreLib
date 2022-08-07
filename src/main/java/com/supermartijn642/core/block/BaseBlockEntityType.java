package com.supermartijn642.core.block;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Created 06/08/2022 by SuperMartijn642
 */
public final class BaseBlockEntityType<T extends BaseBlockEntity> {

    /**
     * Creates a new block entity type.
     * @param entitySupplier used to create new block entities when a world is loaded
     * @param validBlocks    blocks which may hold the block entity
     */
    public static <T extends BaseBlockEntity> BaseBlockEntityType<T> create(Supplier<T> entitySupplier, Block... validBlocks){
        return new BaseBlockEntityType<>(entitySupplier, ImmutableSet.copyOf(validBlocks));
    }

    private final Supplier<T> entitySupplier;
    private final Set<Block> validBlocks;
    final Set<Class<?>> blockEntityClasses = new HashSet<>();

    private BaseBlockEntityType(Supplier<T> entitySupplier, Set<Block> validBlocks){
        this.entitySupplier = entitySupplier;
        this.validBlocks = validBlocks;
    }

    public boolean isValid(IBlockState state){
        return this.validBlocks.contains(state.getBlock());
    }

    public T createBlockEntity(){
        T entity = this.entitySupplier.get();
        this.blockEntityClasses.add(entity.getClass());
        return this.entitySupplier.get();
    }

    /**
     * Do not use!
     */
    @Deprecated
    public boolean containsClass(Class<?> clazz){
        return this.blockEntityClasses.contains(clazz);
    }
}
