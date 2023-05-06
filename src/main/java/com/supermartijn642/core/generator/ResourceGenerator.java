package com.supermartijn642.core.generator;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.registry.RegistryUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

/**
 * Created 04/08/2022 by SuperMartijn642
 */
public abstract class ResourceGenerator {

    /**
     * Wraps the given resource generator in a data provider using the given file helper and data generator.
     * @return a data provider wrapping the resource generator
     */
    public static DataProvider createDataProvider(ResourceGenerator generator){
        return new DataProviderInstance(generator);
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
    public abstract void save();

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

    @ApiStatus.Internal
    public static class DataProviderInstance implements DataProvider {

        private final ResourceGenerator generator;
        private boolean generated = false;

        public DataProviderInstance(ResourceGenerator generator){
            this.generator = generator;
        }

        public void generate(){
            this.generated = true;
            this.generator.generate();
        }

        @Override
        public void run(HashCache output){
            // Run the resource generator
            if(!this.generated)
                this.generator.generate();
            this.generator.save();
        }

        @Override
        public String getName(){
            return this.generator.getName();
        }
    }
}
