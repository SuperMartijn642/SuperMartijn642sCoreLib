package com.supermartijn642.core.data.condition;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created 29/11/2022 by SuperMartijn642
 */
public class OrResourceCondition implements ResourceCondition {

    public static final Serializer SERIALIZER = new Serializer();

    private final List<ICondition> conditions;

    public OrResourceCondition(ICondition... alternatives){
        this.conditions = new ArrayList<>(Arrays.asList(alternatives));
    }

    public OrResourceCondition(ResourceCondition... alternatives){
        this(Arrays.stream(alternatives).map(ResourceCondition::createForgeCondition).toArray(ICondition[]::new));
    }

    @Override
    public boolean test(ResourceConditionContext context){
        for(ICondition condition : this.conditions){
            if(condition.test(context.getUnderlying()))
                return true;
        }
        return false;
    }

    @Override
    public ResourceConditionSerializer<?> getSerializer(){
        return SERIALIZER;
    }

    @Override
    public ResourceCondition or(ResourceCondition alternative){
        this.conditions.add(ResourceCondition.createForgeCondition(alternative));
        return this;
    }

    private static class Serializer implements ResourceConditionSerializer<OrResourceCondition> {

        @Override
        public void serialize(JsonObject json, OrResourceCondition condition){
            JsonArray conditions = new JsonArray();
            for(ICondition alternative : condition.conditions)
                conditions.add(ICondition.CODEC.encodeStart(JsonOps.INSTANCE, alternative).getOrThrow());
            json.add("conditions", conditions);
        }

        @Override
        public OrResourceCondition deserialize(JsonObject json){
            if(!json.has("conditions") || !json.get("conditions").isJsonArray())
                throw new RuntimeException("Condition must have key 'conditions' with a json array!");

            JsonArray conditionsJson = json.getAsJsonArray("conditions");
            ICondition[] conditions = new ICondition[conditionsJson.size()];
            for(int i = 0; i < conditionsJson.size(); i++)
                conditions[i] = ICondition.CODEC.decode(JsonOps.INSTANCE, conditionsJson.get(i).getAsJsonObject()).getOrThrow().getFirst();
            return new OrResourceCondition(conditions);
        }
    }
}
