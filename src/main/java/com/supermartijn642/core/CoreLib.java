package com.supermartijn642.core;

import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created 18/03/2022 by SuperMartijn642
 */
public class CoreLib implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("supermartijn642corelib");

    @Override
    public void onInitialize(){
        CommonUtils.initialize();

        // Load test mod stuff
        if(FabricLoader.getInstance().isDevelopmentEnvironment()){
            ModInitializer testMod = null;
            try{
                Class<?> testModClass = Class.forName("com.supermartijn642.core.test.TestMod");
                testMod = (ModInitializer)testModClass.getDeclaredConstructor().newInstance();
            }catch(Exception ignore){}
            if(testMod != null)
                testMod.onInitialize();
        }
    }

    /**
     * Called right before all {@link ModInitializer}s, {@link ClientModInitializer}'s, and {@link DedicatedServerModInitializer}s are initialized.
     */
    public static void beforeInitialize(){
        RegistryEntryAcceptor.Handler.gatherAnnotatedFields();
    }

    /**
     * Called right after all {@link ModInitializer}s, {@link ClientModInitializer}'s, and {@link DedicatedServerModInitializer}s have been initialized.
     */
    public static void afterInitialize(){
        RegistrationHandler.registerInternal();
        if(CommonUtils.getEnvironmentSide().isClient())
            ClientRegistrationHandler.registerRenderersInternal();
        RegistryEntryAcceptor.Handler.reportMissing();
    }
}
