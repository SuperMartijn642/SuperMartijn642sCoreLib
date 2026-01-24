package com.supermartijn642.core.extensions;

import net.minecraft.block.state.IBlockState;

/**
 * Created 31/08/2022 by SuperMartijn642
 */
public interface LootContextExtension {

    float coreLibGetExplosionRadius();

    void coreLibSetExplosionRadius(float radius);

    IBlockState coreLibGetBlockState();

    void coreLibSetBlockState(IBlockState state);
}
