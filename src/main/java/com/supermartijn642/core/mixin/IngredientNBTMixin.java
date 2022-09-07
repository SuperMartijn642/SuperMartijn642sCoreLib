package com.supermartijn642.core.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.supermartijn642.core.extensions.IngredientExtension;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.crafting.IngredientNBT;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Created 30/08/2022 by SuperMartijn642
 */
@Mixin(IngredientNBT.class)
public class IngredientNBTMixin implements IngredientExtension {

    @Shadow(remap = false)
    @Final
    private ItemStack stack;

    @Override
    public JsonElement coreLibSerialize(){
        //noinspection ConstantConditions
        if((Object)this.getClass() != IngredientNBT.class)
            throw new RuntimeException("Ingredient class '" + this.getClass().getCanonicalName() + "' does not override IngredientExtension#coreLibSerialize and thus is not supported!");

        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:item_nbt");
        json.addProperty("item", Registries.ITEMS.getIdentifier(this.stack.getItem()).toString());
        if(this.stack.getHasSubtypes())
            json.addProperty("data", this.stack.getMetadata());
        if(this.stack.getCount() != 1)
            json.addProperty("count", this.stack.getCount());
        if(this.stack.hasTagCompound())
            json.addProperty("nbt", this.stack.getTagCompound().toString());
        return json;
    }
}
