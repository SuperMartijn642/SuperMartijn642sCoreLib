package com.supermartijn642.core.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.supermartijn642.core.extensions.EnchantmentPredicateExtension;
import com.supermartijn642.core.extensions.ItemPredicateExtension;
import com.supermartijn642.core.extensions.MinMaxBoundsExtension;
import com.supermartijn642.core.extensions.NBTPredicateExtension;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NBTPredicate;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Created 01/09/2022 by SuperMartijn642
 */
@Mixin(ItemPredicate.class)
public class ItemPredicateMixin implements ItemPredicateExtension {

    @Shadow
    @Final
    private Item item;
    @Shadow
    @Final
    private Integer data;
    @Shadow
    @Final
    private MinMaxBounds count;
    @Shadow
    @Final
    private MinMaxBounds durability;
    @Shadow
    @Final
    private EnchantmentPredicate[] enchantments;
    @Shadow
    @Final
    private PotionType potion;
    @Shadow
    @Final
    private NBTPredicate nbt;

    @Override
    public JsonElement coreLibSerialize(){
        if((Object)this.getClass() != ItemPredicate.class)
            throw new RuntimeException("ItemPredicate class '" + this.getClass().getCanonicalName() + "' does not override ItemPredicateExtension#coreLibSerialize and thus is not supported!");

        if((Object)this == ItemPredicate.ANY)
            return JsonNull.INSTANCE;

        JsonObject json = new JsonObject();
        if(this.item != null)
            json.addProperty("item", Registries.ITEMS.getIdentifier(this.item).toString());
        if(this.data != null)
            json.addProperty("data", this.data);

        json.add("count", ((MinMaxBoundsExtension)this.count).coreLibSerialize());
        json.add("durability", ((MinMaxBoundsExtension)this.durability).coreLibSerialize());
        json.add("nbt", ((NBTPredicateExtension)this.nbt).coreLibSerialize());
        if(this.enchantments.length > 0){
            JsonArray enchantmentsJson = new JsonArray();
            for(EnchantmentPredicate predicate : this.enchantments)
                enchantmentsJson.add(((EnchantmentPredicateExtension)predicate).coreLibSerialize());
            json.add("enchantments", enchantmentsJson);
        }

        if(this.potion != null)
            json.addProperty("potion", Registries.POTIONS.getIdentifier(this.potion).toString());

        return json;
    }
}
