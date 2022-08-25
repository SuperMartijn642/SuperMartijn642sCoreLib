package com.supermartijn642.core.registry;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.ResourceGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

    /**
     * Get a registration handler for a given modid. This will always return one unique registration handler per modid.
     * @param modid modid of the mod registering entries
     * @return a unique registration handler for the given modid
     */
    public static GeneratorRegistrationHandler get(String modid){
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
    private final Set<Function<ResourceCache,ResourceGenerator>> generators = new HashSet<>();
    private final Set<BiFunction<DataGenerator,ExistingFileHelper,DataProvider>> providers = new HashSet<>();

    private boolean hasEventBeenFired;

    private GeneratorRegistrationHandler(String modid){
        this.modid = modid;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::handleGatherDataEvent);
    }

    /**
     * Adds the given generator to the list of generators to be run.
     */
    public void addGenerator(Function<ResourceCache,ResourceGenerator> generator){
        if(generator == null)
            throw new IllegalArgumentException("Generator must not be null!");
        if(this.hasEventBeenFired)
            throw new RuntimeException("Generators supplier must be added before the GatherDataEvent gets fired!");

        this.generators.add(generator);
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

        this.providers.add(provider);
    }

    /**
     * Adds the given data provider to the list of providers to be run.
     */
    public void addProvider(Function<DataGenerator,DataProvider> provider){
        if(provider == null)
            throw new IllegalArgumentException("Provider must not be null!");
        if(this.hasEventBeenFired)
            throw new RuntimeException("Providers supplier must be added before the GatherDataEvent gets fired!");

        this.providers.add((dataGenerator, existingFileHelper) -> provider.apply(dataGenerator));
    }

    /**
     * Adds the given data provider to the list of providers to be run.
     */
    public void addProvider(Supplier<DataProvider> provider){
        if(provider == null)
            throw new IllegalArgumentException("Provider must not be null!");
        if(this.hasEventBeenFired)
            throw new RuntimeException("Providers supplier must be added before the GatherDataEvent gets fired!");

        this.providers.add((dataGenerator, existingFileHelper) -> provider.get());
    }

    /**
     * Adds the given data provider to the list of providers to be run.
     */
    public void addProvider(DataProvider provider){
        if(provider == null)
            throw new IllegalArgumentException("Provider must not be null!");
        if(this.hasEventBeenFired)
            throw new RuntimeException("Providers supplier must be added before the GatherDataEvent gets fired!");

        this.providers.add((dataGenerator, existingFileHelper) -> provider);
    }

    private void handleGatherDataEvent(GatherDataEvent e){
        this.hasEventBeenFired = true;

        // Resolve and add all the generators
        this.generators
            .stream()
            .map(generator -> ResourceGenerator.createDataProvider(generator, e.getExistingFileHelper(), e.getGenerator()))
            .forEach(provider -> e.getGenerator().addProvider(provider));
        // Resolve and add all regular data providers
        this.providers
            .stream()
            .map(provider -> provider.apply(e.getGenerator(), e.getExistingFileHelper()))
            .forEach(provider -> e.getGenerator().addProvider(provider));
    }
}
