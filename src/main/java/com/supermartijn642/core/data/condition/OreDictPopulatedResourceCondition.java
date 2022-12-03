package com.supermartijn642.core.data.condition;

import com.google.gson.JsonObject;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Created 30/11/2022 by SuperMartijn642
 */
public class OreDictPopulatedResourceCondition implements ResourceCondition {

    public static final Serializer SERIALIZER = new Serializer();

    private final String tag;

    public OreDictPopulatedResourceCondition(String tag){
        this.tag = tag;
    }

    @Override
    public boolean test(ResourceConditionContext context){
        return OreDictionary.doesOreNameExist(this.tag);
    }

    @Override
    public ResourceConditionSerializer<?> getSerializer(){
        return SERIALIZER;
    }

    private static class Serializer implements ResourceConditionSerializer<OreDictPopulatedResourceCondition> {

        @Override
        public void serialize(JsonObject json, OreDictPopulatedResourceCondition condition){
            json.addProperty("ore", condition.tag);
        }

        @Override
        public OreDictPopulatedResourceCondition deserialize(JsonObject json){
            if(!json.has("ore") || !json.get("ore").isJsonPrimitive() || !json.getAsJsonPrimitive("ore").isString())
                throw new RuntimeException("Condition must have key 'ore' of type string!");

            return new OreDictPopulatedResourceCondition(json.get("ore").getAsString());
        }
    }
}
