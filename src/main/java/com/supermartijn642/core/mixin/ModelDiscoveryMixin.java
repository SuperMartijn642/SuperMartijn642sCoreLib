package com.supermartijn642.core.mixin;

import com.supermartijn642.core.registry.ClientRegistrationHandler;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelDiscovery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Created 18/12/2024 by SuperMartijn642
 */
@Mixin(ModelDiscovery.class)
public class ModelDiscoveryMixin {

    @Shadow
    private void registerTopModel(ModelResourceLocation location, UnbakedModel model){
        throw new AssertionError();
    }

    @Shadow
    UnbakedModel getBlockModel(ResourceLocation location){
        throw new AssertionError();
    }

    @Inject(
        method = "registerStandardModels",
        at = @At("TAIL")
    )
    private void registerStandardModels(BlockStateModelLoader.LoadedModels blockStates, CallbackInfo ci){
        // Add additional special models to be loaded
        Set<ModelResourceLocation> models = new HashSet<>();
        ClientRegistrationHandler.registerSpecialModels(models);
        for(ModelResourceLocation model : models)
            this.registerTopModel(model, this.getBlockModel(model.id()));
    }
}
