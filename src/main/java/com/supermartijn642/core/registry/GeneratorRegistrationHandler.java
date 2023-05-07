package com.supermartijn642.core.registry;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.ResourceGenerator;
import com.supermartijn642.core.util.Either;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.ModLoadingContext;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
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

    @ApiStatus.Internal
    public static boolean hasHandlerForModid(String modid){
        return REGISTRATION_HELPER_MAP.containsKey(modid);
    }

    /**
     * Get a registration handler for a given modid. This will always return one unique registration handler per modid.
     * @param modid modid of the mod registering entries
     * @return a unique registration handler for the given modid
     */
    public static synchronized GeneratorRegistrationHandler get(String modid){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");
        String activeMod = ModLoadingContext.get().getActiveNamespace();
        if(activeMod != null && !activeMod.equals("minecraft") && !activeMod.equals("forge")){
            if(!activeMod.equals(modid))
                CoreLib.LOGGER.warn("Mod '" + ModLoadingContext.get().getActiveContainer().getModInfo().getDisplayName() + "' is requesting registration helper for different modid '" + modid + "'!");
        }else if(modid.equals("minecraft") || modid.equals("forge"))
            CoreLib.LOGGER.warn("Mod is requesting registration helper for modid '" + modid + "'!");

        return REGISTRATION_HELPER_MAP.computeIfAbsent(modid, GeneratorRegistrationHandler::new);
    }

    private final String modid;
    private final List<Either<Function<ResourceCache,ResourceGenerator>,BiFunction<DataGenerator,ExistingFileHelper,DataProvider>>> generatorsAndProviders = new ArrayList<>();

    private boolean hasEventBeenFired;

    private GeneratorRegistrationHandler(String modid){
        this.modid = modid;
    }

    /**
     * Adds the given generator to the list of generators to be run.
     */
    public void addGenerator(Function<ResourceCache,ResourceGenerator> generator){
        if(generator == null)
            throw new IllegalArgumentException("Generator must not be null!");
        if(this.hasEventBeenFired)
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
    public void addProvider(BiFunction<DataGenerator,ExistingFileHelper,DataProvider> provider){
        if(provider == null)
            throw new IllegalArgumentException("Provider must not be null!");
        if(this.hasEventBeenFired)
            throw new RuntimeException("Providers supplier must be added before the GatherDataEvent gets fired!");

        this.generatorsAndProviders.add(Either.right(provider));
    }

    /**
     * Adds the given data provider to the list of providers to be run.
     */
    public void addProvider(Function<DataGenerator,DataProvider> provider){
        if(provider == null)
            throw new IllegalArgumentException("Provider must not be null!");

        this.addProvider((generator, existingFileHelper) -> provider.apply(generator));
    }

    /**
     * Adds the given data provider to the list of providers to be run.
     */
    public void addProvider(Supplier<DataProvider> provider){
        if(provider == null)
            throw new IllegalArgumentException("Provider must not be null!");

        this.addProvider((dataGenerator, existingFileHelper) -> provider.get());
    }

    /**
     * Adds the given data provider to the list of providers to be run.
     */
    public void addProvider(DataProvider provider){
        if(provider == null)
            throw new IllegalArgumentException("Provider must not be null!");

        this.addProvider((dataGenerator, existingFileHelper) -> provider);
    }

    @ApiStatus.Internal
    public void registerProviders(DataGenerator dataGenerator, ExistingFileHelper existingFileHelper, ResourceCache cache){
        this.hasEventBeenFired = true;

        // Resolve and add all the generators and providers
        this.generatorsAndProviders
            .stream()
            .map(either -> either.mapLeft(generator -> generator.apply(cache)))
            .map(either -> either.mapLeft(ResourceGenerator::createDataProvider))
            .map(either -> either.mapRight(provider -> provider.apply(dataGenerator, existingFileHelper)))
            .map(either -> either.leftOrElseGet(either::right))
            .forEach(provider -> dataGenerator.addProvider(true, provider));
    }
}
