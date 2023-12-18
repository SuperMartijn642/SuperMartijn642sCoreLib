package com.supermartijn642.core.data.condition;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.neoforged.neoforge.common.conditions.ICondition;

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
        return TO_UNDERLYING_MAP.computeIfAbsent(serializer, s -> Codec.<ConditionWrapper>of(new Encoder<>() {
            @Override
            public <T> DataResult<T> encode(ConditionWrapper input, DynamicOps<T> ops, T prefix){
                JsonObject json = new JsonObject();
                try{
                    //noinspection unchecked,rawtypes
                    ((ResourceConditionSerializer)serializer).serialize(json, input.condition);
                }catch(Exception e){
                    return DataResult.error(() -> "Failed to encode condition: " + e.getMessage());
                }
                //noinspection unchecked
                return DataResult.success((T)json);
            }
        }, new Decoder<>() {
            @Override
            public <T> DataResult<Pair<ConditionWrapper,T>> decode(DynamicOps<T> ops, T input){
                try{
                    return DataResult.success(Pair.of(new ConditionWrapper(serializer.deserialize(((JsonObject)input))), input));
                }catch(Exception e){
                    throw new RuntimeException("Failed to decode condition for serializer class '" + serializer.getClass() + "'!", e);
                }
            }
        }));
    }

    private static class ConditionWrapper implements ICondition {

        private final ResourceCondition condition;
        private Codec<? extends ICondition> codec;

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
