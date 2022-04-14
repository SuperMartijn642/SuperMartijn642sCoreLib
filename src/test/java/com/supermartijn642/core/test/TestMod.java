package com.supermartijn642.core.test;

import com.google.common.reflect.Reflection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Created 1/23/2021 by SuperMartijn642
 */
public class TestMod implements ModInitializer {

    @Override
    public void onInitialize(){
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
            Reflection.initialize(ClientProxy.class);
    }
}
