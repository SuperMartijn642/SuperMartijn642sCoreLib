package com.supermartijn642.core.data.tag.entries;

import com.google.gson.JsonObject;
import com.supermartijn642.core.data.tag.CustomTagEntry;
import com.supermartijn642.core.data.tag.CustomTagEntrySerializer;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Collections;

/**
 * Created 11/02/2024 by SuperMartijn642
 */
public class TagTagEntry implements CustomTagEntry {

    public static final CustomTagEntrySerializer<TagTagEntry> SERIALIZER = new Serializer();

    private final ResourceLocation tag;
    private final boolean required;

    public TagTagEntry(ResourceLocation tag, boolean required){
        this.tag = tag;
        this.required = required;
    }

    public ResourceLocation getTagIdentifier(){
        return this.tag;
    }

    public boolean isRequired(){
        return this.required;
    }

    @Override
    public <T> Collection<T> resolve(TagEntryResolutionContext<T> context){
        Collection<T> tag = context.getTag(this.tag);
        if(tag == null && this.required)
            throw new RuntimeException("Unknown tag '" + this.tag + "'!");
        return tag;
    }

    @Override
    public Collection<ResourceLocation> getTagDependencies(){
        return Collections.singleton(this.tag);
    }

    @Override
    public CustomTagEntrySerializer<?> getSerializer(){
        return SERIALIZER;
    }

    private static class Serializer implements CustomTagEntrySerializer<TagTagEntry> {
        @Override
        public void serialize(JsonObject json, TagTagEntry entry){
            json.addProperty("tag", entry.tag.toString());
            json.addProperty("required", entry.required);
        }

        @Override
        public TagTagEntry deserialize(JsonObject json){
            return new TagTagEntry(
                new ResourceLocation(json.get("tag").getAsString()),
                !json.has("required") || json.get("required").getAsBoolean()
            );
        }
    }
}
