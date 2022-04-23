package com.supermartijn642.core.test;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Created 1/22/2021 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientProxy {

    @SubscribeEvent
    public static void onBlockBreak(PlayerEvent.ItemPickupEvent e){
        Minecraft.getInstance().setScreen(new TestScreen());
    }

    @SubscribeEvent
    public static void onDrawSelection(DrawBlockHighlightEvent e){
//        Vec3d camera = RenderUtils.getCameraPosition();
//        GlStateManager.pushMatrix();
//        GlStateManager.translated(-camera.x, -camera.y, -camera.z);
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
        GlStateManager.translated(-camera.x, -camera.y, -camera.z);
        RenderUtils.disableDepthTest();
        RenderUtils.renderShape(BlockShape.fullCube(), 1, 1, 0, 0.5f);
        RenderUtils.renderShapeSides(BlockShape.fullCube(), 0, 1, 1, 0.5f);
        RenderUtils.resetState();
        GlStateManager.popMatrix();
    }
}
