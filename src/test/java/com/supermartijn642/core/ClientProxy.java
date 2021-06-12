package com.supermartijn642.core;

import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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

    @SubscribeEvent
    public static void onWorldRender(RenderWorldLastEvent e){
        RenderUtils.renderShape(BlockShape.fullCube(), 0, 5, 0, 1, 1, 0, 0.5f);
    }
}
