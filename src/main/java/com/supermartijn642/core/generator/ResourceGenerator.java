package com.supermartijn642.core.generator;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

/**
 * Created 04/08/2022 by SuperMartijn642
 */
public abstract class ResourceGenerator {

    protected final String modid;
    protected final String modName;
    protected final ResourceCache cache;

    public ResourceGenerator(String modid, ResourceCache cache){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");
        String activeMod = Loader.instance().activeModContainer() == null ? null : Loader.instance().activeModContainer().getModId();
        if(activeMod != null && !activeMod.equals("minecraft") && !activeMod.equals("forge")){
            if(!activeMod.equals(modid))
                CoreLib.LOGGER.warn("Mod '" + Loader.instance().activeModContainer().getName() + "' is creating a resource generator with different modid '" + modid + "'!");
        }else if(modid.equals("minecraft") || modid.equals("forge"))
            CoreLib.LOGGER.warn("Mod is creating a resource generator with modid '" + modid + "'!");

        this.modid = modid;
        this.cache = cache;

        ModContainer container = Loader.instance().getActiveModList().stream().filter(c -> modid.equals(c.getModId())).findAny().orElse(null);
        this.modName = container == null ? modid : container.getName();
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
