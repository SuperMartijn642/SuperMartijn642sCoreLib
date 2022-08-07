package com.supermartijn642.core.registry;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.generator.GatherDataEvent;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.ResourceGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
        String activeMod = Loader.instance().activeModContainer() == null ? null : Loader.instance().activeModContainer().getModId();
        if(activeMod != null && !activeMod.equals("minecraft") && !activeMod.equals("forge")){
            if(!activeMod.equals(modid))
                CoreLib.LOGGER.warn("Mod '" + Loader.instance().activeModContainer().getName() + "' is requesting registration helper for different modid '" + modid + "'!");
        }else if(modid.equals("minecraft") || modid.equals("forge"))
            CoreLib.LOGGER.warn("Mod is requesting registration helper for modid '" + modid + "'!");

        return REGISTRATION_HELPER_MAP.computeIfAbsent(modid, GeneratorRegistrationHandler::new);
    }

    private final String modid;
    private final Set<Function<ResourceCache,ResourceGenerator>> generators = new HashSet<>();

    private boolean hasEventBeenFired;

    private GeneratorRegistrationHandler(String modid){
        this.modid = modid;
        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void handlerGatherDataEvent(GatherDataEvent e){
                GeneratorRegistrationHandler.this.handlerGatherDataEvent(e);
            }
        });
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

    private void handlerGatherDataEvent(GatherDataEvent e){
        // Resolve and add all the generators
        for(Function<ResourceCache,ResourceGenerator> generatorFunction : this.generators){
            ResourceGenerator generator = generatorFunction.apply(e.getResourceCache());
            if(generator == null){
                CoreLib.LOGGER.error("Received null generator from a registered generator supplier from mod '" + this.modid + "'!");
                continue;
            }

            e.addGenerator(generator);
        }
    }
}
