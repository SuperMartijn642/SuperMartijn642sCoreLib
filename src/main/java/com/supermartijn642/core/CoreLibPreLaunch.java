package com.supermartijn642.core;

import com.supermartijn642.core.registry.RegistryEntryPoints;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Created 01/09/2023 by SuperMartijn642
 */
public class CoreLibPreLaunch implements PreLaunchEntrypoint {

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
}
