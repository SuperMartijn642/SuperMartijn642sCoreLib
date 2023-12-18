package com.supermartijn642.core.data.condition;

import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * TODO eventually add stuff similar to {@link ICondition.IContext}
 * <p>
 * Created 14/11/2022 by SuperMartijn642
 */
public class ResourceConditionContext {

    private final ICondition.IContext context;

    ResourceConditionContext(ICondition.IContext context){
        this.context = context;
    }

    @Deprecated
    public ICondition.IContext getUnderlying(){
        return this.context;
    }
}
