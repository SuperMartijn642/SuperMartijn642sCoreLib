package com.supermartijn642.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Created 26/12/2024 by SuperMartijn642
 */
@Mixin(ModelManager.class)
public class ModelManagerMixin {

    @Inject(
        method = "discoverModelDependencies",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/resources/model/ModelDiscovery;missingModel()Lnet/minecraft/client/resources/model/ResolvedModel;",
            shift = At.Shift.BEFORE
        )
    )
    private static void discoverModelDependencies(Map<ResourceLocation,UnbakedModel> models, BlockStateModelLoader.LoadedModels blockStates, ClientItemInfoLoader.LoadedClientInfos itemInfos, CallbackInfoReturnable<?> ci, @Local ModelDiscovery modelDiscovery){
        Predicate<ResourceLocation> markDependency = location -> {
            if(!models.containsKey(location)){
                //noinspection rawtypes,unchecked
                ((Map)modelDiscovery.modelWrappers).put(location, modelDiscovery.missingModel());
                return false;
            }
            modelDiscovery.resolver.markDependency(location);
            return true;
        };
        ClientRegistrationHandler.registerBlockModelConsumerDependenciesInternal(markDependency);
    }
}
