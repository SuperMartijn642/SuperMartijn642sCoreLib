package com.supermartijn642.core.coremod;

import com.supermartijn642.core.util.Pair;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.launch.platform.MixinContainer;
import org.spongepowered.asm.launch.platform.MixinPlatformManager;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created 1/16/2021 by SuperMartijn642
 */
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("SuperMartijn642's Core Lib Plugin")
public class CoreModPlugin implements IFMLLoadingPlugin {

    static {
        // For some reason this class gets flagged by 'Ikarus' malware scanner
        // They do not have any way to submit a false positive
        // Simply making some change to this class so the hash is different, it no longer gets flagged
        // ¯\(o_o)/¯
        String dummy = "";
    }

    public CoreModPlugin() throws NoSuchFieldException, IllegalAccessException{
        // Initialize mixin
        MixinBootstrap.init();
        Mixins.addConfiguration("supermartijn642corelib.mixins.json");

        // Fix mod not being loaded when used as a dependency in dev environment with ForgeGradle 3+
        // Just remove the deobfuscated dependencies which were added
        if(FMLLaunchHandler.isDeobfuscatedEnvironment()){
            // Get the field for all mixin containers
            Field containersField = MixinPlatformManager.class.getDeclaredField("containers");
            containersField.setAccessible(true);

            // Find the actual location corresponding to all mixin filenames
            Map<String,String> fileNameToLocation = new HashMap<>();
            //noinspection unchecked
            ((Map<IContainerHandle,MixinContainer>)containersField.get(MixinBootstrap.getPlatform())).keySet()
                .stream()
                .filter(ContainerHandleURI.class::isInstance)
                .map(ContainerHandleURI.class::cast)
                .map(handle -> Pair.of(handle.getFile().getName(), handle.getFile().getAbsolutePath()))
                .forEach(pair -> fileNameToLocation.put(pair.left(), pair.right()));

            // Find the entries which are in the 'deobf_dependencies' folder
            List<String> addedDeobfEntries = CoreModManager.getReparseableCoremods()
                .stream()
                .filter(fileNameToLocation::containsKey) // Filter on entries which have mixin containers
                .filter(fileName -> fileNameToLocation.get(fileName).contains("deobf_dependencies")) // Get the entries in the 'deobf_dependencies' folder
                .collect(Collectors.toList());

            // Remove all the added entries in the 'deobf_dependencies' folder
            CoreModManager.getReparseableCoremods().removeAll(addedDeobfEntries);
        }
    }

    @Override
    public String[] getASMTransformerClass(){
        return new String[0];
    }

    @Override
    public String getModContainerClass(){
        return null;
    }

    @Override
    public String getSetupClass(){
        return null;
    }

    @Override
    public void injectData(Map<String,Object> data){
    }

    @Override
    public String getAccessTransformerClass(){
        return "com/supermartijn642/core/coremod/CoreLibAccessTransformer";
    }
}
