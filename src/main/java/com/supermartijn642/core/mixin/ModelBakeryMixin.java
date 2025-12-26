package com.supermartijn642.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * Created 21/12/2024 by SuperMartijn642
 */
@Mixin(ModelBakery.class)
public class ModelBakeryMixin {

    @Final
    @Shadow
    private Map<ResourceLocation,ResolvedModel> resolvedModels;

    @Inject(
        method = "bakeModels",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/thread/ParallelMapTransform;schedule(Ljava/util/Map;Ljava/util/function/BiFunction;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
            shift = At.Shift.BEFORE,
            ordinal = 0
        )
    )
    private void bakeModelsHead(SpriteGetter spriteGetter, Executor executor, CallbackInfoReturnable<ModelBakery.BakingResult> ci, @Local ModelBakery.ModelBakerImpl modelBaker){
        // Catch errors here to prevent the model manager from continuously retrying to load models
        try{
            // Apply block model consumers
            Function<ResourceLocation,BlockStateModel> modelGetter = location -> new SingleVariant(SimpleModelWrapper.bake(modelBaker, location, BlockModelRotation.X0_Y0));
            ClientRegistrationHandler.applyBlockModelConsumersInternal(modelGetter);
        }catch(Exception e){
            CoreLib.LOGGER.error("Encountered an error while applying model consumers!", e);
        }
    }

    @Inject(
        method = "bakeModels",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V",
            shift = At.Shift.BEFORE
        ),
        require = 1,
        allow = 1
    )
    private void bakeModelsTail(SpriteGetter spriteGetter, Executor executor, CallbackInfoReturnable<ModelBakery.BakingResult> ci, @Local(ordinal = 0) LocalRef<CompletableFuture<Map<BlockState,BlockStateModel>>> blockModels, @Local(ordinal = 1) LocalRef<CompletableFuture<Map<ResourceLocation,ItemModel>>> itemModels){
        // Catch errors here to prevent the model manager from continuously retrying to load models
        try{
            // Apply block model overwrites
            blockModels.set(blockModels.get().whenCompleteAsync((models, exception) -> {
                if(exception == null)
                    ClientRegistrationHandler.applyBlockModelOverwritesInternal(models);
            }));
            // Apply item model overwrites
            itemModels.set(itemModels.get().whenCompleteAsync((models, exception) -> {
                if(exception == null)
                    ClientRegistrationHandler.applyItemModelOverwritesInternal(models);
            }));
        }catch(Exception e){
            CoreLib.LOGGER.error("Encountered an error while applying model overwrites!", e);
        }
    }
}
