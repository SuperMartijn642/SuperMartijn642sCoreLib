package com.supermartijn642.core.data.condition;

import com.google.gson.JsonObject;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * This is an internal class and <b>not</b> part of stable API.
 * <p>
 * Created 27/08/2022 by SuperMartijn642
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
public class ResourceConditions {

    private static final Map<ResourceConditionSerializer<?>,IConditionFactory> TO_UNDERLYING_MAP = new HashMap<>();

    static BooleanSupplier wrap(ResourceCondition condition){
        return new ConditionWrapper(condition);
    }

    static IConditionFactory wrap(ResourceLocation identifier, ResourceConditionSerializer<?> serializer){
        IConditionFactory factory = new ConditionSerializerWrapper(identifier, serializer);
        TO_UNDERLYING_MAP.put(serializer, factory);
        return factory;
    }

    public static ResourceLocation getIdentifierForSerializer(ResourceConditionSerializer<?> serializer){
        return Registries.RECIPE_CONDITION_SERIALIZERS.getIdentifier(TO_UNDERLYING_MAP.get(serializer));
    }

    public static ResourceConditionSerializer<?> getSerializerForIdentifier(ResourceLocation identifier){
        IConditionFactory factory = Registries.RECIPE_CONDITION_SERIALIZERS.getValue(identifier);
        return factory instanceof ConditionSerializerWrapper ? ((ConditionSerializerWrapper)factory).serializer : null;
    }

    private static class ConditionWrapper implements BooleanSupplier {

        private final ResourceCondition condition;

        ConditionWrapper(ResourceCondition condition){
            this.condition = condition;
        }

        @Override
        public boolean getAsBoolean(){
            return this.condition.test(new ResourceConditionContext());
        }
    }

    private static class ConditionSerializerWrapper implements IConditionFactory {

        private final ResourceConditionSerializer<ResourceCondition> serializer;

        private ConditionSerializerWrapper(ResourceLocation identifier, ResourceConditionSerializer<?> serializer){
            //noinspection unchecked
            this.serializer = (ResourceConditionSerializer<ResourceCondition>)serializer;
        }

        @Override
        public BooleanSupplier parse(JsonContext context, JsonObject json){
            return wrap(this.serializer.deserialize(json));
        }
    }
}
