package com.supermartijn642.core.mixin.dev;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.registries.EmptyTagLookupWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 06/10/2026 by SuperMartijn642
 */
@Mixin(HolderOwner.class)
public interface HolderOwnerMixin {

    @Shadow
    boolean canSerialize(HolderOwner<?> owner);

    @Inject(
        method = "canSerialize",
        at = @At("RETURN"),
        cancellable = true
    )
    default void unwrapRegistry(HolderOwner<?> owner, CallbackInfoReturnable<Boolean> ci){
        // This seems like a vanilla oversight, but if a Holder's owner is wrapped in an EmptyTagLookupWrapper, the canSerialize check will always fail even if the wrapped registry is the same
        if(!ci.getReturnValue() && owner instanceof EmptyTagLookupWrapper<?>(HolderLookup.RegistryLookup<?> parent))
            ci.setReturnValue(this.canSerialize(parent));
    }
}
