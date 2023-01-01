package com.supermartijn642.core.generator;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.registry.RegistryUtil;
import net.fabricmc.fabric.impl.resource.loader.FabricModResourcePack;
import net.fabricmc.fabric.impl.resource.loader.GroupResourcePack;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created 16/08/2022 by SuperMartijn642
 */
public abstract class ResourceCache {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Checks whether a resource exists. The resource may be either a generated file, or a file from a loaded resource pack.
     * @param resourceType whether the resource is part of the server data or the client assets
     * @param namespace    the namespace of the resource
     * @param directory    name of the directory within the namespace
     * @param fileName     name of the file
     * @param extension    the file's extension
     */
    public abstract boolean doesResourceExist(ResourceType resourceType, String namespace, String directory, String fileName, String extension);

    /**
     * Tracks the given location as if a file has been saved there.
     * Specifically, this means {@link #doesResourceExist(ResourceType, String, String, String, String)} will return {@code true} for the given location.
     * A resource should later be saved for the given location.
     * @param resourceType whether the resource is part of the server data or the client assets
     * @param namespace    the namespace of the resource
     * @param directory    name of the directory within the namespace
     * @param fileName     name of the file
     * @param extension    the file's extension
     */
    public abstract void trackToBeGeneratedResource(ResourceType resourceType, String namespace, String directory, String fileName, String extension);

    /**
     * Saves the given data in the appropriate location. Also checks if a file is already present to avoid redundant writes.
     * @param resourceType whether the given data is part of the server data or the client assets
     * @param namespace    the namespace which the data should be saved under
     * @param directory    name of the directory within the namespace
     * @param fileName     name of the file
     * @param extension    extension of the file
     */
    public abstract void saveResource(ResourceType resourceType, byte[] data, String namespace, String directory, String fileName, String extension);

    /**
     * Saves the given data in the appropriate location. Also checks if a file is already present to avoid redundant writes.
     * @param resourceType whether the given data is part of the server data or the client assets
     * @param json         the data to be saved
     * @param namespace    the namespace which the data should be saved under
     * @param directory    name of the directory within the namespace
     * @param fileName     name of the file
     */
    public void saveJsonResource(ResourceType resourceType, JsonObject json, String namespace, String directory, String fileName){
        byte[] bytes = GSON.toJson(json).getBytes(StandardCharsets.UTF_8);
        this.saveResource(resourceType, bytes, namespace, directory, fileName, fileName.endsWith(".json") ? "" : ".json");
    }

    /**
     * Opens an input stream for the requested resource.
     * @param resourceType whether the resource is part of the server data or the client assets
     * @param namespace    the namespace of the resource
     * @param directory    name of the directory within the namespace
     * @param fileName     name of the file
     * @param extension    the file's extension
     * @return an input stream for the requested resource, or an empty optional if the resource does not exist
     */
    public abstract Optional<InputStream> getManualResource(ResourceType resourceType, String namespace, String directory, String fileName, String extension);

    @ApiStatus.Internal
    public static ResourceCache wrap(HashCache cachedOutput, Path outputDirectory, Path manualDirectory){
        return new HashCacheWrapper(outputDirectory, manualDirectory, cachedOutput);
    }

    private static class HashCacheWrapper extends ResourceCache {

        private static final Function<GroupResourcePack,List<PackResources>> groupResourcePackPacks;

        static{
            try{
                Field field = GroupResourcePack.class.getDeclaredField("packs");
                field.setAccessible(true);
                groupResourcePackPacks = pack -> {
                    try{
                        //noinspection unchecked
                        return (List<PackResources>)field.get(pack);
                    }catch(IllegalAccessException e){
                        throw new RuntimeException(e);
                    }
                };
            }catch(NoSuchFieldException e){
                throw new RuntimeException(e);
            }
        }

        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
        private final Map<Path,HashCode> presentFiles = new HashMap<>();
        private final Map<Path,HashCode> writtenFiles = new HashMap<>();
        private final List<Path> toBeGenerated = new ArrayList<>(); // TODO validate this is empty after datagen

        private final Path outputDirectory;
        private final Path manualDirectory;
        private final HashCache cache;
        private final List<PackResources> otherResourcePacks;

        HashCacheWrapper(Path outputFolder, Path manualFolder, HashCache hashCache){
            if(outputFolder == null)
                throw new IllegalArgumentException("Output directory must not be null!");
            this.outputDirectory = outputFolder;
            this.manualDirectory = manualFolder;
            this.cache = hashCache;
            // Copy all the paths from the hash cache
            for(Map.Entry<Path,String> entry : this.cache.oldCache.entrySet())
                this.presentFiles.put(this.outputDirectory.relativize(entry.getKey()), entry.getValue().isEmpty() ? HashCode.fromInt(0) : HashCode.fromString(entry.getValue()));

            // Take all loaded resource packs and remove the one for the datagen modid
            this.otherResourcePacks = new ArrayList<>(ClientUtils.getMinecraft().resourcePackRepository.openAllSelected());
            String modFilter = System.getProperty("fabric-api.datagen.modid");
            if(RegistryUtil.isValidNamespace(modFilter)){
                ModContainer container = FabricLoader.getInstance().getModContainer(modFilter).orElse(null);
                if(container == null)
                    throw new RuntimeException("Property 'fabric-api.datagen.modid' is set to unknown modid '" + modFilter + "'!");
                // Map the grouped fabric resource pack to all the individual mods' resource packs
                this.otherResourcePacks.addAll(
                    this.otherResourcePacks.stream()
                        .filter(pack -> pack instanceof FabricModResourcePack)
                        .map(FabricModResourcePack.class::cast)
                        .map(groupResourcePackPacks)
                        .flatMap(List::stream)
                        .collect(Collectors.toList())
                );
                this.otherResourcePacks.removeIf(pack -> pack instanceof FabricModResourcePack);
                // Remove the resource pack for the datagen mod
                this.otherResourcePacks.removeAll(
                    this.otherResourcePacks.stream()
                        .filter(pack -> pack instanceof ModNioResourcePack)
                        .map(ModNioResourcePack.class::cast)
                        .filter(pack -> pack.getFabricModMetadata() == container.getMetadata())
                        .collect(Collectors.toList())
                );
            }else
                CoreLib.LOGGER.warn("The 'fabric-api.datagen.modid' property has not been set! The resource cache may wrongly identify previously generated files as existing files!");
        }

        private boolean existsInGeneratedFiles(Path path){
            return this.toBeGenerated.contains(path) || this.cache.newCache.containsKey(this.outputDirectory.resolve(path));
        }

        private boolean existsInManualFiles(Path path){
            return this.manualDirectory != null && Files.exists(this.manualDirectory.resolve(path));
        }

        private boolean existsInLoadedResources(ResourceType resourceType, String namespace, String directory, String fileName, String extension){
            ResourceLocation location = new ResourceLocation(namespace, directory + "/" + fileName + extension);
            return this.otherResourcePacks.stream().anyMatch(pack -> pack.hasResource(resourceType == ResourceType.ASSET ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA, location));
        }

        private Path constructPath(ResourceType resourceType, String namespace, String directory, String fileName, String extension){
            return Paths.get(resourceType.getDirectoryName(), namespace, directory, fileName + extension);
        }

        public boolean doesResourceExist(ResourceType resourceType, String namespace, String directory, String fileName, String extension){
            Path path = this.constructPath(resourceType, namespace, directory, fileName, extension);
            return this.existsInGeneratedFiles(path)
                || this.existsInManualFiles(path)
                || this.existsInLoadedResources(resourceType, namespace, directory, fileName, extension);
        }

        @Override
        public void trackToBeGeneratedResource(ResourceType resourceType, String namespace, String directory, String fileName, String extension){
            this.toBeGenerated.add(this.constructPath(resourceType, namespace, directory, fileName, extension));
        }

        @Override
        public Optional<InputStream> getManualResource(ResourceType resourceType, String namespace, String directory, String fileName, String extension){
            Path path = this.constructPath(resourceType, namespace, directory, fileName, extension);
            Path fullPath = this.outputDirectory.resolve(path);
            if(!Files.exists(fullPath))
                return Optional.empty();
            try{
                return Optional.of(Files.newInputStream(fullPath));
            }catch(IOException e){
                throw new RuntimeException(e);
            }
        }

        public void saveResource(ResourceType resourceType, byte[] data, String namespace, String directory, String fileName, String extension){
            Path path = this.constructPath(resourceType, namespace, directory, fileName, extension);
            Path fullPath = this.outputDirectory.resolve(path);
            if(this.writtenFiles.containsKey(path) || this.cache.newCache.containsKey(fullPath))
                throw new RuntimeException("Duplicate file '" + path + "'!");
            if(this.existsInManualFiles(path))
                throw new RuntimeException("File '" + path + "' clashes with a manually created file!");

            // Skip writing if the present file matches the one to be written
            HashCode hashCode = Hashing.sha1().hashBytes(data);
            if(this.presentFiles.containsKey(path) && this.presentFiles.get(path).equals(hashCode) && fullPath.toFile().exists()){
                this.writtenFiles.put(path, hashCode);
                this.toBeGenerated.remove(path);
                this.cache.putNew(fullPath, hashCode.toString());
                return;
            }

            // Write the data to file
            fullPath.toFile().getParentFile().mkdirs();
            try(OutputStream outputStream = Files.newOutputStream(fullPath)){
                outputStream.write(data);
            }catch(IOException e){
                throw new RuntimeException(e);
            }
            this.writtenFiles.put(path, hashCode);
            this.toBeGenerated.remove(path);
            this.cache.putNew(fullPath, hashCode.toString());
        }
    }
}
