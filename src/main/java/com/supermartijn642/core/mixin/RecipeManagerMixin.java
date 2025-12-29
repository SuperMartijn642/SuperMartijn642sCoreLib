package com.supermartijn642.core.mixin;

import com.supermartijn642.core.data.recipe.ConditionalRecipeSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/**
 * Created 12/09/2023 by SuperMartijn642
 */
@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Inject(
        method = "prepare",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/resources/SimpleJsonResourceReloadListener;scanDirectory(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/FileToIdConverter;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;)V",
            shift = At.Shift.AFTER
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller, CallbackInfoReturnable<?> ci, SortedMap<Identifier,Recipe<?>> map){
        // Filter out dummy recipes
        List<Identifier> remove = new ArrayList<>();
        map.forEach((location, recipe) -> {
            if(recipe == ConditionalRecipeSerializer.DUMMY_RECIPE)
                remove.add(location);
        });
        remove.forEach(map::remove);
    }
}
