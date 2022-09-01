package com.supermartijn642.core.mixin;

import com.supermartijn642.core.extensions.ICriterionInstanceExtension;
import net.minecraft.advancements.ICriterionInstance;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Created 01/09/2022 by SuperMartijn642
 */
@Mixin(ICriterionInstance.class)
public interface ICriterionInstanceMixin extends ICriterionInstanceExtension {

}
