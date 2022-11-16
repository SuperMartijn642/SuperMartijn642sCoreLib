package com.supermartijn642.core.data.condition;

import net.minecraftforge.common.crafting.conditions.ICondition;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public interface ResourceCondition {

    static ICondition createForgeCondition(ResourceCondition condition){
        return ResourceConditions.wrap(condition);
    }

    boolean test(ResourceConditionContext context);

    ResourceConditionSerializer<?> getSerializer();
}
