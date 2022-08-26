package com.supermartijn642.core.generator;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.registry.RegistryUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created 04/08/2022 by SuperMartijn642
 */
public abstract class ResourceGenerator {

    /**
     * Wraps the given resource generator in a data provider using the given file helper and data generator.
     * @return a data provider wrapping the resource generator
     */
    public static DataProvider createDataProvider(Function<ResourceCache,ResourceGenerator> generatorProvider, Supplier<ResourceCache> cacheSupplier){
        return new DataProvider() {
            private ResourceGenerator generator;

            private ResourceGenerator getGenerator(){
                if(this.generator == null)
                    this.generator = generatorProvider.apply(cacheSupplier.get());
                return this.generator;
            }

            @Override
            public void run(HashCache cachedOutput){
                // Run the resource generator
                this.getGenerator().generate();
                this.getGenerator().save();
            }

            @Override
            public String getName(){
                return this.getGenerator().getName();
            }
        };
    }

    protected final String modid;
    protected final String modName;
    protected final ResourceCache cache;

    public ResourceGenerator(String modid, ResourceCache cache){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");
        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modid);
        if(modid.equals("minecraft"))
            CoreLib.LOGGER.warn("Mod is creating a resource generator with modid '" + modid + "'!");
        else if(modContainer.isEmpty())
            CoreLib.LOGGER.warn("Mod is creating a resource generator for unknown modid '" + modid + "'!");

        this.modid = modid;
        this.cache = cache;

        this.modName = modContainer.map(ModContainer::getMetadata).map(ModMetadata::getName).orElse(modid);
    }

    /**
     * Generates all data. All files that will be generated should be tracked using {@link ResourceCache#trackToBeGeneratedResource(ResourceType, String, String, String, String)}.
     */
    public abstract void generate();

    /**
     * Saves any generated resources. {@link #cache} may be used to check for existing files and to save the generated files.
     */
    public void save(){
    }

    /**
     * Gives the name of this data generator. A good name should include the name of the owning mod and the type of data the generator generates, e.g. Your Mod's model generator.
     */
    public String getName(){
        return this.modName + " Resource Generator";
    }

    /**
     * Gives the modid of the mod which owns this generator.
     */
    public final String getOwnerModid(){
        return this.modid;
    }
}
