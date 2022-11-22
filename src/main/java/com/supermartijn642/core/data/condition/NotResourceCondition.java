package com.supermartijn642.core.data.condition;

import com.google.gson.JsonObject;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.util.ResourceLocation;

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
            //noinspection unchecked
            ((ResourceConditionSerializer<ResourceCondition>)condition.condition.getSerializer()).serialize(conditionJson, condition.condition);
            conditionJson.addProperty("type", ResourceConditions.getIdentifierForSerializer(condition.condition.getSerializer()).toString());
            json.add("condition", conditionJson);
        }

        @Override
        public NotResourceCondition deserialize(JsonObject json){
            if(!json.has("condition") || !json.get("condition").isJsonObject())
                throw new RuntimeException("Condition must have key 'condition' with a json object!");

            JsonObject conditionJson = json.getAsJsonObject("condition");
            ResourceLocation identifier = new ResourceLocation(conditionJson.get("type").getAsString());
            if(!Registries.RECIPE_CONDITION_SERIALIZERS.hasIdentifier(identifier))
                throw new RuntimeException("");

            return new NotResourceCondition(ResourceConditions.getSerializerForIdentifier(identifier).deserialize(conditionJson));
        }
    }
}
