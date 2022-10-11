package com.supermartijn642.core.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.supermartijn642.core.extensions.ICriterionInstanceExtension;
import com.supermartijn642.core.extensions.IngredientExtension;
import com.supermartijn642.core.recipe.condition.ModLoadedRecipeCondition;
import com.supermartijn642.core.recipe.condition.RecipeCondition;
import com.supermartijn642.core.recipe.condition.RecipeConditionSerializer;
import com.supermartijn642.core.recipe.condition.RecipeConditions;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.critereon.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.advancements.critereon.OredictItemPredicate;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.oredict.OreIngredient;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

/**
 * Created 23/08/2022 by SuperMartijn642
 */
public abstract class RecipeGenerator extends ResourceGenerator {

    /**
     * {@link CraftingHelper.recipes}
     */
    @SuppressWarnings("JavadocReference")
    private static final Supplier<Map<ResourceLocation,IRecipeFactory>> CRAFTING_HELPER_RECIPES;

    static{
        try{
            Field field = CraftingHelper.class.getDeclaredField("recipes");
            field.setAccessible(true);
            CRAFTING_HELPER_RECIPES = () -> {
                try{
                    //noinspection unchecked
                    return (Map<ResourceLocation,IRecipeFactory>)field.get(null);
                }catch(IllegalAccessException e){
                    throw new RuntimeException(e);
                }
            };
        }catch(NoSuchFieldException e){
            throw new RuntimeException(e);
        }
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

            // Set the recipe serializer
            json.addProperty("type", recipeBuilder.serializer);

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
                    keysJson.add(input.getKey().toString(), ((IngredientExtension)input.getValue()).coreLibSerialize());
                json.add("key", keysJson);
                // Result
                JsonObject resultJson = new JsonObject();
                resultJson.addProperty("item", Registries.ITEMS.getIdentifier(recipeBuilder.output).toString());
                if(recipeBuilder.output.getHasSubtypes() || (recipeBuilder.output.isDamageable() && recipeBuilder.outputData >= 0))
                    resultJson.addProperty("data", Math.max(recipeBuilder.outputData, 0));
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
                    ingredientsJson.add(((IngredientExtension)input).coreLibSerialize());
                json.add("ingredients", ingredientsJson);
                // Result
                JsonObject resultJson = new JsonObject();
                resultJson.addProperty("item", Registries.ITEMS.getIdentifier(recipeBuilder.output).toString());
                if(recipeBuilder.output.getHasSubtypes() || (recipeBuilder.output.isDamageable() && recipeBuilder.outputData >= 0))
                    resultJson.addProperty("data", Math.max(recipeBuilder.outputData, 0));
                if(recipeBuilder.outputCount != 1)
                    resultJson.addProperty("count", recipeBuilder.outputCount);
                if(recipeBuilder.outputTag != null)
                    resultJson.addProperty("nbt", recipeBuilder.outputTag.toString());
                json.add("result", resultJson);

            }else if(recipeBuilder instanceof SmeltingRecipeBuilder){
                if(((SmeltingRecipeBuilder)recipeBuilder).includeSmelting){
                    json.addProperty("type", "minecraft:smelting");
                    serializeCookingRecipe(json, (SmeltingRecipeBuilder)recipeBuilder, 1, 200);
                }
                if(((SmeltingRecipeBuilder)recipeBuilder).includeBlasting){
                    json.addProperty("type", "minecraft:blasting");
                    serializeCookingRecipe(json, (SmeltingRecipeBuilder)recipeBuilder, 2, 100);
                }
                if(((SmeltingRecipeBuilder)recipeBuilder).includeSmoking){
                    json.addProperty("type", "minecraft:smoking");
                    serializeCookingRecipe(json, (SmeltingRecipeBuilder)recipeBuilder, 2, 100);
                }
                if(((SmeltingRecipeBuilder)recipeBuilder).includeCampfire){
                    json.addProperty("type", "minecraft:campfire_cooking");
                    serializeCookingRecipe(json, (SmeltingRecipeBuilder)recipeBuilder, 2, 100);
                }
            }else if(recipeBuilder instanceof StoneCuttingRecipeBuilder){
                // Group
                json.addProperty("group", recipeBuilder.group);
                // Ingredient
                json.add("ingredient", ((IngredientExtension)((StoneCuttingRecipeBuilder)recipeBuilder).input).coreLibSerialize());
                // Result
                if(recipeBuilder.output.getHasSubtypes() || (recipeBuilder.output.isDamageable() && recipeBuilder.outputData >= 0)){
                    JsonObject resultJson = new JsonObject();
                    resultJson.addProperty("data", Math.max(recipeBuilder.outputData, 0));
                    resultJson.addProperty("result", Registries.ITEMS.getIdentifier(recipeBuilder.output).toString());
                    resultJson.add("result", resultJson);
                }else
                    json.addProperty("result", Registries.ITEMS.getIdentifier(recipeBuilder.output).toString());
                if(recipeBuilder.output.getHasSubtypes() || (recipeBuilder.output.isDamageable() && recipeBuilder.outputData >= 0))
                    json.addProperty("data", Math.max(recipeBuilder.outputData, 0));
                // Count
                json.addProperty("count", recipeBuilder.outputCount);
            }

            // Conditions
            if(!recipeBuilder.conditions.isEmpty()){
                JsonArray conditionsJson = new JsonArray();
                for(RecipeCondition condition : recipeBuilder.conditions){
                    JsonObject conditionJson = new JsonObject();
                    conditionJson.addProperty("type", RecipeConditions.getIdentifierForSerializer(condition.getSerializer()).toString());
                    //noinspection unchecked
                    ((RecipeConditionSerializer<RecipeCondition>)condition.getSerializer()).serialize(conditionJson, condition);
                    conditionsJson.add(conditionJson);
                }
                json.add("conditions", conditionsJson);
            }

            // Save the object to the cache
            ResourceLocation identifier = recipeBuilder.identifier;
            this.cache.saveJsonResource(ResourceType.ASSET, json, identifier.getResourceDomain(), "recipes", identifier.getResourcePath());
        }

        // Save the advancements
        this.advancements.save();
    }

    private static void serializeCookingRecipe(JsonObject json, SmeltingRecipeBuilder recipeBuilder, int durationDivider, int defaultDuration){
        // Group
        json.addProperty("group", ((RecipeBuilder<?>)recipeBuilder).group);
        // Ingredient
        json.add("ingredient", ((IngredientExtension)recipeBuilder.input).coreLibSerialize());
        // Result
        if(((RecipeBuilder<?>)recipeBuilder).outputTag == null && ((RecipeBuilder<?>)recipeBuilder).outputCount == 1)
            json.addProperty("result", Registries.ITEMS.getIdentifier(((RecipeBuilder<?>)recipeBuilder).output).toString());
        else{
            JsonObject resultJson = new JsonObject();
            resultJson.addProperty("item", Registries.ITEMS.getIdentifier(((RecipeBuilder<?>)recipeBuilder).output).toString());
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

    private static ResourceLocation getRecipeSerializerRegistration(IRecipeFactory serializer){
        for(Map.Entry<ResourceLocation,IRecipeFactory> entry : CRAFTING_HELPER_RECIPES.get().entrySet()){
            if(entry.getValue() == serializer)
                return entry.getKey();
        }
        return null;
    }

    protected <T extends RecipeBuilder<T>> T recipe(ResourceLocation recipeLocation, T builder){
        if(this.recipes.containsKey(recipeLocation))
            throw new RuntimeException("Duplicate recipe '" + recipeLocation + "' of types '" + this.recipes.get(recipeLocation).getClass().getName() + "' and '" + builder.getClass().getName() + "'!");

        this.cache.trackToBeGeneratedResource(ResourceType.ASSET, builder.identifier.getResourceDomain(), "recipes", builder.identifier.getResourcePath(), ".json");
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
    protected ShapedRecipeBuilder shaped(ResourceLocation recipeLocation, Item output, int data, NBTTagCompound nbt, int amount){
        return this.recipe(recipeLocation, new ShapedRecipeBuilder(recipeLocation, output, data, nbt, amount));
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param nbt            nbt tag of the recipe result
     * @param amount         count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(ResourceLocation recipeLocation, Item output, NBTTagCompound nbt, int amount){
        return this.shaped(recipeLocation, output, -1, nbt, amount);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(String namespace, String identifier, Item output, int data, NBTTagCompound nbt, int amount){
        return this.shaped(new ResourceLocation(namespace, identifier), output, data, nbt, amount);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(String namespace, String identifier, Item output, NBTTagCompound nbt, int amount){
        return this.shaped(new ResourceLocation(namespace, identifier), output, nbt, amount);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(String identifier, Item output, int data, NBTTagCompound nbt, int amount){
        return this.shaped(this.modid, identifier, output, data, nbt, amount);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(String identifier, Item output, NBTTagCompound nbt, int amount){
        return this.shaped(this.modid, identifier, output, nbt, amount);
    }

    /**
     * Creates a new shaped recipe builder with the output's identifier as location.
     * @param output recipe result
     * @param nbt    nbt tag of the recipe result
     * @param amount count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(Item output, int data, NBTTagCompound nbt, int amount){
        ResourceLocation identifier = Registries.ITEMS.getIdentifier(output);
        return this.shaped(identifier, output, data, nbt, amount);
    }

    /**
     * Creates a new shaped recipe builder with the output's identifier as location.
     * @param output recipe result
     * @param nbt    nbt tag of the recipe result
     * @param amount count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(Item output, NBTTagCompound nbt, int amount){
        ResourceLocation identifier = Registries.ITEMS.getIdentifier(output);
        return this.shaped(identifier, output, nbt, amount);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param amount         count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(ResourceLocation recipeLocation, Item output, int data, int amount){
        return this.shaped(recipeLocation, output, data, null, amount);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param amount         count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(ResourceLocation recipeLocation, Item output, int amount){
        return this.shaped(recipeLocation, output, null, amount);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(String namespace, String identifier, Item output, int data, int amount){
        return this.shaped(new ResourceLocation(namespace, identifier), output, data, null, amount);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(String namespace, String identifier, Item output, int amount){
        return this.shaped(new ResourceLocation(namespace, identifier), output, null, amount);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(String identifier, Item output, int data, int amount){
        return this.shaped(this.modid, identifier, output, data, null, amount);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(String identifier, Item output, int amount){
        return this.shaped(this.modid, identifier, output, null, amount);
    }

    /**
     * Creates a new shaped recipe builder with the output's identifier as location.
     * @param output recipe result
     * @param amount count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(Item output, int data, int amount){
        return this.shaped(output, data, null, amount);
    }

    /**
     * Creates a new shaped recipe builder with the output's identifier as location.
     * @param output recipe result
     * @param amount count of the recipe result
     */
    protected ShapedRecipeBuilder shaped(Item output, int amount){
        return this.shaped(output, null, amount);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     */
    protected ShapedRecipeBuilder shaped(ResourceLocation recipeLocation, Item output){
        return this.shaped(recipeLocation, output, null, 1);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected ShapedRecipeBuilder shaped(String namespace, String identifier, Item output){
        return this.shaped(new ResourceLocation(namespace, identifier), output, null, 1);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected ShapedRecipeBuilder shaped(String identifier, Item output){
        return this.shaped(this.modid, identifier, output, null, 1);
    }

    /**
     * Creates a new shaped recipe builder with the output's identifier as location.
     * @param output recipe result
     */
    protected ShapedRecipeBuilder shaped(Item output){
        return this.shaped(output, null, 1);
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     */
    protected ShapedRecipeBuilder shaped(ResourceLocation recipeLocation, ItemStack output){
        return this.shaped(recipeLocation, output.getItem(), output.getHasSubtypes() ? output.getMetadata() : -1, output.hasTagCompound() && !output.getTagCompound().hasNoTags() ? output.getTagCompound() : null, output.getCount());
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected ShapedRecipeBuilder shaped(String namespace, String identifier, ItemStack output){
        return this.shaped(new ResourceLocation(namespace, identifier), output.getItem(), output.getHasSubtypes() ? output.getMetadata() : -1, output.hasTagCompound() && !output.getTagCompound().hasNoTags() ? output.getTagCompound() : null, output.getCount());
    }

    /**
     * Creates a new shaped recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected ShapedRecipeBuilder shaped(String identifier, ItemStack output){
        return this.shaped(this.modid, identifier, output.getItem(), output.getHasSubtypes() ? output.getMetadata() : -1, output.hasTagCompound() && !output.getTagCompound().hasNoTags() ? output.getTagCompound() : null, output.getCount());
    }

    /**
     * Creates a new shaped recipe builder with the output's identifier as location.
     * @param output recipe result
     */
    protected ShapedRecipeBuilder shaped(ItemStack output){
        return this.shaped(output.getItem(), output.getHasSubtypes() ? output.getMetadata() : -1, output.hasTagCompound() && !output.getTagCompound().hasNoTags() ? output.getTagCompound() : null, output.getCount());
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param nbt            nbt tag of the recipe result
     * @param amount         count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(ResourceLocation recipeLocation, Item output, int data, NBTTagCompound nbt, int amount){
        return this.recipe(recipeLocation, new ShapelessRecipeBuilder(recipeLocation, output, data, nbt, amount));
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param nbt            nbt tag of the recipe result
     * @param amount         count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(ResourceLocation recipeLocation, Item output, NBTTagCompound nbt, int amount){
        return this.shapeless(recipeLocation, output, -1, nbt, amount);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String namespace, String identifier, Item output, int data, NBTTagCompound nbt, int amount){
        return this.shapeless(new ResourceLocation(namespace, identifier), output, data, nbt, amount);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String namespace, String identifier, Item output, NBTTagCompound nbt, int amount){
        return this.shapeless(new ResourceLocation(namespace, identifier), output, nbt, amount);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String identifier, Item output, int data, NBTTagCompound nbt, int amount){
        return this.shapeless(this.modid, identifier, output, data, nbt, amount);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String identifier, Item output, NBTTagCompound nbt, int amount){
        return this.shapeless(this.modid, identifier, output, nbt, amount);
    }

    /**
     * Creates a new shapeless recipe builder with the output's identifier as location.
     * @param output recipe result
     * @param nbt    nbt tag of the recipe result
     * @param amount count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(Item output, int data, NBTTagCompound nbt, int amount){
        ResourceLocation identifier = Registries.ITEMS.getIdentifier(output);
        return this.shapeless(identifier, output, data, nbt, amount);
    }

    /**
     * Creates a new shapeless recipe builder with the output's identifier as location.
     * @param output recipe result
     * @param nbt    nbt tag of the recipe result
     * @param amount count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(Item output, NBTTagCompound nbt, int amount){
        ResourceLocation identifier = Registries.ITEMS.getIdentifier(output);
        return this.shapeless(identifier, output, nbt, amount);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param amount         count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(ResourceLocation recipeLocation, Item output, int data, int amount){
        return this.shapeless(recipeLocation, output, data, null, amount);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param amount         count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(ResourceLocation recipeLocation, Item output, int amount){
        return this.shapeless(recipeLocation, output, null, amount);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String namespace, String identifier, Item output, int data, int amount){
        return this.shapeless(new ResourceLocation(namespace, identifier), output, data, null, amount);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String namespace, String identifier, Item output, int amount){
        return this.shapeless(new ResourceLocation(namespace, identifier), output, null, amount);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String identifier, Item output, int data, int amount){
        return this.shapeless(this.modid, identifier, output, data, null, amount);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String identifier, Item output, int amount){
        return this.shapeless(this.modid, identifier, output, null, amount);
    }

    /**
     * Creates a new shapeless recipe builder with the output's identifier as location.
     * @param output recipe result
     * @param amount count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(Item output, int data, int amount){
        return this.shapeless(output, data, null, amount);
    }

    /**
     * Creates a new shapeless recipe builder with the output's identifier as location.
     * @param output recipe result
     * @param amount count of the recipe result
     */
    protected ShapelessRecipeBuilder shapeless(Item output, int amount){
        return this.shapeless(output, null, amount);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     */
    protected ShapelessRecipeBuilder shapeless(ResourceLocation recipeLocation, Item output){
        return this.shapeless(recipeLocation, output, null, 1);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String namespace, String identifier, Item output){
        return this.shapeless(new ResourceLocation(namespace, identifier), output, null, 1);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String identifier, Item output){
        return this.shapeless(this.modid, identifier, output, null, 1);
    }

    /**
     * Creates a new shapeless recipe builder with the output's identifier as location.
     * @param output recipe result
     */
    protected ShapelessRecipeBuilder shapeless(Item output){
        return this.shapeless(output, null, 1);
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     */
    protected ShapelessRecipeBuilder shapeless(ResourceLocation recipeLocation, ItemStack output){
        return this.shapeless(recipeLocation, output.getItem(), output.getHasSubtypes() ? output.getMetadata() : -1, output.hasTagCompound() && !output.getTagCompound().hasNoTags() ? output.getTagCompound() : null, output.getCount());
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String namespace, String identifier, ItemStack output){
        return this.shapeless(new ResourceLocation(namespace, identifier), output.getItem(), output.getHasSubtypes() ? output.getMetadata() : -1, output.hasTagCompound() && !output.getTagCompound().hasNoTags() ? output.getTagCompound() : null, output.getCount());
    }

    /**
     * Creates a new shapeless recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected ShapelessRecipeBuilder shapeless(String identifier, ItemStack output){
        return this.shapeless(this.modid, identifier, output.getItem(), output.getHasSubtypes() ? output.getMetadata() : -1, output.hasTagCompound() && !output.getTagCompound().hasNoTags() ? output.getTagCompound() : null, output.getCount());
    }

    /**
     * Creates a new shapeless recipe builder with the output's identifier as location.
     * @param output recipe result
     */
    protected ShapelessRecipeBuilder shapeless(ItemStack output){
        return this.shapeless(output.getItem(), output.getHasSubtypes() ? output.getMetadata() : -1, output.hasTagCompound() && !output.getTagCompound().hasNoTags() ? output.getTagCompound() : null, output.getCount());
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param nbt            nbt tag of the recipe result
     * @param amount         count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(ResourceLocation recipeLocation, Item output, int data, NBTTagCompound nbt, int amount){
        return this.recipe(recipeLocation, new SmeltingRecipeBuilder(recipeLocation, output, data, nbt, amount)).includeSmelting();
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param nbt            nbt tag of the recipe result
     * @param amount         count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(ResourceLocation recipeLocation, Item output, NBTTagCompound nbt, int amount){
        return this.smelting(recipeLocation, output, -1, nbt, amount);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(String namespace, String identifier, Item output, int data, NBTTagCompound nbt, int amount){
        return this.smelting(new ResourceLocation(namespace, identifier), output, data, nbt, amount);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(String namespace, String identifier, Item output, NBTTagCompound nbt, int amount){
        return this.smelting(new ResourceLocation(namespace, identifier), output, nbt, amount);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(String identifier, Item output, int data, NBTTagCompound nbt, int amount){
        return this.smelting(this.modid, identifier, output, data, nbt, amount);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param nbt        nbt tag of the recipe result
     * @param amount     count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(String identifier, Item output, NBTTagCompound nbt, int amount){
        return this.smelting(this.modid, identifier, output, nbt, amount);
    }

    /**
     * Creates a new smelting recipe builder with the output's identifier as location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param output recipe result
     * @param nbt    nbt tag of the recipe result
     * @param amount count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(Item output, int data, NBTTagCompound nbt, int amount){
        ResourceLocation identifier = Registries.ITEMS.getIdentifier(output);
        return this.smelting(identifier, output, data, nbt, amount);
    }

    /**
     * Creates a new smelting recipe builder with the output's identifier as location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param output recipe result
     * @param nbt    nbt tag of the recipe result
     * @param amount count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(Item output, NBTTagCompound nbt, int amount){
        ResourceLocation identifier = Registries.ITEMS.getIdentifier(output);
        return this.smelting(identifier, output, nbt, amount);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param amount         count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(ResourceLocation recipeLocation, Item output, int data, int amount){
        return this.smelting(recipeLocation, output, data, null, amount);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param amount         count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(ResourceLocation recipeLocation, Item output, int amount){
        return this.smelting(recipeLocation, output, null, amount);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(String namespace, String identifier, Item output, int data, int amount){
        return this.smelting(new ResourceLocation(namespace, identifier), output, data, null, amount);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(String namespace, String identifier, Item output, int amount){
        return this.smelting(new ResourceLocation(namespace, identifier), output, null, amount);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(String identifier, Item output, int data, int amount){
        return this.smelting(this.modid, identifier, output, data, null, amount);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(String identifier, Item output, int amount){
        return this.smelting(this.modid, identifier, output, null, amount);
    }

    /**
     * Creates a new smelting recipe builder with the output's identifier as location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param output recipe result
     * @param amount count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(Item output, int data, int amount){
        return this.smelting(output, data, null, amount);
    }

    /**
     * Creates a new smelting recipe builder with the output's identifier as location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param output recipe result
     * @param amount count of the recipe result
     */
    protected SmeltingRecipeBuilder smelting(Item output, int amount){
        return this.smelting(output, null, amount);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     */
    protected SmeltingRecipeBuilder smelting(ResourceLocation recipeLocation, Item output){
        return this.smelting(recipeLocation, output, null, 1);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected SmeltingRecipeBuilder smelting(String namespace, String identifier, Item output){
        return this.smelting(new ResourceLocation(namespace, identifier), output, null, 1);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected SmeltingRecipeBuilder smelting(String identifier, Item output){
        return this.smelting(this.modid, identifier, output, null, 1);
    }

    /**
     * Creates a new smelting recipe builder with the output's identifier as location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param output recipe result
     */
    protected SmeltingRecipeBuilder smelting(Item output){
        return this.smelting(output, null, 1);
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     */
    protected SmeltingRecipeBuilder smelting(ResourceLocation recipeLocation, ItemStack output){
        return this.smelting(recipeLocation, output.getItem(), output.getHasSubtypes() ? output.getMetadata() : -1, output.hasTagCompound() && !output.getTagCompound().hasNoTags() ? output.getTagCompound() : null, output.getCount());
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected SmeltingRecipeBuilder smelting(String namespace, String identifier, ItemStack output){
        return this.smelting(new ResourceLocation(namespace, identifier), output.getItem(), output.getHasSubtypes() ? output.getMetadata() : -1, output.hasTagCompound() && !output.getTagCompound().hasNoTags() ? output.getTagCompound() : null, output.getCount());
    }

    /**
     * Creates a new smelting recipe builder for the given location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected SmeltingRecipeBuilder smelting(String identifier, ItemStack output){
        return this.smelting(this.modid, identifier, output.getItem(), output.getHasSubtypes() ? output.getMetadata() : -1, output.hasTagCompound() && !output.getTagCompound().hasNoTags() ? output.getTagCompound() : null, output.getCount());
    }

    /**
     * Creates a new smelting recipe builder with the output's identifier as location. The smelting recipe builder can be used for furnace, blasting, smoking, campfire recipes.
     * @param output recipe result
     */
    protected SmeltingRecipeBuilder smelting(ItemStack output){
        return this.smelting(output.getItem(), output.getHasSubtypes() ? output.getMetadata() : -1, output.hasTagCompound() && !output.getTagCompound().hasNoTags() ? output.getTagCompound() : null, output.getCount());
    }

    /**
     * Creates a new stonecutting recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param amount         count of the recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(ResourceLocation recipeLocation, Item output, int data, int amount){
        return this.recipe(recipeLocation, new StoneCuttingRecipeBuilder(recipeLocation, output, data, amount));
    }

    /**
     * Creates a new stonecutting recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     * @param amount         count of the recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(ResourceLocation recipeLocation, Item output, int amount){
        return this.stoneCutting(recipeLocation, output, -1, amount);
    }

    /**
     * Creates a new stonecutting recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(String namespace, String identifier, Item output, int data, int amount){
        return this.stoneCutting(new ResourceLocation(namespace, identifier), output, data, amount);
    }

    /**
     * Creates a new stonecutting recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(String namespace, String identifier, Item output, int amount){
        return this.stoneCutting(new ResourceLocation(namespace, identifier), output, amount);
    }

    /**
     * Creates a new stonecutting recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(String identifier, Item output, int data, int amount){
        return this.stoneCutting(this.modid, identifier, output, data, amount);
    }

    /**
     * Creates a new stonecutting recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     * @param amount     count of the recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(String identifier, Item output, int amount){
        return this.stoneCutting(this.modid, identifier, output, amount);
    }

    /**
     * Creates a new stonecutting recipe builder with the output's identifier as location.
     * @param output recipe result
     * @param amount count of the recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(Item output, int data, int amount){
        ResourceLocation identifier = Registries.ITEMS.getIdentifier(output);
        return this.stoneCutting(identifier, output, data, amount);
    }

    /**
     * Creates a new stonecutting recipe builder with the output's identifier as location.
     * @param output recipe result
     * @param amount count of the recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(Item output, int amount){
        ResourceLocation identifier = Registries.ITEMS.getIdentifier(output);
        return this.stoneCutting(identifier, output, amount);
    }

    /**
     * Creates a new stonecutting recipe builder for the given location.
     * @param recipeLocation location of the recipe
     * @param output         recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(ResourceLocation recipeLocation, Item output){
        return this.stoneCutting(recipeLocation, output, 1);
    }

    /**
     * Creates a new stonecutting recipe builder for the given location.
     * @param namespace  namespace of the recipe
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(String namespace, String identifier, Item output){
        return this.stoneCutting(new ResourceLocation(namespace, identifier), output, 1);
    }

    /**
     * Creates a new stonecutting recipe builder for the given location.
     * @param identifier path of the recipe
     * @param output     recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(String identifier, Item output){
        return this.stoneCutting(this.modid, identifier, output, 1);
    }

    /**
     * Creates a new stonecutting recipe builder with the output's identifier as location.
     * @param output recipe result
     */
    protected StoneCuttingRecipeBuilder stoneCutting(Item output){
        return this.stoneCutting(output, 1);
    }

    @Override
    public String getName(){
        return this.modName + " Recipe Generator";
    }

    public static abstract class RecipeBuilder<T extends RecipeBuilder<T>> {

        protected final ResourceLocation identifier;
        private final List<RecipeCondition> conditions = new ArrayList<>();
        private final Item output;
        private final int outputData;
        private final NBTTagCompound outputTag;
        private final int outputCount;
        private String serializer;
        private String group;
        private boolean hasAdvancement = true;
        private final List<ICriterionInstance> unlockedBy = new ArrayList<>();

        protected RecipeBuilder(ResourceLocation identifier, String serializer, Item output, int outputData, NBTTagCompound outputTag, int outputCount){
            this.identifier = identifier;
            this.output = output;
            this.outputData = outputData;
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
        public T condition(RecipeCondition condition){
            this.conditions.add(condition);
            return this.self();
        }

        /**
         * Adds a condition to only load this recipe when a mod with the given modid is present.
         */
        public T modLoadedCondition(String modid){
            return this.condition(new ModLoadedRecipeCondition(modid));
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
        public T unlockedBy(ICriterionInstance criterion){
            if(this.unlockedBy.contains(criterion))
                throw new RuntimeException("Duplicate unlockedBy criterion '" + criterion + "'!");

            this.unlockedBy.add(criterion);
            return this.self();
        }

        /**
         * Sets which items the player should have to unlock this recipe in its generated advancement.
         */
        public T unlockedBy(Item... items){
            return this.unlockedBy(
                new InventoryChangeTrigger.Instance(
                    MinMaxBounds.UNBOUNDED,
                    MinMaxBounds.UNBOUNDED,
                    MinMaxBounds.UNBOUNDED,
                    Arrays.stream(items).map(item -> new ItemPredicate(item, 0, MinMaxBounds.UNBOUNDED, MinMaxBounds.UNBOUNDED, new EnchantmentPredicate[0], null, NBTPredicate.ANY)).toArray(ItemPredicate[]::new)
                )
            );
        }

        /**
         * Sets which items the player should have to unlock this recipe in its generated advancement.
         */
        public T unlockedByOreDict(String ore){
            return this.unlockedBy(new InventoryChangeTrigger.Instance(
                MinMaxBounds.UNBOUNDED,
                MinMaxBounds.UNBOUNDED,
                MinMaxBounds.UNBOUNDED,
                new ItemPredicate[]{new OredictItemPredicate(ore)}
            ));
        }

        /**
         * Sets a different recipe serializer. This may not have an effect for all recipe types, most notably the smelting recipes.
         */
        public T customSerializer(IRecipeFactory serializer){
            ResourceLocation identifier = getRecipeSerializerRegistration(serializer);
            if(identifier == null)
                throw new IllegalArgumentException("Cannot use unregistered recipe factory '" + serializer.getClass() + "' for recipe '" + this.identifier + "'!");
            this.serializer = identifier.toString();
            return this.self();
        }

        /**
         * Sets a different recipe serializer. This may not have an effect for all recipe types, most notably the smelting recipes.
         */
        public T customSerializer(ResourceLocation serializer){
            if(!CRAFTING_HELPER_RECIPES.get().containsKey(serializer))
                throw new IllegalArgumentException("Cannot use unknown recipe factory '" + serializer + "' for recipe '" + this.identifier + "'!");
            this.serializer = serializer.toString();
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

        private ShapedRecipeBuilder(ResourceLocation identifier, Item output, int outputData, NBTTagCompound outputTag, int outputCount){
            super(identifier, "forge:ore_shaped", output, outputData, outputTag, outputCount);
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
            return this.input(key, Ingredient.merge(Arrays.asList(ingredients)));
        }

        /**
         * Defines the ingredient corresponding to the given character. These characters may be used in the pattern for this recipe.
         * @param key   key to be defined
         * @param items items to be associated with the key
         */
        public ShapedRecipeBuilder input(char key, Item... items){
            return this.input(key, Ingredient.fromItems(items));
        }

        /**
         * Defines the ingredient corresponding to the given character. These characters may be used in the pattern for this recipe.
         * @param key        key to be defined
         * @param itemStacks items to be associated with the key
         */
        public ShapedRecipeBuilder input(char key, ItemStack... itemStacks){
            return this.input(key, Ingredient.fromStacks(itemStacks));
        }

        /**
         * Defines the ingredient corresponding to the given character. These characters may be used in the pattern for this recipe.
         * @param key     key to be defined
         * @param oreDict ore dictionary tag to be used as input
         */
        public ShapedRecipeBuilder input(char key, String oreDict){
            return this.input(key, new OreIngredient(oreDict));
        }
    }

    protected static class ShapelessRecipeBuilder extends RecipeBuilder<ShapelessRecipeBuilder> {

        private final List<Ingredient> inputs = new ArrayList<>();

        private ShapelessRecipeBuilder(ResourceLocation identifier, Item output, int outputData, NBTTagCompound outputTag, int outputCount){
            super(identifier, "forge:ore_shapeless", output, outputData, outputTag, outputCount);
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
        public ShapelessRecipeBuilder input(Item item, int count){
            return this.input(Ingredient.fromItem(item), count);
        }

        /**
         * Adds an item ingredient for this recipe.
         * @param item ingredient to be added
         */
        public ShapelessRecipeBuilder input(Item item){
            return this.input(item, 1);
        }

        /**
         * Adds an item stack ingredient for this recipe. The ingredient will be added {@code count} times.
         * @param itemStack ingredient to be added
         * @param count     the number of times to add the ingredient
         */
        public ShapelessRecipeBuilder input(ItemStack itemStack, int count){
            return this.input(Ingredient.fromStacks(itemStack), count);
        }

        /**
         * Adds an item stack ingredient for this recipe.
         * @param itemStack ingredient to be added
         */
        public ShapelessRecipeBuilder input(ItemStack itemStack){
            return this.input(itemStack, 1);
        }

        /**
         * Adds an ore dictionary ingredient for this recipe. The ingredient will be added {@code count} times.
         * @param oreDict ore dictionary ingredient to be added
         * @param count   the number of times to add the ingredient
         */
        public ShapelessRecipeBuilder input(String oreDict, int count){
            return this.input(new OreIngredient(oreDict), count);
        }

        /**
         * Adds an ore dictionary ingredient for this recipe.
         * @param oreDict ore dictionary ingredient to be added
         */
        public ShapelessRecipeBuilder input(String oreDict){
            return this.input(oreDict, 1);
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
        public ShapelessRecipeBuilder inputs(Item... items){
            for(Item item : items)
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

        /**
         * Adds all the given ore dictionary tags as ingredients to this recipe.
         * @param oreDicts ore dictionary ingredients to be added
         */
        public ShapelessRecipeBuilder inputs(String... oreDicts){
            for(String oreDict : oreDicts)
                this.input(oreDict);
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

        private SmeltingRecipeBuilder(ResourceLocation identifier, Item output, int outputData, NBTTagCompound outputTag, int count){
            super(identifier, "minecraft:smelting", output, outputData, outputTag, count);
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
            return this.input(Ingredient.merge(Arrays.asList(ingredients)));
        }

        /**
         * Sets the input for this recipe.
         * @param items items to be accepted as input
         */
        public SmeltingRecipeBuilder input(Item... items){
            return this.input(Ingredient.fromItems(items));
        }

        /**
         * Sets the input for this recipe.
         * @param itemStacks items to be accepted as input
         */
        public SmeltingRecipeBuilder input(ItemStack... itemStacks){
            return this.input(Ingredient.fromStacks(itemStacks));
        }

        /**
         * Sets the input for this recipe.
         * @param oreDict ore dictionary tag to be accepted as input
         */
        public SmeltingRecipeBuilder input(String oreDict){
            return this.input(new OreIngredient(oreDict));
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

    protected static class StoneCuttingRecipeBuilder extends RecipeBuilder<StoneCuttingRecipeBuilder> {

        private Ingredient input;

        private StoneCuttingRecipeBuilder(ResourceLocation identifier, Item output, int outputData, int outputCount){
            super(identifier, "minecraft:stonecutting", output, outputData, null, outputCount);
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
            return this.input(Ingredient.merge(Arrays.asList(ingredients)));
        }

        /**
         * Sets the input ingredient for this recipe.
         * @param items items to be accepted as input
         */
        public StoneCuttingRecipeBuilder input(Item... items){
            return this.input(Ingredient.fromItems(items));
        }

        /**
         * Sets the input ingredient for this recipe.
         * @param itemStacks items to be accepted as input
         */
        public StoneCuttingRecipeBuilder input(ItemStack... itemStacks){
            return this.input(Ingredient.fromStacks(itemStacks));
        }

        /**
         * Sets the input ingredient for this recipe.
         * @param oreDict ore dictionary tag to be accepted as input
         */
        public StoneCuttingRecipeBuilder input(String oreDict){
            return this.input(new OreIngredient(oreDict));
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

                CreativeTabs tab = recipe.output.getCreativeTab();
                String namespace = recipe.identifier.getResourceDomain();
                String identifier = "recipes/" + (tab == null ? "" : tab.getTabLabel() + "/") + recipe.identifier.getResourcePath();

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
                .parent(new ResourceLocation("minecraft", "recipes/root"))
                .criterion("has_the_recipe", new FakeRecipeUnlockedTrigger(recipe.identifier))
                .icon(recipe.output, recipe.outputTag)
                .dontShowToast()
                .dontAnnounceToChat()
                .rewardRecipe(recipe.identifier);
            String[] conditions = new String[recipe.unlockedBy.size() + 1];
            conditions[0] = "has_the_recipe";
            if(recipe.unlockedBy.size() == 1){
                builder.criterion("recipe_condition", recipe.unlockedBy.get(0));
                conditions[1] = "recipe_condition";
            }else{
                for(int i = 0; i < recipe.unlockedBy.size(); i++){
                    builder.criterion("recipe_condition" + (i + 1), recipe.unlockedBy.get(i));
                    conditions[i + 1] = "recipe_condition" + (i + 1);
                }
            }
            builder.requirementGroup(conditions);
        }

        private class FakeRecipeUnlockedTrigger implements ICriterionInstance, ICriterionInstanceExtension {

            private final ResourceLocation recipeLocation;

            private FakeRecipeUnlockedTrigger(ResourceLocation recipeLocation){
                this.recipeLocation = recipeLocation;
            }

            @Override
            public ResourceLocation getId(){
                return new ResourceLocation("recipe_unlocked");
            }

            @Override
            public void coreLibSerialize(JsonObject json){
                json.addProperty("recipe", this.recipeLocation.toString());
            }
        }
    }
}
