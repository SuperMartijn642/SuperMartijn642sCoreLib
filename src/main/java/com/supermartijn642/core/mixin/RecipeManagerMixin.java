package com.supermartijn642.core.mixin;

import com.google.gson.JsonObject;
import com.supermartijn642.core.data.recipe.ConditionalRecipeSerializer;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 12/09/2023 by SuperMartijn642
 */
@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Inject(
        method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lnet/minecraft/world/item/crafting/RecipeHolder;",
        at = @At("HEAD"),
        cancellable = true
    )
    public static void fromJson(ResourceLocation recipeLocation, JsonObject json, CallbackInfoReturnable<RecipeHolder<?>> ci){
        // Intercept conditional recipes
        if(json != null && json.has("type") && json.get("type").isJsonPrimitive() && json.getAsJsonPrimitive("type").isString()){
            String type = json.get("type").getAsString();
            if(RegistryUtil.isValidIdentifier(type) && new ResourceLocation(type).equals(Registries.RECIPE_SERIALIZERS.getIdentifier(ConditionalRecipeSerializer.INSTANCE)))
                ci.setReturnValue(ConditionalRecipeSerializer.fromJson(recipeLocation, json));
        }
    }
}
