package com.supermartijn642.core.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.supermartijn642.core.extensions.IngredientExtension;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CompoundIngredient;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Created 30/08/2022 by SuperMartijn642
 */
@Mixin(CompoundIngredient.class)
public class CompoundIngredientMixin implements IngredientExtension {

    @Override
    public JsonElement coreLibSerialize(){
        //noinspection ConstantConditions
        if((Object)this.getClass() != CompoundIngredient.class)
            throw new RuntimeException("Ingredient class '" + this.getClass().getCanonicalName() + "' does not override IngredientExtension#coreLibSerialize and thus is not supported!");

        CompoundIngredient ingredient = (CompoundIngredient)(Object)this;
        if(ingredient.getChildren().isEmpty())
            throw new RuntimeException("Cannot serialize an empty compound ingredient!");

        JsonArray items = new JsonArray();
        for(Ingredient child : ingredient.getChildren()){
            JsonElement serialized = ((IngredientExtension)child).coreLibSerialize();
            if(serialized.isJsonArray())
                items.addAll(serialized.getAsJsonArray());
            else
                items.add(serialized);
        }
        return items;
    }
}
