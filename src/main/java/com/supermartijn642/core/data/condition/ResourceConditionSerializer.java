package com.supermartijn642.core.data.condition;

import com.google.gson.JsonObject;

/**
 * Created 26/08/2022 by SuperMartijn642
 */
public interface ResourceConditionSerializer<T extends ResourceCondition> {

    void serialize(JsonObject json, T condition);

    T deserialize(JsonObject json);
}
