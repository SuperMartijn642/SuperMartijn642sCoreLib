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

    /**
     * Negates the effect of the resource condition.
     * @see NotResourceCondition
     */
    default ResourceCondition negate(){
        return new NotResourceCondition(this);
    }

    /**
     * Adds an alternative to the resource condition.
     * @see OrResourceCondition
     */
    default ResourceCondition or(ResourceCondition alternative){
        return new OrResourceCondition(this, alternative);
    }

    /**
     * Adds a requirement to the resource condition.
     * @see AndResourceCondition
     */
    default ResourceCondition and(ResourceCondition condition){
        return new AndResourceCondition(this, condition);
    }
}
