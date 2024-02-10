package com.supermartijn642.core.data.tag;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
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
        TagEntry.CODEC = Codec.either(Codec.<TagEntry>of(
                new Encoder<>() {
                    @Override
                    public <T> DataResult<T> encode(TagEntry input, DynamicOps<T> ops, T prefix){
                        if(!(input instanceof TagEntryAdapter))
                            return DataResult.error(() -> "Tag entry is not a custom entry!");

                        JsonObject json = new JsonObject();
                        CustomTagEntrySerializer<?> serializer = ((TagEntryAdapter)input).customEntry.getSerializer();
                        json.addProperty("type", Registries.CUSTOM_TAG_ENTRY_SERIALIZERS.getIdentifier(serializer).toString());
                        try{
                            //noinspection unchecked,rawtypes
                            ((CustomTagEntrySerializer)serializer).serialize(json, ((TagEntryAdapter)input).customEntry);
                        }catch(Exception e){
                            throw new RuntimeException("Encountered an exception whilst serializing custom tag entry for type '" + Registries.CUSTOM_TAG_ENTRY_SERIALIZERS.getIdentifier(serializer).toString() + "'!");
                        }
                        //noinspection unchecked
                        return DataResult.success((T)json);
                    }
                },
                new Decoder<>() {
                    @Override
                    public <T> DataResult<Pair<TagEntry,T>> decode(DynamicOps<T> ops, T input){
                        if(!(input instanceof JsonObject))
                            return DataResult.error(() -> "Entry must be an object!");
                        JsonObject json = (JsonObject)input;
                        if(!json.has("type") || !json.get("type").isJsonPrimitive() || !json.getAsJsonPrimitive("type").isString())
                            return DataResult.error(() -> "Missing string key 'type'!");
                        String typeString = json.get("type").getAsString();
                        if(!RegistryUtil.isValidIdentifier(typeString))
                            return DataResult.error(() -> "Invalid identifier '" + typeString + "'!");
                        ResourceLocation type = new ResourceLocation(typeString);
                        if(!Registries.CUSTOM_TAG_ENTRY_SERIALIZERS.hasIdentifier(type))
                            return DataResult.error(() -> "Unknown custom tag entry serializer '" + typeString + "'!");
                        CustomTagEntrySerializer<?> serializer = Registries.CUSTOM_TAG_ENTRY_SERIALIZERS.getValue(type);
                        CustomTagEntry customEntry;
                        try{
                            customEntry = serializer.deserialize(json);
                        }catch(JsonParseException e){
                            return DataResult.error(e::getMessage);
                        }catch(Exception e){
                            throw new RuntimeException("Encountered an exception whilst deserializing custom tag entry for type '" + type + "'!", e);
                        }
                        return DataResult.success(Pair.of(new TagEntryAdapter(type, customEntry), input));
                    }
                }
            ), TagEntry.CODEC)
            .xmap(
                either -> either.map(Function.identity(), Function.identity()),
                tagEntry -> tagEntry instanceof TagEntryAdapter ? Either.left(tagEntry) : Either.right(tagEntry)
            );
    }

    static TagEntry wrap(CustomTagEntry customEntry){
        return new TagEntryAdapter(Registries.CUSTOM_TAG_ENTRY_SERIALIZERS.getIdentifier(customEntry.getSerializer()), customEntry);
    }
}
