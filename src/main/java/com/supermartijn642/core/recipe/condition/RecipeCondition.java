package com.supermartijn642.core.recipe.condition;

import java.util.function.BooleanSupplier;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public interface RecipeCondition {

    static BooleanSupplier createForgeCondition(RecipeCondition condition){
        return RecipeConditions.wrap(condition);
    }

    boolean test();

    RecipeConditionSerializer<?> getSerializer();
}
