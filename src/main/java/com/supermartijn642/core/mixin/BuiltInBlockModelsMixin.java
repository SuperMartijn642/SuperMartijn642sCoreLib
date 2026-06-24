package com.supermartijn642.core.mixin;

import com.supermartijn642.core.registry.ClientRegistrationHandler;
import net.minecraft.client.renderer.block.BuiltInBlockModels;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 24/06/2026 by SuperMartijn642
 */
@Mixin(BuiltInBlockModels.class)
public class BuiltInBlockModelsMixin {

    @Inject(
        method = "addDefaults",
        at = @At("TAIL")
    )
    private static void addDefaults(BuiltInBlockModels.Builder builder, CallbackInfo ci) {
        ClientRegistrationHandler.registerBuiltinBlockModelsInternal(builder);
    }
}
