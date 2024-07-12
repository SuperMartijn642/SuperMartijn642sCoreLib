package com.supermartijn642.core.data.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.supermartijn642.core.data.condition.ResourceCondition;
import com.supermartijn642.core.data.condition.ResourceConditionContext;
import com.supermartijn642.core.data.condition.ResourceConditionSerializer;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.Collection;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public final class ConditionalRecipeSerializer implements RecipeSerializer<Recipe<?>> {

    private static final RecipeType<DummyRecipe> DUMMY_RECIPE_TYPE = Registry.register(BuiltInRegistries.RECIPE_TYPE, ResourceLocation.fromNamespaceAndPath("supermartijn642corelib", "dummy"), new RecipeType<>() {
        public String toString(){
            return "supermartijn642corelib:dummy";
        }
    });
    private static final DummyRecipe DUMMY_RECIPE = new DummyRecipe();
    public static final ConditionalRecipeSerializer INSTANCE = new ConditionalRecipeSerializer();

    public static JsonObject wrapRecipe(JsonObject recipe, Collection<ResourceCondition> conditions){
        JsonObject json = new JsonObject();
        json.addProperty("type", Registries.RECIPE_SERIALIZERS.getIdentifier(ConditionalRecipeSerializer.INSTANCE).toString());
        JsonArray conditionsJson = new JsonArray();
        for(ResourceCondition condition : conditions){
            JsonObject conditionJson = new JsonObject();
            conditionJson.addProperty("type", Registries.RESOURCE_CONDITION_SERIALIZERS.getIdentifier(condition.getSerializer()).toString());
            //noinspection unchecked,rawtypes
            ((ResourceConditionSerializer)condition.getSerializer()).serialize(conditionJson, condition);
            conditionsJson.add(conditionJson);
        }
        json.add("conditions", conditionsJson);
        json.add("recipe", recipe);
        return json;
    }

    private ConditionalRecipeSerializer(){
    }

    public static RecipeHolder<?> fromJson(ResourceLocation location, JsonObject json, HolderLookup.Provider provider){
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

            ResourceConditionSerializer<?> serializer = Registries.RESOURCE_CONDITION_SERIALIZERS.getValue(ResourceLocation.parse(type));
            if(serializer == null)
                throw new RuntimeException("Condition for recipe '" + location + "' has unknown type '" + ResourceLocation.parse(type) + "'!");

            ResourceCondition condition;
            try{
                condition = serializer.deserialize(conditionJson);
            }catch(Exception e){
                throw new RuntimeException("Encountered exception whilst testing condition '" + ResourceLocation.parse(type) + "' for recipe '" + location + "'!");
            }

            if(!condition.test(ResourceConditionContext.EMPTY))
                return new RecipeHolder<>(location, DUMMY_RECIPE);
        }

        // Now return the recipe
        return RecipeManager.fromJson(location, json.getAsJsonObject("recipe"), provider);
    }

    @Override
    public MapCodec<Recipe<?>> codec(){
        return MapCodec.unit(null);
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf,Recipe<?>> streamCodec(){
        return StreamCodec.unit(null);
    }

    private static class DummyRecipe implements Recipe<RecipeInput> {

        @Override
        public boolean matches(RecipeInput container, Level level){
            return false;
        }

        @Override
        public ItemStack assemble(RecipeInput container, HolderLookup.Provider provider){
            return ItemStack.EMPTY;
        }

        @Override
        public boolean canCraftInDimensions(int i, int j){
            return false;
        }

        @Override
        public ItemStack getResultItem(HolderLookup.Provider provider){
            return ItemStack.EMPTY;
        }

        @Override
        public RecipeSerializer<?> getSerializer(){
            return INSTANCE;
        }

        @Override
        public RecipeType<?> getType(){
            return DUMMY_RECIPE_TYPE;
        }

        @Override
        public boolean isIncomplete(){
            return true;
        }
    }
}
