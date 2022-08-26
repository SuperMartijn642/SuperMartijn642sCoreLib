package com.supermartijn642.core.recipe.condition;

import com.google.gson.JsonObject;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public interface RecipeConditionSerializer<T extends RecipeCondition> {

    void serialize(JsonObject json, T condition);

    T deserialize(JsonObject json);
}
