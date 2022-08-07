package com.supermartijn642.core.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.supermartijn642.core.data.TagLoader;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created 05/08/2022 by SuperMartijn642
 */
public abstract class TagGenerator<T extends IForgeRegistryEntry<T>> extends ResourceGenerator {

    private final Map<ResourceLocation,TagBuilder> tags = new HashMap<>();
    protected final String directoryName;
    protected final Registries.Registry<T> registry;

    public TagGenerator(String modid, ResourceCache cache, String directoryName, Registries.Registry<T> registry){
        super(modid, cache);
        this.directoryName = directoryName;
        this.registry = registry;
    }

    @Override
    public void finish(){
        // Loop over all tags
        for(Map.Entry<ResourceLocation,TagBuilder> entry : this.tags.entrySet()){
            TagBuilder tag = entry.getValue();

            // Validate tag references
            for(ResourceLocation reference : tag.references){
                if(this.tags.containsKey(reference))
                    continue;
                if(TagLoader.getTag(this.registry, reference) != null)
                    continue;
                if(this.cache.doesResourceExist(ResourceType.DATA, reference.getResourceDomain(), "tags/" + this.directoryName, reference.getResourcePath(), ".json"))
                    continue;

                throw new RuntimeException("Could not find tag reference '" + reference + "'!");
            }

            // Convert the tag into a json object
            JsonObject object = new JsonObject();
            // Replace
            object.addProperty("replace", tag.replace);
            // Entries & references
            JsonArray entries = new JsonArray();
            tag.entries.forEach(entries::add);
            tag.references.stream().map(ResourceLocation::toString).map(s -> "#" + s).forEach(entries::add);
            tag.optionalEntries.stream().map(e -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", e);
                jsonObject.addProperty("required", false);
                return jsonObject;
            }).forEach(entries::add);
            tag.optionalReferences.stream().map(e -> {
                JsonObject entryObject = new JsonObject();
                entryObject.addProperty("id", "#" + e);
                entryObject.addProperty("required", false);
                return entryObject;
            }).forEach(entries::add);
            if(entries.size() > 0 || tag.remove.isEmpty())
                object.add("values", entries);
            // Removed
            JsonArray removedEntries = new JsonArray();
            tag.remove.forEach(removedEntries::add);
            if(removedEntries.size() > 0)
                object.add("remove", removedEntries);

            // Save the object to the cache
            ResourceLocation identifier = entry.getKey();
            this.cache.saveJsonResource(ResourceType.DATA, object, identifier.getResourceDomain(), "tags/" + this.directoryName, identifier.getResourcePath());
        }
    }

    protected TagBuilder tag(ResourceLocation identifier){
        return this.tags.computeIfAbsent(identifier, TagBuilder::new);
    }

    protected TagBuilder tag(String namespace, String identifier){
        return this.tag(new ResourceLocation(namespace, identifier));
    }

    @Override
    public String getName(){
        return this.modName + " Tag Generator";
    }

    protected class TagBuilder {

        private final ResourceLocation identifier;
        private final Set<String> entries = new HashSet<>();
        private final Set<String> optionalEntries = new HashSet<>();
        private final Set<ResourceLocation> references = new HashSet<>();
        private final Set<String> optionalReferences = new HashSet<>();
        private final Set<String> remove = new HashSet<>();
        private boolean replace;

        private TagBuilder(ResourceLocation identifier){
            this.identifier = identifier;
        }

        public TagBuilder replace(){
            this.replace = true;
            return this;
        }

        public TagBuilder add(T entry){
            this.entries.add(entry.getRegistryName().toString());
            return this;
        }

        public TagBuilder add(ResourceLocation entry){
            if(!TagGenerator.this.registry.hasIdentifier(entry))
                throw new RuntimeException("Could find any object registered under '" + entry + "'!");

            this.entries.add(entry.toString());
            return this;
        }

        public TagBuilder add(String namespace, String identifier){
            if(!RegistryUtil.isValidNamespace(namespace))
                throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
            if(!RegistryUtil.isValidPath(identifier))
                throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

            this.add(new ResourceLocation(namespace, identifier));
            return this;
        }

        public TagBuilder add(String entry){
            if(!RegistryUtil.isValidIdentifier(entry))
                throw new IllegalArgumentException("Entry identifier '" + entry + "' contains invalid characters!");

            this.add(new ResourceLocation(entry));
            return this;
        }

        public TagBuilder addOptional(ResourceLocation entry){
            this.optionalEntries.add(entry.toString());
            return this;
        }

        public TagBuilder addOptional(String namespace, String identifier){
            if(!RegistryUtil.isValidNamespace(namespace))
                throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
            if(!RegistryUtil.isValidPath(identifier))
                throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

            this.addOptional(new ResourceLocation(namespace, identifier));
            return this;
        }

        public TagBuilder addOptional(String entry){
            if(!RegistryUtil.isValidIdentifier(entry))
                throw new IllegalArgumentException("Identifier '" + entry + "' contains invalid characters!");

            this.addOptional(new ResourceLocation(entry));
            return this;
        }

        /**
         * Adds a reference to the given tag.
         */
        public TagBuilder addReference(ResourceLocation tag){
            if(this.identifier.equals(tag))
                throw new IllegalArgumentException("Cannot add self reference to tag '" + tag + "'!");

            this.references.add(tag);
            return this;
        }

        /**
         * Adds a reference to the given tag.
         */
        public TagBuilder addReference(String namespace, String identifier){
            if(!RegistryUtil.isValidNamespace(namespace))
                throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
            if(!RegistryUtil.isValidPath(identifier))
                throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

            this.addReference(new ResourceLocation(namespace, identifier));
            return this;
        }

        /**
         * Adds a reference to the given tag.
         */
        public TagBuilder addReference(String tag){
            if(!RegistryUtil.isValidIdentifier(tag))
                throw new IllegalArgumentException("Tag identifier '" + tag + "' contains invalid characters!");

            this.addReference(new ResourceLocation(tag));
            return this;
        }

        /**
         * Adds an optional reference to the given tag.
         */
        public TagBuilder addOptionalReference(ResourceLocation tag){
            if(this.identifier.equals(tag))
                throw new IllegalArgumentException("Cannot add self reference to tag '" + tag + "'!");

            this.optionalReferences.add(tag.toString());
            return this;
        }

        /**
         * Adds a reference to the given tag.
         */
        public TagBuilder addOptionalReference(String namespace, String identifier){
            if(!RegistryUtil.isValidNamespace(namespace))
                throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
            if(!RegistryUtil.isValidPath(identifier))
                throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

            this.addOptionalReference(new ResourceLocation(namespace, identifier));
            return this;
        }

        /**
         * Adds an optional reference to the given tag.
         */
        public TagBuilder addOptionalReference(String tag){
            if(!RegistryUtil.isValidIdentifier(tag))
                throw new IllegalArgumentException("Tag identifier '" + tag + "' contains invalid characters!");

            this.addOptionalReference(new ResourceLocation(tag));
            return this;
        }

        public TagBuilder remove(T entry){
            this.remove.add(entry.getRegistryName().toString());
            return this;
        }

        public TagBuilder remove(ResourceLocation entry){
            if(!TagGenerator.this.registry.hasIdentifier(entry))
                throw new RuntimeException("Could find any object registered under '" + entry + "'!");

            this.remove.add(entry.toString());
            return this;
        }

        public TagBuilder remove(String namespace, String identifier){
            if(!RegistryUtil.isValidNamespace(namespace))
                throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
            if(!RegistryUtil.isValidPath(identifier))
                throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

            this.remove(new ResourceLocation(namespace, identifier));
            return this;
        }

        public TagBuilder remove(String entry){
            if(!RegistryUtil.isValidIdentifier(entry))
                throw new IllegalArgumentException("Entry identifier '" + entry + "' contains invalid characters!");

            this.remove(new ResourceLocation(entry));
            return this;
        }

        public TagBuilder removeOptional(ResourceLocation entry){
            this.remove.add(entry.toString());
            return this;
        }

        public TagBuilder removeOptional(String namespace, String identifier){
            if(!RegistryUtil.isValidNamespace(namespace))
                throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
            if(!RegistryUtil.isValidPath(identifier))
                throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

            this.removeOptional(new ResourceLocation(namespace, identifier));
            return this;
        }

        public TagBuilder removeOptional(String entry){
            if(!RegistryUtil.isValidIdentifier(entry))
                throw new IllegalArgumentException("Identifier '" + entry + "' contains invalid characters!");

            this.removeOptional(new ResourceLocation(entry));
            return this;
        }
    }
}
