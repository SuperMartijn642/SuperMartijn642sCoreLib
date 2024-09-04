package com.supermartijn642.core.data.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.supermartijn642.core.data.condition.ResourceCondition;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public final class ConditionalRecipeSerializer implements RecipeSerializer<Recipe<?>> {

    public static final RecipeType<Recipe<?>> DUMMY_RECIPE_TYPE = RecipeType.simple(new ResourceLocation("supermartijn642corelib:dummy"));
    public static final Recipe<?> DUMMY_RECIPE = new DummyRecipe();
    public static final ConditionalRecipeSerializer INSTANCE = new ConditionalRecipeSerializer();

    public static JsonObject wrapRecipeWithForgeConditions(JsonObject recipe, Collection<ICondition> conditions){
        JsonObject json = new JsonObject();
        json.addProperty("type", Registries.RECIPE_SERIALIZERS.getIdentifier(ConditionalRecipeSerializer.INSTANCE).toString());
        JsonArray conditionsJson = new JsonArray();
        for(ICondition condition : conditions)
            conditionsJson.add(ICondition.CODEC.encodeStart(JsonOps.INSTANCE, condition).getOrThrow(false, s -> {}));
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

    public static JsonElement unwrapRecipe(ResourceLocation location, JsonObject json, ICondition.IContext conditionContext){
        if(!json.has("conditions") || !json.get("conditions").isJsonArray())
            throw new RuntimeException("Conditional recipe '" + location + "' must have 'conditions' array!");
        if(!json.has("recipe") || !json.get("recipe").isJsonObject())
            throw new RuntimeException("Conditional recipe '" + location + "' must have 'recipe' object!");

        // Test all conditions
        JsonArray conditions = json.getAsJsonArray("conditions");
        DynamicOps<JsonElement> ops = JsonOps.INSTANCE;
        for(JsonElement conditionElement : conditions){
            ICondition condition;
            try{
                condition = ICondition.CODEC.decode(ops, conditionElement).getOrThrow(false, s -> {}).getFirst();
            }catch(Exception e){
                throw new RuntimeException("Encountered exception whilst testing conditions for recipe '" + location + "'!", e);
            }

            if(!condition.test(conditionContext))
                return null;
        }

        // Now return the recipe
        return json.getAsJsonObject("recipe");
    }

    @Override
    public Codec<Recipe<?>> codec(){
        return Codec.unit(null);
    }

    @Override
    public Recipe<?> fromNetwork(FriendlyByteBuf friendlyByteBuf){
        return new DummyRecipe();
    }

    @Override
    public void toNetwork(FriendlyByteBuf friendlyByteBuf, Recipe<?> recipe){
    }

    private static class DummyRecipe implements Recipe<Container> {

        @Override
        public boolean matches(Container container, Level level){
            return false;
        }

        @Override
        public ItemStack assemble(Container container, RegistryAccess registryAccess){
            return ItemStack.EMPTY;
        }

        @Override
        public boolean canCraftInDimensions(int i, int j){
            return false;
        }

        @Override
        public ItemStack getResultItem(RegistryAccess registryAccess){
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
