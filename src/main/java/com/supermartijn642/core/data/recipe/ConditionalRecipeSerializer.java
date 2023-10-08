package com.supermartijn642.core.data.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.supermartijn642.core.data.condition.ResourceCondition;
import com.supermartijn642.core.data.condition.ResourceConditionSerializer;
import com.supermartijn642.core.data.condition.ResourceConditions;

import java.util.Collection;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public class ConditionalRecipeSerializer {

    public static JsonObject wrapRecipe(JsonObject recipe, Collection<ResourceCondition> conditions){
        JsonArray conditionsJson = new JsonArray();
        for(ResourceCondition condition : conditions){
            JsonObject conditionJson = new JsonObject();
            conditionJson.addProperty("type", ResourceConditions.getIdentifierForSerializer(condition.getSerializer()).toString());
            //noinspection unchecked
            ((ResourceConditionSerializer<ResourceCondition>)condition.getSerializer()).serialize(conditionJson, condition);
            conditionsJson.add(conditionJson);
        }
        recipe.add("conditions", conditionsJson);
        return recipe;
    }
}
