package com.supermartijn642.core.mixin;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.extensions.CoreLibModelBakery;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created 21/12/2024 by SuperMartijn642
 */
@Mixin(ModelBakery.class)
public class ModelBakeryMixin implements CoreLibModelBakery {

    @Unique
    private Map<ResourceLocation,Function<BakedModel,BakedModel>> modelOverwrites;
    @Unique
    private Set<ResourceLocation> missingOverwriteModels;

    @Override
    public Function<BakedModel,BakedModel> supermartijn642corelibGetModelOverwrite(ResourceLocation location){
        if(this.modelOverwrites == null)
            return null;
        this.missingOverwriteModels.remove(location);
        return this.modelOverwrites.get(location);
    }

    @Inject(
        method = "bakeModels",
        at = @At("HEAD")
    )
    private void bakeModelsHead(CallbackInfoReturnable<?> ci){
        // Catch errors here to prevent the model manager from continuously retrying to load models
        try{
            this.modelOverwrites = ClientRegistrationHandler.gatherModelOverwritesInternal();
            this.missingOverwriteModels = new HashSet<>(this.modelOverwrites.keySet());
        }catch(Exception e){
            CoreLib.LOGGER.error("Encountered an error while applying model overwrites!", e);
        }
    }

    @Inject(
        method = "bakeModels",
        at = @At("RETURN")
    )
    private void bakeModelsTail(ModelBakery.TextureGetter textureGetter, CallbackInfoReturnable<ModelBakery.BakingResult> ci){
        // Catch errors here to prevent the model manager from continuously retrying to load models
        try{
            // Apply model consumers
            //noinspection DataFlowIssue
            ModelBakery modelBakery = (ModelBakery)(Object)this;
            ClientRegistrationHandler.applyModelConsumersInternal(
                location -> modelBakery.new ModelBakerImpl(textureGetter, location::toString).bake(location, BlockModelRotation.X0_Y0)
            );

            ModelBakery.BakingResult bakingResults = ci.getReturnValue();

            // Apply block model overwrites
            Map<ResourceLocation,Function<BakedModel,BakedModel>> blockOverwrites = ClientRegistrationHandler.gatherBlockModelOverwritesInternal();
            Map<ModelResourceLocation,BakedModel> blockModels = bakingResults.blockStateModels();
            Set<ResourceLocation> missingModels = new HashSet<>(blockOverwrites.keySet());
            for(ModelResourceLocation location : blockModels.keySet()){
                Function<BakedModel,BakedModel> overwrite = blockOverwrites.get(location.id());
                if(overwrite != null){
                    missingModels.remove(location.id());
                    BakedModel model;
                    try{
                        model = overwrite.apply(blockModels.get(location));
                    }catch(Exception e){
                        CoreLib.LOGGER.error("Encountered an error while applying block model overwrite for block state '{}'!", location, e);
                        continue;
                    }
                    if(model == null){
                        CoreLib.LOGGER.error("Block model overwrite for block state '{}' returned null!", location);
                        continue;
                    }
                    blockModels.put(location, model);
                }
            }
            if(!missingModels.isEmpty())
                CoreLib.LOGGER.error("Missing models for block model overwrites: {}", missingModels.stream().map(l -> "'" + l + "'").collect(Collectors.joining(", ")));

            // Apply item model overwrites
            Map<ResourceLocation,Function<ItemModel,ItemModel>> itemOverwrites = ClientRegistrationHandler.gatherItemModelOverwritesInternal();
            Map<ResourceLocation,ItemModel> itemModels = bakingResults.itemStackModels();
            missingModels = new HashSet<>(itemOverwrites.keySet());
            for(Map.Entry<ResourceLocation,Function<ItemModel,ItemModel>> entry : itemOverwrites.entrySet()){
                ResourceLocation location = entry.getKey();
                ItemModel model = itemModels.get(location);
                if(model == null){
                    missingModels.add(location);
                    continue;
                }
                try{
                    model = entry.getValue().apply(itemModels.get(location));
                }catch(Exception e){
                    CoreLib.LOGGER.error("Encountered an error while applying item model overwrite for item '{}'!", location, e);
                    continue;
                }
                if(model == null){
                    CoreLib.LOGGER.error("Item model overwrite for item '{}' returned null!", location);
                    continue;
                }
                itemModels.put(location, model);
            }
            if(!missingModels.isEmpty())
                CoreLib.LOGGER.error("Missing models for item model overwrites: {}", missingModels.stream().map(l -> "'" + l + "'").collect(Collectors.joining(", ")));

            // Clear overwrites
            if(!this.missingOverwriteModels.isEmpty())
                CoreLib.LOGGER.error("Missing models for model overwrites: {}", this.missingOverwriteModels.stream().map(l -> "'" + l + "'").collect(Collectors.joining(", ")));
            this.modelOverwrites = null;
            this.missingOverwriteModels = null;
        }catch(Exception e){
            CoreLib.LOGGER.error("Encountered an error while applying model overwrites!", e);
        }
    }
}
