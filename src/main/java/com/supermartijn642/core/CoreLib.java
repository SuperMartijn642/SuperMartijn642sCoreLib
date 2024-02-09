package com.supermartijn642.core;

import com.supermartijn642.core.data.condition.*;
import com.supermartijn642.core.data.recipe.ConditionalRecipeSerializer;
import com.supermartijn642.core.data.tag.CustomTagEntryLoader;
import com.supermartijn642.core.data.tag.entries.NamespaceTagEntry;
import com.supermartijn642.core.generator.standard.CoreLibAccessWidenerGenerator;
import com.supermartijn642.core.generator.standard.CoreLibMiningTagGenerator;
import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.BaseItem;
import com.supermartijn642.core.registry.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

/**
 * Created 18/03/2022 by SuperMartijn642
 */
public class CoreLib implements ModInitializer {

    public static final Logger LOGGER = CommonUtils.getLogger("supermartijn642corelib");

    public static boolean isArchitecturyLoaded = false;

    @Override
    public void onInitialize(){
        isArchitecturyLoaded = CommonUtils.isModLoaded("architectury");
        CommonUtils.initialize();
        CustomTagEntryLoader.init();

        // Register conditional recipe type
        RegistrationHandler handler = RegistrationHandler.get("supermartijn642corelib");
        handler.registerRecipeSerializer("conditional", ConditionalRecipeSerializer.INSTANCE);
        handler.registerResourceConditionSerializer("mod_loaded", ModLoadedResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("not", NotResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("or", OrResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("and", AndResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("tag_populated", TagPopulatedResourceCondition.SERIALIZER);

        // Register custom tag entry types
        handler.registerCustomTagEntrySerializer("namespace", NamespaceTagEntry.SERIALIZER);

        // Register generator for default tags
        GeneratorRegistrationHandler.get("supermartijn642corelib").addGenerator(cache -> new CoreLibMiningTagGenerator("supermartijn642corelib", cache));
        // Register generator for access widener entries
        GeneratorRegistrationHandler.get("supermartijn642corelib").addGenerator(cache -> new CoreLibAccessWidenerGenerator("supermartijn642corelib", cache));

        // Add all BaseItem instances to their respective creative tabs
        ItemGroupEvents.MODIFY_ENTRIES_ALL.register(((group, entries) -> {
            Registries.ITEMS.getValues().stream()
                .filter(item -> item instanceof BaseItem || item instanceof BaseBlockItem)
                .filter(item -> item instanceof BaseItem ? ((BaseItem)item).isInCreativeGroup(group) : ((BaseBlockItem)item).isInCreativeGroup(group))
                .forEach(entries::accept);
        }));

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
     * Called right after all {@link ModInitializer}s have been initialized.
     */
    public static void afterInitialize(){
        RegistrationHandler.registerInternal();
        RegistryEntryAcceptor.Handler.reportMissing();
    }

    /**
     * Called right after all {@link ClientModInitializer}'s have been initialized.
     */
    public static void afterInitializeClient(){
        ClientRegistrationHandler.registerRenderersInternal();
    }

    /**
     * Called right after all {@link ModInitializer}s, {@link ClientModInitializer}'s, and {@link DedicatedServerModInitializer}s have been initialized.
     */
    public static void afterInitializeAll(){
    }
}
