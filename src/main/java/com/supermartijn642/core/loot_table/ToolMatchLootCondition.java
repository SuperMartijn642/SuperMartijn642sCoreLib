package com.supermartijn642.core.loot_table;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.supermartijn642.core.extensions.ItemPredicateExtension;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

import java.util.Random;

/**
 * Created 28/09/2022 by SuperMartijn642
 */
public class ToolMatchLootCondition implements LootCondition {

    public static final LootCondition.Serializer<?> SERIALIZER = new Serializer();

    private final ItemPredicate predicate;

    public ToolMatchLootCondition(ItemPredicate predicate){
        this.predicate = predicate;
    }

    @Override
    public boolean testCondition(Random rand, LootContext context){
        Entity player = context.getKillerPlayer();
        return player instanceof EntityLivingBase && this.predicate.test(((EntityLivingBase)player).getHeldItemMainhand());
    }

    private static class Serializer extends LootCondition.Serializer<ToolMatchLootCondition> {

        private Serializer(){
            super(new ResourceLocation("supermartijn642corelib", "match_tool"), ToolMatchLootCondition.class);
        }

        @Override
        public void serialize(JsonObject json, ToolMatchLootCondition value, JsonSerializationContext context){
            json.add("predicate", ((ItemPredicateExtension)value.predicate).coreLibSerialize());
        }

        @Override
        public ToolMatchLootCondition deserialize(JsonObject json, JsonDeserializationContext context){
            return new ToolMatchLootCondition(ItemPredicate.deserialize(json.get("predicate")));
        }
    }
}
