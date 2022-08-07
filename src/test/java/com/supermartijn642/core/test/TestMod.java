package com.supermartijn642.core.test;

import com.supermartijn642.core.item.BaseItem;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;

/**
 * Created 1/23/2021 by SuperMartijn642
 */
@Mod(modid = TestMod.MODID, name = TestMod.NAME, version = TestMod.VERSION)
public class TestMod {

    public static final String MODID = "corelibtestmod";
    public static final String NAME = "SuperMartijn642's Core Lib Test Mod";
    public static final String VERSION = "1.0.0";

    @RegistryEntryAcceptor(namespace = "corelibtestmod", identifier = "test_item", registry = RegistryEntryAcceptor.Registry.ITEMS)
    public static Item test_item;

    public TestMod(){
        RegistrationHandler handler = RegistrationHandler.get("corelibtestmod");
        handler.registerItem("test_item", () -> new BaseItem(ItemProperties.create()));
        ClientRegistrationHandler.get("corelibtestmod").registerCustomItemRenderer(() -> test_item, () -> (itemStack) -> {

        });
    }
}
