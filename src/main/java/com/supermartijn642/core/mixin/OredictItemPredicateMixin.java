package com.supermartijn642.core.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.supermartijn642.core.extensions.ItemPredicateExtension;
import net.minecraftforge.advancements.critereon.OredictItemPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Created 01/09/2022 by SuperMartijn642
 */
@Mixin(OredictItemPredicate.class)
public class OredictItemPredicateMixin implements ItemPredicateExtension {

    @Shadow(remap = false)
    private final String ore = null;

    @Override
    public JsonElement coreLibSerialize(){
        JsonObject json = new JsonObject();
        json.addProperty("type", "forge:ore_dict");
        json.addProperty("ore", this.ore);
        return null;
    }
}
