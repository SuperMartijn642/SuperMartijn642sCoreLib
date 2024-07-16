package com.supermartijn642.core.data.condition;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public interface ResourceConditionSerializer<T extends ResourceCondition> {

    static MapCodec<? extends ICondition> createForgeConditionCodec(ResourceConditionSerializer<?> serializer){
        return ResourceConditions.serializerCodec(serializer);
    }

    void serialize(JsonObject json, T condition);

    T deserialize(JsonObject json);
}
