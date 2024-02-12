package com.supermartijn642.core.data.tag;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.supermartijn642.core.data.tag.entries.ElementTagEntry;
import com.supermartijn642.core.data.tag.entries.TagTagEntry;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.util.ResourceLocation;

/**
 * Created 09/02/2024 by SuperMartijn642
 */
public class CustomTagEntries {

    public static JsonElement serialize(CustomTagEntry entry){
        // Special case for vanilla standards
        if(entry instanceof ElementTagEntry){
            if(((ElementTagEntry)entry).isRequired()){
                JsonObject json = new JsonObject();
                json.addProperty("id", ((ElementTagEntry)entry).getElementIdentifier().toString());
                json.addProperty("required", true);
                return json;
            }
            return new JsonPrimitive(((ElementTagEntry)entry).getElementIdentifier().toString());
        }else if(entry instanceof TagTagEntry){
            if(((TagTagEntry)entry).isRequired()){
                JsonObject json = new JsonObject();
                json.addProperty("id", "#" + ((TagTagEntry)entry).getTagIdentifier());
                json.addProperty("required", true);
                return json;
            }
            return new JsonPrimitive("#" + ((TagTagEntry)entry).getTagIdentifier());
        }

        JsonObject json = new JsonObject();
        CustomTagEntrySerializer<?> serializer = entry.getSerializer();
        json.addProperty("type", Registries.CUSTOM_TAG_ENTRY_SERIALIZERS.getIdentifier(serializer).toString());
        try{
            //noinspection unchecked,rawtypes
            ((CustomTagEntrySerializer)serializer).serialize(json, entry);
        }catch(Exception e){
            throw new RuntimeException("Encountered an exception whilst serializing custom tag entry for type '" + Registries.CUSTOM_TAG_ENTRY_SERIALIZERS.getIdentifier(serializer).toString() + "'!");
        }
        return json;
    }

    public static CustomTagEntry deserialize(JsonElement input){
        // Special case for vanilla standards
        if(input.isJsonPrimitive() && input.getAsJsonPrimitive().isString()){
            String s = input.getAsString();
            return s.startsWith("#") ?
                new TagTagEntry(new ResourceLocation(s.substring(1)), true) :
                new ElementTagEntry(new ResourceLocation(s), true);
        }

        if(!(input instanceof JsonObject))
            throw new JsonParseException("Entry must be an object!");
        JsonObject json = (JsonObject)input;

        // Special case for vanilla standards
        if(!json.has("type") || !json.get("type").isJsonPrimitive() || !json.getAsJsonPrimitive("type").isString()){
            if(!json.has("id") || !json.get("id").isJsonPrimitive() || !json.getAsJsonPrimitive("id").isString())
                throw new JsonParseException("Missing string field 'id'!");
            if(json.has("required") && (!json.get("required").isJsonPrimitive() || !json.get("required").getAsJsonPrimitive().isBoolean()))
                throw new RuntimeException("Field 'required' must be a boolean!");
            boolean required = !json.has("required") || json.get("required").getAsBoolean();
            String s = json.get("id").getAsString();
            return s.startsWith("#") ?
                new TagTagEntry(new ResourceLocation(s.substring(1)), required) :
                new ElementTagEntry(new ResourceLocation(s), required);
        }

        String typeString = json.get("type").getAsString();
        if(!RegistryUtil.isValidIdentifier(typeString))
            throw new JsonParseException("Invalid identifier '" + typeString + "'!");
        ResourceLocation type = new ResourceLocation(typeString);
        if(!Registries.CUSTOM_TAG_ENTRY_SERIALIZERS.hasIdentifier(type))
            throw new JsonParseException("Unknown custom tag entry serializer '" + typeString + "'!");
        CustomTagEntrySerializer<?> serializer = Registries.CUSTOM_TAG_ENTRY_SERIALIZERS.getValue(type);
        try{
            return serializer.deserialize(json);
        }catch(JsonParseException e){
            throw new RuntimeException("Encountered an exception whilst deserializing custom tag entry for type '" + type + "'!", e);
        }
    }
}
