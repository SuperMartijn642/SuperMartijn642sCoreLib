package com.supermartijn642.core.mixin;

import com.google.gson.JsonObject;
import com.supermartijn642.core.extensions.ICriterionInstanceExtension;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.item.crafting.IRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Created 01/09/2022 by SuperMartijn642
 */
@Mixin(RecipeUnlockedTrigger.Instance.class)
public class RecipeUnlockedTriggerInstanceMixin implements ICriterionInstanceExtension {

    @Shadow
    private final IRecipe recipe = null;

    @Override
    public void coreLibSerialize(JsonObject json){
        json.addProperty("recipe", this.recipe.getRegistryName().toString());
    }
}
