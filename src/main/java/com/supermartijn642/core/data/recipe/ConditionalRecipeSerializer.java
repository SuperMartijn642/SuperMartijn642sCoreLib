package com.supermartijn642.core.data.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
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
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public final class ConditionalRecipeSerializer implements RecipeSerializer<Recipe<?>> {

    private static final RecipeType<DummyRecipe> DUMMY_RECIPE_TYPE = Registry.register(BuiltInRegistries.RECIPE_TYPE, ResourceLocation.fromNamespaceAndPath("supermartijn642corelib", "dummy"), new RecipeType<>() {
        public String toString(){
            return "supermartijn642corelib:dummy";
        }
    });
    public static final Recipe<?> DUMMY_RECIPE = new DummyRecipe();
    public static final ConditionalRecipeSerializer INSTANCE = new ConditionalRecipeSerializer();

    private static final MapCodec<Recipe<?>> CODEC = new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops){
            return Stream.empty();
        }

        @Override
        public <T> DataResult<Recipe<?>> decode(DynamicOps<T> ops, MapLike<T> input){
            // Convert the input to json
            JsonObject json = new JsonObject();
            input.entries()
                .map(entry -> {
                    DataResult<String> key = ops.getStringValue(entry.getFirst());
                    return key.isSuccess() ? Pair.of(key.getOrThrow(), entry.getSecond()) : null;
                })
                .filter(Objects::nonNull)
                .forEach(entry -> json.add(entry.getFirst(), ops.convertTo(JsonOps.INSTANCE, entry.getSecond())));
            // Unwrap recipe
            JsonElement recipeJson = unwrapRecipe(null, json);
            if(recipeJson == null)
                return DataResult.success(DUMMY_RECIPE);
            // Convert json to ops type
            T t = JsonOps.INSTANCE.convertTo(ops, recipeJson);
            // Decode the actual recipe
            return Recipe.CODEC.parse(ops, t);
        }

        @Override
        public <T> RecordBuilder<T> encode(Recipe<?> input, DynamicOps<T> ops, RecordBuilder<T> prefix){
            return Recipe.CODEC.encodeStart(ops, input).flatMap(output -> {
                JsonElement element = ops.convertTo(JsonOps.INSTANCE, output);
                if(element.isJsonObject()){
                    RecordBuilder<T> map = ops.mapBuilder();
                    for(String key : element.getAsJsonObject().keySet())
                        map.add(key, JsonOps.INSTANCE.convertTo(ops, element.getAsJsonObject().get(key)));
                    return DataResult.success(map);
                }
                return DataResult.error(() -> "Expected object but got " + element + " from recipe codec!");
            }).getOrThrow();
        }
    };

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

    public static JsonElement unwrapRecipe(ResourceLocation location, JsonObject json){
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
                return null;
        }

        // Now return the recipe
        return json.getAsJsonObject("recipe");
    }

    @Override
    public MapCodec<Recipe<?>> codec(){
        return CODEC;
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
        public RecipeSerializer<DummyRecipe> getSerializer(){
            return null;
        }

        @Override
        public RecipeType<DummyRecipe> getType(){
            return DUMMY_RECIPE_TYPE;
        }

        @Override
        public PlacementInfo placementInfo(){
            return PlacementInfo.NOT_PLACEABLE;
        }

        @Override
        public RecipeBookCategory recipeBookCategory(){
            return RecipeBookCategories.CRAFTING_MISC;
        }
    }
}
