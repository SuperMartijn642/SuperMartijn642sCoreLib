package com.supermartijn642.core.data.condition;

import java.util.function.BooleanSupplier;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public interface ResourceCondition {

    static BooleanSupplier createForgeCondition(ResourceCondition condition){
        return ResourceConditions.wrap(condition);
    }

    boolean test(ResourceConditionContext context);

    ResourceConditionSerializer<?> getSerializer();
}
