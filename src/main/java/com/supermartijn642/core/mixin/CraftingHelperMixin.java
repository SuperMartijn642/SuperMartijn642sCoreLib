package com.supermartijn642.core.mixin;

import com.supermartijn642.core.registry.Registries;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 27/08/2022 by SuperMartijn642
 */
@Mixin(CraftingHelper.class)
public class CraftingHelperMixin {

    @Inject(
        method = "register(Lnet/minecraftforge/common/crafting/conditions/IConditionSerializer;)Lnet/minecraftforge/common/crafting/conditions/IConditionSerializer;",
        at = @At("TAIL"),
        remap = false
    )
    public static void registerConditionSerializer(IConditionSerializer<?> conditionSerializer, CallbackInfo ci){
        Registries.onRecipeConditionSerializerAdded(conditionSerializer);
    }
}
