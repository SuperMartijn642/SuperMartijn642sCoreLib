package com.supermartijn642.core.mixin;

import com.supermartijn642.core.registry.ClientRegistrationHandler;
import net.minecraft.client.resources.model.ModelDiscovery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * Created 18/12/2024 by SuperMartijn642
 */
@Mixin(ModelDiscovery.class)
public class ModelDiscoveryMixin {

    @Inject(
        method = "listMandatoryModels",
        at = @At("RETURN")
    )
    private static void listMandatoryModels(CallbackInfoReturnable<Set<ModelResourceLocation>> ci){
        // Add additional special models to be loaded
        ClientRegistrationHandler.registerSpecialModels(ci.getReturnValue());
    }
}
