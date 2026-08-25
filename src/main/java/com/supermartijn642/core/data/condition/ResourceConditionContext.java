package com.supermartijn642.core.data.condition;

import com.mojang.serialization.DynamicOps;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.crafting.conditions.ICondition;

/**
 * TODO eventually add stuff similar to {@link net.minecraftforge.common.crafting.conditions.ICondition.IContext}
 * <p>
 * Created 14/11/2022 by SuperMartijn642
 */
public class ResourceConditionContext {

    private final ICondition.IContext context;
    private final DynamicOps<?> dynamicOps;

    ResourceConditionContext(ICondition.IContext context, DynamicOps<?> dynamicOps){
        this.context = context;
        this.dynamicOps = dynamicOps;
    }

    public boolean isTagPopulated(TagKey<?> tag){
        return !this.context.getTag(tag).isEmpty();
    }

    @Deprecated
    public ICondition.IContext getUnderlying(){
        return this.context;
    }

    public DynamicOps<?> getDynamicOps(){
        return this.dynamicOps;
    }
}
