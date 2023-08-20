package com.supermartijn642.core.mixin;

import com.supermartijn642.core.block.BaseBlock;
import net.minecraft.world.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 31/08/2022 by SuperMartijn642
 */
@Mixin(Explosion.class)
public class ExplosionMixin {

    // Can't just do a proper mixin here because SpongeForge just overwrites the entire method

    @Inject(
        method = "doExplosionB",
        at = @At("HEAD")
    )
    private void doExplosionBHead(boolean spawnParticles, CallbackInfo ci){
        //noinspection DataFlowIssue
        BaseBlock.IN_EXPLOSION.set((Explosion)(Object)this);
    }

    @Inject(
        method = "doExplosionB",
        at = @At("RETURN")
    )
    private void doExplosionBTail(boolean spawnParticles, CallbackInfo ci){
        BaseBlock.IN_EXPLOSION.set(null);
    }
}
