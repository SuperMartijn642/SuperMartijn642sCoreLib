package com.supermartijn642.core.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.supermartijn642.core.extensions.IngredientExtension;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 30/08/2022 by SuperMartijn642
 */
@Mixin(Ingredient.class)
public class IngredientMixin implements IngredientExtension {

    private ItemStack[] coreLibOriginalInput;

    @Inject(
        method = "<init>([Lnet/minecraft/item/ItemStack;)V",
        at = @At("RETURN")
    )
    private void constructor(ItemStack[] input, CallbackInfo ci){
        this.coreLibOriginalInput = input;
    }

    @Override
    public JsonElement coreLibSerialize(){
        //noinspection ConstantConditions
        if((Object)this == Ingredient.EMPTY){
            JsonObject json = new JsonObject();
            json.addProperty("type", "minecraft:empty");
            return json;
        }

        //noinspection ConstantConditions
        if((Object)this.getClass() != Ingredient.class)
            throw new RuntimeException("Ingredient class '" + this.getClass().getCanonicalName() + "' does not override IngredientExtension#coreLibSerialize and thus is not supported!");
        if((this.coreLibOriginalInput == null || this.coreLibOriginalInput.length == 0))
            throw new RuntimeException("Cannot serialize an empty ingredient!");

        JsonArray arr = new JsonArray();
        for(ItemStack stack : this.coreLibOriginalInput){
            JsonObject json = new JsonObject();
            json.addProperty("type", "minecraft:item");
            json.addProperty("item", Registries.ITEMS.getIdentifier(stack.getItem()).toString());
            if(stack.getHasSubtypes())
                json.addProperty("data", stack.getMetadata());
            arr.add(json);
        }
        return arr;
    }
}
