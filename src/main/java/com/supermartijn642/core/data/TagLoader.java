package com.supermartijn642.core.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.supermartijn642.core.CoreLib;
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
        Loader.instance().getActiveModList().forEach(TagLoader::loadTags);
        CoreLib.LOGGER.info("Loaded '" + TAGS.getOrDefault(Registries.BLOCKS, Collections.emptyMap()).keySet().size() + "' block tags");
        CoreLib.LOGGER.info("Loaded '" + TAGS.getOrDefault(Registries.ITEMS, Collections.emptyMap()).keySet().size() + "' item tags");
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
                    fs = FileSystems.newFileSystem(source.toPath(), (ClassLoader)null);
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

            // Now go through all namespaces
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

                // Go over the different registry types
                for(Map.Entry<String,Registries.Registry<?>> tagType : TAG_TYPES.entrySet()){
                    Path tagTypeFolder = tagsFolder.resolve(tagType.getKey());
                    if(!Files.exists(tagTypeFolder) || !Files.isDirectory(tagTypeFolder))
                        continue;

                    // Keep track of references to other tags
                    Map<ResourceLocation,Set<ResourceLocation>> references = new HashMap<>();
                    Map<ResourceLocation,Set<ResourceLocation>> optionalReferences = new HashMap<>();
                    Map<ResourceLocation,Set<ResourceLocation>> removeEntries = new HashMap<>();

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
                                readTagFile(mod, fullIdentifier, path, tagType.getKey(), tagType.getValue(), references.computeIfAbsent(fullIdentifier, f -> new HashSet<>()), optionalReferences.computeIfAbsent(fullIdentifier, f -> new HashSet<>()), removeEntries.computeIfAbsent(fullIdentifier, f -> new HashSet<>()));
                            }
                        );
                    }

                    // Apply references TODO maybe move this to after all mods are done, depending on behaviour in 1.14+
                    loop:
                    for(Map.Entry<ResourceLocation,Set<ResourceLocation>> entry : references.entrySet()){
                        for(ResourceLocation reference : entry.getValue()){
                            if(!TAGS.get(tagType.getValue()).containsKey(reference)){
                                CoreLib.LOGGER.warn("Tag file '" + reference.getResourceDomain() + ":" + tagType.getKey() + "/" + reference.getResourcePath() + ".json' from mod '" + mod.getName() + "' references unknown tag '" + reference + "'!");
                                TAGS.get(tagType.getValue()).get(entry.getKey()).clear();
                                continue loop;
                            }

                            TAGS.get(tagType.getValue()).get(entry.getKey()).addAll(TAGS.get(tagType.getValue()).get(reference));
                        }
                    }
                    for(Map.Entry<ResourceLocation,Set<ResourceLocation>> entry : optionalReferences.entrySet()){
                        for(ResourceLocation reference : entry.getValue()){
                            if(TAGS.get(tagType.getValue()).containsKey(reference))
                                TAGS.get(tagType.getValue()).get(entry.getKey()).addAll(TAGS.get(tagType.getValue()).get(reference));
                        }
                    }

                    // Apply remove entries
                    for(Map.Entry<ResourceLocation,Set<ResourceLocation>> entry : removeEntries.entrySet())
                        TAGS.get(tagType.getValue()).get(entry.getKey()).removeAll(entry.getValue());
                }
            }
        }catch(IOException e){
            throw new RuntimeException("Encountered an exception whilst loading tags for mod '" + mod.getName() + "'!", e);
        }finally{
            IOUtils.closeQuietly(fs);
        }
    }

    private static void readTagFile(ModContainer mod, ResourceLocation identifier, Path file, String registryName, Registries.Registry<?> registry, Set<ResourceLocation> references, Set<ResourceLocation> optionalReferences, Set<ResourceLocation> removeEntries){
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
        Set<ResourceLocation> tagEntries = TAGS.computeIfAbsent(registry, r -> new HashMap<>()).computeIfAbsent(identifier, i -> new HashSet<>());

        try{
            // Check for replace flag
            if(json.has("required") && (!json.get("required").isJsonPrimitive() || !json.get("required").getAsJsonPrimitive().isBoolean()))
                throw new RuntimeException("'replace' must be a boolean!");
            boolean replace = json.has("replace") && json.get("replace").getAsBoolean();
            if(replace)
                tagEntries.clear();

            // Read the 'values' array
            List<String> entries = new ArrayList<>();
            List<String> optionalEntries = new ArrayList<>();
            if(json.has("values")){
                if(!json.get("values").isJsonArray())
                    throw new RuntimeException("'values' must be an array!");

                // Loop over the entries in 'values'
                json.get("values").getAsJsonArray().forEach(
                    element -> {
                        if(element.isJsonObject()){
                            JsonObject object = element.getAsJsonObject();
                            if(!object.has("id") || !object.get("id").isJsonPrimitive() || !object.get("id").getAsJsonPrimitive().isString())
                                throw new RuntimeException("Entries in 'values' must contain key 'id'!");
                            if(object.has("required") && (!object.get("required").isJsonPrimitive() || !object.get("required").getAsJsonPrimitive().isBoolean()))
                                throw new RuntimeException("Key 'required' for entries in 'values' must be a boolean!");

                            if(!object.has("required") || object.get("required").getAsBoolean())
                                entries.add(element.getAsJsonObject().get("id").getAsString());
                            else
                                optionalEntries.add(element.getAsJsonObject().get("id").getAsString());
                        }else if(element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())
                            entries.add(element.getAsString());
                        else
                            throw new RuntimeException("'values' must only contain objects and strings!");
                    }
                );
            }

            // Move values starting with '#' into references
            List<String> rawReferences = new ArrayList<>();
            entries.stream().filter(s -> s.charAt(0) == '#').map(s -> s.substring(1)).forEach(rawReferences::add);
            entries.removeIf(s -> s.charAt(0) == '#');
            List<String> rawOptionalReferences = new ArrayList<>();
            optionalEntries.stream().filter(s -> s.charAt(0) == '#').map(s -> s.substring(1)).forEach(rawOptionalReferences::add);
            optionalEntries.removeIf(s -> s.charAt(0) == '#');

            // Read the 'remove' array
            List<String> remove = new ArrayList<>();
            List<String> optionalRemove = new ArrayList<>();
            if(json.has("remove")){
                if(!json.get("remove").isJsonArray())
                    throw new RuntimeException("'remove' must be an array!");

                // Loop over the entries in 'remove'
                json.get("remove").getAsJsonArray().forEach(
                    element -> {
                        if(element.isJsonObject()){
                            JsonObject object = element.getAsJsonObject();
                            if(!object.has("id") || !object.get("id").isJsonPrimitive() || !object.get("id").getAsJsonPrimitive().isString())
                                throw new RuntimeException("Entries in 'remove' must contain key 'id'!");
                            if(object.has("required") && (!object.get("required").isJsonPrimitive() || !object.get("required").getAsJsonPrimitive().isBoolean()))
                                throw new RuntimeException("Key 'required' for entries in 'remove' must be a boolean!");

                            if(!object.has("required") || object.get("required").getAsBoolean())
                                remove.add(element.getAsJsonObject().get("id").getAsString());
                            else
                                optionalRemove.add(element.getAsJsonObject().get("id").getAsString());
                        }else if(element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())
                            remove.add(element.getAsString());
                        else
                            throw new RuntimeException("'remove' must only contain objects and strings!");
                    }
                );
            }

            // Validate entries
            for(String entry : entries){
                if(!RegistryUtil.isValidIdentifier(entry))
                    throw new RuntimeException("'values' entry '" + entry + "' is not a valid identifier!");

                ResourceLocation entryIdentifier = new ResourceLocation(entry);
                if(!registry.hasIdentifier(entryIdentifier))
                    throw new RuntimeException("Could not find a registered object for 'values' entry '" + entryIdentifier + "'!");

                tagEntries.add(entryIdentifier);
            }

            // Add optional entries
            for(String entry : optionalEntries){
                if(!RegistryUtil.isValidIdentifier(entry))
                    throw new RuntimeException("'values' optional entry '" + entry + "' is not a valid identifier!");

                ResourceLocation entryIdentifier = new ResourceLocation(entry);
                if(registry.hasIdentifier(entryIdentifier))
                    tagEntries.add(entryIdentifier);
            }

            // Add references
            for(String reference : rawReferences){
                if(!RegistryUtil.isValidIdentifier(reference))
                    throw new RuntimeException("'values' reference '#" + reference + "' is not a valid identifier!");

                references.add(new ResourceLocation(reference));
            }

            // Add optional references
            for(String reference : rawOptionalReferences){
                if(!RegistryUtil.isValidIdentifier(reference))
                    throw new RuntimeException("'values' reference '#" + reference + "' is not a valid identifier!");

                optionalReferences.add(new ResourceLocation(reference));
            }

            // Validate remove entries
            for(String entry : remove){
                if(!RegistryUtil.isValidIdentifier(entry))
                    throw new RuntimeException("'remove' entry '" + entry + "' is not a valid identifier!");

                ResourceLocation entryIdentifier = new ResourceLocation(entry);
                if(!registry.hasIdentifier(entryIdentifier))
                    throw new RuntimeException("Could not find a registered object for 'remove' entry '" + entryIdentifier + "'!");

                removeEntries.add(entryIdentifier);
            }

            // Add optional remove entries
            for(String entry : optionalRemove){
                if(!RegistryUtil.isValidIdentifier(entry))
                    throw new RuntimeException("'remove' optional entry '" + entry + "' is not a valid identifier!");

                ResourceLocation entryIdentifier = new ResourceLocation(entry);
                if(registry.hasIdentifier(entryIdentifier))
                    removeEntries.add(entryIdentifier);
            }
        }catch(Exception e){
            CoreLib.LOGGER.error("Encountered exception in tag json '" + identifier.getResourceDomain() + ":" + registryName + "/" + identifier.getResourcePath() + ".json' in mod '" + mod.getName() + "'!", e);
            tagEntries.clear();
            references.clear();
            optionalReferences.clear();
        }
    }

    public static Set<ResourceLocation> getTag(Registries.Registry<?> registry, ResourceLocation identifier){
        return TAGS.containsKey(registry) ? TAGS.get(registry).get(identifier) : null;
    }
}
