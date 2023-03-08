package com.supermartijn642.core.data.condition;

import com.google.gson.JsonObject;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.resources.ResourceLocation;

/**
 * Created 14/11/2022 by SuperMartijn642
 */
public class NotResourceCondition implements ResourceCondition {

    public static final Serializer SERIALIZER = new Serializer();

    private final ResourceCondition condition;

    public NotResourceCondition(ResourceCondition condition){
        this.condition = condition;
    }

    @Override
    public boolean test(ResourceConditionContext context){
        return !this.condition.test(context);
    }

    @Override
    public ResourceConditionSerializer<?> getSerializer(){
        return SERIALIZER;
    }

    private static class Serializer implements ResourceConditionSerializer<NotResourceCondition> {

        @Override
        public void serialize(JsonObject json, NotResourceCondition condition){
            JsonObject conditionJson = new JsonObject();
            //noinspection unchecked,rawtypes
            ((ResourceConditionSerializer)condition.condition.getSerializer()).serialize(conditionJson, condition.condition);
            conditionJson.addProperty("condition", Registries.RESOURCE_CONDITION_SERIALIZERS.getIdentifier(condition.condition.getSerializer()).toString());
            json.add("value", conditionJson);
        }

        @Override
        public NotResourceCondition deserialize(JsonObject json){
            if(!json.has("value") || !json.get("value").isJsonObject())
                throw new RuntimeException("Condition must have key 'value' with a json object!");

            JsonObject conditionJson = json.getAsJsonObject("value");
            ResourceLocation identifier = ResourceLocation.tryParse(conditionJson.get("condition").getAsString());
            if(!Registries.RESOURCE_CONDITION_SERIALIZERS.hasIdentifier(identifier))
                throw new RuntimeException("Could not find any resource condition with identifier '" + identifier + "'!");

            return new NotResourceCondition(Registries.RESOURCE_CONDITION_SERIALIZERS.getValue(identifier).deserialize(conditionJson));
        }
    }
}
