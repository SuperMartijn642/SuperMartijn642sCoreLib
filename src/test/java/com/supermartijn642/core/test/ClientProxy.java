package com.supermartijn642.core.test;

import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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
    public static void onDrawSelection(DrawSelectionEvent e){
        Vec3 camera = RenderUtils.getCameraPosition();
        e.getMatrix().pushPose();
        e.getMatrix().translate(-camera.x, -camera.y, -camera.z);
        RenderUtils.disableDepthTest();
        RenderUtils.renderShape(e.getMatrix(), BlockShape.fullCube(), 1, 1, 0, 0.5f);
        RenderUtils.renderShapeSides(e.getMatrix(), BlockShape.fullCube(), 0, 1, 1, 0.5f);
        RenderUtils.resetState();
        e.getMatrix().popPose();
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderWorldLastEvent e){
//        Vec3 camera = RenderUtils.getCameraPosition();
//        e.getMatrixStack().pushPose();
//        e.getMatrixStack().translate(-camera.x, -camera.y, -camera.z);
//        RenderUtils.disableDepthTest();
//        RenderUtils.renderShape(e.getMatrixStack(), BlockShape.fullCube(), 1, 1, 0, 0.5f);
//        RenderUtils.renderShapeSides(e.getMatrixStack(), BlockShape.fullCube(), 0, 1, 1, 0.5f);
//        RenderUtils.resetState();
//        e.getMatrixStack().popPose();
    }
}
