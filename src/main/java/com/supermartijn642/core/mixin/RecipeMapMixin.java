package com.supermartijn642.core.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.supermartijn642.core.data.recipe.ConditionalRecipeSerializer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 05/09/2026 by SuperMartijn642
 */
@Mixin(RecipeMap.class)
public class RecipeMapMixin {

    @Inject(
        method = "lambda$create$0(Lcom/google/common/collect/ImmutableMultimap$Builder;Lcom/google/common/collect/ImmutableMap$Builder;Lnet/minecraft/core/Holder$Reference;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void filterDummyRecipes(ImmutableMultimap.Builder<RecipeType<?>,RecipeHolder<?>> recipesByType, ImmutableMap.Builder<ResourceKey<Recipe<?>>,RecipeHolder<?>> recipesByKey,
                                    Holder.Reference<Recipe<?>> recipe,
                                    CallbackInfo ci){
        if(recipe.isBound() && ConditionalRecipeSerializer.isDummyRecipe(recipe.value()))
            ci.cancel();
    }
}
