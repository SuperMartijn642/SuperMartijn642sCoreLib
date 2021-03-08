package com.supermartijn642.core;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created 1/22/2021 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Side.CLIENT)
public class ClientProxy {

    @SubscribeEvent
    public static void onBlockBreak(PlayerEvent.ItemPickupEvent e){
        Minecraft.getMinecraft().displayGuiScreen(new TestScreen());
    }
}
