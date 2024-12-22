package com.supermartijn642.core.mixin;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.extensions.CoreLibModelBakery;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Function;

/**
 * Created 21/12/2024 by SuperMartijn642
 */
@Mixin(ModelBakery.ModelBakerImpl.class)
public class ModelBakerImplMixin {

    @Shadow(aliases = "this$0")
    private ModelBakery modelBakery;

    @ModifyVariable(
        method = "bake",
        at = @At("STORE"),
        ordinal = 1
    )
    private BakedModel bake(BakedModel model, ResourceLocation location, ModelState modelState){
        // Apply any model overwrites
        Function<BakedModel,BakedModel> overwrite = ((CoreLibModelBakery)this.modelBakery).supermartijn642corelibGetModelOverwrite(location);
        if(overwrite != null){
            BakedModel newModel;
            try{
                newModel = overwrite.apply(model);
            }catch(Exception e){
                CoreLib.LOGGER.error("Encountered an error while applying model overwrite for location '{}'!", location, e);
                return model;
            }
            if(newModel == null){
                CoreLib.LOGGER.error("Model overwrite for location '{}' returned null!", location);
                return model;
            }
            return newModel;
        }
        return model;
    }
}
