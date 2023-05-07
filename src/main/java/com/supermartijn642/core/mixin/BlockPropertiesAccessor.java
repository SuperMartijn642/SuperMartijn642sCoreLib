package com.supermartijn642.core.mixin;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Supplier;

/**
 * Created 07/05/2023 by SuperMartijn642
 */
@Mixin(Block.Properties.class)
public interface BlockPropertiesAccessor {

    @Accessor(value = "lootTableSupplier", remap = false)
    Supplier<ResourceLocation> getLootTableSupplier();

    @Accessor(value = "lootTableSupplier", remap = false)
    void setLootTableSupplier(Supplier<ResourceLocation> supplier);
}
