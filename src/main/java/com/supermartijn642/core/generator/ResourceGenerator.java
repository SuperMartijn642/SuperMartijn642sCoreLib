package com.supermartijn642.core.generator;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;

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
    public static IDataProvider createDataProvider(Function<ResourceCache,ResourceGenerator> generatorProvider, Supplier<ResourceCache> cacheSupplier){
        return new IDataProvider() {
            private ResourceGenerator generator;

            private ResourceGenerator getGenerator(){
                if(this.generator == null)
                    this.generator = generatorProvider.apply(cacheSupplier.get());
                return this.generator;
            }

            @Override
            public void run(DirectoryCache cachedOutput){
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
        String activeMod = ModLoadingContext.get().getActiveNamespace();
        if(activeMod != null && !activeMod.equals("minecraft") && !activeMod.equals("forge")){
            if(!activeMod.equals(modid))
                CoreLib.LOGGER.warn("Mod '" + ModLoadingContext.get().getActiveContainer().getModInfo().getDisplayName() + "' is creating a resource generator with different modid '" + modid + "'!");
        }else if(modid.equals("minecraft") || modid.equals("forge"))
            CoreLib.LOGGER.warn("Mod is creating a resource generator with modid '" + modid + "'!");

        this.modid = modid;
        this.cache = cache;

        Optional<? extends ModContainer> modContainer = ModList.get().getModContainerById(modid);
        this.modName = modContainer.map(ModContainer::getModInfo).map(IModInfo::getDisplayName).orElse(modid);
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
