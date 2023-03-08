package com.supermartijn642.core.mixin;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Created 27/07/2022 by SuperMartijn642
 */
@Mixin(ModelManager.class)
public class ModelManagerMixin {

    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/resources/model/ModelBakery;getBakedTopLevelModels()Ljava/util/Map;",
            ordinal = 0,
            shift = At.Shift.BEFORE
        ),
        method = "loadModels"
    )
    private void loadModels(ProfilerFiller profilerFiller, Map<ResourceLocation,AtlasSet.StitchResult> map, ModelBakery modelBakery, CallbackInfoReturnable<ModelManager.ReloadState> ci){
        // Catch errors here to prevent the model manager from continuously retrying to load models
        try{
            ClientRegistrationHandler.registerModelOverwritesInternal(modelBakery.getBakedTopLevelModels());
        }catch(Exception e){
            CoreLib.LOGGER.error("Encountered an error while applying model overwrites!", e);
        }
    }
}
