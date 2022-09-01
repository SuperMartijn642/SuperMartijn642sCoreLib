package com.supermartijn642.core.mixin;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.supermartijn642.core.loot_table.BinomialNumberProvider;
import net.minecraft.util.JsonUtils;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;

/**
 * Created 31/08/2022 by SuperMartijn642
 */
@Mixin(LootPool.Serializer.class)
public class LootPoolSerializerMixin {

    @Inject(
        method = "deserialize",
        at = @At("HEAD"),
        cancellable = true
    )
    private void deserialize(JsonElement json, Type type, JsonDeserializationContext context, CallbackInfoReturnable<LootPool> ci){
        JsonObject jsonObject = JsonUtils.getJsonObject(json, "loot pool"); // I have absolutely no clue why it gets an element with key 'loot pool'
        boolean hasBinomialRolls = jsonObject.has("rolls") && jsonObject.get("rolls").isJsonObject() && jsonObject.getAsJsonObject("rolls").has("type") && jsonObject.getAsJsonObject("rolls").get("type").isJsonPrimitive() && jsonObject.getAsJsonObject("rolls").getAsJsonPrimitive("type").isString() && jsonObject.getAsJsonObject("rolls").get("type").getAsString().equals("supermartijn642corelib:binomial");
        boolean hasBinomialBonusRolls = jsonObject.has("bonus_rolls") && jsonObject.get("bonus_rolls").isJsonObject() && jsonObject.getAsJsonObject("bonus_rolls").has("type") && jsonObject.getAsJsonObject("bonus_rolls").get("type").isJsonPrimitive() && jsonObject.getAsJsonObject("bonus_rolls").getAsJsonPrimitive("type").isString() && jsonObject.getAsJsonObject("bonus_rolls").get("type").getAsString().equals("supermartijn642corelib:binomial");
        if(hasBinomialRolls || hasBinomialBonusRolls){
            String name = ForgeHooks.readPoolName(jsonObject);
            LootEntry[] entries = JsonUtils.deserializeClass(jsonObject, "entries", context, LootEntry[].class);
            LootCondition[] conditions = JsonUtils.deserializeClass(jsonObject, "conditions", new LootCondition[0], context, LootCondition[].class);
            // Create binomial number providers if the correct type is set
            RandomValueRange rolls = hasBinomialRolls ? new BinomialNumberProvider(JsonUtils.getInt(jsonObject.get("rolls"), "n"), JsonUtils.getInt(jsonObject.get("rolls"), "p"))
                : JsonUtils.deserializeClass(jsonObject, "rolls", context, RandomValueRange.class);
            RandomValueRange bonus_rolls = hasBinomialBonusRolls ? new BinomialNumberProvider(JsonUtils.getInt(jsonObject.get("bonus_rolls"), "n"), JsonUtils.getInt(jsonObject.get("bonus_rolls"), "p"))
                : JsonUtils.deserializeClass(jsonObject, "bonus_rolls", context, RandomValueRange.class);
            ci.setReturnValue(new LootPool(entries, conditions, rolls, bonus_rolls, name));
        }
    }
}
