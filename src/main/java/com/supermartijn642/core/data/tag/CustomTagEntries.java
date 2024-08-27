package com.supermartijn642.core.data.tag;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.supermartijn642.core.codec.CodecHelper;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;

import java.util.function.Function;

/**
 * Created 09/02/2024 by SuperMartijn642
 */
public class CustomTagEntries {

    public static void init(){
        TagEntry.CODEC = Codec.either(CodecHelper.jsonSerializerToCodec(
                input -> {
                    JsonObject json = new JsonObject();
                    CustomTagEntrySerializer<?> serializer = input.customEntry.getSerializer();
                    json.addProperty("type", Registries.CUSTOM_TAG_ENTRY_SERIALIZERS.getIdentifier(serializer).toString());
                    try{
                        //noinspection unchecked,rawtypes
                        ((CustomTagEntrySerializer)serializer).serialize(json, input.customEntry);
                    }catch(Exception e){
                        throw new RuntimeException("Encountered an exception whilst serializing custom tag entry for type '" + Registries.CUSTOM_TAG_ENTRY_SERIALIZERS.getIdentifier(serializer).toString() + "'!");
                    }
                    return json;
                },
                element -> {
                    if(!(element instanceof JsonObject))
                        throw new JsonParseException("Entry must be an object!");
                    JsonObject json = (JsonObject)element;
                    if(!json.has("type") || !json.get("type").isJsonPrimitive() || !json.getAsJsonPrimitive("type").isString())
                        throw new JsonParseException("Missing string key 'type'!");
                    String typeString = json.get("type").getAsString();
                    if(!RegistryUtil.isValidIdentifier(typeString))
                        throw new JsonParseException("Invalid identifier '" + typeString + "'!");
                    ResourceLocation type = new ResourceLocation(typeString);
                    if(!Registries.CUSTOM_TAG_ENTRY_SERIALIZERS.hasIdentifier(type))
                        throw new JsonParseException("Unknown custom tag entry serializer '" + typeString + "'!");
                    CustomTagEntrySerializer<?> serializer = Registries.CUSTOM_TAG_ENTRY_SERIALIZERS.getValue(type);
                    CustomTagEntry customEntry;
                    try{
                        customEntry = serializer.deserialize(json);
                    }catch(JsonParseException e){
                        throw new JsonParseException("Encountered an exception whilst deserializing custom tag entry for type '" + type + "'!", e);
                    }
                    return new TagEntryAdapter(type, customEntry);
                }
            ), TagEntry.CODEC)
            .xmap(
                either -> either.map(Function.identity(), Function.identity()),
                tagEntry -> tagEntry instanceof TagEntryAdapter ? Either.left((TagEntryAdapter)tagEntry) : Either.right(tagEntry)
            );
    }

    static TagEntry wrap(CustomTagEntry customEntry){
        return new TagEntryAdapter(Registries.CUSTOM_TAG_ENTRY_SERIALIZERS.getIdentifier(customEntry.getSerializer()), customEntry);
    }
}
