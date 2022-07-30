package com.supermartijn642.core.mixin;

import com.supermartijn642.core.mixin.extensions.EntityExtension;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 29/07/2022 by SuperMartijn642
 */
@Mixin(Entity.class)
public class EntityMixin implements EntityExtension {

    @Inject(
        method = "fireImmune",
        at = @At("HEAD"),
        cancellable = true
    )
    public void fireImmune(CallbackInfoReturnable<Boolean> ci){
        if(this.coreLibIsFireImmune())
            ci.setReturnValue(true);
    }

    @Override
    public boolean coreLibIsFireImmune(){
        return false;
    }
}
