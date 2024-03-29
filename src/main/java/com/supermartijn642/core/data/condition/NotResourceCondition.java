package com.supermartijn642.core.data.condition;

import com.google.gson.JsonObject;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

/**
 * Created 14/11/2022 by SuperMartijn642
 */
public class NotResourceCondition implements ResourceCondition {

    public static final Serializer SERIALIZER = new Serializer();

    private final ICondition condition;

    public NotResourceCondition(ICondition condition){
        this.condition = condition;
    }

    public NotResourceCondition(ResourceCondition condition){
        this(ResourceCondition.createForgeCondition(condition));
    }

    @Override
    public boolean test(ResourceConditionContext context){
        return !this.condition.test();
    }

    @Override
    public ResourceConditionSerializer<?> getSerializer(){
        return SERIALIZER;
    }

    private static class Serializer implements ResourceConditionSerializer<NotResourceCondition> {

        @Override
        public void serialize(JsonObject json, NotResourceCondition condition){
            json.add("condition", CraftingHelper.serialize(condition.condition));
        }

        @Override
        public NotResourceCondition deserialize(JsonObject json){
            if(!json.has("condition") || !json.get("condition").isJsonObject())
                throw new RuntimeException("Condition must have key 'condition' with a json object!");

            return new NotResourceCondition(CraftingHelper.getCondition(json.get("condition").getAsJsonObject()));
        }
    }
}
