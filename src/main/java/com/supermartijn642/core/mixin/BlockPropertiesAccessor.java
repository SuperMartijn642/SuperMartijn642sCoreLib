package com.supermartijn642.core.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Supplier;

/**
 * Created 07/05/2023 by SuperMartijn642
 */
@Mixin(BlockBehaviour.Properties.class)
public interface BlockPropertiesAccessor {

    @Accessor(value = "lootTableSupplier", remap = false)
    Supplier<ResourceKey<LootTable>> getLootTableSupplier();

    @Accessor(value = "lootTableSupplier", remap = false)
    void setLootTableSupplier(Supplier<ResourceKey<LootTable>> supplier);
}
