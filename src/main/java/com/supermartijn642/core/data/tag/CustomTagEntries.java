package com.supermartijn642.core.data.tag;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

/**
 * Created 09/02/2024 by SuperMartijn642
 */
public class CustomTagEntries {

    static JsonObject serialize(TagEntryAdapter entry){
        JsonObject json = new JsonObject();
        CustomTagEntrySerializer<?> serializer = entry.customEntry.getSerializer();
        json.addProperty("type", Registries.CUSTOM_TAG_ENTRY_SERIALIZERS.getIdentifier(serializer).toString());
        try{
            //noinspection unchecked,rawtypes
            ((CustomTagEntrySerializer)serializer).serialize(json, entry.customEntry);
        }catch(Exception e){
            throw new RuntimeException("Encountered an exception whilst serializing custom tag entry for type '" + Registries.CUSTOM_TAG_ENTRY_SERIALIZERS.getIdentifier(serializer).toString() + "'!");
        }
        return json;
    }

    public static TagEntryAdapter potentiallyDeserialize(JsonElement input){
        if(!(input instanceof JsonObject))
            return null;
        JsonObject json = (JsonObject)input;
        if(!json.has("type") || !json.get("type").isJsonPrimitive() || !json.getAsJsonPrimitive("type").isString())
            return null;
        String typeString = json.get("type").getAsString();
        if(!RegistryUtil.isValidIdentifier(typeString))
            return null;
        ResourceLocation type = new ResourceLocation(typeString);
        if(!Registries.CUSTOM_TAG_ENTRY_SERIALIZERS.hasIdentifier(type))
            return null;
        CustomTagEntrySerializer<?> serializer = Registries.CUSTOM_TAG_ENTRY_SERIALIZERS.getValue(type);
        CustomTagEntry customEntry;
        try{
            customEntry = serializer.deserialize(json);
        }catch(JsonParseException e){
            throw new RuntimeException("Encountered an exception whilst deserializing custom tag entry for type '" + type + "'!", e);
        }
        return new TagEntryAdapter(type, customEntry);
    }

    static ITag.ITagEntry wrap(CustomTagEntry customEntry){
        return new TagEntryAdapter(Registries.CUSTOM_TAG_ENTRY_SERIALIZERS.getIdentifier(customEntry.getSerializer()), customEntry);
    }
}
