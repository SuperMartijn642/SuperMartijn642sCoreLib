package com.supermartijn642.core.data;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.MalformedJsonException;
import com.supermartijn642.core.CoreLib;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Created 07/08/2022 by SuperMartijn642
 */
public class LanguageLoader {

    private static final Gson GSON = new GsonBuilder().setLenient().create();

    public static void loadLanguageJson(Map<String,String> translationsMap, IResource resource){
        try(Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)){
            JsonObject json = GSON.fromJson(reader, JsonObject.class);

            // Read all the keys
            for(Map.Entry<String,JsonElement> entry : json.entrySet()){
                // Check if the value is a string
                if(!entry.getValue().isJsonPrimitive() || !entry.getValue().getAsJsonPrimitive().isString())
                    throw new RuntimeException("Value for key '" + entry.getKey() + "' must be a string!");

                String key = entry.getKey();
                String translation = entry.getValue().getAsString().replaceAll("%(\\d+\\$)?[\\d\\.]*[df]", "%$1s");
                translationsMap.put(key, translation);
            }
        }catch(MalformedJsonException e){
            CoreLib.LOGGER.error("Encountered malformed language json '" + resource.getResourceLocation() + "' in resource pack '" + resource.getResourcePackName() + "'!", e);
        }catch(IOException e){
            throw new RuntimeException(e);
        }catch(Exception e){
            CoreLib.LOGGER.error("Encountered exception from language json '" + resource.getResourceLocation() + "' in resource pack '" + resource.getResourcePackName() + "'!", e);
        }
    }

    public static List<IResource> findAllResources(IResourceManager resourceManager, ResourceLocation langLocation) throws IOException{
        // Recursively find the FallbackResourceManager for the namespace
        IResourceManager domainManager = findFallbackResourceManager(resourceManager, langLocation.getResourceDomain());
        // If it's not a FallbackResourceManager or SimpleReloadableResourceManager, just return the original values
        if(!(domainManager instanceof FallbackResourceManager))
            return resourceManager.getAllResources(langLocation);

        FallbackResourceManager fallbackResourceManager = (FallbackResourceManager)domainManager;
        fallbackResourceManager.checkResourcePath(langLocation);
        List<IResource> resourceList = Lists.newArrayList();
        // Get the location for the .json file
        ResourceLocation langMcMetaLocation = FallbackResourceManager.getLocationMcmeta(langLocation);
        ResourceLocation jsonLocation = new ResourceLocation(langLocation.getResourceDomain(), langLocation.getResourcePath().substring(0, langLocation.getResourcePath().length() - ".lang".length()) + ".json");
        ResourceLocation jsonMcMetaLocation = FallbackResourceManager.getLocationMcmeta(jsonLocation);

        for(IResourcePack resourcePack : fallbackResourceManager.resourcePacks){
            // Load the .lang file
            if(resourcePack.resourceExists(langLocation)){
                InputStream inputStream = resourcePack.resourceExists(langMcMetaLocation) ? fallbackResourceManager.getInputStream(langMcMetaLocation, resourcePack) : null;
                resourceList.add(new SimpleResource(resourcePack.getPackName(), langLocation, fallbackResourceManager.getInputStream(langLocation, resourcePack), inputStream, fallbackResourceManager.frmMetadataSerializer));
            }
            // Load the .json file
            if(resourcePack.resourceExists(jsonLocation)){
                InputStream inputStream = resourcePack.resourceExists(jsonMcMetaLocation) ? fallbackResourceManager.getInputStream(jsonMcMetaLocation, resourcePack) : null;
                resourceList.add(new SimpleResource(resourcePack.getPackName(), jsonLocation, fallbackResourceManager.getInputStream(jsonLocation, resourcePack), inputStream, fallbackResourceManager.frmMetadataSerializer));
            }
        }

        // Check if any resources were found
        if(resourceList.isEmpty())
            throw new FileNotFoundException(langLocation.toString());

        return resourceList;
    }

    private static IResourceManager findFallbackResourceManager(IResourceManager resourceManager, String namespace){
        if(resourceManager instanceof FallbackResourceManager)
            return resourceManager;
        if(resourceManager instanceof SimpleReloadableResourceManager)
            return findFallbackResourceManager(((SimpleReloadableResourceManager)resourceManager).domainResourceManagers.get(namespace), namespace);
        return resourceManager;
    }
}
