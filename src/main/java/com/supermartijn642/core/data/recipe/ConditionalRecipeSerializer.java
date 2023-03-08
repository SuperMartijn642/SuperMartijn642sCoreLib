package com.supermartijn642.core.data.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public class ConditionalRecipeSerializer implements RecipeSerializer<Recipe<?>> {

    public static final ConditionalRecipeSerializer INSTANCE = new ConditionalRecipeSerializer();

    private ResourceLocation registryName;

    private ConditionalRecipeSerializer(){
    }

    @Override
    public Recipe<?> fromJson(ResourceLocation location, JsonObject json){
        if(!json.has("conditions") || !json.get("conditions").isJsonArray())
            throw new RuntimeException("Conditional recipe '" + location + "' must have 'conditions' array!");
        if(!json.has("recipe") || !json.get("recipe").isJsonObject())
            throw new RuntimeException("Conditional recipe '" + location + "' must have 'recipe' object!");

        // Test all conditions
        JsonArray conditions = json.getAsJsonArray("conditions");
        for(JsonElement conditionElement : conditions){
            if(!conditionElement.isJsonObject())
                throw new RuntimeException("Conditions array for recipe '" + location + "' must only contain objects!");
            JsonObject conditionJson = conditionElement.getAsJsonObject();
            if(!conditionJson.has("type") || !conditionJson.get("type").isJsonPrimitive() || !conditionJson.get("type").getAsJsonPrimitive().isString())
                throw new RuntimeException("Condition for recipe '" + location + "' is missing 'type' key!");
            String type = conditionJson.get("type").getAsString();
            if(!RegistryUtil.isValidIdentifier(type))
                throw new RuntimeException("Condition for recipe '" + location + "' has invalid type '" + type + "'!");

            IConditionSerializer<?> serializer = Registries.RECIPE_CONDITION_SERIALIZERS.getValue(new ResourceLocation(type));
            if(serializer == null)
                throw new RuntimeException("Condition for recipe '" + location + "' has unknown type '" + new ResourceLocation(type) + "'!");

            ICondition condition;
            try{
                condition = serializer.read(conditionJson);
            }catch(Exception e){
                throw new RuntimeException("Encountered exception whilst testing condition '" + new ResourceLocation(type) + "' for recipe '" + location + "'!");
            }

            if(!condition.test())
                return null;
        }

        // Now return the recipe
        return RecipeManager.fromJson(location, json.getAsJsonObject("recipe"));
    }

    @Override
    public Recipe<?> fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf){
        return null;
    }

    @Override
    public void toNetwork(FriendlyByteBuf friendlyByteBuf, Recipe<?> recipe){
    }

    @Override
    public RecipeSerializer<?> setRegistryName(ResourceLocation name){
        this.registryName = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName(){
        return this.registryName;
    }

    @Override
    public Class<RecipeSerializer<?>> getRegistryType(){
        return ForgeRegistries.RECIPE_SERIALIZERS.getRegistrySuperType();
    }
}
