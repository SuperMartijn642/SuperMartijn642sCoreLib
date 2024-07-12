package com.supermartijn642.core.data.condition;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.supermartijn642.core.util.Holder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

/**
 * Created 27/08/2022 by SuperMartijn642
 */
public class ResourceConditions {

    public static void registerFabricResourceCondition(ResourceLocation identifier, ResourceConditionSerializer<?> serializer){
        Holder<ResourceConditionType<ResourceConditionWrapper>> typeHolder = new Holder<>();
        ResourceConditionType<ResourceConditionWrapper> type = ResourceConditionType.create(identifier, new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops){
                return Stream.empty();
            }

            @Override
            public <T> DataResult<ResourceConditionWrapper> decode(DynamicOps<T> ops, MapLike<T> input){
                JsonObject json = (JsonObject)JsonOps.INSTANCE.createMap(
                    input.entries()
                        .map(pair -> Pair.of(ops.convertTo(JsonOps.INSTANCE, pair.getFirst()), ops.convertTo(JsonOps.INSTANCE, pair.getSecond())))
                );
                ResourceCondition condition;
                try{
                    condition = serializer.deserialize(json);
                }catch(Exception e){
                    return DataResult.error(() -> "Encountered an exception: " + e.getMessage());
                }
                return DataResult.success(new ResourceConditionWrapper(typeHolder.get(), condition));
            }

            @Override
            public <T> RecordBuilder<T> encode(ResourceConditionWrapper input, DynamicOps<T> ops, RecordBuilder<T> prefix){
                JsonObject json = new JsonObject();
                try{
                    // noinspection unchecked,rawtypes
                    ((ResourceConditionSerializer)serializer).serialize(json, input.condition);
                }catch(Exception e){
                    throw new RuntimeException("Encountered an exception whilst serializing resource condition!", e);
                }
                for(String key : json.keySet())
                    prefix.add(key, JsonOps.INSTANCE.convertTo(ops, json.get(key)));
                return prefix;
            }
        });
        typeHolder.set(type);
        // Register the Fabric resource condition type
        net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions.register(type);
    }

    private static class ResourceConditionWrapper implements net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition {

        public final ResourceConditionType<?> type;
        public final ResourceCondition condition;

        private ResourceConditionWrapper(ResourceConditionType<?> type, ResourceCondition condition){
            this.type = type;
            this.condition = condition;
        }

        @Override
        public ResourceConditionType<?> getType(){
            return this.type;
        }

        @Override
        public boolean test(@Nullable HolderLookup.Provider registryLookup){
            return this.condition.test(ResourceConditionContext.EMPTY);
        }
    }
}
