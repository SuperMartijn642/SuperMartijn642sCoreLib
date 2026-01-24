package com.supermartijn642.core.mixin;

import com.supermartijn642.core.extensions.LootContextExtension;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Created 31/08/2022 by SuperMartijn642
 */
@Mixin(LootContext.class)
public class LootContextMixin implements LootContextExtension {

    @Unique
    private float explosionRadius;
    @Unique
    private IBlockState blockState;

    @Override
    public float coreLibGetExplosionRadius(){
        return this.explosionRadius;
    }

    @Override
    public void coreLibSetExplosionRadius(float radius){
        this.explosionRadius = radius;
    }

    @Override
    public IBlockState coreLibGetBlockState(){
        return this.blockState;
    }

    @Override
    public void coreLibSetBlockState(IBlockState state){
        this.blockState = state;
    }
}
