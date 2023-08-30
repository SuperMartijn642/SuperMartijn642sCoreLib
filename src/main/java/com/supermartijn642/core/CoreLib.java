package com.supermartijn642.core;

import com.supermartijn642.core.data.condition.*;
import com.supermartijn642.core.data.recipe.ConditionalRecipeSerializer;
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
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.entrypoint.EntrypointStorage;
import net.fabricmc.loader.impl.util.DefaultLanguageAdapter;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.plugin.ModContainerExt;
import org.quiltmc.loader.impl.QuiltLoaderImpl;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Created 18/03/2022 by SuperMartijn642
 */
public class CoreLib implements ModInitializer, PreLaunchEntrypoint {

    public static final Logger LOGGER = CommonUtils.getLogger("supermartijn642corelib");

    public static boolean isArchitecturyLoaded = false;

    @Override
    public void onInitialize(){
        isArchitecturyLoaded = CommonUtils.isModLoaded("architectury");
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

    @Override
    public void onPreLaunch(){
        try{
            // Check if Quilt loader is present
            boolean quiltLoaded = false;
            try{
                Class.forName("org.quiltmc.loader.impl.QuiltLoaderImpl");
                quiltLoaded = true;
            }catch(ClassNotFoundException ignore){}

            if(quiltLoaded){ // Quilt loader
                Field storageField = QuiltLoaderImpl.class.getDeclaredField("entrypointStorage");
                storageField.setAccessible(true);
                Class<?> entrypointStorageClass = Class.forName("org.quiltmc.loader.impl.entrypoint.EntrypointStorage");
                Field entriesField = entrypointStorageClass.getDeclaredField("entryMap");
                entriesField.setAccessible(true);
                Class<?> entryClass = Class.forName("org.quiltmc.loader.impl.entrypoint.EntrypointStorage$NewEntry");
                Constructor<?> entryConstructor = entryClass.getDeclaredConstructors()[0];
                entryConstructor.setAccessible(true);

                // Get the entrypoint map
                Object storage = storageField.get(QuiltLoaderImpl.INSTANCE);
                //noinspection unchecked
                Map<String,List<Object>> entries = (Map<String,List<Object>>)entriesField.get(storage);

                // Get the core lib mod container
                //noinspection OptionalGetWithoutIsPresent
                ModContainerExt coreLibContainer = QuiltLoader.getModContainer("supermartijn642corelib").map(ModContainerExt.class::cast).get();
                org.quiltmc.loader.api.LanguageAdapter languageAdapter = org.quiltmc.loader.impl.util.DefaultLanguageAdapter.INSTANCE;

                // Add 'main' entrypoint
                Object mainEntrypoint = entryConstructor.newInstance(coreLibContainer, languageAdapter, RegistryEntryPoints.class.getCanonicalName());
                entries.get("main").add(mainEntrypoint);
                // Add 'client' entrypoint
                Object clientEntrypoint = entryConstructor.newInstance(coreLibContainer, languageAdapter, RegistryEntryPoints.class.getCanonicalName());
                entries.get("client").add(clientEntrypoint);
            }else{ // Fabric loader
                Field storageField = FabricLoaderImpl.class.getDeclaredField("entrypointStorage");
                storageField.setAccessible(true);
                Field entriesField = EntrypointStorage.class.getDeclaredField("entryMap");
                entriesField.setAccessible(true);
                Class<?> entryClass = Class.forName("net.fabricmc.loader.impl.entrypoint.EntrypointStorage$NewEntry");
                Constructor<?> entryConstructor = entryClass.getDeclaredConstructor(ModContainerImpl.class, LanguageAdapter.class, String.class);
                entryConstructor.setAccessible(true);

                // Get the entrypoint map
                EntrypointStorage storage = (EntrypointStorage)storageField.get(FabricLoaderImpl.INSTANCE);
                //noinspection unchecked
                Map<String,List<Object>> entries = (Map<String,List<Object>>)entriesField.get(storage);

                // Get the core lib mod container
                //noinspection OptionalGetWithoutIsPresent
                ModContainerImpl coreLibContainer = FabricLoader.getInstance().getModContainer("supermartijn642corelib").map(ModContainerImpl.class::cast).get();
                LanguageAdapter languageAdapter = DefaultLanguageAdapter.INSTANCE;

                // Add 'main' entrypoint
                Object mainEntrypoint = entryConstructor.newInstance(coreLibContainer, languageAdapter, RegistryEntryPoints.class.getCanonicalName());
                entries.get("main").add(mainEntrypoint);
                // Add 'client' entrypoint
                Object clientEntrypoint = entryConstructor.newInstance(coreLibContainer, languageAdapter, RegistryEntryPoints.class.getCanonicalName());
                entries.get("client").add(clientEntrypoint);
            }
        }catch(Exception e){
            throw new RuntimeException("Failed to apply Core Lib registry entry points!", e);
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
