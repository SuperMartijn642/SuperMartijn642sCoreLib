package com.supermartijn642.core.generator;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.data.DirectoryCache;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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

    /**
     * Only for internal use. Do NOT use!
     */
    @Deprecated
    public static ResourceCache wrap(ExistingFileHelper existingFileHelper, DirectoryCache cachedOutput, Path outputDirectory){
        return new ExistingFileHelperWrapper(existingFileHelper, cachedOutput, outputDirectory);
    }

    private static class ExistingFileHelperWrapper extends ResourceCache {

        private final Map<Path,HashCode> writtenFiles = new HashMap<>();
        private final List<Path> toBeGenerated = new ArrayList<>(); // TODO validate this is empty after datagen

        private final ExistingFileHelper existingFileHelper;
        private final Path outputDirectory;
        private final DirectoryCache cache;

        private ExistingFileHelperWrapper(ExistingFileHelper existingFileHelper, DirectoryCache hashCache, Path outputFolder){
            if(outputFolder == null)
                throw new IllegalArgumentException("Output directory must not be null!");
            this.outputDirectory = outputFolder;
            this.existingFileHelper = existingFileHelper;
            this.cache = hashCache;
        }

        @Override
        public boolean doesResourceExist(ResourceType resourceType, String namespace, String directory, String fileName, String extension){
            Path path = this.constructPath(resourceType, namespace, directory, fileName, extension);
            ResourceLocation location = new ResourceLocation(namespace, fileName);
            return this.toBeGenerated.contains(path)
                || this.writtenFiles.containsKey(path)
                || this.cache.newCache.containsKey(this.outputDirectory.resolve(path))
                || this.existingFileHelper.exists(location, resourceType == ResourceType.DATA ? ResourcePackType.SERVER_DATA : ResourcePackType.CLIENT_RESOURCES, extension, directory);
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

        private Path constructPath(ResourceType resourceType, String namespace, String directory, String fileName, String extension){
            return Paths.get(resourceType.getDirectoryName(), namespace, directory, fileName + extension);
        }

        @SuppressWarnings("UnstableApiUsage")
        @Override
        public void saveResource(ResourceType resourceType, byte[] data, String namespace, String directory, String fileName, String extension){
            Path path = this.constructPath(resourceType, namespace, directory, fileName, extension);
            Path fullPath = this.outputDirectory.resolve(path);
            if(this.writtenFiles.containsKey(path) || this.cache.newCache.containsKey(fullPath))
                throw new RuntimeException("Duplicate file '" + path + "'!");

            // Skip writing if the present file matches the one to be written
            HashCode hashCode = Hashing.sha1().hashBytes(data);
            if(hashCode.toString().equals(this.cache.getHash(fullPath)) && fullPath.toFile().exists()){
                this.writtenFiles.put(path, hashCode);
                this.toBeGenerated.remove(path);
                this.cache.putNew(fullPath, hashCode.toString());
                return;
            }

            // Write the data to file
            fullPath.getParent().toFile().mkdirs();
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
