package com.supermartijn642.core.generator;

import com.google.common.hash.Funnels;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created 04/08/2022 by SuperMartijn642
 */
@SuppressWarnings("UnstableApiUsage")
public class ResourceCache {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Map<Path,HashCode> presentFiles = new HashMap<>();
    private final Map<Path,HashCode> writtenFiles = new HashMap<>();

    private final File outputDirectory;
    private final File manualDirectory;
    private final File cacheFile;

    ResourceCache(File outputFolder, File manualFolder){
        if(outputFolder == null)
            throw new IllegalArgumentException("Output directory must not be null!");
        if(manualFolder == null)
            throw new IllegalArgumentException("Manual directory must not be null!");
        this.outputDirectory = outputFolder;
        this.manualDirectory = manualFolder;
        this.cacheFile = new File(this.outputDirectory, ".cache/cache");

        this.loadCacheFromFile();
    }

    private void loadCacheFromFile(){
        if(!this.cacheFile.exists())
            return;

        // Read cache
        try(BufferedReader reader = new BufferedReader(new FileReader(this.cacheFile))){
            String line;
            while((line = reader.readLine()) != null){
                String[] parts = line.split(" ");
                if(parts.length > 2)
                    continue;
                this.presentFiles.put(Paths.get(parts[0]), HashCode.fromString(parts[0]));
            }
        }catch(IOException e){
            throw new RuntimeException("Encountered an exception whilst trying to read the generator cache file!", e);
        }

        // Look for files which are not in the cache
        Path outputPath = this.outputDirectory.toPath();
        try(Stream<Path> paths = Files.walk(this.outputDirectory.toPath())){
            paths
                .filter(path -> !this.presentFiles.containsKey(outputPath.relativize(path)))
                .filter(path -> path.toFile().exists() && path.toFile().isFile() && !"cache".equals(path.toFile().getName()))
                .forEach(
                    path -> {
                        // Calculate hash
                        HashCode hashCode;
                        try(InputStream inputStream = Files.newInputStream(path.toFile().toPath())){
                            Hasher hasher = Hashing.sha1().newHasher();
                            ByteStreams.copy(inputStream, Funnels.asOutputStream(hasher));
                            hashCode = hasher.hash();
                        }catch(IOException e){
                            throw new RuntimeException(e);
                        }

                        // Put the file into the cache
                        this.presentFiles.put(outputPath.relativize(path), hashCode);
                    }
                );
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    void writeCacheToFile(){
        if(!this.cacheFile.getParentFile().exists())
            this.cacheFile.getParentFile().mkdirs();

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(this.cacheFile))){
            for(Map.Entry<Path,HashCode> entry : this.writtenFiles.entrySet()){
                writer.write(entry.getValue().toString());
                writer.write(' ');
                writer.write(entry.getKey().toString());
                writer.newLine();
            }
        }catch(IOException e){
            throw new RuntimeException("Encountered an exception whilst trying to write the generator cache file!", e);
        }
    }

    void removeRemnants(){
        // Remove remaining files
        this.presentFiles.keySet()
            .stream()
            .filter(((Predicate<Path>)this.writtenFiles::containsKey).negate())
            .map(path -> this.outputDirectory.toPath().resolve(path).toFile())
            .filter(File::exists)
            .forEach(File::delete);

        // Remove empty folders
        List<Path> pathList = new ArrayList<>();
        try(Stream<Path> paths = Files.walk(this.outputDirectory.toPath())){
            paths.filter(Files::isDirectory).forEach(pathList::add);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
        // For some reason the walking is not depth-first, even though the javadoc says so 🤷
        pathList.sort((a, b) -> Integer.compare(b.toString().length(), a.toString().length()));
        pathList.stream()
            .filter(Files::isDirectory)
            .filter(
                path -> {
                    System.out.println("path: " + path);
                    try(DirectoryStream<Path> files = Files.newDirectoryStream(path)){
                        return !files.iterator().hasNext();
                    }catch(IOException e){
                        throw new RuntimeException(e);
                    }
                }
            )
            .map(Path::toFile)
            .forEach(File::delete);
    }

    private boolean existsInGeneratedFiles(Path path){
        return this.writtenFiles.containsKey(path);
    }

    private boolean existsInManualFiles(Path path){
        return this.manualDirectory.toPath().resolve(path).toFile().exists();
    }

    private boolean existsInLoadedResources(ResourceType resourceType, String namespace, String directory, String fileName, String extension){
        // In 1.12, we only have to check resource packs
        IResourceManager resourceManager = ClientUtils.getMinecraft().getResourceManager();
        try(IResource resource = resourceManager.getResource(new ResourceLocation(namespace, directory + File.separator + fileName + File.separator + extension))){
            return true;
        }catch(FileNotFoundException ignored){
            return false;
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    private Path constructPath(ResourceType resourceType, String namespace, String directory, String fileName, String extension){
        return Paths.get(resourceType.getDirectoryName(), namespace, directory, fileName + extension);
    }

    /**
     * Checks whether a resource exists. The resource may be either a generated file, or a file from a loaded resource pack.
     * @param resourceType whether the resource is part of the server data or the client assets
     * @param namespace    the namespace of the resource
     * @param directory    name of the directory within the namespace
     * @param fileName     name of the file
     * @param extension    the file's extension
     */
    public boolean doesResourceExist(ResourceType resourceType, String namespace, String directory, String fileName, String extension){
        Path path = this.constructPath(resourceType, namespace, directory, fileName, extension);
        return this.existsInGeneratedFiles(path)
            || this.existsInManualFiles(path)
            || this.existsInLoadedResources(resourceType, namespace, directory, fileName, extension);
    }

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
     * Saves the given data in the appropriate location. Also checks if a file is already present to avoid redundant writes.
     * @param resourceType whether the given data is part of the server data or the client assets
     * @param namespace    the namespace which the data should be saved under
     * @param directory    name of the directory within the namespace
     * @param fileName     name of the file
     * @param extension    extension of the file
     */
    public void saveResource(ResourceType resourceType, byte[] data, String namespace, String directory, String fileName, String extension){
        Path path = this.constructPath(resourceType, namespace, directory, fileName, extension);
        if(this.writtenFiles.containsKey(path))
            throw new RuntimeException("Duplicate file '" + path + "'!");
        if(this.existsInManualFiles(path))
            throw new RuntimeException("File '" + path + "' clashes with a manually created file!");

        // Skip writing if the present file matches the one to be written
        Path fullPath = this.outputDirectory.toPath().resolve(path);
        HashCode hashCode = Hashing.sha1().hashBytes(data);
        if(this.presentFiles.containsKey(path) && this.presentFiles.get(path).equals(hashCode) && fullPath.toFile().exists()){
            this.writtenFiles.put(path, hashCode);
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
    }
}
