package com.supermartijn642.core.recipe.condition;

import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public class ModLoadedRecipeCondition implements RecipeCondition {

    public static final Serializer SERIALIZER = new Serializer();

    private final String modid;

    public ModLoadedRecipeCondition(String modid){
        this.modid = modid;
    }

    @Override
    public boolean test(){
        return FabricLoader.getInstance().isModLoaded(this.modid);
    }

    @Override
    public RecipeConditionSerializer<?> getSerializer(){
        return SERIALIZER;
    }

    private static class Serializer implements RecipeConditionSerializer<ModLoadedRecipeCondition> {

        @Override
        public void serialize(JsonObject json, ModLoadedRecipeCondition condition){
            json.addProperty("modid", condition.modid);
        }

        @Override
        public ModLoadedRecipeCondition deserialize(JsonObject json){
            if(!json.has("modid") || !json.get("modid").isJsonPrimitive() || !json.get("modid").getAsJsonPrimitive().isString())
                throw new RuntimeException("Condition must have key 'modid' with a string value!");

            return new ModLoadedRecipeCondition(json.get("modid").getAsString());
        }
    }
}
