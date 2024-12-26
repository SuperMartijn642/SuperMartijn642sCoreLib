package com.supermartijn642.core.mixin;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Created 26/12/2024 by SuperMartijn642
 */
@Mixin(ModelManager.class)
public class ModelManagerMixin {

    @Inject(
        method = "discoverModelDependencies",
        at = @At("RETURN")
    )
    private static void discoverModelDependencies(UnbakedModel missingModel, Map<ResourceLocation, UnbakedModel> models, BlockStateModelLoader.LoadedModels blockStates, ClientItemInfoLoader.LoadedClientInfos itemInfos, CallbackInfoReturnable<ModelDiscovery> ci) {
        ModelDiscovery modelDiscovery = ci.getReturnValue();
        ModelDiscovery.ResolverImpl resolver = modelDiscovery.new ResolverImpl();
        for(ResourceLocation model : ClientRegistrationHandler.getModelConsumerLocations()){
            if(!models.containsKey(model))
                CoreLib.LOGGER.warn("Missing model for model consumer: {}", model);
            resolver.resolve(model);
        }
    }
}
