package com.supermartijn642.core.generator;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.ApiStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

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
    public abstract Optional<InputStream> getExistingResource(ResourceType resourceType, String namespace, String directory, String fileName, String extension);

    @ApiStatus.Internal
    static ResourceCache wrap(ExistingFileHelper existingFileHelper, CachedOutput cachedOutput, Path outputDirectory){
        return new ExistingFileHelperWrapper(existingFileHelper, cachedOutput, outputDirectory);
    }

    private static class ExistingFileHelperWrapper extends ResourceCache {

        private final Map<Path,HashCode> writtenFiles = new HashMap<>();

        private final ExistingFileHelper existingFileHelper;
        private final Path outputDirectory;
        private final CachedOutput cache;

        private ExistingFileHelperWrapper(ExistingFileHelper existingFileHelper, CachedOutput cache, Path outputFolder){
            if(outputFolder == null)
                throw new IllegalArgumentException("Output directory must not be null!");
            this.outputDirectory = outputFolder;
            this.existingFileHelper = existingFileHelper;
            this.cache = cache;
        }

        @Override
        public boolean doesResourceExist(ResourceType resourceType, String namespace, String directory, String fileName, String extension){
            ResourceLocation location = new ResourceLocation(namespace, directory + "/" + fileName + extension);
            return this.existingFileHelper.exists(location, resourceType == ResourceType.DATA ? PackType.SERVER_DATA : PackType.CLIENT_RESOURCES);
        }

        @Override
        public void trackToBeGeneratedResource(ResourceType resourceType, String namespace, String directory, String fileName, String extension){
            ResourceLocation location = new ResourceLocation(namespace, fileName);
            this.existingFileHelper.trackGenerated(location, resourceType == ResourceType.DATA ? PackType.SERVER_DATA : PackType.CLIENT_RESOURCES, extension, directory);
        }

        @Override
        public Optional<InputStream> getExistingResource(ResourceType resourceType, String namespace, String directory, String fileName, String extension){
            try{
                Resource resource = this.existingFileHelper.getResource(new ResourceLocation(namespace, directory + "/" + fileName + extension), PackType.CLIENT_RESOURCES);
                return Optional.of(resource.open());
            }catch(FileNotFoundException | NoSuchElementException e){
                return Optional.empty();
            }catch(IOException e){
                throw new RuntimeException(e);
            }
        }

        private Path constructPath(ResourceType resourceType, String namespace, String directory, String fileName, String extension){
            return Paths.get(resourceType.getDirectoryName(), namespace, directory, fileName + extension);
        }

        @Override
        public void saveResource(ResourceType resourceType, byte[] data, String namespace, String directory, String fileName, String extension){
            Path path = this.constructPath(resourceType, namespace, directory, fileName, extension);
            Path fullPath = this.outputDirectory.resolve(path);
            if(this.writtenFiles.containsKey(path))
                throw new RuntimeException("Duplicate file '" + path + "'!");

            // Skip writing if the present file matches the one to be written
            HashCode hashCode = Hashing.sha1().hashBytes(data);

            // Write the data to file
            try{
                this.cache.writeIfNeeded(fullPath, data, hashCode);
            }catch(IOException e){
                throw new RuntimeException(e);
            }
            this.writtenFiles.put(path, hashCode);
        }
    }
}
