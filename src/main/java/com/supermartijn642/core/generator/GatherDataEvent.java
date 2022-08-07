package com.supermartijn642.core.generator;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.IContextSetter;

import java.util.function.Consumer;

/**
 * Created 04/08/2022 by SuperMartijn642
 */
public class GatherDataEvent extends Event implements IContextSetter {

    private final ResourceCache cache;
    private final Consumer<ResourceGenerator> generatorConsumer;

    public GatherDataEvent(ResourceCache cache, Consumer<ResourceGenerator> generatorConsumer){
        this.cache = cache;
        this.generatorConsumer = generatorConsumer;
    }

    public ResourceCache getResourceCache(){
        return this.cache;
    }

    public void addGenerator(ResourceGenerator generator){
        this.generatorConsumer.accept(generator);
    }
}
