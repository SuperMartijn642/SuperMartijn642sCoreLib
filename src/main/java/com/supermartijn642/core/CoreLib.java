package com.supermartijn642.core;

import com.supermartijn642.core.data.TagLoader;
import com.supermartijn642.core.data.condition.*;
import com.supermartijn642.core.generator.GeneratorManager;
import com.supermartijn642.core.generator.standard.CoreLibMiningTagGenerator;
import com.supermartijn642.core.loot_table.SurvivesExplosionLootCondition;
import com.supermartijn642.core.loot_table.ToolMatchLootCondition;
import com.supermartijn642.core.network.OpenContainerPacket;
import com.supermartijn642.core.network.PacketChannel;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
@Mod(modid = "@mod_id@", name = "@mod_name@", version = "@mod_version@")
public class CoreLib {

    public static final Logger LOGGER = LogManager.getLogger("supermartijn642corelib");

    public static final PacketChannel CHANNEL = PacketChannel.create("supermartijn642corelib");

    public CoreLib(){
        CHANNEL.registerMessage(OpenContainerPacket.class, OpenContainerPacket::new, true);

        CommonUtils.initialize();

        // Register conditional recipe type
        RegistrationHandler handler = RegistrationHandler.get("supermartijn642corelib");
        handler.registerResourceConditionSerializer("mod_loaded", ModLoadedResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("not", NotResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("or", OrResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("and", AndResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("ore_dict_populated", OreDictPopulatedResourceCondition.SERIALIZER);

        // Register loot condition
        LootConditionManager.registerCondition(SurvivesExplosionLootCondition.SERIALIZER);
        LootConditionManager.registerCondition(ToolMatchLootCondition.SERIALIZER);

        // Register generator for default tags
        GeneratorRegistrationHandler.get("supermartijn642corelib").addGenerator(cache -> new CoreLibMiningTagGenerator("supermartijn642corelib", cache));
    }

    @Mod.EventHandler
    private static void onPreInitialization(FMLPreInitializationEvent e){
        RegistryEntryAcceptor.Handler.gatherAnnotatedFields(e.getAsmData());
    }

    @Mod.EventHandler
    private static void onInitialization(FMLInitializationEvent e){
        if(CommonUtils.getEnvironmentSide().isClient())
            ClientRegistrationHandler.registerAllRenderers();
    }

    @Mod.EventHandler
    private static void onLoadComplete(FMLLoadCompleteEvent e){
        // Load all tags
        TagLoader.loadTags();

        // Run generators
        String generatorModid = System.getProperty("--generatorModid");
        if(generatorModid != null){
            String outputDirectory = System.getProperty("--output");
            if(outputDirectory == null)
                throw new RuntimeException("Missing property '--output' for the generator output directory!");
            String existingDirectory = System.getProperty("--existing");
            if(existingDirectory == null)
                throw new RuntimeException("Missing property '--existing' for the generator existing files directory!");

            GeneratorManager.gatherAndRunGenerators(generatorModid, outputDirectory, existingDirectory);
            FMLCommonHandler.instance().exitJava(1, false);
        }
    }
}
