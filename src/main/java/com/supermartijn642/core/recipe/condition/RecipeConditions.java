package com.supermartijn642.core.recipe.condition;

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
public class RecipeConditions {

    private static final Map<RecipeConditionSerializer<?>,IConditionFactory> TO_UNDERLYING_MAP = new HashMap<>();

    static BooleanSupplier wrap(RecipeCondition condition){
        return new ConditionWrapper(condition);
    }

    static IConditionFactory wrap(ResourceLocation identifier, RecipeConditionSerializer<?> serializer){
        return new ConditionSerializerWrapper(identifier, serializer);
    }

    public static ResourceLocation getIdentifierForSerializer(RecipeConditionSerializer<?> serializer){
        return Registries.RECIPE_CONDITION_SERIALIZERS.getIdentifier(TO_UNDERLYING_MAP.get(serializer));
    }

    private static class ConditionWrapper implements BooleanSupplier {

        private final RecipeCondition condition;

        ConditionWrapper(RecipeCondition condition){
            this.condition = condition;
        }

        @Override
        public boolean getAsBoolean(){
            return this.condition.test();
        }
    }

    private static class ConditionSerializerWrapper implements IConditionFactory {

        private final RecipeConditionSerializer<RecipeCondition> serializer;

        private ConditionSerializerWrapper(ResourceLocation identifier, RecipeConditionSerializer<?> serializer){
            //noinspection unchecked
            this.serializer = (RecipeConditionSerializer<RecipeCondition>)serializer;
        }

        @Override
        public BooleanSupplier parse(JsonContext context, JsonObject json){
            return wrap(this.serializer.deserialize(json));
        }
    }
}
