package com.supermartijn642.core.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.supermartijn642.core.extensions.ICriterionInstanceExtension;
import com.supermartijn642.core.extensions.ItemPredicateExtension;
import com.supermartijn642.core.extensions.MinMaxBoundsExtension;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Created 01/09/2022 by SuperMartijn642
 */
@Mixin(InventoryChangeTrigger.Instance.class)
public class InventoryChangeTriggerInstanceMixin implements ICriterionInstanceExtension {

    @Shadow
    @Final
    private MinMaxBounds occupied;
    @Shadow
    @Final
    private MinMaxBounds full;
    @Shadow
    @Final
    private MinMaxBounds empty;
    @Shadow
    @Final
    private ItemPredicate[] items;

    @Override
    public void coreLibSerialize(JsonObject json){
        JsonObject slotsJson = new JsonObject();
        if(this.occupied != null && this.occupied != MinMaxBounds.UNBOUNDED)
            slotsJson.add("occupied", ((MinMaxBoundsExtension)this.occupied).coreLibSerialize());
        if(this.full != null && this.full != MinMaxBounds.UNBOUNDED)
            slotsJson.add("full", ((MinMaxBoundsExtension)this.full).coreLibSerialize());
        if(this.empty != null && this.empty != MinMaxBounds.UNBOUNDED)
            slotsJson.add("empty", ((MinMaxBoundsExtension)this.empty).coreLibSerialize());
        if(slotsJson.size() > 0)
            json.add("slots", slotsJson);

        if(this.items != null && this.items.length > 0){
            JsonArray itemsJson = new JsonArray();
            for(ItemPredicate predicate : this.items)
                itemsJson.add(((ItemPredicateExtension)predicate).coreLibSerialize());
            json.add("items", itemsJson);
        }
    }
}
