package com.supermartijn642.core.mixin;

import com.supermartijn642.core.extensions.LootContextExtension;
import net.minecraft.world.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Created 31/08/2022 by SuperMartijn642
 */
@Mixin(LootContext.class)
public class LootContextMixin implements LootContextExtension {

    private float coreLibExplosionRadius;

    @Override
    public float coreLibGetExplosionRadius(){
        return this.coreLibExplosionRadius;
    }

    @Override
    public void coreLibSetExplosionRadius(float radius){
        this.coreLibExplosionRadius = radius;
    }
}
