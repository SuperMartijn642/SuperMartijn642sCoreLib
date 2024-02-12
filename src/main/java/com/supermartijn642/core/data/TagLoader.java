package com.supermartijn642.core.data;

import com.google.gson.*;
import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.data.tag.CustomTagEntries;
import com.supermartijn642.core.data.tag.CustomTagEntry;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created 05/08/2022 by SuperMartijn642
 */
public class TagLoader {

    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static final Map<String,Registries.Registry<?>> TAG_TYPES = new HashMap<>();
    private static final Map<Registries.Registry<?>,Map<ResourceLocation,Set<ResourceLocation>>> TAGS = new HashMap<>();

    static{
        TAG_TYPES.put("blocks", Registries.BLOCKS);
        TAG_TYPES.put("items", Registries.ITEMS);
    }

    public static void loadTags(){
        for(Registries.Registry<?> registry : TAG_TYPES.values())
            TAGS.put(registry, new HashMap<>());
        Loader.instance().getActiveModList().forEach(TagLoader::loadTags);
        CoreLib.LOGGER.info("Loaded '" + TAGS.get(Registries.BLOCKS).keySet().size() + "' block tags");
        CoreLib.LOGGER.info("Loaded '" + TAGS.get(Registries.ITEMS).keySet().size() + "' item tags");
    }

    private static void loadTags(ModContainer mod){
        File source = mod.getSource();

        // Special case to ignore Minecraft itself
        if("minecraft".equals(mod.getModId()))
            return;

        FileSystem fs = null;
        try{
            // Find the root path
            Path root = null;

            if(source.isFile()){
                try{
                    fs = FileSystems.newFileSystem(source.toPath(), null);
                    root = fs.getPath("/data");
                }catch(IOException e){
                    CoreLib.LOGGER.error("Error loading FileSystem from jar!", e);
                    return;
                }
            }else if(source.isDirectory()){
                root = source.toPath().resolve("data");
            }

            // Return if the folder does not exist
            if(root == null || !Files.exists(root))
                return;

            // Find all files inside the data folder
            List<Path> namespaceFolders;
            try(Stream<Path> stream = Files.walk(root, 1)){
                namespaceFolders = stream.filter(Predicate.isEqual(root).negate()).collect(Collectors.toList());
            }

            // Go over the different registry types
            for(Map.Entry<String,Registries.Registry<?>> tagType : TAG_TYPES.entrySet()){
                // Keep track of the entries per tag
                Map<ResourceLocation,List<CustomTagEntry>> entries = new HashMap<>();
                Map<ResourceLocation,List<CustomTagEntry>> removeEntries = new HashMap<>();

                // First, go through all folder and read all tags
                for(Path namespaceFolder : namespaceFolders){
                    if(!Files.isDirectory(namespaceFolder))
                        continue;

                    String fileName = namespaceFolder.getFileName().toString();
                    String namespace = fileName.endsWith("/") ? fileName.substring(0, fileName.length() - 1) : fileName;
                    if(!RegistryUtil.isValidNamespace(namespace))
                        continue;

                    // Find the 'tags' folder
                    Path tagsFolder = namespaceFolder.resolve("tags");
                    if(!Files.exists(tagsFolder) || !Files.isDirectory(tagsFolder))
                        continue;

                    Path tagTypeFolder = tagsFolder.resolve(tagType.getKey());
                    if(!Files.exists(tagTypeFolder) || !Files.isDirectory(tagTypeFolder))
                        continue;

                    // Now walk through all files in the folder to find jsons
                    try(Stream<Path> paths = Files.walk(tagTypeFolder)){
                        paths.forEach(
                            path -> {
                                if(Files.isDirectory(path) || !path.getFileName().toString().endsWith(".json"))
                                    return;

                                String identifier = tagTypeFolder.relativize(path).toString();
                                identifier = identifier.substring(0, identifier.length() - ".json".length());
                                identifier = identifier.replace('\\', '/');
                                if(!RegistryUtil.isValidPath(identifier)){
                                    CoreLib.LOGGER.warn("Tag filename '" + namespace + ":" + tagType.getKey() + "/" + identifier + "' from mod '" + mod.getName() + "' contains invalid characters!");
                                    return;
                                }

                                ResourceLocation fullIdentifier = new ResourceLocation(namespace, identifier);
                                readTagFile(mod, fullIdentifier, path, tagType.getKey(), tagType.getValue(), entries.computeIfAbsent(fullIdentifier, f -> new ArrayList<>()), removeEntries.computeIfAbsent(fullIdentifier, f -> new ArrayList<>()));
                            }
                        );
                    }
                }

                // Finally, resolve the tags
                Stack<ResourceLocation> dependencyStack = new Stack<>();
                //noinspection unchecked
                Registries.Registry<Object> registry = (Registries.Registry<Object>)tagType.getValue();
                CustomTagEntry.TagEntryResolutionContext<Object> entryResolutionContext = new CustomTagEntry.TagEntryResolutionContext<Object>() {
                    @Override
                    public Object getElement(ResourceLocation identifier){
                        return registry.getValue(identifier);
                    }

                    @Override
                    public Collection<Object> getTag(ResourceLocation identifier){
                        return TAGS.get(registry).get(identifier).stream().map(registry::getValue).collect(Collectors.toList());
                    }

                    @Override
                    public Collection<Object> getAllElements(){
                        return registry.getValues();
                    }

                    @Override
                    public Set<ResourceLocation> getAllIdentifiers(){
                        return registry.getIdentifiers();
                    }
                };
                while(!entries.isEmpty()){
                    ResourceLocation tag = entries.keySet().stream().findAny().get();
                    try{
                        resolve(tag, entries, removeEntries, dependencyStack, registry, entryResolutionContext, mod);
                    }catch(JsonParseException e){
                        CoreLib.LOGGER.error(e);
                    }
                }
            }
        }catch(IOException e){
            throw new RuntimeException("Encountered an exception whilst loading tags for mod '" + mod.getName() + "'!", e);
        }finally{
            IOUtils.closeQuietly(fs);
        }
    }

    private static void readTagFile(ModContainer mod, ResourceLocation identifier, Path file, String registryName, Registries.Registry<?> registry, Collection<CustomTagEntry> entries, Collection<CustomTagEntry> removeEntries){
        // Read the file contents as a json object
        JsonObject json;
        try(Reader reader = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8)){
            json = GSON.fromJson(reader, JsonObject.class);
        }catch(IOException e){
            throw new RuntimeException(e);
        }catch(JsonSyntaxException e){
            CoreLib.LOGGER.error("Malformed tag json '" + identifier.getResourceDomain() + ":" + registryName + "/" + identifier.getResourcePath() + ".json' in mod '" + mod.getName() + "'!", e);
            return;
        }

        // Add the tag if not present, do this here to prevent other tags referencing this from throwing an error
        Set<ResourceLocation> tagEntries = TAGS.get(registry).computeIfAbsent(identifier, i -> new HashSet<>());

        try{
            // Check for replace flag
            if(json.has("replace") && (!json.get("replace").isJsonPrimitive() || !json.get("replace").getAsJsonPrimitive().isBoolean()))
                throw new RuntimeException("'replace' must be a boolean!");
            boolean replace = json.has("replace") && json.get("replace").getAsBoolean();
            if(replace)
                tagEntries.clear();

            // Read the 'values' array
            if(json.has("values")){
                if(!json.get("values").isJsonArray())
                    throw new RuntimeException("'values' must be an array!");

                // Loop over the entries in 'values'
                json.get("values").getAsJsonArray().forEach(
                    element -> entries.add(CustomTagEntries.deserialize(element))
                );
            }

            // Read the 'remove' array
            if(json.has("remove")){
                if(!json.get("remove").isJsonArray())
                    throw new RuntimeException("'remove' must be an array!");

                // Loop over the entries in 'remove'
                json.get("remove").getAsJsonArray().forEach(
                    element -> removeEntries.add(CustomTagEntries.deserialize(element))
                );
            }
        }catch(Exception e){
            CoreLib.LOGGER.error("Encountered exception in tag json '" + identifier.getResourceDomain() + ":" + registryName + "/" + identifier.getResourcePath() + ".json' in mod '" + mod.getName() + "'!", e);
            tagEntries.clear();
            entries.clear();
            removeEntries.clear();
        }
    }

    private static <T> void resolve(ResourceLocation tagIdentifier, Map<ResourceLocation,List<CustomTagEntry>> entries, Map<ResourceLocation,List<CustomTagEntry>> removeEntries, Stack<ResourceLocation> dependencyStack, Registries.Registry<T> registry, CustomTagEntry.TagEntryResolutionContext<T> entryResolutionContext, ModContainer mod){
        // Check for circular dependencies
        if(dependencyStack.contains(tagIdentifier)){
            TAGS.get(registry).get(tagIdentifier).clear();
            entries.remove(tagIdentifier);
            removeEntries.remove(tagIdentifier);
            throw new JsonParseException("Mod " + mod.getName() + " has contains a circular tag dependency: " + dependencyStack.stream().map(ResourceLocation::toString).map(s -> "'" + s + "'").collect(Collectors.joining(" -> ")));
        }
        dependencyStack.push(tagIdentifier);

        // Resolve dependencies first
        try{
            for(Map.Entry<ResourceLocation,List<CustomTagEntry>> tag : entries.entrySet()){
                for(CustomTagEntry entry : tag.getValue()){
                    Set<ResourceLocation> dependencies = new HashSet<>(entry.getTagDependencies());
                    for(ResourceLocation dependency : dependencies){
                        if(entries.containsKey(dependency))
                            resolve(dependency, entries, removeEntries, dependencyStack, registry, entryResolutionContext, mod);
                    }
                }
            }
        }catch(Exception e){
            TAGS.get(registry).get(tagIdentifier).clear();
            entries.remove(tagIdentifier);
            removeEntries.remove(tagIdentifier);
            dependencyStack.pop();
            throw e;
        }

        try{
            // Add elements
            for(CustomTagEntry entry : entries.get(tagIdentifier)){
                Collection<T> elements = entry.resolve(entryResolutionContext);
                if(elements != null)
                    TAGS.get(registry).get(tagIdentifier).addAll(elements.stream().map(registry::getIdentifier).collect(Collectors.toList()));
            }

            // Remove elements
            for(CustomTagEntry entry : removeEntries.get(tagIdentifier)){
                Collection<T> elements = entry.resolve(entryResolutionContext);
                if(elements != null)
                    TAGS.get(registry).get(tagIdentifier).removeAll(elements.stream().map(registry::getIdentifier).collect(Collectors.toList()));
            }
        }catch(Exception e){
            CoreLib.LOGGER.error("Encountered exception in tag json '" + tagIdentifier.getResourceDomain() + ":" + registry.getRegistryIdentifier().getResourcePath() + "/" + tagIdentifier.getResourcePath() + ".json' in mod '" + mod.getName() + "'!", e);
            TAGS.get(registry).get(tagIdentifier).clear();
        }

        entries.remove(tagIdentifier);
        removeEntries.remove(tagIdentifier);
        dependencyStack.pop();
    }

    public static Set<ResourceLocation> getTag(Registries.Registry<?> registry, ResourceLocation identifier){
        return TAGS.containsKey(registry) ? TAGS.get(registry).get(identifier) : null;
    }
}
