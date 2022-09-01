package com.supermartijn642.core.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.supermartijn642.core.extensions.IngredientExtension;
import net.minecraftforge.oredict.OreIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 29/08/2022 by SuperMartijn642
 */
@Mixin(OreIngredient.class)
public class OreIngredientMixin implements IngredientExtension {

    private String coreLibOre;

    @Inject(
        method = "<init>(Ljava/lang/String;)V",
        at = @At("TAIL"),
        remap = false
    )
    private void constructor(String ore, CallbackInfo ci){
        this.coreLibOre = ore;
    }

    @Override
    public JsonElement coreLibSerialize(){
        //noinspection ConstantConditions
        if((Object)this.getClass() != OreIngredient.class)
            throw new RuntimeException("Ingredient class '" + this.getClass().getCanonicalName() + "' does not override IngredientExtension#coreLibSerialize and thus is not supported!");

        JsonObject json = new JsonObject();
        json.addProperty("type", "forge:ore_dict");
        json.addProperty("ore", this.coreLibOre);
        return json;
    }
}
