package com.supermartijn642.core.recipe.condition;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public interface RecipeCondition {

    boolean test();

    RecipeConditionSerializer<?> getSerializer();
}
