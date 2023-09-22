package com.supermartijn642.core.data.condition;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.crafting.conditions.ICondition;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public interface ResourceConditionSerializer<T extends ResourceCondition> {

    static Codec<? extends ICondition> createForgeConditionCodec(ResourceConditionSerializer<?> serializer){
        return ResourceConditions.serializerCodec(serializer);
    }

    void serialize(JsonObject json, T condition);

    T deserialize(JsonObject json);
}
