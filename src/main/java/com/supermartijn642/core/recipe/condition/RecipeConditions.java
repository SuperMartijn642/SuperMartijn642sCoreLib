package com.supermartijn642.core.recipe.condition;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 27/08/2022 by SuperMartijn642
 */
class RecipeConditions {

    private static final Map<RecipeConditionSerializer<?>,IConditionSerializer<?>> TO_UNDERLYING_MAP = new HashMap<>();

    static ICondition wrap(RecipeCondition condition){
        return new ConditionWrapper(condition);
    }

    static IConditionSerializer<?> wrap(ResourceLocation identifier, RecipeConditionSerializer<?> serializer){
        IConditionSerializer<?> forgeSerializer = new ConditionSerializerWrapper(identifier, serializer);
        TO_UNDERLYING_MAP.put(serializer, forgeSerializer);
        return forgeSerializer;
    }

    private static class ConditionWrapper implements ICondition {

        private final RecipeCondition condition;

        ConditionWrapper(RecipeCondition condition){
            this.condition = condition;
        }

        @Override
        public ResourceLocation getID(){
            return TO_UNDERLYING_MAP.get(this.condition.getSerializer()).getID();
        }

        @Override
        public boolean test(){
            return this.condition.test();
        }
    }

    private static class ConditionSerializerWrapper implements IConditionSerializer<ICondition> {

        private final ResourceLocation identifier;
        private final RecipeConditionSerializer<RecipeCondition> serializer;

        private ConditionSerializerWrapper(ResourceLocation identifier, RecipeConditionSerializer<?> serializer){
            this.identifier = identifier;
            //noinspection unchecked
            this.serializer = (RecipeConditionSerializer<RecipeCondition>)serializer;
        }

        @Override
        public void write(JsonObject json, ICondition value){
            this.serializer.serialize(json, (RecipeCondition)value);
        }

        @Override
        public ICondition read(JsonObject json){
            return wrap(this.serializer.deserialize(json));
        }

        @Override
        public ResourceLocation getID(){
            return this.identifier;
        }
    }
}
