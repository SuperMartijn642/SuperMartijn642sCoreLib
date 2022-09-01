package com.supermartijn642.core.generator;

import com.google.common.base.Stopwatch;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.CoreLib;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.client.FMLFolderResourcePack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created 04/08/2022 by SuperMartijn642
 */
public class GeneratorManager {

    private static final List<ResourceGenerator> GENERATORS = new ArrayList<>();

    public static void gatherAndRunGenerators(String modid, String outputFolder, String existingFolder){
        // Create the data cache
        File outputFolderFile = new File(outputFolder);
        File existingFolderFile = new File(existingFolder);
        if(!outputFolderFile.exists())
            outputFolderFile.mkdirs();
        ResourceCache cache = ResourceCache.create(outputFolderFile.toPath(), existingFolderFile.toPath());

        // Remove the mod's resources from the resource manager
        for(FallbackResourceManager resourceManager : ((SimpleReloadableResourceManager)ClientUtils.getMinecraft().getResourceManager()).domainResourceManagers.values()){
            resourceManager.resourcePacks.removeIf(
                pack -> (pack instanceof FMLFileResourcePack && modid.equals(((FMLFileResourcePack)pack).getFMLContainer().getModId()))
                    || (pack instanceof FMLFolderResourcePack && modid.equals(((FMLFolderResourcePack)pack).getFMLContainer().getModId()))
            );
        }

        // Gather all generators
        GatherDataEvent event = new GatherDataEvent(cache, GENERATORS::add);
        MinecraftForge.EVENT_BUS.post(event);

        // Filter generators by modid
        GENERATORS.removeIf(generator -> !modid.equals(generator.getOwnerModid()));
        CoreLib.LOGGER.info("Found " + GENERATORS.size() + " generators for modid '" + modid + "'");

        // Run all generators
        try{
            Stopwatch stopwatch = Stopwatch.createUnstarted();
            Stopwatch totalStopwatch = Stopwatch.createStarted();
            for(ResourceGenerator generator : GENERATORS){
                CoreLib.LOGGER.info("Starting generator '" + generator.getName() + "'...");
                stopwatch.start();
                generator.generate();
                stopwatch.stop();
                CoreLib.LOGGER.info("Generator '" + generator.getName() + "' finished after " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");
                stopwatch.reset();
            }
            for(ResourceGenerator generator : GENERATORS){
                CoreLib.LOGGER.info("Saving generator '" + generator.getName() + "'...");
                stopwatch.start();
                generator.save();
                stopwatch.stop();
                CoreLib.LOGGER.info("Generator '" + generator.getName() + "' took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms to save");
                stopwatch.reset();
            }
            totalStopwatch.stop();
            CoreLib.LOGGER.info("All generators for modid '" + modid + "' took " + totalStopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");
        }catch(Exception e){
            CoreLib.LOGGER.error("Encountered an exception when running generators!", e);
            return;
        }

        // Write the cache file
        ((ResourceCache.Impl)cache).removeRemnants();
        ((ResourceCache.Impl)cache).writeCacheToFile();
    }
}
