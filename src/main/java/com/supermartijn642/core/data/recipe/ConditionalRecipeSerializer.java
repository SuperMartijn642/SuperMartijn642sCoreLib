package com.supermartijn642.core.data.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.supermartijn642.core.data.condition.ResourceCondition;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public final class ConditionalRecipeSerializer implements RecipeSerializer<Recipe<?>> {

    public static final RecipeType<DummyRecipe> DUMMY_RECIPE_TYPE = RecipeType.simple(ResourceLocation.fromNamespaceAndPath("supermartijn642corelib", "dummy"));
    public static final Recipe<?> DUMMY_RECIPE = new DummyRecipe();
    public static final ConditionalRecipeSerializer INSTANCE = new ConditionalRecipeSerializer();

    public static JsonObject wrapRecipeWithForgeConditions(JsonObject recipe, Collection<ICondition> conditions){
        JsonObject json = new JsonObject();
        json.addProperty("type", Registries.RECIPE_SERIALIZERS.getIdentifier(ConditionalRecipeSerializer.INSTANCE).toString());
        JsonArray conditionsJson = new JsonArray();
        for(ICondition condition : conditions)
            conditionsJson.add(ICondition.CODEC.encodeStart(JsonOps.INSTANCE, condition).getOrThrow());
        json.add("conditions", conditionsJson);
        json.add("recipe", recipe);
        return json;
    }

    public static JsonObject wrapRecipe(JsonObject recipe, Collection<ResourceCondition> conditions){
        return wrapRecipeWithForgeConditions(
            recipe,
            conditions.stream()
                .map(ResourceCondition::createForgeCondition)
                .collect(Collectors.toList())
        );
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
            ICondition condition;
            try{
                condition = ICondition.CODEC.decode(JsonOps.INSTANCE, conditionElement).getOrThrow().getFirst();
            }catch(Exception e){
                throw new RuntimeException("Encountered exception whilst testing conditions for recipe '" + location + "'!", e);
            }

            if(!condition.test(ICondition.IContext.EMPTY))
                return null;
        }

        // Now return the recipe
        return json.getAsJsonObject("recipe");
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
