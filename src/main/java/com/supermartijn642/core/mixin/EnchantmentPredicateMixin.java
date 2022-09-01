package com.supermartijn642.core.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.supermartijn642.core.extensions.EnchantmentPredicateExtension;
import com.supermartijn642.core.extensions.MinMaxBoundsExtension;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Created 01/09/2022 by SuperMartijn642
 */
@Mixin(EnchantmentPredicate.class)
public class EnchantmentPredicateMixin implements EnchantmentPredicateExtension {

    @Shadow
    private final Enchantment enchantment = null;
    @Shadow
    private final MinMaxBounds levels = null;

    @SuppressWarnings("ConstantConditions")
    @Override
    public JsonElement coreLibSerialize(){
        if((Object)this == EnchantmentPredicate.ANY)
            return JsonNull.INSTANCE;

        JsonObject json = new JsonObject();
        if(this.enchantment != null)
            json.addProperty("enchantment", Registries.ENCHANTMENTS.getIdentifier(this.enchantment).toString());
        json.add("levels", ((MinMaxBoundsExtension)this.levels).coreLibSerialize());
        return json;
    }
}
