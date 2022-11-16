package com.supermartijn642.core.data.condition;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public interface ResourceCondition {

    boolean test(ResourceConditionContext context);

    ResourceConditionSerializer<?> getSerializer();
}
