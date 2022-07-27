package com.supermartijn642.core.mixin;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 27/07/2022 by SuperMartijn642
 */
@Mixin(ModelManager.class)
public class ModelManagerMixin {

    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
            ordinal = 0,
            shift = At.Shift.BEFORE
        ),
        method = "apply"
    )
    private void apply(ModelBakery modelBakery, ResourceManager resourceManager, ProfilerFiller profilerFiller, CallbackInfo ci){
        // Catch errors here to prevent the model manager from continuously retrying to load models
        try{
            ClientRegistrationHandler.registerModelOverwritesInternal(modelBakery.getBakedTopLevelModels());
        }catch(Exception e){
            CoreLib.LOGGER.error("Encountered an error while applying model overwrites!", e);
        }
    }
}
