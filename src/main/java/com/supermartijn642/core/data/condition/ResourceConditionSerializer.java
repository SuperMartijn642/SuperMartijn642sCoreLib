package com.supermartijn642.core.data.condition;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public interface ResourceConditionSerializer<T extends ResourceCondition> {

    static IConditionSerializer<?> createForgeConditionSerializer(ResourceLocation identifier, ResourceConditionSerializer<?> serializer){
        return ResourceConditions.wrap(identifier, serializer);
    }

    void serialize(JsonObject json, T condition);

    T deserialize(JsonObject json);
}
