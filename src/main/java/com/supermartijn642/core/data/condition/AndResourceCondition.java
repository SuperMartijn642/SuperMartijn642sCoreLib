package com.supermartijn642.core.data.condition;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created 29/11/2022 by SuperMartijn642
 */
public class AndResourceCondition implements ResourceCondition {

    public static final Serializer SERIALIZER = new Serializer();

    private final List<ICondition> conditions;

    public AndResourceCondition(ICondition... conditions){
        this.conditions = new ArrayList<>(Arrays.asList(conditions));
    }

    public AndResourceCondition(ResourceCondition... conditions){
        this(Arrays.stream(conditions).map(ResourceCondition::createForgeCondition).toArray(ICondition[]::new));
    }

    @Override
    public boolean test(ResourceConditionContext context){
        for(ICondition condition : this.conditions){
            if(!condition.test(context.getUnderlying()))
                return false;
        }
        return true;
    }

    @Override
    public ResourceConditionSerializer<?> getSerializer(){
        return SERIALIZER;
    }

    @Override
    public ResourceCondition and(ResourceCondition condition){
        this.conditions.add(ResourceCondition.createForgeCondition(condition));
        return this;
    }

    private static class Serializer implements ResourceConditionSerializer<AndResourceCondition> {

        @Override
        public void serialize(JsonObject json, AndResourceCondition condition){
            JsonArray conditions = new JsonArray();
            for(ICondition alternative : condition.conditions)
                conditions.add(ICondition.CODEC.encodeStart(JsonOps.INSTANCE, alternative).getOrThrow(false, s -> {}));
            json.add("conditions", conditions);
        }

        @Override
        public AndResourceCondition deserialize(JsonObject json){
            if(!json.has("conditions") || !json.get("conditions").isJsonArray())
                throw new RuntimeException("Condition must have key 'conditions' with a json array!");

            JsonArray conditionsJson = json.getAsJsonArray("conditions");
            ICondition[] conditions = new ICondition[conditionsJson.size()];
            for(int i = 0; i < conditionsJson.size(); i++)
                conditions[i] = ICondition.CODEC.decode(JsonOps.INSTANCE, conditionsJson.get(i).getAsJsonObject()).getOrThrow(false, s -> {}).getFirst();
            return new AndResourceCondition(conditions);
        }
    }
}
