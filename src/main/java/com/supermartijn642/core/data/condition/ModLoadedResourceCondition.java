package com.supermartijn642.core.data.condition;

import com.google.gson.JsonObject;
import com.supermartijn642.core.CommonUtils;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public class ModLoadedResourceCondition implements ResourceCondition {

    public static final Serializer SERIALIZER = new Serializer();

    private final String modid;

    public ModLoadedResourceCondition(String modid){
        this.modid = modid;
    }

    @Override
    public boolean test(ResourceConditionContext context){
        return CommonUtils.isModLoaded(this.modid);
    }

    @Override
    public ResourceConditionSerializer<?> getSerializer(){
        return SERIALIZER;
    }

    private static class Serializer implements ResourceConditionSerializer<ModLoadedResourceCondition> {

        @Override
        public void serialize(JsonObject json, ModLoadedResourceCondition condition){
            json.addProperty("modid", condition.modid);
        }

        @Override
        public ModLoadedResourceCondition deserialize(JsonObject json){
            if(!json.has("modid") || !json.get("modid").isJsonPrimitive() || !json.get("modid").getAsJsonPrimitive().isString())
                throw new RuntimeException("Condition must have key 'modid' with a string value!");

            return new ModLoadedResourceCondition(json.get("modid").getAsString());
        }
    }
}
