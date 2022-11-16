package com.supermartijn642.core.data.condition;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IConditionFactory;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public interface ResourceConditionSerializer<T extends ResourceCondition> {

    static IConditionFactory createForgeConditionSerializer(ResourceLocation identifier, ResourceConditionSerializer<?> serializer){
        return ResourceConditions.wrap(identifier, serializer);
    }

    void serialize(JsonObject json, T condition);

    T deserialize(JsonObject json);
}
