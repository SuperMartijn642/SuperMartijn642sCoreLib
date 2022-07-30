package com.supermartijn642.core;

import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
@Mod("supermartijn642corelib")
public class CoreLib {

    public static final Logger LOGGER = LogManager.getLogger("supermartijn642corelib");

    public CoreLib(){
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterRegistries);
    }

    private void onRegisterRegistries(RegistryEvent.NewRegistry e){
        RegistryEntryAcceptor.Handler.gatherAnnotatedFields();
    }
}
