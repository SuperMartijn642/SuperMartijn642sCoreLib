package com.supermartijn642.core.recipe.condition;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public interface RecipeConditionSerializer<T extends RecipeCondition> {

    static IConditionSerializer<?> createForgeConditionSerializer(ResourceLocation identifier, RecipeConditionSerializer<?> serializer){
        return RecipeConditions.wrap(identifier, serializer);
    }

    void serialize(JsonObject json, T condition);

    T deserialize(JsonObject json);
}
