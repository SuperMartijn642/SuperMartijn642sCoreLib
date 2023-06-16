package com.supermartijn642.core;

import com.supermartijn642.core.data.condition.*;
import com.supermartijn642.core.data.recipe.ConditionalRecipeSerializer;
import com.supermartijn642.core.generator.standard.CoreLibAccessWidenerGenerator;
import com.supermartijn642.core.generator.standard.CoreLibMiningTagGenerator;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
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

        // Register conditional recipe type
        RegistrationHandler handler = RegistrationHandler.get("supermartijn642corelib");
        handler.registerRecipeSerializer("conditional", ConditionalRecipeSerializer.INSTANCE);
        handler.registerResourceConditionSerializer("mod_loaded", ModLoadedResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("not", NotResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("or", OrResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("and", AndResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("tag_populated", TagPopulatedResourceCondition.SERIALIZER);

        // Register generator for default tags
        GeneratorRegistrationHandler.get("supermartijn642corelib").addGenerator(cache -> new CoreLibMiningTagGenerator("supermartijn642corelib", cache));
        // Register generator for access widener entries
        GeneratorRegistrationHandler.get("supermartijn642corelib").addGenerator(cache -> new CoreLibAccessWidenerGenerator("supermartijn642corelib", cache));

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
