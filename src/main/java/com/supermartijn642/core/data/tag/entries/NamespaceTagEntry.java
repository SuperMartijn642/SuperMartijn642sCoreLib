package com.supermartijn642.core.data.tag.entries;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.supermartijn642.core.data.tag.CustomTagEntry;
import com.supermartijn642.core.data.tag.CustomTagEntrySerializer;
import com.supermartijn642.core.registry.RegistryUtil;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created 09/02/2024 by SuperMartijn642
 */
public class NamespaceTagEntry implements CustomTagEntry {

    public static final CustomTagEntrySerializer<NamespaceTagEntry> SERIALIZER = new Serializer();

    private final String namespace;

    public NamespaceTagEntry(String namespace){
        this.namespace = namespace;
    }

    @Override
    public <T> Collection<T> resolve(TagEntryResolutionContext<T> context){
        return context.getAllIdentifiers().stream()
            .filter(i -> i.getNamespace().equals(this.namespace))
            .map(context::getElement)
            .collect(Collectors.toList());
    }

    @Override
    public CustomTagEntrySerializer<?> getSerializer(){
        return SERIALIZER;
    }

    private static class Serializer implements CustomTagEntrySerializer<NamespaceTagEntry> {

        @Override
        public void serialize(JsonObject json, NamespaceTagEntry entry){
            json.addProperty("namespace", entry.namespace);
        }

        @Override
        public NamespaceTagEntry deserialize(JsonObject json){
            if(!json.has("namespace") || !json.get("namespace").isJsonPrimitive() || !json.getAsJsonPrimitive("namespace").isString())
                throw new JsonParseException("Tag entry must have string key 'namespace'!");
            String namespace = json.get("namespace").getAsString();
            if(!RegistryUtil.isValidNamespace(namespace))
                throw new JsonParseException("Invalid namespace '" + namespace + "'!");
            return new NamespaceTagEntry(namespace);
        }
    }
}
