package com.supermartijn642.core.loot_table;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.supermartijn642.core.extensions.LootContextExtension;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

import java.util.Random;

/**
 * Created 31/08/2022 by SuperMartijn642
 */
public class SurvivesExplosionLootCondition implements LootCondition {

    public static final LootCondition.Serializer<?> SERIALIZER = new Serializer();

    @Override
    public boolean testCondition(Random random, LootContext context){
        float explosionRadius = ((LootContextExtension)context).coreLibGetExplosionRadius();
        return explosionRadius <= 0 || random.nextFloat() <= 1 / explosionRadius;
    }

    private static class Serializer extends LootCondition.Serializer<SurvivesExplosionLootCondition> {

        private Serializer(){
            super(new ResourceLocation("supermartijn642corelib", "survives_explosion"), SurvivesExplosionLootCondition.class);
        }

        @Override
        public void serialize(JsonObject json, SurvivesExplosionLootCondition value, JsonSerializationContext context){
        }

        @Override
        public SurvivesExplosionLootCondition deserialize(JsonObject json, JsonDeserializationContext context){
            return new SurvivesExplosionLootCondition();
        }
    }
}
