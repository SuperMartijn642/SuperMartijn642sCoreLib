package com.supermartijn642.core.mixin;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created 05/08/2022 by SuperMartijn642
 */
@Mixin(ModelLoader.class)
public class ModelLoaderMixin {

    @Shadow(remap = false)
    @Final
    private Map<ModelResourceLocation,IModel> stateModels;
    @Shadow(remap = false)
    @Final
    private Map<ResourceLocation,Exception> loadingExceptions;

    private List<ResourceLocation> coreLibSpecialModels = new ArrayList<>();

    @Inject(
        method = "setupModelRegistry",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/client/model/ModelLoader;loadVariantItemModels()V",
            shift = At.Shift.AFTER
        )
    )
    private void loadModelsInject(CallbackInfoReturnable<?> ci){
        ModelLoader bakery = (ModelLoader)(Object)this;

        // Get all special models from ClientRegistrationHandlers
        ClientRegistrationHandler.registerAllSpecialModels(this.coreLibSpecialModels::add);

        // Copy the behaviour of loadModels in ModelBakery
        Deque<ResourceLocation> modelsToBeLoaded = Queues.newArrayDeque(this.coreLibSpecialModels);
        Set<ResourceLocation> loadedModels = Sets.newHashSet();

        for(ResourceLocation location : bakery.models.keySet()){
            loadedModels.add(location);
            bakery.addModelParentLocation(modelsToBeLoaded, loadedModels, bakery.models.get(location));
        }

        while(!modelsToBeLoaded.isEmpty()){
            ResourceLocation modelLocation = modelsToBeLoaded.pop();

            try{
                if(bakery.models.get(modelLocation) != null){
                    continue;
                }

                ModelBlock modelBlock = bakery.loadModel(new ResourceLocation(modelLocation.getResourceDomain(), "models/" + modelLocation.getResourcePath()));
                bakery.models.put(modelLocation, modelBlock);
                bakery.addModelParentLocation(modelsToBeLoaded, loadedModels, modelBlock);
            }catch(Exception exception){
                ModelBakery.LOGGER.warn("In parent chain: {}; unable to load model: '{}'", ModelBakery.JOINER.join(bakery.getParentPath(modelLocation)), modelLocation, exception);
            }

            loadedModels.add(modelLocation);
        }
    }

    @Inject(
        method = "setupModelRegistry",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/client/model/ModelLoader;loadVariantItemModels()V",
            shift = At.Shift.AFTER
        )
    )
    private void bakeModelsInject(CallbackInfoReturnable<?> ci) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException{
        for(ResourceLocation modelLocation : this.coreLibSpecialModels){
            IModel model;
            try{
                model = ModelLoaderRegistry.getModel(modelLocation);
            }catch(Exception e){
                this.loadingExceptions.put(modelLocation, e);
                Method getMissingModel = ModelLoaderRegistry.class.getDeclaredMethod("getMissingModel", ResourceLocation.class, Throwable.class);
                getMissingModel.setAccessible(true);
                model = (IModel)getMissingModel.invoke(null, modelLocation, e);
            }
            this.stateModels.put(new ModelResourceLocation(modelLocation, ""), model);
        }
    }
}
