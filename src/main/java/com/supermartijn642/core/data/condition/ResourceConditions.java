package com.supermartijn642.core.data.condition;

import com.google.gson.JsonObject;
import com.supermartijn642.core.codec.CodecHelper;
import com.supermartijn642.core.util.Holder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Created 27/08/2022 by SuperMartijn642
 */
public class ResourceConditions {

    public static void registerFabricResourceCondition(ResourceLocation identifier, ResourceConditionSerializer<?> serializer){
        Holder<ResourceConditionType<ResourceConditionWrapper>> typeHolder = new Holder<>();
        ResourceConditionType<ResourceConditionWrapper> type = ResourceConditionType.create(identifier, CodecHelper.jsonSerializerToMapCodec(
            input -> {
                JsonObject json = new JsonObject();
                //noinspection rawtypes,unchecked
                ((ResourceConditionSerializer)serializer).serialize(json, input.condition);
                return json;
            },
            json -> new ResourceConditionWrapper(typeHolder.get(), serializer.deserialize(json))
        ));
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
