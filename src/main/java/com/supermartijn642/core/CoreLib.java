package com.supermartijn642.core;

import com.google.common.reflect.Reflection;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Created 18/03/2022 by SuperMartijn642
 */
public class CoreLib implements ModInitializer {

    @Override
    public void onInitialize(){
        Reflection.initialize(CommonUtils.class);

        // Load test mod stuff
        if(FabricLoader.getInstance().isDevelopmentEnvironment()){
            ModInitializer testMod = null;
            try{
                Class<?> testModClass = Class.forName("com.supermartijn642.core.test.TestMod");
                testMod = (ModInitializer)testModClass.getConstructors()[0].newInstance();
            }catch(Exception e){
                e.printStackTrace();
            }
            if(testMod != null)
                testMod.onInitialize();
        }
    }
}
