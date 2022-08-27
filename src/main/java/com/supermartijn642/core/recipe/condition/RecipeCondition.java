package com.supermartijn642.core.recipe.condition;

import net.minecraftforge.common.crafting.conditions.ICondition;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public interface RecipeCondition {

    static ICondition createForgeCondition(RecipeCondition condition){
        return RecipeConditions.wrap(condition);
    }

    boolean test();

    RecipeConditionSerializer<?> getSerializer();
}
