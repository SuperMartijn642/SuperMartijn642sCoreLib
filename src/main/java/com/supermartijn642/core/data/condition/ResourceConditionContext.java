package com.supermartijn642.core.data.condition;

import net.minecraft.tags.TagKey;
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

    public boolean isTagAvailable(TagKey<?> tag){
        return this.context.isTagLoaded(tag);
    }

    public boolean isTagPopulated(TagKey<?> tag){
        return !this.context.getTag(tag).isEmpty();
    }

    @Deprecated
    public ICondition.IContext getUnderlying(){
        return this.context;
    }
}
