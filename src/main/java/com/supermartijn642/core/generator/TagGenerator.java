package com.supermartijn642.core.generator;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import com.supermartijn642.core.data.tag.CustomTagEntry;
import com.supermartijn642.core.generator.aggregator.ResourceAggregator;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

/**
 * Created 05/08/2022 by SuperMartijn642
 */
public abstract class TagGenerator extends ResourceGenerator {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final ResourceAggregator<TagBuilder<?>,TagBuilder<?>> AGGREGATOR = new ResourceAggregator<>() {
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
            tag.entries.stream()
                .map(entry -> TagEntry.CODEC.encodeStart(JsonOps.INSTANCE, entry).getOrThrow())
                .sorted(TagGenerator::compareJson)
                .forEach(entries::add);
            if(!entries.isEmpty() || tag.remove.isEmpty())
                json.add("values", entries);
            // Removed
            JsonArray removedEntries = new JsonArray();
            tag.remove.stream()
                .map(entry -> TagEntry.CODEC.encodeStart(JsonOps.INSTANCE, entry).getOrThrow())
                .sorted(TagGenerator::compareJson)
                .forEach(removedEntries::add);
            if(!removedEntries.isEmpty())
                json.add("remove", removedEntries);

            // Write the data
            try(Writer writer = new OutputStreamWriter(stream)){
                GSON.toJson(json, writer);
            }
        }
    };

    private final Map<Registries.Registry<?>,Map<Identifier,TagBuilder<?>>> tags = new HashMap<>();

    public TagGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    @Override
    public void save(){
        // Loop over all registries
        for(Map.Entry<Registries.Registry<?>,Map<Identifier,TagBuilder<?>>> registryEntry : this.tags.entrySet()){
            String directoryName = getTagDirectoryName(registryEntry.getKey());
            // Loop over all tags
            for(TagBuilder<?> tag : registryEntry.getValue().values()){
                // Validate tag references
                for(TagEntry entry : tag.entries){
                    if(!entry.tag || !entry.required)
                        continue;
                    Identifier reference = entry.id;
                    if(registryEntry.getValue().containsKey(reference))
                        continue;
                    if(this.cache.doesResourceExist(ResourceType.DATA, reference.getNamespace(), directoryName, reference.getPath(), ".json"))
                        continue;

                    throw new RuntimeException("Could not find tag reference '" + reference + "' in '" + tag.identifier + "'!");
                }
                // Save the object to the cache
                Identifier identifier = tag.identifier;
                this.cache.saveResource(ResourceType.DATA, AGGREGATOR, tag, identifier.getNamespace(), directoryName, identifier.getPath(), ".json");
            }
        }
    }

    private static String getTagDirectoryName(Registries.Registry<?> registry){
        return net.minecraft.core.registries.Registries.tagsDirPath(registry.getVanillaRegistry().key());
    }

    /**
     * Gets a tag builder for the given identifier. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param identifier resource location of the tag
     */
    protected <T> TagBuilder<T> tag(Registries.Registry<T> registry, Identifier identifier){
        this.cache.trackToBeGeneratedResource(ResourceType.DATA, identifier.getNamespace(), getTagDirectoryName(registry), identifier.getPath(), ".json");
        //noinspection unchecked
        return (TagBuilder<T>)this.tags.computeIfAbsent(registry, o -> new HashMap<>()).computeIfAbsent(identifier, identifier1 -> new TagBuilder<>(registry, identifier1));
    }

    /**
     * Gets a tag builder for the given key. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param tagKey key of the tag
     */
    protected <T> TagBuilder<T> tag(Registries.Registry<T> registry, TagKey<T> tagKey){
        return this.tag(registry, tagKey.location());
    }

    /**
     * Gets a tag builder for the given namespace and identifier. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param namespace  namespace of the tag's identifier
     * @param identifier path of the tag's identifier
     */
    protected <T> TagBuilder<T> tag(Registries.Registry<T> registry, String namespace, String identifier){
        return this.tag(registry, Identifier.fromNamespaceAndPath(namespace, identifier));
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
    protected TagBuilder<Block> blockTag(Identifier identifier){
        return this.tag(Registries.BLOCKS, identifier);
    }

    /**
     * Gets a tag builder for the given key. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param tagKey key of the tag
     */
    protected TagBuilder<Block> blockTag(TagKey<Block> tagKey){
        return this.tag(Registries.BLOCKS, tagKey);
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
    protected TagBuilder<Item> itemTag(Identifier identifier){
        return this.tag(Registries.ITEMS, identifier);
    }

    /**
     * Gets a tag builder for the given key. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param tagKey key of the tag
     */
    protected TagBuilder<Item> itemTag(TagKey<Item> tagKey){
        return this.tag(Registries.ITEMS, tagKey);
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
    protected TagBuilder<EntityType<?>> entityTag(Identifier identifier){
        return this.tag(Registries.ENTITY_TYPES, identifier);
    }

    /**
     * Gets a tag builder for the given key. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param tagKey key of the tag
     */
    protected TagBuilder<EntityType<?>> entityTag(TagKey<EntityType<?>> tagKey){
        return this.tag(Registries.ENTITY_TYPES, tagKey);
    }

    /**
     * Gets a tag builder for the given namespace and identifier. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param namespace  namespace of the tag's identifier
     * @param identifier path of the tag's identifier
     */
    protected TagBuilder<EntityType<?>> entityTag(String namespace, String identifier){
        return this.tag(Registries.ENTITY_TYPES, namespace, identifier);
    }

    /**
     * Gets a tag builder for the given identifier. The returned tag builder may be a new tag builder or an existing one if requested before.
     * @param identifier path of the tag's identifier
     */
    protected TagBuilder<EntityType<?>> entityTag(String identifier){
        return this.tag(Registries.ENTITY_TYPES, identifier);
    }

    /**
     * Gets a tag builder for the 'minecraft:mineable/axe' tag.
     */
    protected TagBuilder<Block> blockMineableWithAxe(){
        return this.blockTag(BlockTags.MINEABLE_WITH_AXE);
    }

    /**
     * Gets a tag builder for the 'minecraft:mineable/hoe' tag.
     */
    protected TagBuilder<Block> blockMineableWithHoe(){
        return this.blockTag(BlockTags.MINEABLE_WITH_HOE);
    }

    /**
     * Gets a tag builder for the 'minecraft:mineable/pickaxe' tag.
     */
    protected TagBuilder<Block> blockMineableWithPickaxe(){
        return this.blockTag(BlockTags.MINEABLE_WITH_PICKAXE);
    }

    /**
     * Gets a tag builder for the 'minecraft:mineable/shovel' tag.
     */
    protected TagBuilder<Block> blockMineableWithShovel(){
        return this.blockTag(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    /**
     * Gets a tag builder for the 'minecraft:needs_stone_tool' tag.
     */
    protected TagBuilder<Block> blockNeedsStoneTool(){
        return this.blockTag(BlockTags.NEEDS_STONE_TOOL);
    }

    /**
     * Gets a tag builder for the 'minecraft:needs_iron_tool' tag.
     */
    protected TagBuilder<Block> blockNeedsIronTool(){
        return this.blockTag(BlockTags.NEEDS_IRON_TOOL);
    }

    /**
     * Gets a tag builder for the 'minecraft:needs_diamond_tool' tag.
     */
    protected TagBuilder<Block> blockNeedsDiamondTool(){
        return this.blockTag(BlockTags.NEEDS_DIAMOND_TOOL);
    }

    @Override
    public String getName(){
        return this.modName + " Tag Generator";
    }

    protected static class TagBuilder<T> {

        private final Registries.Registry<T> registry;
        protected final Identifier identifier;
        private final Set<TagEntry> entries = new HashSet<>();
        private final Set<TagEntry> remove = new HashSet<>();
        private boolean replace;

        protected TagBuilder(Registries.Registry<T> registry, Identifier identifier){
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
            this.entries.add(new TagEntry(this.registry.getIdentifier(entry), false, true));
            return this;
        }

        /**
         * Adds an entry to this tag.
         * @param entry entry to be added
         */
        public TagBuilder<T> add(Identifier entry){
            if(!this.registry.hasIdentifier(entry))
                throw new RuntimeException("Could not find any object registered under '" + entry + "'!");

            this.entries.add(new TagEntry(entry, false, true));
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

            this.add(Identifier.fromNamespaceAndPath(namespace, identifier));
            return this;
        }

        /**
         * Adds an entry to this tag.
         * @param entry entry to be added, must be a valid identifier
         */
        public TagBuilder<T> add(String entry){
            if(!RegistryUtil.isValidIdentifier(entry))
                throw new IllegalArgumentException("Entry identifier '" + entry + "' contains invalid characters!");

            this.add(Identifier.parse(entry));
            return this;
        }

        /**
         * Adds an optional entry to this tag. The entry can be absent when the tag is loaded without an error being thrown.
         * @param entry entry to be added
         */
        public TagBuilder<T> addOptional(T entry){
            this.entries.add(new TagEntry(this.registry.getIdentifier(entry), false, false));
            return this;
        }

        /**
         * Adds an optional entry to this tag. The entry can be absent when the tag is loaded without an error being thrown.
         * @param entry entry to be added
         */
        public TagBuilder<T> addOptional(Identifier entry){
            this.entries.add(new TagEntry(entry, false, false));
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

            this.addOptional(Identifier.fromNamespaceAndPath(namespace, identifier));
            return this;
        }

        /**
         * Adds an optional entry to this tag. The entry can be absent when the tag is loaded without an error being thrown.
         * @param entry entry to be added
         */
        public TagBuilder<T> addOptional(String entry){
            if(!RegistryUtil.isValidIdentifier(entry))
                throw new IllegalArgumentException("Identifier '" + entry + "' contains invalid characters!");

            this.addOptional(Identifier.parse(entry));
            return this;
        }

        /**
         * Adds an optional custom entry to this tag.
         * @param entry entry to be added
         */
        public TagBuilder<T> addOptional(CustomTagEntry entry){
            this.entries.add(CustomTagEntry.createVanillaEntry(entry));
            return this;
        }

        /**
         * Adds a reference to the given tag.
         */
        public TagBuilder<T> addReference(Identifier tag){
            if(this.identifier.equals(tag))
                throw new IllegalArgumentException("Cannot add self reference to tag '" + tag + "'!");

            this.entries.add(new TagEntry(tag, true, true));
            return this;
        }

        /**
         * Adds a reference to the given tag.
         */
        public TagBuilder<T> addReference(TagKey<T> tag){
            return this.addReference(tag.location());
        }

        /**
         * Adds a reference to the given tag.
         */
        public TagBuilder<T> addReference(String namespace, String identifier){
            if(!RegistryUtil.isValidNamespace(namespace))
                throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
            if(!RegistryUtil.isValidPath(identifier))
                throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

            this.entries.add(new TagEntry(Identifier.fromNamespaceAndPath(namespace, identifier), true, true));
            return this;
        }

        /**
         * Adds a reference to the given tag.
         */
        public TagBuilder<T> addReference(String tag){
            if(!RegistryUtil.isValidIdentifier(tag))
                throw new IllegalArgumentException("Tag identifier '" + tag + "' contains invalid characters!");

            this.entries.add(new TagEntry(Identifier.parse(tag), true, true));
            return this;
        }

        /**
         * Adds an optional reference to the given tag.
         */
        public TagBuilder<T> addOptionalReference(Identifier tag){
            if(this.identifier.equals(tag))
                throw new IllegalArgumentException("Cannot add self reference to tag '" + tag + "'!");

            this.entries.add(new TagEntry(tag, true, false));
            return this;
        }

        /**
         * Adds a reference to the given tag.
         */
        public TagBuilder<T> addOptionalReference(TagKey<T> tag){
            return this.addOptionalReference(tag.location());
        }

        /**
         * Adds a reference to the given tag.
         */
        public TagBuilder<T> addOptionalReference(String namespace, String identifier){
            if(!RegistryUtil.isValidNamespace(namespace))
                throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
            if(!RegistryUtil.isValidPath(identifier))
                throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

            this.addOptionalReference(Identifier.fromNamespaceAndPath(namespace, identifier));
            return this;
        }

        /**
         * Adds an optional reference to the given tag.
         */
        public TagBuilder<T> addOptionalReference(String tag){
            if(!RegistryUtil.isValidIdentifier(tag))
                throw new IllegalArgumentException("Tag identifier '" + tag + "' contains invalid characters!");

            this.addOptionalReference(Identifier.parse(tag));
            return this;
        }

        /**
         * Adds an entry to be removed from the tag files lower in the datapack order. Has no effect if {@link #replace(boolean)} is set to {@code true}.
         * @param entry entry to be removed
         */
        public TagBuilder<T> remove(T entry){
            this.remove.add(new TagEntry(this.registry.getIdentifier(entry), false, true));
            return this;
        }

        /**
         * Adds an entry to be removed from the tag files lower in the datapack order. Has no effect if {@link #replace(boolean)} is set to {@code true}.
         * @param entry entry to be removed
         */
        public TagBuilder<T> remove(Identifier entry){
            if(!this.registry.hasIdentifier(entry))
                throw new RuntimeException("Could not find any object registered under '" + entry + "'!");

            this.remove.add(new TagEntry(entry, false, true));
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

            this.remove(Identifier.fromNamespaceAndPath(namespace, identifier));
            return this;
        }

        /**
         * Adds an entry to be removed from the tag files lower in the datapack order. Has no effect if {@link #replace(boolean)} is set to {@code true}.
         * @param entry entry to be removed
         */
        public TagBuilder<T> remove(String entry){
            if(!RegistryUtil.isValidIdentifier(entry))
                throw new IllegalArgumentException("Entry identifier '" + entry + "' contains invalid characters!");

            this.remove(Identifier.parse(entry));
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
        public TagBuilder<T> removeOptional(Identifier entry){
            this.remove.add(new TagEntry(entry, false, false));
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

            this.removeOptional(Identifier.fromNamespaceAndPath(namespace, identifier));
            return this;
        }

        /**
         * Adds an entry to be removed from the tag files lower in the datapack order. Has no effect if {@link #replace(boolean)} is set to {@code true}.
         * @param entry entry to be removed
         */
        public TagBuilder<T> removeOptional(String entry){
            if(!RegistryUtil.isValidIdentifier(entry))
                throw new IllegalArgumentException("Identifier '" + entry + "' contains invalid characters!");

            this.removeOptional(Identifier.parse(entry));
            return this;
        }

        private void addAll(TagBuilder<T> other){
            this.entries.addAll(other.entries);
            this.remove.addAll(other.remove);
        }
    }

    private static int compareJson(JsonElement element1, JsonElement element2){
        if(element1.isJsonNull()){
            if(!element2.isJsonNull())
                return -1;
            return 0;
        }else if(element2.isJsonNull())
            return 1;
        if(element1.isJsonPrimitive()){
            if(!element2.isJsonPrimitive())
                return -1;
            JsonPrimitive primitive1 = element1.getAsJsonPrimitive();
            JsonPrimitive primitive2 = element2.getAsJsonPrimitive();
            if(primitive1.isString()){
                if(!primitive2.isString())
                    return -1;
                return primitive1.getAsString().compareTo(primitive2.getAsString());
            }else if(primitive2.isString())
                return 1;
            if(primitive1.isNumber()){
                if(!primitive2.isNumber())
                    return -1;
                return Double.compare(primitive1.getAsDouble(), primitive2.getAsDouble());
            }else if(primitive2.isNumber())
                return 1;
            if(primitive1.isBoolean()){
                if(!primitive2.isBoolean())
                    return -1;
            }else if(primitive2.isBoolean())
                return 1;
        }else if(element2.isJsonPrimitive())
            return 1;
        if(element1.isJsonObject()){
            if(!element2.isJsonObject())
                return -1;
            JsonObject object1 = element1.getAsJsonObject();
            JsonObject object2 = element2.getAsJsonObject();
            if(object1.size() != object2.size())
                return object1.size() - object2.size();
            Iterator<String> iterator1 = object1.keySet().iterator();
            Iterator<String> iterator2 = object2.keySet().iterator();
            while(iterator1.hasNext() && iterator2.hasNext()){
                String key1 = iterator1.next();
                String key2 = iterator2.next();
                if(!key1.equals(key2))
                    return key1.compareTo(key2);
                int compare = compareJson(object1.get(key1), object2.get(key2));
                if(compare != 0)
                    return compare;
            }
            if(iterator1.hasNext() || iterator2.hasNext())
                throw new AssertionError();
            return 0;
        }else if(element2.isJsonObject())
            return 1;
        if(element1.isJsonArray()){
            if(!element2.isJsonArray())
                return -1;
            JsonArray array1 = element1.getAsJsonArray();
            JsonArray array2 = element2.getAsJsonArray();
            if(array1.size() != array2.size())
                return array1.size() - array2.size();
            for(int i = 0; i < array1.size(); i++){
                int compare = compareJson(array1.get(i), array2.get(i));
                if(compare != 0)
                    return compare;
            }
            return 0;
        }else if(element2.isJsonArray())
            return 1;
        throw new AssertionError("Unknown json element type '" + element1.getClass() + "'!");
    }
}
