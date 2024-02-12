package com.supermartijn642.core.generator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.supermartijn642.core.data.TagLoader;
import com.supermartijn642.core.data.tag.CustomTagEntries;
import com.supermartijn642.core.data.tag.CustomTagEntry;
import com.supermartijn642.core.data.tag.entries.ElementTagEntry;
import com.supermartijn642.core.data.tag.entries.TagTagEntry;
import com.supermartijn642.core.generator.aggregator.ResourceAggregator;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created 05/08/2022 by SuperMartijn642
 */
public abstract class TagGenerator extends ResourceGenerator {

    private static final Map<Registries.Registry<?>,String> TAG_DIRECTORIES = new HashMap<>();

    static{
        TAG_DIRECTORIES.put(Registries.BLOCKS, "blocks");
        TAG_DIRECTORIES.put(Registries.FLUIDS, "fluids");
        TAG_DIRECTORIES.put(Registries.ITEMS, "items");
        TAG_DIRECTORIES.put(Registries.ENTITY_TYPES, "entity_types");
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final ResourceAggregator<TagBuilder<?>,TagBuilder<?>> AGGREGATOR = new ResourceAggregator<TagBuilder<?>,TagBuilder<?>>() {
        @Override
        public TagBuilder<?> initialData(){
            return null;
        }

        @Override
        public TagBuilder<?> combine(TagBuilder<?> data, TagBuilder<?> newData){
            if(data != null){
                //noinspection unchecked,rawtypes
                ((TagBuilder)data).addAll(newData);
                return data;
            }
            return newData;
        }

        @Override
        public void write(OutputStream stream, TagBuilder<?> tag) throws IOException{
            // Convert the tag into a json object
            JsonObject json = new JsonObject();
            // Replace
            json.addProperty("replace", tag.replace);
            // Entries & references
            JsonArray entries = new JsonArray();
            tag.entries.forEach(entry -> entries.add(CustomTagEntries.serialize(entry)));
            if(entries.size() == 0 || tag.remove.isEmpty())
                json.add("values", entries);
            // Removed
            JsonArray removedEntries = new JsonArray();
            tag.remove.forEach(entry -> removedEntries.add(CustomTagEntries.serialize(entry)));
            if(removedEntries.size() > 0)
                json.add("remove", removedEntries);

            // Write the data
            try(Writer writer = new OutputStreamWriter(stream)){
                GSON.toJson(json, writer);
            }
        }
    };

    private final Map<Registries.Registry<?>,Map<ResourceLocation,TagBuilder<?>>> tags = new HashMap<>();

    public TagGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    @Override
    public void save(){
        // Loop over all registries
        for(Map.Entry<Registries.Registry<?>,Map<ResourceLocation,TagBuilder<?>>> registryEntry : this.tags.entrySet()){
            String directoryName = getTagDirectoryName(registryEntry.getKey());
            // Loop over all tags
            for(TagBuilder<?> tag : registryEntry.getValue().values()){
                // Validate tag references
                for(CustomTagEntry entry : tag.entries){
                    if(!(entry instanceof TagTagEntry))
                        continue;
                    ResourceLocation reference = ((TagTagEntry)entry).getTagIdentifier();
                    if(registryEntry.getValue().containsKey(reference))
                        continue;
                    if(TagLoader.getTag(registryEntry.getKey(), reference) != null)
                        continue;
                    if(this.cache.doesResourceExist(ResourceType.DATA, reference.getResourceDomain(), directoryName, reference.getResourcePath(), ".json"))
                        continue;

                    throw new RuntimeException("Could not find tag reference '" + reference + "'!");
                }
                // Save the object to the cache
                ResourceLocation identifier = tag.identifier;
                this.cache.saveResource(ResourceType.DATA, AGGREGATOR, tag, identifier.getResourceDomain(), directoryName, identifier.getResourcePath(), ".json");
            }
        }
    }

    private static String getTagDirectoryName(Registries.Registry<?> registry){
        return "tags/" + TAG_DIRECTORIES.get(registry);
    }

    /**
     * Gets a tag builder for the given identifier. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param identifier resource location of the tag
     */
    protected <T> TagBuilder<T> tag(Registries.Registry<T> registry, ResourceLocation identifier){
        this.cache.trackToBeGeneratedResource(ResourceType.DATA, identifier.getResourceDomain(), getTagDirectoryName(registry), identifier.getResourcePath(), ".json");
        //noinspection unchecked
        return (TagBuilder<T>)this.tags.computeIfAbsent(registry, o -> new HashMap<>()).computeIfAbsent(identifier, identifier1 -> new TagBuilder<>(registry, identifier1));
    }

    /**
     * Gets a tag builder for the given namespace and identifier. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param namespace  namespace of the tag's identifier
     * @param identifier path of the tag's identifier
     */
    protected <T> TagBuilder<T> tag(Registries.Registry<T> registry, String namespace, String identifier){
        return this.tag(registry, new ResourceLocation(namespace, identifier));
    }

    /**
     * Gets a tag builder for the given identifier. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param identifier path of the tag's identifier
     */
    protected <T> TagBuilder<T> tag(Registries.Registry<T> registry, String identifier){
        return this.tag(registry, this.modid, identifier);
    }

    /**
     * Gets a tag builder for the given identifier. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param identifier resource location of the tag
     */
    protected TagBuilder<Block> blockTag(ResourceLocation identifier){
        return this.tag(Registries.BLOCKS, identifier);
    }

    /**
     * Gets a tag builder for the given namespace and identifier. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param namespace  namespace of the tag's identifier
     * @param identifier path of the tag's identifier
     */
    protected TagBuilder<Block> blockTag(String namespace, String identifier){
        return this.tag(Registries.BLOCKS, namespace, identifier);
    }

    /**
     * Gets a tag builder for the given identifier. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param identifier path of the tag's identifier
     */
    protected TagBuilder<Block> blockTag(String identifier){
        return this.tag(Registries.BLOCKS, identifier);
    }

    /**
     * Gets a tag builder for the given identifier. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param identifier resource location of the tag
     */
    protected TagBuilder<Item> itemTag(ResourceLocation identifier){
        return this.tag(Registries.ITEMS, identifier);
    }

    /**
     * Gets a tag builder for the given namespace and identifier. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param namespace  namespace of the tag's identifier
     * @param identifier path of the tag's identifier
     */
    protected TagBuilder<Item> itemTag(String namespace, String identifier){
        return this.tag(Registries.ITEMS, namespace, identifier);
    }

    /**
     * Gets a tag builder for the given identifier. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param identifier path of the tag's identifier
     */
    protected TagBuilder<Item> itemTag(String identifier){
        return this.tag(Registries.ITEMS, identifier);
    }

    /**
     * Gets a tag builder for the given identifier. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param identifier resource location of the tag
     */
    protected TagBuilder<EntityEntry> entityTag(ResourceLocation identifier){
        return this.tag(Registries.ENTITY_TYPES, identifier);
    }


    /**
     * Gets a tag builder for the given namespace and identifier. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param namespace  namespace of the tag's identifier
     * @param identifier path of the tag's identifier
     */
    protected TagBuilder<EntityEntry> entityTag(String namespace, String identifier){
        return this.tag(Registries.ENTITY_TYPES, namespace, identifier);
    }

    /**
     * Gets a tag builder for the given identifier. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param identifier path of the tag's identifier
     */
    protected TagBuilder<EntityEntry> entityTag(String identifier){
        return this.tag(Registries.ENTITY_TYPES, identifier);
    }

    /**
     * Gets a tag builder for the 'minecraft:mineable/axe' tag.
     */
    protected TagBuilder<Block> blockMineableWithAxe(){
        return this.blockTag("minecraft", "mineable/axe");
    }

    /**
     * Gets a tag builder for the 'minecraft:mineable/hoe' tag.
     */
    protected TagBuilder<Block> blockMineableWithHoe(){
        return this.blockTag("minecraft", "mineable/hoe");
    }

    /**
     * Gets a tag builder for the 'minecraft:mineable/pickaxe' tag.
     */
    protected TagBuilder<Block> blockMineableWithPickaxe(){
        return this.blockTag("minecraft", "mineable/pickaxe");
    }

    /**
     * Gets a tag builder for the 'minecraft:mineable/shovel' tag.
     */
    protected TagBuilder<Block> blockMineableWithShovel(){
        return this.blockTag("minecraft", "mineable/shovel");
    }

    /**
     * Gets a tag builder for the 'minecraft:needs_stone_tool' tag.
     */
    protected TagBuilder<Block> blockNeedsStoneTool(){
        return this.blockTag("minecraft", "needs_stone_tool");
    }

    /**
     * Gets a tag builder for the 'minecraft:needs_iron_tool' tag.
     */
    protected TagBuilder<Block> blockNeedsIronTool(){
        return this.blockTag("minecraft", "needs_iron_tool");
    }

    /**
     * Gets a tag builder for the 'minecraft:needs_diamond_tool' tag.
     */
    protected TagBuilder<Block> blockNeedsDiamondTool(){
        return this.blockTag("minecraft", "needs_diamond_tool");
    }

    @Override
    public String getName(){
        return this.modName + " Tag Generator";
    }

    protected static class TagBuilder<T> {

        private final Registries.Registry<T> registry;
        protected final ResourceLocation identifier;
        private final Set<CustomTagEntry> entries = new HashSet<>();
        private final Set<CustomTagEntry> remove = new HashSet<>();
        private boolean replace;

        protected TagBuilder(Registries.Registry<T> registry, ResourceLocation identifier){
            this.registry = registry;
            this.identifier = identifier;
        }

        /**
         * Set whether to replace tag files lower in the datapack order. By default, this is set to {@code false}.
         * @param replace whether to overwrite tag files lower in the datapack order
         */
        public TagBuilder<T> replace(boolean replace){
            this.replace = replace;
            return this;
        }

        /**
         * Sets to replace tag files lower in the datapack order. By default, this is not the case.
         */
        public TagBuilder<T> replace(){
            return this.replace(true);
        }

        /**
         * Adds an entry to this tag.
         * @param entry entry to be added
         */
        public TagBuilder<T> add(T entry){
            this.entries.add(new ElementTagEntry(this.registry.getIdentifier(entry), true));
            return this;
        }

        /**
         * Adds an entry to this tag.
         * @param entry entry to be added
         */
        public TagBuilder<T> add(ResourceLocation entry){
            if(!this.registry.hasIdentifier(entry))
                throw new RuntimeException("Could not find any object registered under '" + entry + "'!");

            this.entries.add(new ElementTagEntry(entry, true));
            return this;
        }

        /**
         * Adds an entry to this tag.
         * @param namespace  namespace of the entry to be added
         * @param identifier path of the entry to be added
         */
        public TagBuilder<T> add(String namespace, String identifier){
            if(!RegistryUtil.isValidNamespace(namespace))
                throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
            if(!RegistryUtil.isValidPath(identifier))
                throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

            this.add(new ResourceLocation(namespace, identifier));
            return this;
        }

        /**
         * Adds an entry to this tag.
         * @param entry entry to be added, must be a valid identifier
         */
        public TagBuilder<T> add(String entry){
            if(!RegistryUtil.isValidIdentifier(entry))
                throw new IllegalArgumentException("Entry identifier '" + entry + "' contains invalid characters!");

            this.add(new ResourceLocation(entry));
            return this;
        }

        /**
         * Adds an optional entry to this tag. The entry can be absent when the tag is loaded without an error being thrown.
         * @param entry entry to be added
         */
        public TagBuilder<T> addOptional(T entry){
            this.entries.add(new ElementTagEntry(this.registry.getIdentifier(entry), false));
            return this;
        }

        /**
         * Adds an optional entry to this tag. The entry can be absent when the tag is loaded without an error being thrown.
         * @param entry entry to be added
         */
        public TagBuilder<T> addOptional(ResourceLocation entry){
            this.entries.add(new ElementTagEntry(entry, false));
            return this;
        }

        /**
         * Adds an optional entry to this tag. The entry can be absent when the tag is loaded without an error being thrown.
         * @param namespace  namespace of the entry to be added
         * @param identifier path of the entry to be added
         */
        public TagBuilder<T> addOptional(String namespace, String identifier){
            if(!RegistryUtil.isValidNamespace(namespace))
                throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
            if(!RegistryUtil.isValidPath(identifier))
                throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

            this.addOptional(new ResourceLocation(namespace, identifier));
            return this;
        }

        /**
         * Adds an optional entry to this tag. The entry can be absent when the tag is loaded without an error being thrown.
         * @param entry entry to be added
         */
        public TagBuilder<T> addOptional(String entry){
            if(!RegistryUtil.isValidIdentifier(entry))
                throw new IllegalArgumentException("Identifier '" + entry + "' contains invalid characters!");

            this.addOptional(new ResourceLocation(entry));
            return this;
        }

        /**
         * Adds an optional custom entry to this tag.
         * @param entry entry to be added
         */
        public TagBuilder<T> addOptional(CustomTagEntry entry){
            this.entries.add(entry);
            return this;
        }

        /**
         * Adds a reference to the given tag.
         */
        public TagBuilder<T> addReference(ResourceLocation tag){
            if(this.identifier.equals(tag))
                throw new IllegalArgumentException("Cannot add self reference to tag '" + tag + "'!");

            this.entries.add(new TagTagEntry(tag, true));
            return this;
        }

        /**
         * Adds a reference to the given tag.
         */
        public TagBuilder<T> addReference(String namespace, String identifier){
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
        public TagBuilder<T> addReference(String tag){
            if(!RegistryUtil.isValidIdentifier(tag))
                throw new IllegalArgumentException("Tag identifier '" + tag + "' contains invalid characters!");

            this.addReference(new ResourceLocation(tag));
            return this;
        }

        /**
         * Adds an optional reference to the given tag.
         */
        public TagBuilder<T> addOptionalReference(ResourceLocation tag){
            if(this.identifier.equals(tag))
                throw new IllegalArgumentException("Cannot add self reference to tag '" + tag + "'!");

            this.entries.add(new TagTagEntry(tag, false));
            return this;
        }

        /**
         * Adds a reference to the given tag.
         */
        public TagBuilder<T> addOptionalReference(String namespace, String identifier){
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
        public TagBuilder<T> addOptionalReference(String tag){
            if(!RegistryUtil.isValidIdentifier(tag))
                throw new IllegalArgumentException("Tag identifier '" + tag + "' contains invalid characters!");

            this.addOptionalReference(new ResourceLocation(tag));
            return this;
        }

        /**
         * Adds an entry to be removed from the tag files lower in the datapack order. Has no effect if {@link #replace(boolean)} is set to {@code true}.
         * @param entry entry to be removed
         */
        public TagBuilder<T> remove(T entry){
            this.remove.add(new ElementTagEntry(this.registry.getIdentifier(entry), true));
            return this;
        }

        /**
         * Adds an entry to be removed from the tag files lower in the datapack order. Has no effect if {@link #replace(boolean)} is set to {@code true}.
         * @param entry entry to be removed
         */
        public TagBuilder<T> remove(ResourceLocation entry){
            if(!this.registry.hasIdentifier(entry))
                throw new RuntimeException("Could not find any object registered under '" + entry + "'!");

            this.remove.add(new ElementTagEntry(entry, true));
            return this;
        }

        /**
         * Adds an entry to be removed from the tag files lower in the datapack order. Has no effect if {@link #replace(boolean)} is set to {@code true}.
         * @param namespace  namespace of the entry to be removed
         * @param identifier path of the entry to be removed
         */
        public TagBuilder<T> remove(String namespace, String identifier){
            if(!RegistryUtil.isValidNamespace(namespace))
                throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
            if(!RegistryUtil.isValidPath(identifier))
                throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

            this.remove(new ResourceLocation(namespace, identifier));
            return this;
        }

        /**
         * Adds an entry to be removed from the tag files lower in the datapack order. Has no effect if {@link #replace(boolean)} is set to {@code true}.
         * @param entry entry to be removed
         */
        public TagBuilder<T> remove(String entry){
            if(!RegistryUtil.isValidIdentifier(entry))
                throw new IllegalArgumentException("Entry identifier '" + entry + "' contains invalid characters!");

            this.remove(new ResourceLocation(entry));
            return this;
        }

        /**
         * Adds an entry to be removed from the tag files lower in the datapack order. Has no effect if {@link #replace(boolean)} is set to {@code true}.
         * @param entry entry to be removed
         */
        public TagBuilder<T> removeOptional(T entry){
            return this.removeOptional(this.registry.getIdentifier(entry));
        }

        /**
         * Adds an entry to be removed from the tag files lower in the datapack order. Has no effect if {@link #replace(boolean)} is set to {@code true}.
         * @param entry entry to be removed
         */
        public TagBuilder<T> removeOptional(ResourceLocation entry){
            this.remove.add(new ElementTagEntry(entry, false));
            return this;
        }

        /**
         * Adds an entry to be removed from the tag files lower in the datapack order. Has no effect if {@link #replace(boolean)} is set to {@code true}.
         * @param namespace  namespace of the entry to be removed
         * @param identifier path of the entry to be removed
         */
        public TagBuilder<T> removeOptional(String namespace, String identifier){
            if(!RegistryUtil.isValidNamespace(namespace))
                throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
            if(!RegistryUtil.isValidPath(identifier))
                throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

            this.removeOptional(new ResourceLocation(namespace, identifier));
            return this;
        }

        /**
         * Adds an entry to be removed from the tag files lower in the datapack order. Has no effect if {@link #replace(boolean)} is set to {@code true}.
         * @param entry entry to be removed
         */
        public TagBuilder<T> removeOptional(String entry){
            if(!RegistryUtil.isValidIdentifier(entry))
                throw new IllegalArgumentException("Identifier '" + entry + "' contains invalid characters!");

            this.removeOptional(new ResourceLocation(entry));
            return this;
        }

        private void addAll(TagBuilder<T> other){
            this.entries.addAll(other.entries);
            this.remove.addAll(other.remove);
        }
    }
}
