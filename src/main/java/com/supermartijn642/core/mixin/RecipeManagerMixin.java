package com.supermartijn642.core.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.supermartijn642.core.data.recipe.ConditionalRecipeSerializer;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created 12/09/2023 by SuperMartijn642
 */
@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Final
    @Shadow
    private HolderLookup.Provider registries;

    @Inject(
        method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
        at = @At("HEAD")
    )
    private void apply(Map<ResourceLocation,JsonElement> recipes, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci){
        // Process any conditional recipes
        Set<ResourceLocation> removed = null;
        for(Map.Entry<ResourceLocation,JsonElement> entry : recipes.entrySet()){
            if(entry.getValue() == null || !entry.getValue().isJsonObject())
                continue;
            ResourceLocation identifier = entry.getKey();
            JsonObject json = entry.getValue().getAsJsonObject();
            if(json != null && json.has("type") && json.get("type").isJsonPrimitive() && json.getAsJsonPrimitive("type").isString()){
                String type = json.get("type").getAsString();
                if(RegistryUtil.isValidIdentifier(type) && ResourceLocation.parse(type).equals(Registries.RECIPE_SERIALIZERS.getIdentifier(ConditionalRecipeSerializer.INSTANCE))){
                    JsonElement recipeJson = ConditionalRecipeSerializer.unwrapRecipe(identifier, json, this.registries);
                    if(recipeJson == null){
                        if(removed == null)
                            removed = new HashSet<>();
                        removed.add(identifier);
                    }else
                        recipes.put(identifier, recipeJson);
                }
            }
        }
        if(removed != null)
            removed.forEach(recipes::remove);
    }

    @Inject(
        method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/crafting/RecipeHolder;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void fromJson(ResourceLocation recipeLocation, JsonObject json, HolderLookup.Provider provider, CallbackInfoReturnable<RecipeHolder<?>> ci){
        // Intercept conditional recipes
        if(json != null && json.has("type") && json.get("type").isJsonPrimitive() && json.getAsJsonPrimitive("type").isString()){
            String type = json.get("type").getAsString();
            if(RegistryUtil.isValidIdentifier(type) && ResourceLocation.parse(type).equals(Registries.RECIPE_SERIALIZERS.getIdentifier(ConditionalRecipeSerializer.INSTANCE))){
                JsonElement recipeJson = ConditionalRecipeSerializer.unwrapRecipe(recipeLocation, json, provider);
                if(recipeJson == null)
                    ci.setReturnValue(new RecipeHolder<>(recipeLocation, ConditionalRecipeSerializer.DUMMY_RECIPE));
                else
                    ci.setReturnValue(RecipeManager.fromJson(recipeLocation, json.getAsJsonObject("recipe"), provider));
            }
        }
    }
}
