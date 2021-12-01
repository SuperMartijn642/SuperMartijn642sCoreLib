package com.supermartijn642.core.test;

import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
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
    public static void onDrawSelection(DrawBlockHighlightEvent e){
//        Vec3d camera = RenderUtils.getCameraPosition();
//        GlStateManager.pushMatrix();
//        GlStateManager.translate(-camera.x, -camera.y, -camera.z);
//        RenderUtils.disableDepthTest();
//        RenderUtils.renderShape(BlockShape.fullCube(), 1, 1, 0, 0.5f);
//        RenderUtils.renderShapeSides(BlockShape.fullCube(), 0, 1, 1, 0.5f);
//        RenderUtils.resetState();
//        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderWorldEvent e){
        Vec3d camera = RenderUtils.getCameraPosition();
        GlStateManager.pushMatrix();
        GlStateManager.translate(-camera.x, -camera.y, -camera.z);
        RenderUtils.disableDepthTest();
        RenderUtils.renderShape(BlockShape.fullCube(), 1, 1, 0, 0.5f);
        RenderUtils.renderShapeSides(BlockShape.fullCube(), 0, 1, 1, 0.5f);
        RenderUtils.resetState();
        GlStateManager.popMatrix();
    }
}
