package com.supermartijn642.core.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.supermartijn642.core.data.condition.ModLoadedResourceCondition;
import com.supermartijn642.core.data.condition.NotResourceCondition;
import com.supermartijn642.core.data.condition.ResourceCondition;
import com.supermartijn642.core.data.recipe.ConditionalRecipeSerializer;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.util.Pair;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;

import java.util.*;

/**
 * Created 23/08/2022 by SuperMartijn642
 */
public abstract class RecipeGenerator extends ResourceGenerator {

    private static final Map<ResourceKey<CreativeModeTab>,RecipeCategory> TAB_TO_CATEGORY = new HashMap<>();

    static{
        TAB_TO_CATEGORY.put(CreativeModeTabs.BUILDING_BLOCKS, RecipeCategory.BUILDING_BLOCKS);
        TAB_TO_CATEGORY.put(CreativeModeTabs.COMBAT, RecipeCategory.COMBAT);
        TAB_TO_CATEGORY.put(CreativeModeTabs.COLORED_BLOCKS, RecipeCategory.BUILDING_BLOCKS);
        TAB_TO_CATEGORY.put(CreativeModeTabs.FOOD_AND_DRINKS, RecipeCategory.FOOD);
        TAB_TO_CATEGORY.put(CreativeModeTabs.FUNCTIONAL_BLOCKS, RecipeCategory.BUILDING_BLOCKS);
        TAB_TO_CATEGORY.put(CreativeModeTabs.INGREDIENTS, RecipeCategory.MISC);
        TAB_TO_CATEGORY.put(CreativeModeTabs.NATURAL_BLOCKS, RecipeCategory.DECORATIONS);
        TAB_TO_CATEGORY.put(CreativeModeTabs.REDSTONE_BLOCKS, RecipeCategory.REDSTONE);
        TAB_TO_CATEGORY.put(CreativeModeTabs.OP_BLOCKS, RecipeCategory.MISC);
        TAB_TO_CATEGORY.put(CreativeModeTabs.SPAWN_EGGS, RecipeCategory.MISC);
        TAB_TO_CATEGORY.put(CreativeModeTabs.TOOLS_AND_UTILITIES, RecipeCategory.TOOLS);
        TAB_TO_CATEGORY.put(CreativeModeTabs.SEARCH, RecipeCategory.MISC);
    }

    private static Ingredient mergeIngredients(Ingredient... ingredients){
        if(ingredients.length == 1)
            return ingredients[0];
        return CompoundIngredient.of(ingredients);
    }

    private final Map<ResourceLocation,RecipeBuilder<?>> recipes = new HashMap<>();
    private final Advancements advancements;

    public RecipeGenerator(String modid, ResourceCache cache){
        super(modid, cache);
        this.advancements = new Advancements(modid, cache);
    }

    @Override
    public void save(){
        // Generate the advancements
        this.advancements.generate();

        // Loop over all recipes
        for(RecipeBuilder<?> recipeBuilder : this.recipes.values()){
            JsonObject json = new JsonObject();
            Map<String,JsonObject> subRecipes = new HashMap<>();
            subRecipes.put("", json);

            // Set the recipe serializer
            json.addProperty("type", Registries.RECIPE_SERIALIZERS.getIdentifier(recipeBuilder.serializer).toString());

            // Filter by recipe builder
            if(recipeBuilder instanceof ShapedRecipeBuilder){
                // Verify all keys are defined
                Set<Character> characters = new HashSet<>();
                for(String row : ((ShapedRecipeBuilder)recipeBuilder).pattern){
                    for(char c : row.toCharArray()){
                        if(c != ' ' && characters.add(c) && !((ShapedRecipeBuilder)recipeBuilder).inputs.containsKey(c))
                            throw new RuntimeException("Recipe '" + recipeBuilder.identifier + "' is missing an input for character '" + c + "'!");
                    }
                }
                for(Character character : ((ShapedRecipeBuilder)recipeBuilder).inputs.keySet()){
                    if(!characters.contains(character))
                        throw new RuntimeException("Recipe '" + recipeBuilder.identifier + "' has unused input with key '" + character + "'!");
                }

                // Group
                json.addProperty("group", recipeBuilder.group);
                // Pattern
                json.add("pattern", createArray(((ShapedRecipeBuilder)recipeBuilder).pattern));
                // Keys
                JsonObject keysJson = new JsonObject();
                for(Map.Entry<Character,Ingredient> input : ((ShapedRecipeBuilder)recipeBuilder).inputs.entrySet())
                    keysJson.add(input.getKey().toString(), Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, input.getValue()).getOrThrow(false, error -> {}));
                json.add("key", keysJson);
                // Result
                JsonObject resultJson = new JsonObject();
                resultJson.addProperty("item", Registries.ITEMS.getIdentifier(recipeBuilder.output.asItem()).toString());
                if(recipeBuilder.outputCount != 1)
                    resultJson.addProperty("count", recipeBuilder.outputCount);
                if(recipeBuilder.outputTag != null)
                    resultJson.addProperty("nbt", recipeBuilder.outputTag.toString());
                json.add("result", resultJson);

            }else if(recipeBuilder instanceof ShapelessRecipeBuilder){
                // Group
                json.addProperty("group", recipeBuilder.group);
                // Ingredients
                JsonArray ingredientsJson = new JsonArray();
                for(Ingredient input : ((ShapelessRecipeBuilder)recipeBuilder).inputs)
                    ingredientsJson.add(Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, input).getOrThrow(false, error -> {}));
                json.add("ingredients", ingredientsJson);
                // Result
                JsonObject resultJson = new JsonObject();
                resultJson.addProperty("item", Registries.ITEMS.getIdentifier(recipeBuilder.output.asItem()).toString());
                if(recipeBuilder.outputCount != 1)
                    resultJson.addProperty("count", recipeBuilder.outputCount);
                if(recipeBuilder.outputTag != null)
                    resultJson.addProperty("nbt", recipeBuilder.outputTag.toString());
                json.add("result", resultJson);

            }else if(recipeBuilder instanceof SmeltingRecipeBuilder){
                if(((SmeltingRecipeBuilder)recipeBuilder).includeBlasting){
                    JsonObject recipeJson = new JsonObject();
                    recipeJson.addProperty("type", "minecraft:blasting");
                    serializeCookingRecipe(recipeJson, (SmeltingRecipeBuilder)recipeBuilder, 2, 100);
                    subRecipes.put("_blasting", recipeJson);
                }
                if(((SmeltingRecipeBuilder)recipeBuilder).includeSmoking){
                    JsonObject recipeJson = new JsonObject();
                    recipeJson.addProperty("type", "minecraft:smoking");
                    serializeCookingRecipe(json, (SmeltingRecipeBuilder)recipeBuilder, 2, 100);
                    subRecipes.put("_smoking", recipeJson);
                }
                if(((SmeltingRecipeBuilder)recipeBuilder).includeCampfire){
                    JsonObject recipeJson = new JsonObject();
                    recipeJson.addProperty("type", "minecraft:campfire_cooking");
                    serializeCookingRecipe(json, (SmeltingRecipeBuilder)recipeBuilder, 2, 100);
                    subRecipes.put("_campfire", recipeJson);
                }
                if(((SmeltingRecipeBuilder)recipeBuilder).includeSmelting)
                    serializeCookingRecipe(json, (SmeltingRecipeBuilder)recipeBuilder, 1, 200);
                else
                    subRecipes.remove("");
            }else if(recipeBuilder instanceof SmithingRecipeBuilder){
                // Group
                json.addProperty("group", recipeBuilder.group);
                // Base
                json.add("base", Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ((SmithingRecipeBuilder)recipeBuilder).base).getOrThrow(false, error -> {}));
                // Addition
                json.add("addition", Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ((SmithingRecipeBuilder)recipeBuilder).addition).getOrThrow(false, error -> {}));
                // Result
                JsonObject resultJson = new JsonObject();
                resultJson.addProperty("item", Registries.ITEMS.getIdentifier(recipeBuilder.output.asItem()).toString());
                if(recipeBuilder.outputCount != 1)
                    resultJson.addProperty("count", recipeBuilder.outputCount);
                if(recipeBuilder.outputTag != null)
                    resultJson.addProperty("nbt", recipeBuilder.outputTag.toString());
                json.add("result", resultJson);

            }else if(recipeBuilder instanceof StoneCuttingRecipeBuilder){
                // Group
                json.addProperty("group", recipeBuilder.group);
                // Ingredient
                json.add("ingredient", Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ((StoneCuttingRecipeBuilder)recipeBuilder).input).getOrThrow(false, error -> {}));
                // Result
                json.addProperty("result", Registries.ITEMS.getIdentifier(recipeBuilder.output.asItem()).toString());
                // Count
                json.addProperty("count", recipeBuilder.outputCount);
            }

            for(Map.Entry<String,JsonObject> subRecipe : subRecipes.entrySet()){
                json = subRecipe.getValue();

                // Conditions
                if(!recipeBuilder.conditions.isEmpty())
                    json = ConditionalRecipeSerializer.wrapRecipeWithForgeConditions(json, recipeBuilder.conditions);

                // Save the object to the cache
                ResourceLocation identifier = recipeBuilder.identifier;
                this.cache.saveJsonResource(ResourceType.DATA, json, identifier.getNamespace(), "recipes", identifier.getPath() + subRecipe.getKey());
            }
        }

        // Save the advancements
        this.advancements.save();
    }

    private static void serializeCookingRecipe(JsonObject json, SmeltingRecipeBuilder recipeBuilder, int durationDivider, int defaultDuration){
        // Group
        json.addProperty("group", ((RecipeBuilder<?>)recipeBuilder).group);
        // Ingredient
        json.add("ingredient", Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, recipeBuilder.input).getOrThrow(false, error -> {}));
        // Result
        if(((RecipeBuilder<?>)recipeBuilder).outputTag == null && ((RecipeBuilder<?>)recipeBuilder).outputCount == 1)
            json.addProperty("result", Registries.ITEMS.getIdentifier(((RecipeBuilder<?>)recipeBuilder).output.asItem()).toString());
        else{
            JsonObject resultJson = new JsonObject();
            resultJson.addProperty("item", Registries.ITEMS.getIdentifier(((RecipeBuilder<?>)recipeBuilder).output.asItem()).toString());
            if(((RecipeBuilder<?>)recipeBuilder).outputCount != 1)
                resultJson.addProperty("count", ((RecipeBuilder<?>)recipeBuilder).outputCount);
            if(((RecipeBuilder<?>)recipeBuilder).outputTag != null)
                resultJson.addProperty("nbt", ((RecipeBuilder<?>)recipeBuilder).outputTag.toString());
            json.add("result", resultJson);
        }
        // Experience
        if(recipeBuilder.experience != 0)
            json.addProperty("experience", recipeBuilder.experience);
        // Duration
        int duration = recipeBuilder.duration / durationDivider;
        if(duration != defaultDuration)
            json.addProperty("cookingtime", duration);
    }

    private static JsonArray createArray(Iterable<String> elements){
        JsonArray array = new JsonArray();
        for(String element : elements)
            array.add(element);
        return array;
    }

    protected <T extends RecipeBuilder<T>> T recipe(ResourceLocation recipeLocation, T builder){
        if(this.recipes.containsKey(recipeLocation))
            throw new RuntimeException("Duplicate recipe '" + recipeLocation + "' of types '" + this.recipes.get(recipeLocation).getClass().getName() + "' and '" + builder.getClass().getName() + "'!");

        this.cache.trackToBeGeneratedResource(ResourceType.DATA, builder.identifier.getNamespace(), "recipes", builder.identifier.getPath(), ".json");
        this.recipes.put(recipeLocation, builder);
        return builder;
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param nbt            nbt tag of the recipe result
     * @param amount         count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(ResourceLocation recipeLocation, ItemLike output, CompoundTag nbt, int amount){
        return this.recipe(recipeLocation, new ShapedRecipeBuilder(recipeLocation, output, nbt, amount));
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(String namespace, String identifier, ItemLike output, CompoundTag nbt, int amount){
        return this.shaped(new ResourceLocation(namespace, identifier), output, nbt, amount);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(String identifier, ItemLike output, CompoundTag nbt, int amount){
        return this.shaped(this.modid, identifier, output, nbt, amount);
    }

    /**
     * Creates a new shaped recipe builder with the output's identifier as location.
     * @param output recipe result
     * @param nbt    nbt tag of the recipe result
     * @param amount count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(ItemLike output, CompoundTag nbt, int amount){
        ResourceLocation identifier = Registries.ITEMS.getIdentifier(output.asItem());
        return this.recipe(identifier, new ShapedRecipeBuilder(identifier, output, nbt, amount));
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param amount         count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(ResourceLocation recipeLocation, ItemLike output, int amount){
        return this.shaped(recipeLocation, output, null, amount);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(String namespace, String identifier, ItemLike output, int amount){
        return this.shaped(new ResourceLocation(namespace, identifier), output, null, amount);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(String identifier, ItemLike output, int amount){
        return this.shaped(this.modid, identifier, output, null, amount);
    }

    /**
     * Creates a new shaped recipe builder with the output's identifier as location.
     * @param output recipe result
     * @param amount count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(ItemLike output, int amount){
        return this.shaped(output, null, amount);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     */
    protected ShapedRecipeBuilder shaped(ResourceLocation recipeLocation, ItemLike output){
        return this.shaped(recipeLocation, output, null, 1);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected ShapedRecipeBuilder shaped(String namespace, String identifier, ItemLike output){
        return this.shaped(new ResourceLocation(namespace, identifier), output, null, 1);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected ShapedRecipeBuilder shaped(String identifier, ItemLike output){
        return this.shaped(this.modid, identifier, output, null, 1);
    }

    /**
     * Creates a new shaped recipe builder with the output's identifier as location.
     * @param output recipe result
     */
    protected ShapedRecipeBuilder shaped(ItemLike output){
        return this.shaped(output, null, 1);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     */
    protected ShapedRecipeBuilder shaped(ResourceLocation recipeLocation, ItemStack output){
        return this.shaped(recipeLocation, output.getItem(), output.hasTag() && !output.getTag().isEmpty() ? output.getTag() : null, output.getCount());
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected ShapedRecipeBuilder shaped(String namespace, String identifier, ItemStack output){
        return this.shaped(new ResourceLocation(namespace, identifier), output.getItem(), output.hasTag() && !output.getTag().isEmpty() ? output.getTag() : null, output.getCount());
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected ShapedRecipeBuilder shaped(String identifier, ItemStack output){
        return this.shaped(this.modid, identifier, output.getItem(), output.hasTag() && !output.getTag().isEmpty() ? output.getTag() : null, output.getCount());
    }

    /**
     * Creates a new shaped recipe builder with the output's identifier as location.
     * @param output recipe result
     */
    protected ShapedRecipeBuilder shaped(ItemStack output){
        return this.shaped(output.getItem(), output.hasTag() && !output.getTag().isEmpty() ? output.getTag() : null, output.getCount());
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param nbt            nbt tag of the recipe result
     * @param amount         count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(ResourceLocation recipeLocation, ItemLike output, CompoundTag nbt, int amount){
        return this.recipe(recipeLocation, new ShapelessRecipeBuilder(recipeLocation, output, nbt, amount));
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String namespace, String identifier, ItemLike output, CompoundTag nbt, int amount){
        return this.shapeless(new ResourceLocation(namespace, identifier), output, nbt, amount);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String identifier, ItemLike output, CompoundTag nbt, int amount){
        return this.shapeless(this.modid, identifier, output, nbt, amount);
    }

    /**
     * Creates a new shapeless recipe builder with the output's identifier as location.
     * @param output recipe result
     * @param nbt    nbt tag of the recipe result
     * @param amount count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(ItemLike output, CompoundTag nbt, int amount){
        ResourceLocation identifier = Registries.ITEMS.getIdentifier(output.asItem());
        return this.shapeless(identifier, output, nbt, amount);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param amount         count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(ResourceLocation recipeLocation, ItemLike output, int amount){
        return this.shapeless(recipeLocation, output, null, amount);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String namespace, String identifier, ItemLike output, int amount){
        return this.shapeless(new ResourceLocation(namespace, identifier), output, null, amount);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String identifier, ItemLike output, int amount){
        return this.shapeless(this.modid, identifier, output, null, amount);
    }

    /**
     * Creates a new shapeless recipe builder with the output's identifier as location.
     * @param output recipe result
     * @param amount count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(ItemLike output, int amount){
        return this.shapeless(output, null, amount);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     */
    protected ShapelessRecipeBuilder shapeless(ResourceLocation recipeLocation, ItemLike output){
        return this.shapeless(recipeLocation, output, null, 1);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String namespace, String identifier, ItemLike output){
        return this.shapeless(new ResourceLocation(namespace, identifier), output, null, 1);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String identifier, ItemLike output){
        return this.shapeless(this.modid, identifier, output, null, 1);
    }

    /**
     * Creates a new shapeless recipe builder with the output's identifier as location.
     * @param output recipe result
     */
    protected ShapelessRecipeBuilder shapeless(ItemLike output){
        return this.shapeless(output, null, 1);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     */
    protected ShapelessRecipeBuilder shapeless(ResourceLocation recipeLocation, ItemStack output){
        return this.shapeless(recipeLocation, output.getItem(), output.hasTag() && !output.getTag().isEmpty() ? output.getTag() : null, output.getCount());
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String namespace, String identifier, ItemStack output){
        return this.shapeless(new ResourceLocation(namespace, identifier), output.getItem(), output.hasTag() && !output.getTag().isEmpty() ? output.getTag() : null, output.getCount());
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String identifier, ItemStack output){
        return this.shapeless(this.modid, identifier, output.getItem(), output.hasTag() && !output.getTag().isEmpty() ? output.getTag() : null, output.getCount());
    }

    /**
     * Creates a new shapeless recipe builder with the output's identifier as location.
     * @param output recipe result
     */
    protected ShapelessRecipeBuilder shapeless(ItemStack output){
        return this.shapeless(output.getItem(), output.hasTag() && !output.getTag().isEmpty() ? output.getTag() : null, output.getCount());
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param nbt            nbt tag of the recipe result
     * @param amount         count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(ResourceLocation recipeLocation, ItemLike output, CompoundTag nbt, int amount){
        return this.recipe(recipeLocation, new SmeltingRecipeBuilder(recipeLocation, output, nbt, amount)).includeSmelting();
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(String namespace, String identifier, ItemLike output, CompoundTag nbt, int amount){
        return this.smelting(new ResourceLocation(namespace, identifier), output, nbt, amount);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(String identifier, ItemLike output, CompoundTag nbt, int amount){
        return this.smelting(this.modid, identifier, output, nbt, amount);
    }

    /**
     * Creates a new smelting recipe builder with the output's identifier as location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param output recipe result
     * @param nbt    nbt tag of the recipe result
     * @param amount count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(ItemLike output, CompoundTag nbt, int amount){
        ResourceLocation identifier = Registries.ITEMS.getIdentifier(output.asItem());
        return this.smelting(identifier, output, nbt, amount);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param amount         count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(ResourceLocation recipeLocation, ItemLike output, int amount){
        return this.smelting(recipeLocation, output, null, amount);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(String namespace, String identifier, ItemLike output, int amount){
        return this.smelting(new ResourceLocation(namespace, identifier), output, null, amount);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(String identifier, ItemLike output, int amount){
        return this.smelting(this.modid, identifier, output, null, amount);
    }

    /**
     * Creates a new smelting recipe builder with the output's identifier as location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param output recipe result
     * @param amount count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(ItemLike output, int amount){
        return this.smelting(output, null, amount);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     */
    protected SmeltingRecipeBuilder smelting(ResourceLocation recipeLocation, ItemLike output){
        return this.smelting(recipeLocation, output, null, 1);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected SmeltingRecipeBuilder smelting(String namespace, String identifier, ItemLike output){
        return this.smelting(new ResourceLocation(namespace, identifier), output, null, 1);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected SmeltingRecipeBuilder smelting(String identifier, ItemLike output){
        return this.smelting(this.modid, identifier, output, null, 1);
    }

    /**
     * Creates a new smelting recipe builder with the output's identifier as location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param output recipe result
     */
    protected SmeltingRecipeBuilder smelting(ItemLike output){
        return this.smelting(output, null, 1);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     */
    protected SmeltingRecipeBuilder smelting(ResourceLocation recipeLocation, ItemStack output){
        return this.smelting(recipeLocation, output.getItem(), output.hasTag() && !output.getTag().isEmpty() ? output.getTag() : null, output.getCount());
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected SmeltingRecipeBuilder smelting(String namespace, String identifier, ItemStack output){
        return this.smelting(new ResourceLocation(namespace, identifier), output.getItem(), output.hasTag() && !output.getTag().isEmpty() ? output.getTag() : null, output.getCount());
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected SmeltingRecipeBuilder smelting(String identifier, ItemStack output){
        return this.smelting(this.modid, identifier, output.getItem(), output.hasTag() && !output.getTag().isEmpty() ? output.getTag() : null, output.getCount());
    }

    /**
     * Creates a new smelting recipe builder with the output's identifier as location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param output recipe result
     */
    protected SmeltingRecipeBuilder smelting(ItemStack output){
        return this.smelting(output.getItem(), output.hasTag() && !output.getTag().isEmpty() ? output.getTag() : null, output.getCount());
    }

    /**
     * Creates a new smithing recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param nbt            nbt tag of the recipe result
     * @param amount         count of the recipe result
     */
    protected SmithingRecipeBuilder smithing(ResourceLocation recipeLocation, ItemLike output, CompoundTag nbt, int amount){
        return this.recipe(recipeLocation, new SmithingRecipeBuilder(recipeLocation, output, nbt, amount));
    }

    /**
     * Creates a new smithing recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected SmithingRecipeBuilder smithing(String namespace, String identifier, ItemLike output, CompoundTag nbt, int amount){
        return this.smithing(new ResourceLocation(namespace, identifier), output, nbt, amount);
    }

    /**
     * Creates a new smithing recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected SmithingRecipeBuilder smithing(String identifier, ItemLike output, CompoundTag nbt, int amount){
        return this.smithing(this.modid, identifier, output, nbt, amount);
    }

    /**
     * Creates a new smithing recipe builder with the output's identifier as location.
     * @param output recipe result
     * @param nbt    nbt tag of the recipe result
     * @param amount count of the recipe result
     */
    protected SmithingRecipeBuilder smithing(ItemLike output, CompoundTag nbt, int amount){
        ResourceLocation identifier = Registries.ITEMS.getIdentifier(output.asItem());
        return this.smithing(identifier, output, nbt, amount);
    }

    /**
     * Creates a new smithing recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param amount         count of the recipe result
     */
    protected SmithingRecipeBuilder smithing(ResourceLocation recipeLocation, ItemLike output, int amount){
        return this.smithing(recipeLocation, output, null, amount);
    }

    /**
     * Creates a new smithing recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected SmithingRecipeBuilder smithing(String namespace, String identifier, ItemLike output, int amount){
        return this.smithing(new ResourceLocation(namespace, identifier), output, null, amount);
    }

    /**
     * Creates a new smithing recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected SmithingRecipeBuilder smithing(String identifier, ItemLike output, int amount){
        return this.smithing(this.modid, identifier, output, null, amount);
    }

    /**
     * Creates a new smithing recipe builder with the output's identifier as location.
     * @param output recipe result
     * @param amount count of the recipe result
     */
    protected SmithingRecipeBuilder smithing(ItemLike output, int amount){
        return this.smithing(output, null, amount);
    }

    /**
     * Creates a new smithing recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     */
    protected SmithingRecipeBuilder smithing(ResourceLocation recipeLocation, ItemLike output){
        return this.smithing(recipeLocation, output, null, 1);
    }

    /**
     * Creates a new smithing recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected SmithingRecipeBuilder smithing(String namespace, String identifier, ItemLike output){
        return this.smithing(new ResourceLocation(namespace, identifier), output, null, 1);
    }

    /**
     * Creates a new smithing recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected SmithingRecipeBuilder smithing(String identifier, ItemLike output){
        return this.smithing(this.modid, identifier, output, null, 1);
    }

    /**
     * Creates a new smithing recipe builder with the output's identifier as location.
     * @param output recipe result
     */
    protected SmithingRecipeBuilder smithing(ItemLike output){
        return this.smithing(output, null, 1);
    }

    /**
     * Creates a new smithing recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     */
    protected SmithingRecipeBuilder smithing(ResourceLocation recipeLocation, ItemStack output){
        return this.smithing(recipeLocation, output.getItem(), output.hasTag() && !output.getTag().isEmpty() ? output.getTag() : null, output.getCount());
    }

    /**
     * Creates a new smithing recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected SmithingRecipeBuilder smithing(String namespace, String identifier, ItemStack output){
        return this.smithing(new ResourceLocation(namespace, identifier), output.getItem(), output.hasTag() && !output.getTag().isEmpty() ? output.getTag() : null, output.getCount());
    }

    /**
     * Creates a new smithing recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected SmithingRecipeBuilder smithing(String identifier, ItemStack output){
        return this.smithing(this.modid, identifier, output.getItem(), output.hasTag() && !output.getTag().isEmpty() ? output.getTag() : null, output.getCount());
    }

    /**
     * Creates a new smithing recipe builder with the output's identifier as location.
     * @param output recipe result
     */
    protected SmithingRecipeBuilder smithing(ItemStack output){
        return this.smithing(output.getItem(), output.hasTag() && !output.getTag().isEmpty() ? output.getTag() : null, output.getCount());
    }

    /**
     * Creates a new stonecutting recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param amount         count of the recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(ResourceLocation recipeLocation, ItemLike output, int amount){
        return this.recipe(recipeLocation, new StoneCuttingRecipeBuilder(recipeLocation, output, amount));
    }

    /**
     * Creates a new stonecutting recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(String namespace, String identifier, ItemLike output, int amount){
        return this.stoneCutting(new ResourceLocation(namespace, identifier), output, amount);
    }

    /**
     * Creates a new stonecutting recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(String identifier, ItemLike output, int amount){
        return this.stoneCutting(this.modid, identifier, output, amount);
    }

    /**
     * Creates a new stonecutting recipe builder with the output's identifier as location.
     * @param output recipe result
     * @param amount count of the recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(ItemLike output, int amount){
        ResourceLocation identifier = Registries.ITEMS.getIdentifier(output.asItem());
        return this.stoneCutting(identifier, output, amount);
    }

    /**
     * Creates a new stonecutting recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(ResourceLocation recipeLocation, ItemLike output){
        return this.stoneCutting(recipeLocation, output, 1);
    }

    /**
     * Creates a new stonecutting recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(String namespace, String identifier, ItemLike output){
        return this.stoneCutting(new ResourceLocation(namespace, identifier), output, 1);
    }

    /**
     * Creates a new stonecutting recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(String identifier, ItemLike output){
        return this.stoneCutting(this.modid, identifier, output, 1);
    }

    /**
     * Creates a new stonecutting recipe builder with the output's identifier as location.
     * @param output recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(ItemLike output){
        return this.stoneCutting(output, 1);
    }

    @Override
    public String getName(){
        return this.modName + " Recipe Generator";
    }

    public static abstract class RecipeBuilder<T extends RecipeBuilder<T>> {

        protected final ResourceLocation identifier;
        private final List<ICondition> conditions = new ArrayList<>();
        private final ItemLike output;
        private final CompoundTag outputTag;
        private final int outputCount;
        private RecipeSerializer<?> serializer;
        private String group;
        private boolean hasAdvancement = true;
        private final List<Pair<CriterionTrigger<?>,CriterionTriggerInstance>> unlockedBy = new ArrayList<>();

        protected RecipeBuilder(ResourceLocation identifier, RecipeSerializer<?> serializer, ItemLike output, CompoundTag outputTag, int outputCount){
            this.identifier = identifier;
            this.output = output;
            this.outputTag = outputTag;
            this.outputCount = outputCount;
            this.serializer = serializer;
        }

        /**
         * Sets the group for this recipe. Multiple recipes with the same group will be grouped together in the recipe book.
         * @param group group for the recipe
         */
        public T group(String group){
            this.group = group == null || group.trim().isEmpty() ? null : group;
            return this.self();
        }

        /**
         * Adds a condition for this recipe to be loaded.
         */
        public T condition(ICondition condition){
            this.conditions.add(condition);
            return this.self();
        }

        /**
         * Adds a condition for this recipe to be loaded.
         */
        public T condition(ResourceCondition condition){
            return this.condition(ResourceCondition.createForgeCondition(condition));
        }

        /**
         * Adds a condition to only load this recipe when the given condition is <b>not</b> satisfied.
         */
        public T notCondition(ICondition condition){
            return this.condition(new NotResourceCondition(condition));
        }

        /**
         * Adds a condition to only load this recipe when the given condition is <b>not</b> satisfied.
         */
        public T notCondition(ResourceCondition condition){
            return this.condition(new NotResourceCondition(condition));
        }

        /**
         * Adds a condition to only load this recipe when a mod with the given modid is present.
         */
        public T modLoadedCondition(String modid){
            return this.condition(new ModLoadedResourceCondition(modid));
        }

        /**
         * Sets whether to generate an advancement to unlock this recipe.
         * @param generate whether to generate an advancement
         */
        public T advancement(boolean generate){
            this.hasAdvancement = generate;
            return this.self();
        }

        /**
         * Sets to not generate an advancement for this recipe.
         */
        public T noAdvancement(){
            return this.advancement(false);
        }

        /**
         * Sets which criterion should be met to unlock this recipe in its generated advancement.
         */
        public <S extends CriterionTriggerInstance> T unlockedBy(CriterionTrigger<S> trigger, S instance){
            this.unlockedBy.add(Pair.of(trigger, instance));
            return this.self();
        }

        /**
         * Sets which criterion should be met to unlock this recipe in its generated advancement.
         */
        public <S extends CriterionTriggerInstance> T unlockedBy(Criterion<S> criterion){
            return this.unlockedBy(criterion.trigger(), criterion.triggerInstance());
        }

        /**
         * Sets which items the player should have to unlock this recipe in its generated advancement.
         */
        public T unlockedBy(ItemLike... items){
            return this.unlockedBy(InventoryChangeTrigger.TriggerInstance.hasItems(items));
        }

        /**
         * Sets which items the player should have to unlock this recipe in its generated advancement.
         */
        public T unlockedBy(TagKey<Item> tagKey){
            return this.unlockedBy(InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tagKey).build()));
        }

        /**
         * Sets a different recipe serializer. This may not have an effect for all recipe types, most notably the smelting recipes.
         */
        public T customSerializer(RecipeSerializer<?> serializer){
            this.serializer = serializer;
            return this.self();
        }

        private T self(){
            //noinspection unchecked
            return (T)this;
        }
    }

    protected static class ShapedRecipeBuilder extends RecipeBuilder<ShapedRecipeBuilder> {

        private final List<String> pattern = new ArrayList<>();
        private final Map<Character,Ingredient> inputs = new HashMap<>();

        private ShapedRecipeBuilder(ResourceLocation identifier, ItemLike output, CompoundTag outputTag, int outputCount){
            super(identifier, RecipeSerializer.SHAPED_RECIPE, output, outputTag, outputCount);
        }

        /**
         * Adds a row to the pattern for this recipe.
         * The row should consist of at most 3 characters, where {@code ' '} (space) may be used for an empty space.
         * All characters used should be defined using {@link #input(char, Ingredient)}.
         * @param row a row for the pattern
         */
        public ShapedRecipeBuilder pattern(String row){
            if(row.isEmpty())
                throw new IllegalArgumentException("Pattern row for recipe '" + this.identifier + "' cannot be empty!");
            if(row.length() > 3)
                throw new IllegalArgumentException("Pattern row for recipe '" + this.identifier + "' can have at most 3 characters, not '" + row.length() + "'!");
            for(String otherRow : this.pattern){
                if(row.length() != otherRow.length())
                    throw new IllegalArgumentException("Pattern rows for recipe '" + this.identifier + "' must have the same length!");
            }

            this.pattern.add(row);
            return this;
        }

        /**
         * Adds the given rows to the pattern for this recipe.
         * Each row should consist of at most 3 characters, where {@code ' '} (space) may be used for an empty space.
         * All characters used should be defined using {@link #input(char, Ingredient)}.
         * @param rows rows for the pattern
         */
        public ShapedRecipeBuilder pattern(String... rows){
            for(String row : rows)
                this.pattern(row);
            return this;
        }

        /**
         * Defines the ingredient corresponding to the given character. These characters may be used in the pattern for this recipe.
         * @param key        key to be defined
         * @param ingredient ingredient to be associated with the key
         */
        public ShapedRecipeBuilder input(char key, Ingredient ingredient){
            if(this.inputs.containsKey(key))
                throw new RuntimeException("Duplicate key '" + key + "' for recipe '" + this.identifier + "'!");

            this.inputs.put(key, ingredient);
            return this;
        }

        /**
         * Defines the ingredient corresponding to the given character. These characters may be used in the pattern for this recipe.
         * @param key         key to be defined
         * @param ingredients ingredients to be associated with the key
         */
        public ShapedRecipeBuilder input(char key, Ingredient... ingredients){
            return this.input(key, mergeIngredients(ingredients));
        }

        /**
         * Defines the ingredient corresponding to the given character. These characters may be used in the pattern for this recipe.
         * @param key   key to be defined
         * @param items items to be associated with the key
         */
        public ShapedRecipeBuilder input(char key, ItemLike... items){
            return this.input(key, Ingredient.of(items));
        }

        /**
         * Defines the ingredient corresponding to the given character. These characters may be used in the pattern for this recipe.
         * @param key        key to be defined
         * @param itemStacks items to be associated with the key
         */
        public ShapedRecipeBuilder input(char key, ItemStack... itemStacks){
            return this.input(key, Ingredient.of(itemStacks));
        }

        /**
         * Defines the ingredient corresponding to the given character. These characters may be used in the pattern for this recipe.
         * @param key key to be defined
         * @param tag tag to be associated with the key
         */
        public ShapedRecipeBuilder input(char key, TagKey<Item> tag){
            return this.input(key, Ingredient.of(tag));
        }
    }

    protected static class ShapelessRecipeBuilder extends RecipeBuilder<ShapelessRecipeBuilder> {

        private final List<Ingredient> inputs = new ArrayList<>();

        private ShapelessRecipeBuilder(ResourceLocation identifier, ItemLike output, CompoundTag outputTag, int outputCount){
            super(identifier, RecipeSerializer.SHAPELESS_RECIPE, output, outputTag, outputCount);
        }

        /**
         * Adds an ingredient for this recipe. The ingredient will be added {@code count} times.
         * @param ingredient ingredient to be added
         * @param count      the number of times to add the ingredient
         */
        public ShapelessRecipeBuilder input(Ingredient ingredient, int count){
            if(count <= 0)
                throw new IllegalArgumentException("Cannot add an ingredient '" + count + "' times to recipe '" + this.identifier + "'!");
            if(this.inputs.size() + count > 9)
                throw new RuntimeException("Recipe '" + this.identifier + "' can have at most 9 inputs!");

            for(int i = 0; i < count; i++)
                this.inputs.add(ingredient);
            return this;
        }

        /**
         * Adds an ingredient for this recipe.
         * @param ingredient ingredient to be added
         */
        public ShapelessRecipeBuilder input(Ingredient ingredient){
            return this.input(ingredient, 1);
        }

        /**
         * Adds an item ingredient for this recipe. The ingredient will be added {@code count} times.
         * @param item  ingredient to be added
         * @param count the number of times to add the ingredient
         */
        public ShapelessRecipeBuilder input(ItemLike item, int count){
            return this.input(Ingredient.of(item), count);
        }

        /**
         * Adds an item ingredient for this recipe.
         * @param item ingredient to be added
         */
        public ShapelessRecipeBuilder input(ItemLike item){
            return this.input(item, 1);
        }

        /**
         * Adds an item stack ingredient for this recipe. The ingredient will be added {@code count} times.
         * @param itemStack ingredient to be added
         * @param count     the number of times to add the ingredient
         */
        public ShapelessRecipeBuilder input(ItemStack itemStack, int count){
            return this.input(Ingredient.of(itemStack), count);
        }

        /**
         * Adds an item stack ingredient for this recipe.
         * @param itemStack ingredient to be added
         */
        public ShapelessRecipeBuilder input(ItemStack itemStack){
            return this.input(itemStack, 1);
        }

        /**
         * Adds a tag ingredient for this recipe. The ingredient will be added {@code count} times.
         * @param tag   ingredient to be added
         * @param count the number of times to add the ingredient
         */
        public ShapelessRecipeBuilder input(TagKey<Item> tag, int count){
            return this.input(Ingredient.of(tag), count);
        }

        /**
         * Adds a tag ingredient for this recipe. The ingredient will be added {@code count} times.
         * @param tag ingredient to be added
         */
        public ShapelessRecipeBuilder input(TagKey<Item> tag){
            return this.input(Ingredient.of(tag), 1);
        }

        /**
         * Adds all the given ingredients to this recipe.
         * @param ingredients ingredients to be added
         */
        public ShapelessRecipeBuilder inputs(Ingredient... ingredients){
            for(Ingredient ingredient : ingredients)
                this.input(ingredient);
            return this;
        }

        /**
         * Adds all the given items as ingredients to this recipe.
         * @param items ingredients to be added
         */
        public ShapelessRecipeBuilder inputs(ItemLike... items){
            for(ItemLike item : items)
                this.input(item);
            return this;
        }

        /**
         * Adds all the given item stacks as ingredients to this recipe.
         * @param itemStacks ingredients to be added
         */
        public ShapelessRecipeBuilder inputs(ItemStack... itemStacks){
            for(ItemStack itemStack : itemStacks)
                this.input(itemStack);
            return this;
        }
    }

    protected static class SmeltingRecipeBuilder extends RecipeBuilder<SmeltingRecipeBuilder> {

        private boolean includeSmelting;
        private boolean includeBlasting;
        private boolean includeCampfire;
        private boolean includeSmoking;
        private Ingredient input;
        private int experience;
        private int duration = 200;

        private SmeltingRecipeBuilder(ResourceLocation identifier, ItemLike output, CompoundTag outputTag, int count){
            super(identifier, RecipeSerializer.SMELTING_RECIPE, output, outputTag, count);
        }

        /**
         * Whether to generate a furnace smelting recipe.
         */
        public SmeltingRecipeBuilder includeSmelting(boolean includeSmelting){
            this.includeSmelting = includeSmelting;
            return this;
        }

        /**
         * Sets to generate a furnace smelting recipe.
         */
        public SmeltingRecipeBuilder includeSmelting(){
            return this.includeSmelting(true);
        }

        /**
         * Whether to generate a blasting recipe.
         */
        public SmeltingRecipeBuilder includeBlasting(boolean includeBlasting){
            this.includeBlasting = includeBlasting;
            return this;
        }

        /**
         * Sets to generate a blasting recipe.
         */
        public SmeltingRecipeBuilder includeBlasting(){
            return this.includeBlasting(true);
        }

        /**
         * Whether to generate a campfire cooking recipe.
         */
        public SmeltingRecipeBuilder includeCampfire(boolean includeCampfire){
            this.includeCampfire = includeCampfire;
            return this;
        }

        /**
         * Sets to generate a campfire cooking recipe.
         */
        public SmeltingRecipeBuilder includeCampfire(){
            return this.includeCampfire(true);
        }

        /**
         * Whether to generate a smoking recipe.
         */
        public SmeltingRecipeBuilder includeSmoking(boolean includeSmoking){
            this.includeSmoking = includeSmoking;
            return this;
        }

        /**
         * Sets to generate a smoking recipe.
         */
        public SmeltingRecipeBuilder includeSmoking(){
            return this.includeSmoking(true);
        }

        /**
         * Sets the input for this recipe.
         * @param ingredient input ingredient
         */
        public SmeltingRecipeBuilder input(Ingredient ingredient){
            this.input = ingredient;
            return this;
        }

        /**
         * Sets the input for this recipe.
         * @param ingredients ingredients to be accepted as input
         */
        public SmeltingRecipeBuilder input(Ingredient... ingredients){
            return this.input(mergeIngredients(ingredients));
        }

        /**
         * Sets the input for this recipe.
         * @param items items to be accepted as input
         */
        public SmeltingRecipeBuilder input(ItemLike... items){
            return this.input(Ingredient.of(items));
        }

        /**
         * Sets the input for this recipe.
         * @param itemStacks items to be accepted as input
         */
        public SmeltingRecipeBuilder input(ItemStack... itemStacks){
            return this.input(Ingredient.of(itemStacks));
        }

        /**
         * Sets the input for this recipe.
         * @param tag item tag to be accepted as input
         */
        public SmeltingRecipeBuilder input(TagKey<Item> tag){
            return this.input(Ingredient.of(tag));
        }

        /**
         * Sets the experience gained from this recipe.
         * @param experience the amount of experience
         */
        public SmeltingRecipeBuilder experience(int experience){
            if(experience < 0)
                throw new IllegalArgumentException("Experience for recipe '" + this.identifier + "' cannot be negative!");

            this.experience = experience;
            return this;
        }

        /**
         * Sets the duration of this recipe. The given duration corresponds to the duration for the furnace recipe.
         * Blasting, smoking and campfire cooking will have half the given duration.
         * @param ticks the duration in ticks
         */
        public SmeltingRecipeBuilder duration(int ticks){
            if(ticks <= 0)
                throw new IllegalArgumentException("Duration for recipe '" + this.identifier + "' must be greater than 0!");

            this.duration = ticks;
            return this;
        }

        /**
         * Sets the duration of this recipe. The given duration corresponds to the duration for the furnace recipe.
         * Blasting, smoking and campfire cooking will have half the given duration.
         * @param seconds the duration in seconds
         */
        public SmeltingRecipeBuilder durationSeconds(int seconds){
            return this.duration(seconds * 20);
        }
    }

    protected static class SmithingRecipeBuilder extends RecipeBuilder<SmithingRecipeBuilder> {

        private Ingredient base, addition;

        private SmithingRecipeBuilder(ResourceLocation identifier, ItemLike output, CompoundTag outputTag, int outputCount){
            super(identifier, RecipeSerializer.SMITHING_TRANSFORM, output, outputTag, outputCount);
        }

        /**
         * Sets the base ingredient for this recipe.
         * @param ingredient ingredient to be used as base
         */
        public SmithingRecipeBuilder base(Ingredient ingredient){
            this.base = ingredient;
            return this;
        }

        /**
         * Sets the base ingredient for this recipe.
         * @param ingredients ingredients to be accepted as base
         */
        public SmithingRecipeBuilder base(Ingredient... ingredients){
            return this.base(mergeIngredients(ingredients));
        }

        /**
         * Sets the base ingredient for this recipe.
         * @param items items to be accepted as base
         */
        public SmithingRecipeBuilder base(ItemLike... items){
            return this.base(Ingredient.of(items));
        }

        /**
         * Sets the base ingredient for this recipe.
         * @param itemStacks items to be accepted as base
         */
        public SmithingRecipeBuilder base(ItemStack... itemStacks){
            return this.base(Ingredient.of(itemStacks));
        }

        /**
         * Sets the base ingredient for this recipe.
         * @param tag item tag to be accepted as base
         */
        public SmithingRecipeBuilder base(TagKey<Item> tag){
            return this.base(Ingredient.of(tag));
        }

        /**
         * Sets the addition ingredient for this recipe.
         * @param ingredient ingredient to be used as addition
         */
        public SmithingRecipeBuilder addition(Ingredient ingredient){
            this.addition = ingredient;
            return this;
        }

        /**
         * Sets the addition ingredient for this recipe.
         * @param ingredients ingredients to be accepted as addition
         */
        public SmithingRecipeBuilder addition(Ingredient... ingredients){
            return this.addition(mergeIngredients(ingredients));
        }

        /**
         * Sets the addition ingredient for this recipe.
         * @param items items to be accepted as addition
         */
        public SmithingRecipeBuilder addition(ItemLike... items){
            return this.addition(Ingredient.of(items));
        }

        /**
         * Sets the addition ingredient for this recipe.
         * @param itemStacks items to be accepted as addition
         */
        public SmithingRecipeBuilder addition(ItemStack... itemStacks){
            return this.addition(Ingredient.of(itemStacks));
        }

        /**
         * Sets the addition ingredient for this recipe.
         * @param tag item tag to be accepted as addition
         */
        public SmithingRecipeBuilder addition(TagKey<Item> tag){
            return this.addition(Ingredient.of(tag));
        }
    }

    protected static class StoneCuttingRecipeBuilder extends RecipeBuilder<StoneCuttingRecipeBuilder> {

        private Ingredient input;

        private StoneCuttingRecipeBuilder(ResourceLocation identifier, ItemLike output, int outputCount){
            super(identifier, RecipeSerializer.STONECUTTER, output, null, outputCount);
        }

        /**
         * Sets the input ingredient for this recipe.
         * @param ingredient ingredient to be used as input
         */
        public StoneCuttingRecipeBuilder input(Ingredient ingredient){
            this.input = ingredient;
            return this;
        }

        /**
         * Sets the input ingredient for this recipe.
         * @param ingredients ingredients to be accepted as input
         */
        public StoneCuttingRecipeBuilder input(Ingredient... ingredients){
            return this.input(mergeIngredients(ingredients));
        }

        /**
         * Sets the input ingredient for this recipe.
         * @param items items to be accepted as input
         */
        public StoneCuttingRecipeBuilder input(ItemLike... items){
            return this.input(Ingredient.of(items));
        }

        /**
         * Sets the input ingredient for this recipe.
         * @param itemStacks items to be accepted as input
         */
        public StoneCuttingRecipeBuilder input(ItemStack... itemStacks){
            return this.input(Ingredient.of(itemStacks));
        }

        /**
         * Sets the input ingredient for this recipe.
         * @param tag item tag to be accepted as input
         */
        public StoneCuttingRecipeBuilder input(TagKey<Item> tag){
            return this.input(Ingredient.of(tag));
        }
    }

    private final class Advancements extends AdvancementGenerator {

        public Advancements(String modid, ResourceCache cache){
            super(modid, cache);
        }

        @Override
        public void generate(){
            for(RecipeBuilder<?> recipe : RecipeGenerator.this.recipes.values()){
                if(!recipe.hasAdvancement)
                    continue;

                Item outputItem = recipe.output.asItem();
                String category = RecipeCategory.MISC.getFolderName();
                for(CreativeModeTab tab : CreativeModeTabs.allTabs()){
                    if(tab.contains(outputItem.getDefaultInstance())){
                        ResourceKey<CreativeModeTab> key = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(tab).orElse(null);
                        if(key != null){
                            category = TAB_TO_CATEGORY.containsKey(key) ?
                                TAB_TO_CATEGORY.get(key).getFolderName() :
                                key.location().getPath();
                        }
                    }
                }
                String namespace = recipe.identifier.getNamespace();
                String identifier = "recipes/" + category + "/" + recipe.identifier.getPath();

                if(recipe instanceof SmeltingRecipeBuilder){
                    if(((SmeltingRecipeBuilder)recipe).includeSmelting)
                        this.createAdvancement(namespace, identifier + "_smelting", recipe);
                    if(((SmeltingRecipeBuilder)recipe).includeBlasting)
                        this.createAdvancement(namespace, identifier + "_blasting", recipe);
                    if(((SmeltingRecipeBuilder)recipe).includeSmoking)
                        this.createAdvancement(namespace, identifier + "_smoking", recipe);
                    if(((SmeltingRecipeBuilder)recipe).includeCampfire)
                        this.createAdvancement(namespace, identifier + "_campfire", recipe);
                }else
                    this.createAdvancement(namespace, identifier, recipe);
            }
        }

        private void createAdvancement(String namespace, String identifier, RecipeBuilder<?> recipe){
            AdvancementBuilder builder = this.advancement(namespace, identifier)
                .parent(net.minecraft.data.recipes.RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)
                .criterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipe.identifier))
                .icon(recipe.output, recipe.outputTag)
                .dontShowToast()
                .dontAnnounceToChat()
                .rewardRecipe(recipe.identifier);
            String[] triggers = new String[recipe.unlockedBy.size() + 1];
            triggers[0] = "has_the_recipe";
            if(recipe.unlockedBy.size() == 1){
                Pair<CriterionTrigger<?>,CriterionTriggerInstance> criterion = recipe.unlockedBy.get(0);
                //noinspection unchecked,rawtypes
                builder.criterion("recipe_condition", new Criterion(criterion.left(), criterion.right()));
                triggers[1] = "recipe_condition";
            }else{
                for(int i = 0; i < recipe.unlockedBy.size(); i++){
                    Pair<CriterionTrigger<?>,CriterionTriggerInstance> criterion = recipe.unlockedBy.get(i);
                    //noinspection unchecked,rawtypes
                    builder.criterion("recipe_condition" + (i + 1), new Criterion(criterion.left(), criterion.right()));
                    triggers[i + 1] = "recipe_condition" + (i + 1);
                }
            }
            builder.requirementGroup(triggers);
            // Add the same conditions the recipe has to its advancement
            recipe.conditions.forEach(builder::condition);
        }
    }
}
