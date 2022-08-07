package com.supermartijn642.core.mixin;

import com.supermartijn642.core.extensions.RegistrySimpleExtension;
import net.minecraft.util.registry.RegistrySimple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 01/08/2022 by SuperMartijn642
 */
@Mixin(RegistrySimple.class)
public class RegistrySimpleMixin implements RegistrySimpleExtension {

    // Objects can technically be registered under multiple keys, however in practise this should be very rare so not that big of an issue
    private final Map<Object,Object> objectToKeyMap = new HashMap<>();

    @Inject(
        method = "putObject",
        at = @At("TAIL")
    )
    private void putObject(Object key, Object value, CallbackInfo ci){
        this.objectToKeyMap.put(value, key);
    }

    @Override
    public Object coreLibGetKey(Object object){
        return this.objectToKeyMap.get(object);
    }
}
