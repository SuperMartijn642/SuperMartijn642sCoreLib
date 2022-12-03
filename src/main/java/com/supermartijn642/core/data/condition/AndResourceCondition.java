package com.supermartijn642.core.data.condition;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created 29/11/2022 by SuperMartijn642
 */
public class AndResourceCondition implements ResourceCondition {

    public static final Serializer SERIALIZER = new Serializer();

    private final List<ResourceCondition> conditions;

    public AndResourceCondition(ResourceCondition... conditions){
        this.conditions = new ArrayList<>(Arrays.asList(conditions));
    }

    @Override
    public boolean test(ResourceConditionContext context){
        for(ResourceCondition condition : this.conditions){
            if(!condition.test(context))
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
        this.conditions.add(condition);
        return this;
    }

    private static class Serializer implements ResourceConditionSerializer<AndResourceCondition> {

        @Override
        public void serialize(JsonObject json, AndResourceCondition condition){
            JsonArray conditions = new JsonArray();
            for(ResourceCondition alternative : condition.conditions){
                JsonObject conditionJson = new JsonObject();
                //noinspection unchecked,rawtypes
                ((ResourceConditionSerializer)alternative.getSerializer()).serialize(conditionJson, condition);
                conditionJson.addProperty("condition", ResourceConditions.getIdentifierForSerializer(alternative.getSerializer()).toString());
                conditions.add(conditionJson);
            }
            json.add("conditions", conditions);
        }

        @Override
        public AndResourceCondition deserialize(JsonObject json){
            if(!json.has("conditions") || !json.get("conditions").isJsonArray())
                throw new RuntimeException("Condition must have key 'conditions' with a json array!");

            JsonArray conditionsJson = json.getAsJsonArray("conditions");
            ResourceCondition[] conditions = new ResourceCondition[conditionsJson.size()];
            for(int i = 0; i < conditionsJson.size(); i++){
                JsonObject conditionJson = conditionsJson.get(i).getAsJsonObject();
                ResourceLocation identifier = new ResourceLocation(conditionJson.get("condition").getAsString());
                if(!Registries.RECIPE_CONDITION_SERIALIZERS.hasIdentifier(identifier))
                    throw new RuntimeException("Could not find any resource condition with identifier '" + identifier + "'!");
                conditions[i] = ResourceConditions.getSerializerForIdentifier(identifier).deserialize(conditionJson);
            }
            return new AndResourceCondition(conditions);
        }
    }
}
