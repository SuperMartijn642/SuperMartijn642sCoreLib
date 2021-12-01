package com.supermartijn642.core.mixin;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

/**
 * Created 1/16/2021 by SuperMartijn642
 */
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("Scarecrow's Territory Plugin")
public class CoreModPlugin implements IFMLLoadingPlugin {

    public CoreModPlugin(){
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.supermartijn642corelib.json");
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
        return null;
    }
}
