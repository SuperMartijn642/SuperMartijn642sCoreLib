package com.supermartijn642.core.mixin;

import com.google.gson.JsonObject;
import com.supermartijn642.core.extensions.ICriterionInstanceExtension;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Created 01/09/2022 by SuperMartijn642
 */
@Mixin(ImpossibleTrigger.Instance.class)
public class ImpossibleTriggerInstanceMixin implements ICriterionInstanceExtension {

    @Override
    public void coreLibSerialize(JsonObject json){
    }
}
