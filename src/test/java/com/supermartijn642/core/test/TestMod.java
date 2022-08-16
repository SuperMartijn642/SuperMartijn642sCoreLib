package com.supermartijn642.core.test;

import com.google.common.reflect.Reflection;
import com.supermartijn642.core.item.BaseItem;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.Item;

/**
 * Created 1/23/2021 by SuperMartijn642
 */
public class TestMod implements ModInitializer {

    @RegistryEntryAcceptor(namespace = "corelibtestmod", identifier = "test_item", registry = RegistryEntryAcceptor.Registry.ITEMS)
    public static Item test_item;

    @Override
    public void onInitialize(){
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
            Reflection.initialize(TestModClient.class);

        RegistrationHandler handler = RegistrationHandler.get("corelibtestmod");
        handler.registerItem("test_item", () -> new BaseItem(ItemProperties.create()));
    }
}
