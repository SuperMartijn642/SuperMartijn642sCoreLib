package com.supermartijn642.core.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.supermartijn642.core.data.TagLoader;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;

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

    private final Map<Registries.Registry<?>,Map<ResourceLocation,TagBuilder<?>>> tags = new HashMap<>();

    public TagGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    @Override
    public void save(){
        // Loop over all registries
        for(Map.Entry<Registries.Registry<?>,Map<ResourceLocation,TagBuilder<?>>> registryEntry : this.tags.entrySet()){
            String directoryName = getTagDirectoryName(registryEntry.getKey());
            if(directoryName.startsWith("tags/"))
                directoryName = directoryName.substring("tags/".length());
            // Loop over all tags
            for(TagBuilder<?> tag : registryEntry.getValue().values()){
                // Validate tag references
                for(ResourceLocation reference : tag.references){
                    if(registryEntry.getValue().containsKey(reference))
                        continue;
                    if(TagLoader.getTag(registryEntry.getKey(), reference) != null)
                        continue;
                    if(this.cache.doesResourceExist(ResourceType.DATA, reference.getResourceDomain(), "tags/" + directoryName, reference.getResourcePath(), ".json"))
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
                tag.optionalRemove.stream().map(e -> {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("id", e);
                    jsonObject.addProperty("required", false);
                    return jsonObject;
                }).forEach(entries::add);
                if(removedEntries.size() > 0)
                    object.add("remove", removedEntries);

                // Save the object to the cache
                ResourceLocation identifier = tag.identifier;
                this.cache.saveJsonResource(ResourceType.DATA, object, identifier.getResourceDomain(), "tags/" + directoryName, identifier.getResourcePath());
            }
        }
    }

    private static String getTagDirectoryName(Registries.Registry<?> registry){
        return TAG_DIRECTORIES.get(registry);
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
        private final Set<String> entries = new HashSet<>();
        private final Set<String> optionalEntries = new HashSet<>();
        private final Set<ResourceLocation> references = new HashSet<>();
        private final Set<String> optionalReferences = new HashSet<>();
        private final Set<String> remove = new HashSet<>();
        private final Set<String> optionalRemove = new HashSet<>();
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
            this.entries.add(this.registry.getIdentifier(entry).toString());
            return this;
        }

        /**
         * Adds an entry to this tag.
         * @param entry entry to be added
         */
        public TagBuilder<T> add(ResourceLocation entry){
            if(!this.registry.hasIdentifier(entry))
                throw new RuntimeException("Could not find any object registered under '" + entry + "'!");

            this.entries.add(entry.toString());
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
            this.optionalEntries.add(this.registry.getIdentifier(entry).toString());
            return this;
        }

        /**
         * Adds an optional entry to this tag. The entry can be absent when the tag is loaded without an error being thrown.
         * @param entry entry to be added
         */
        public TagBuilder<T> addOptional(ResourceLocation entry){
            this.optionalEntries.add(entry.toString());
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
         * Adds a reference to the given tag.
         */
        public TagBuilder<T> addReference(ResourceLocation tag){
            if(this.identifier.equals(tag))
                throw new IllegalArgumentException("Cannot add self reference to tag '" + tag + "'!");

            this.references.add(tag);
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

            this.optionalReferences.add(tag.toString());
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
            this.remove.add(this.registry.getIdentifier(entry).toString());
            return this;
        }

        /**
         * Adds an entry to be removed from the tag files lower in the datapack order. Has no effect if {@link #replace(boolean)} is set to {@code true}.
         * @param entry entry to be removed
         */
        public TagBuilder<T> remove(ResourceLocation entry){
            if(!this.registry.hasIdentifier(entry))
                throw new RuntimeException("Could not find any object registered under '" + entry + "'!");

            this.remove.add(entry.toString());
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
            this.optionalRemove.add(entry.toString());
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
    }
}
