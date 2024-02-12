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
public class ElementTagEntry implements CustomTagEntry {

    public static final CustomTagEntrySerializer<ElementTagEntry> SERIALIZER = new Serializer();

    private final ResourceLocation identifier;
    private final boolean required;

    public ElementTagEntry(ResourceLocation identifier, boolean required){
        this.identifier = identifier;
        this.required = required;
    }

    public ResourceLocation getElementIdentifier(){
        return this.identifier;
    }

    public boolean isRequired(){
        return this.required;
    }

    @Override
    public <T> Collection<T> resolve(TagEntryResolutionContext<T> context){
        T element = context.getElement(this.identifier);
        if(element == null && this.required)
            throw new RuntimeException("Unknown identifier '" + this.identifier + "'!");
        return element == null ? null : Collections.singleton(element);
    }

    @Override
    public CustomTagEntrySerializer<?> getSerializer(){
        return SERIALIZER;
    }

    private static class Serializer implements CustomTagEntrySerializer<ElementTagEntry> {
        @Override
        public void serialize(JsonObject json, ElementTagEntry entry){
            json.addProperty("identifier", entry.identifier.toString());
            json.addProperty("required", entry.required);
        }

        @Override
        public ElementTagEntry deserialize(JsonObject json){
            return new ElementTagEntry(
                new ResourceLocation(json.get("identifier").getAsString()),
                !json.has("required") || json.get("required").getAsBoolean()
            );
        }
    }
}
