package com.supermartijn642.core.data.tag;

import com.google.gson.JsonObject;

/**
 * Created 09/02/2024 by SuperMartijn642
 */
public interface CustomTagEntrySerializer<T extends CustomTagEntry> {

    void serialize(JsonObject json, T entry);

    T deserialize(JsonObject json);
}
