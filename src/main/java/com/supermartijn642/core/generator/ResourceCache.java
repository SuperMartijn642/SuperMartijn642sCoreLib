package com.supermartijn642.core.generator;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.generator.aggregator.ResourceAggregator;
import com.supermartijn642.core.registry.RegistryUtil;
import com.supermartijn642.core.util.Pair;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.ApiStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
     * @param aggregator   aggregator used when multiple generator write to the same file location
     * @param namespace    the namespace which the data should be saved under
     * @param directory    name of the directory within the namespace
     * @param fileName     name of the file
     * @param extension    extension of the file
     */
    public abstract <T> void saveResource(ResourceType resourceType, ResourceAggregator<?,T> aggregator, T data, String namespace, String directory, String fileName, String extension);

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
    public abstract Optional<InputStream> getExistingResource(ResourceType resourceType, String namespace, String directory, String fileName, String extension);

    @ApiStatus.Internal
    public static ResourceCache wrap(HashCache cachedOutput, Path outputDirectory, Path manualDirectory){
        return new HashCacheWrapper(outputDirectory, manualDirectory, cachedOutput);
    }

    @ApiStatus.Internal
    public static class HashCacheWrapper extends ResourceCache {

        private final Map<Path,HashCode> presentFiles = new HashMap<>();
        private final Map<Path,HashCode> writtenFiles = new HashMap<>();
        private final Map<Path,Pair<ResourceAggregator<Object,Object>,Object>> aggregatedResources = new HashMap<>();
        private final Set<Path> toBeGenerated = new HashSet<>();

        private final Path outputDirectory;
        private final Path manualDirectory;
        private final HashCache cache;
        private final List<PackResources> otherResourcePacks;
        private boolean allowWrites = true;
        private int writes = 0;

        HashCacheWrapper(Path outputFolder, Path manualFolder, HashCache hashCache){
            if(outputFolder == null)
                throw new IllegalArgumentException("Output directory must not be null!");
            this.outputDirectory = outputFolder;
            this.manualDirectory = manualFolder;
            this.cache = hashCache;

            // Take all loaded resource packs and remove the one for the datagen modid
            this.otherResourcePacks = new ArrayList<>(ClientUtils.getMinecraft().resourcePackRepository.openAllSelected());
            String modFilter = System.getProperty("fabric-api.datagen.modid");
            if(RegistryUtil.isValidNamespace(modFilter)){
                ModContainer container = FabricLoader.getInstance().getModContainer(modFilter).orElse(null);
                if(container == null)
                    throw new RuntimeException("Property 'fabric-api.datagen.modid' is set to unknown modid '" + modFilter + "'!");
                // Remove the resource pack for the datagen mod
                this.otherResourcePacks.removeIf(pack ->
                    pack instanceof ModNioResourcePack && ((ModNioResourcePack)pack).getFabricModMetadata() == container.getMetadata()
                );
            }else
                CoreLib.LOGGER.warn("The 'fabric-api.datagen.modid' property has not been set! The resource cache may wrongly identify previously generated files as existing files!");
        }

        public void readHashCache(){
            // Add special cache
            Path cachePath = this.cache.getProviderCachePath("Core Lib Generators");
            this.cache.cachePaths.add(cachePath);
            HashCache.ProviderCache providerCache = HashCache.readCache(this.outputDirectory, cachePath);
            this.cache.caches.put("Core Lib Generators", providerCache);
            this.cache.initialCount += providerCache.count();
            // Copy all the paths from the hash cache
            this.cache.caches.values().stream()
                .flatMap(cache -> cache.data().entrySet().stream())
                .forEach(entry -> this.presentFiles.put(this.outputDirectory.relativize(entry.getKey()), entry.getValue()));
        }

        private boolean existsInGeneratedFiles(Path path){
            return this.toBeGenerated.contains(path) || this.aggregatedResources.containsKey(path) || this.writtenFiles.containsKey(path)
                || this.cache.caches.entrySet().stream()
                .filter(entry -> this.cache.cachesToWrite.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(cache -> cache.data().entrySet().stream())
                .map(Map.Entry::getKey)
                .anyMatch(this.outputDirectory.resolve(path)::equals);
        }

        private boolean existsInManualFiles(Path path){
            return this.manualDirectory != null && Files.exists(this.manualDirectory.resolve(path));
        }

        private boolean existsInLoadedResources(ResourceType resourceType, String namespace, String directory, String fileName, String extension){
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(namespace, directory + "/" + fileName + extension);
            return this.otherResourcePacks.stream().anyMatch(pack -> pack.getResource(resourceType == ResourceType.ASSET ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA, location) != null);
        }

        private Path constructPath(ResourceType resourceType, String namespace, String directory, String fileName, String extension){
            return Paths.get(resourceType.getDirectoryName(), namespace, directory, fileName + extension);
        }

        @Override
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
        public Optional<InputStream> getExistingResource(ResourceType resourceType, String namespace, String directory, String fileName, String extension){
            Path path = this.constructPath(resourceType, namespace, directory, fileName, extension);
            Path fullPath = this.manualDirectory.resolve(path);
            if(!Files.exists(fullPath))
                return Optional.empty();
            try{
                return Optional.of(Files.newInputStream(fullPath));
            }catch(IOException e){
                throw new RuntimeException(e);
            }
        }

        public void saveResource(ResourceType resourceType, byte[] data, String namespace, String directory, String fileName, String extension){
            if(!this.allowWrites)
                throw new RuntimeException("Resources cannot be saved during this stage!");

            Path path = this.constructPath(resourceType, namespace, directory, fileName, extension);
            Path fullPath = this.outputDirectory.resolve(path);
            if(this.writtenFiles.containsKey(path) || this.aggregatedResources.containsKey(path)
                || this.cache.caches.entrySet().stream()
                .filter(entry -> this.cache.cachesToWrite.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(cache -> cache.data().entrySet().stream())
                .map(Map.Entry::getKey)
                .anyMatch(fullPath::equals))
                throw new RuntimeException("Duplicate file '" + path + "'!");
            if(this.existsInManualFiles(path))
                throw new RuntimeException("File '" + path + "' clashes with a manually created file!");

            // Skip writing if the present file matches the one to be written
            HashCode hashCode = Hashing.sha1().hashBytes(data);
            if(this.presentFiles.containsKey(path) && this.presentFiles.get(path).equals(hashCode) && fullPath.toFile().exists()){
                this.writtenFiles.put(path, hashCode);
                this.toBeGenerated.remove(path);
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
            this.writes++;
        }

        @Override
        public <T> void saveResource(ResourceType resourceType, ResourceAggregator<?,T> aggregator, T data, String namespace, String directory, String fileName, String extension){
            if(!this.allowWrites)
                throw new RuntimeException("Resources cannot be saved during this stage!");

            Path path = this.constructPath(resourceType, namespace, directory, fileName, extension);
            Path fullPath = this.outputDirectory.resolve(path);
            if(this.writtenFiles.containsKey(path)
                || this.cache.caches.entrySet().stream()
                .filter(entry -> this.cache.cachesToWrite.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(cache -> cache.data().entrySet().stream())
                .map(Map.Entry::getKey)
                .anyMatch(fullPath::equals))
                throw new RuntimeException("Duplicate file '" + path + "'!");
            if(this.existsInManualFiles(path))
                throw new RuntimeException("File '" + path + "' clashes with a manually created file!");

            // Validate the aggregators match
            Pair<ResourceAggregator<Object,Object>,Object> oldEntry = this.aggregatedResources.get(path);
            if(oldEntry != null && oldEntry.left() != aggregator)
                throw new RuntimeException("Incompatible aggregators for file '" + path + "': '" + oldEntry.left().getClass() + "' and '" + aggregator.getClass() + "'!");

            // Combine the old with the new data
            Object oldData = oldEntry == null ? aggregator.initialData() : oldEntry.right();
            try{
                //noinspection unchecked
                oldData = ((ResourceAggregator<Object,Object>)aggregator).combine(oldData, data);
            }catch(Exception e){
                throw new RuntimeException("Failed to combine data for file '" + path + "'!", e);
            }
            //noinspection unchecked
            this.aggregatedResources.put(path, Pair.of((ResourceAggregator<Object,Object>)aggregator, oldData));
        }

        public void allowWrites(boolean allow){
            this.allowWrites = allow;
        }

        public void finish(){
            // Write all aggregated resources
            this.aggregatedResources.forEach((path, pair) -> {
                // Convert the data to bytes
                ResourceAggregator<Object,Object> aggregator = pair.left();
                Object data = pair.right();
                byte[] bytes;
                try(ByteArrayOutputStream stream = new ByteArrayOutputStream()){
                    aggregator.write(stream, data);
                    bytes = stream.toByteArray();
                }catch(Exception e){
                    throw new RuntimeException(e);
                }

                // Skip writing if the present file matches the one to be written
                Path fullPath = this.outputDirectory.resolve(path);
                HashCode hashCode = Hashing.sha1().hashBytes(bytes);
                if(this.presentFiles.containsKey(path) && this.presentFiles.get(path).equals(hashCode) && fullPath.toFile().exists()){
                    this.writtenFiles.put(path, hashCode);
                    this.toBeGenerated.remove(path);
                    return;
                }

                // Write the data to file
                fullPath.toFile().getParentFile().mkdirs();
                try(OutputStream outputStream = Files.newOutputStream(fullPath)){
                    outputStream.write(bytes);
                }catch(IOException e){
                    throw new RuntimeException(e);
                }
                this.writtenFiles.put(path, hashCode);
                this.toBeGenerated.remove(path);
                this.writes++;
            });

            // Add a cache to the HashCache
            ImmutableMap.Builder<Path,HashCode> builder = ImmutableMap.builder();
            this.writtenFiles.forEach((path, hashCode) -> builder.put(this.outputDirectory.resolve(path), hashCode));
            HashCache.ProviderCache cache = new HashCache.ProviderCache("this.cache.versionId", builder.build());
            this.cache.applyUpdate(new HashCache.UpdateResult("Core Lib Generators", cache, this.writes));
            this.cache.cachePaths.add(this.cache.getProviderCachePath("Core Lib Generators"));

            // Validate all promised files have actually been written
            if(!this.toBeGenerated.isEmpty())
                throw new RuntimeException("Some tracked files did not get written: " + this.toBeGenerated.stream().map(Path::toString).map(s -> "'" + s + "'").collect(Collectors.joining(",")));
        }
    }
}
