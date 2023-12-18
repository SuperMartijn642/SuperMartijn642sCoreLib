package com.supermartijn642.core.test;

import com.supermartijn642.core.item.BaseItem;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import net.minecraft.world.item.Item;
import net.neoforged.fml.common.Mod;

/**
 * Created 1/23/2021 by SuperMartijn642
 */
@Mod("corelibtestmod")
public class TestMod {

    @RegistryEntryAcceptor(namespace = "corelibtestmod", identifier = "test_item", registry = RegistryEntryAcceptor.Registry.ITEMS)
    public static Item test_item;

    public TestMod(){
        RegistrationHandler handler = RegistrationHandler.get("corelibtestmod");
        handler.registerItem("test_item", () -> new BaseItem(ItemProperties.create()));
    }
}
