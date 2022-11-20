package com.supermartijn642.core.data.condition;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 27/08/2022 by SuperMartijn642
 */
class ResourceConditions {

    private static final Map<ResourceConditionSerializer<?>,IConditionSerializer<?>> TO_UNDERLYING_MAP = new HashMap<>();

    static ConditionWrapper wrap(ResourceCondition condition){
        return new ConditionWrapper(condition);
    }

    static IConditionSerializer<?> wrap(ResourceLocation identifier, ResourceConditionSerializer<?> serializer){
        IConditionSerializer<?> forgeSerializer = new ConditionSerializerWrapper(identifier, serializer);
        TO_UNDERLYING_MAP.put(serializer, forgeSerializer);
        return forgeSerializer;
    }

    private static class ConditionWrapper implements ICondition {

        private final ResourceCondition condition;

        ConditionWrapper(ResourceCondition condition){
            this.condition = condition;
        }

        @Override
        public ResourceLocation getID(){
            return TO_UNDERLYING_MAP.get(this.condition.getSerializer()).getID();
        }

        @SuppressWarnings("removal")
        @Override
        public boolean test(){
            return this.test(IContext.EMPTY);
        }

        @Override
        public boolean test(IContext context){
            return this.condition.test(new ResourceConditionContext(context));
        }
    }

    private static class ConditionSerializerWrapper implements IConditionSerializer<ConditionWrapper> {

        private final ResourceLocation identifier;
        private final ResourceConditionSerializer<ResourceCondition> serializer;

        private ConditionSerializerWrapper(ResourceLocation identifier, ResourceConditionSerializer<?> serializer){
            this.identifier = identifier;
            //noinspection unchecked
            this.serializer = (ResourceConditionSerializer<ResourceCondition>)serializer;
        }

        @Override
        public void write(JsonObject json, ConditionWrapper value){
            this.serializer.serialize(json, value.condition);
        }

        @Override
        public ConditionWrapper read(JsonObject json){
            return wrap(this.serializer.deserialize(json));
        }

        @Override
        public ResourceLocation getID(){
            return this.identifier;
        }
    }
}
