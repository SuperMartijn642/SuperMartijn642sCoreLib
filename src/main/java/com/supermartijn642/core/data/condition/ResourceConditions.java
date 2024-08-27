package com.supermartijn642.core.data.condition;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.supermartijn642.core.codec.CodecHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 27/08/2022 by SuperMartijn642
 */
class ResourceConditions {

    private static final Map<ResourceConditionSerializer<?>,Codec<? extends ICondition>> TO_UNDERLYING_MAP = new HashMap<>();

    static ConditionWrapper wrap(ResourceCondition condition){
        return new ConditionWrapper(condition);
    }

    static Codec<? extends ICondition> serializerCodec(ResourceConditionSerializer<?> serializer){
        return TO_UNDERLYING_MAP.computeIfAbsent(serializer, s -> CodecHelper.jsonSerializerToCodec(
            input -> {
                JsonObject json = new JsonObject();
                //noinspection rawtypes,unchecked
                ((ResourceConditionSerializer)serializer).serialize(json, input.condition);
                return json;
            },
            json -> new ConditionWrapper(serializer.deserialize(json.getAsJsonObject()))
        ));
    }

    private static class ConditionWrapper implements ICondition {

        private final ResourceCondition condition;

        ConditionWrapper(ResourceCondition condition){
            this.condition = condition;
        }

        @Override
        public boolean test(IContext context){
            return this.condition.test(new ResourceConditionContext(context));
        }

        @Override
        public Codec<? extends ICondition> codec(){
            return serializerCodec(this.condition.getSerializer());
        }
    }
}
