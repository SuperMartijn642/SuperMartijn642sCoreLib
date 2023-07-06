package com.supermartijn642.core.registry;

import com.supermartijn642.core.CoreLib;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

/**
 * Created 06/07/2023 by SuperMartijn642
 */
public class RegistryEntryPoints implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitialize(){
        CoreLib.afterInitialize();
    }

    @Override
    public void onInitializeClient(){
        CoreLib.afterInitializeClient();
    }
}
