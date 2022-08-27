package com.supermartijn642.core;

import com.supermartijn642.core.generator.standard.CoreLibMiningTagGenerator;
import com.supermartijn642.core.recipe.ConditionalRecipeSerializer;
import com.supermartijn642.core.recipe.condition.ModLoadedRecipeCondition;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
@Mod("supermartijn642corelib")
public class CoreLib {

    public static final Logger LOGGER = LoggerFactory.getLogger("supermartijn642corelib");

    public CoreLib(){
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConstructMod);

        // Register conditional recipe type
        RegistrationHandler handler = RegistrationHandler.get("supermartijn642corelib");
        handler.registerRecipeSerializer("conditional", ConditionalRecipeSerializer.INSTANCE);
        handler.registerRecipeConditionSerializer("mod_loaded", ModLoadedRecipeCondition.SERIALIZER);

        // Register generator for default tags
        GeneratorRegistrationHandler.get("supermartijn642corelib").addGenerator(cache -> new CoreLibMiningTagGenerator("supermartijn642corelib", cache));
    }

    private void onConstructMod(FMLConstructModEvent e){
        RegistryEntryAcceptor.Handler.gatherAnnotatedFields();
    }
}
