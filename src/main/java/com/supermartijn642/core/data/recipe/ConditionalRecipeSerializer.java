package com.supermartijn642.core.data.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.conditions.ConditionCodec;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public final class ConditionalRecipeSerializer {

    public static final RecipeType<DummyRecipe> DUMMY_RECIPE_TYPE = RecipeType.simple(Identifier.fromNamespaceAndPath("supermartijn642corelib", "dummy"));
    public static final Recipe<?> DUMMY_RECIPE = new DummyRecipe();
    public static final RecipeSerializer<Recipe<?>> SERIALIZER;

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
            JsonElement recipeJson = unwrapRecipe(null, json, ops);
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

    static{
        SERIALIZER = new RecipeSerializer<>(CODEC, StreamCodec.unit(null));
    }

    public static JsonObject wrapRecipeWithForgeConditions(JsonObject recipe, Collection<ICondition> conditions){
        JsonObject json = new JsonObject();
        json.addProperty("type", Registries.RECIPE_SERIALIZERS.getIdentifier(ConditionalRecipeSerializer.SERIALIZER).toString());
        JsonArray conditionsJson = new JsonArray();
        for(ICondition condition : conditions)
            conditionsJson.add(ICondition.CODEC.encodeStart(JsonOps.INSTANCE, condition).getOrThrow());
        json.add("conditions", conditionsJson);
        json.add("recipe", recipe);
        return json;
    }

    private ConditionalRecipeSerializer(){
    }

    public static <T> JsonElement unwrapRecipe(Identifier location, JsonObject json, DynamicOps<T> ops){
        if(!json.has("conditions") || !json.get("conditions").isJsonArray())
            throw new RuntimeException("Conditional recipe '" + location + "' must have 'conditions' array!");
        if(!json.has("recipe") || !json.get("recipe").isJsonObject())
            throw new RuntimeException("Conditional recipe '" + location + "' must have 'recipe' object!");

        // Retrieve condition context
        ICondition.IContext context = ConditionCodec.getContext(ops);

        // Test all conditions
        JsonArray conditions = json.getAsJsonArray("conditions");
        for(JsonElement conditionElement : conditions){
            ICondition condition;
            try{
                T t = JsonOps.INSTANCE.convertTo(ops, conditionElement);
                condition = ICondition.CODEC.decode(ops, t).getOrThrow().getFirst();
            }catch(Exception e){
                throw new RuntimeException("Encountered exception whilst testing conditions for recipe '" + location + "'!", e);
            }

            if(!condition.test(context, ops))
                return null;
        }

        // Now return the recipe
        return json.getAsJsonObject("recipe");
    }

    private static class DummyRecipe implements Recipe<RecipeInput> {

        @Override
        public boolean matches(RecipeInput container, Level level){
            return false;
        }

        @Override
        public ItemStack assemble(RecipeInput input){
            return ItemStack.EMPTY;
        }

        @Override
        public boolean showNotification(){
            return false;
        }

        @Override
        public String group(){
            return "";
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
