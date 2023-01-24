package com.supermartijn642.core.registry;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.ResourceGenerator;
import com.supermartijn642.core.util.Either;
import com.supermartijn642.core.util.Pair;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created 04/08/2022 by SuperMartijn642
 */
public class GeneratorRegistrationHandler {

    /**
     * Contains one registration helper per modid
     */
    private static final Map<String,GeneratorRegistrationHandler> REGISTRATION_HELPER_MAP = new HashMap<>();
    private static boolean haveProvidersBeenRegistered = false;
    private static HashCache hashCache;

    @ApiStatus.Internal
    @Deprecated
    public static Map<String,GeneratorRegistrationHandler> getAllHandlers(){
        haveProvidersBeenRegistered = true;
        return Collections.unmodifiableMap(REGISTRATION_HELPER_MAP);
    }

    @ApiStatus.Internal
    public static void setCache(HashCache cache){
        hashCache = cache;
    }

    /**
     * Get a registration handler for a given modid. This will always return one unique registration handler per modid.
     * @param modid modid of the mod registering entries
     * @return a unique registration handler for the given modid
     */
    public static synchronized GeneratorRegistrationHandler get(String modid){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");
        if(modid.equals("minecraft"))
            CoreLib.LOGGER.warn("Mod is requesting registration helper for modid '" + modid + "'!");
        else{
            ModContainer container = FabricLoader.getInstance().getModContainer(modid).orElse(null);
            if(container == null)
                CoreLib.LOGGER.warn("Mod is requesting registration helper for unknown modid '" + modid + "'!");
        }

        return REGISTRATION_HELPER_MAP.computeIfAbsent(modid, GeneratorRegistrationHandler::new);
    }

    private final String modid;
    private final List<Either<Function<ResourceCache,ResourceGenerator>,Function<FabricDataOutput,DataProvider>>> generatorsAndProviders = new ArrayList<>();

    private GeneratorRegistrationHandler(String modid){
        this.modid = modid;
    }

    /**
     * Adds the given generator to the list of generators to be run.
     */
    public void addGenerator(Function<ResourceCache,ResourceGenerator> generator){
        if(generator == null)
            throw new IllegalArgumentException("Generator must not be null!");
        if(haveProvidersBeenRegistered)
            throw new RuntimeException("Generators supplier must be added before the GatherDataEvent gets fired!");

        this.generatorsAndProviders.add(Either.left(generator));
    }

    /**
     * Adds the given generator to the list of generators to be run.
     */
    public void addGenerator(Supplier<ResourceGenerator> generator){
        if(generator == null)
            throw new IllegalArgumentException("Generator supplier must not be null!");

        this.addGenerator(cache -> generator.get());
    }

    /**
     * Adds the given generator to the list of generators to be run.
     */
    public void addGenerator(ResourceGenerator generator){
        if(generator == null)
            throw new IllegalArgumentException("Generator must not be null!");

        this.addGenerator(cache -> generator);
    }

    /**
     * Adds the given data provider to the list of providers to be run.
     */
    public void addProvider(Function<FabricDataOutput,DataProvider> provider){
        if(provider == null)
            throw new IllegalArgumentException("Provider must not be null!");
        if(haveProvidersBeenRegistered)
            throw new RuntimeException("Providers supplier must be added before the GatherDataEvent gets fired!");

        this.generatorsAndProviders.add(Either.right(provider));
    }

    /**
     * Adds the given data provider to the list of providers to be run.
     */
    public void addProvider(Supplier<DataProvider> provider){
        if(provider == null)
            throw new IllegalArgumentException("Provider must not be null!");

        this.addProvider(dataGenerator -> provider.get());
    }

    /**
     * Adds the given data provider to the list of providers to be run.
     */
    public void addProvider(DataProvider provider){
        if(provider == null)
            throw new IllegalArgumentException("Provider must not be null!");

        this.addProvider(dataGenerator -> provider);
    }

    @ApiStatus.Internal
    @Deprecated
    public void registerProviders(FabricDataGenerator dataGenerator){
        // Get the manual files folder
        String manualFolderProperty = System.getProperty("fabric-api.datagen.manual-dir");
        Path manualFolder = manualFolderProperty == null || manualFolderProperty.isBlank() ? null : Paths.get(manualFolderProperty);
        if(manualFolder == null)
            CoreLib.LOGGER.warn("Property 'fabric-api.datagen.manual-dir' has not been set! Manually created files may not be recognised!");

        // Create a new pack
        FabricDataGenerator.Pack pack = dataGenerator.createPack();
        Path outputFolder = pack.output.getOutputFolder();

        // Create a resource cache and consumer to update the active CachedOutput instance
        Pair<ResourceCache,Consumer<CachedOutput>> resourceCachePair = ResourceCache.wrap(() -> hashCache, outputFolder, manualFolder);

        // Resolve and add all the generators and providers
        this.generatorsAndProviders
            .stream()
            .map(either -> either.mapLeft(generatorSupplier -> generatorSupplier.apply(resourceCachePair.left())))
            .map(either -> either.mapLeft(generator -> (FabricDataGenerator.Pack.Factory<DataProvider>)output -> ResourceGenerator.createDataProvider(generator, resourceCachePair.right())))
            .map(either -> either.mapRight(provider -> (FabricDataGenerator.Pack.Factory<DataProvider>)provider::apply))
            .map(either -> either.leftOrElseGet(either::right))
            .forEach(pack::addProvider);
    }
}
