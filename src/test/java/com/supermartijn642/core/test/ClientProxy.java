package com.supermartijn642.core.test;

import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawHighlightEvent;
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
        Minecraft.getInstance().displayGuiScreen(new TestScreen());
    }

    @SubscribeEvent
    public static void onDrawSelection(DrawHighlightEvent e){
        Vec3d camera = RenderUtils.getCameraPosition();
        e.getMatrix().push();
        e.getMatrix().translate(-camera.x, -camera.y, -camera.z);
        RenderUtils.disableDepthTest();
        RenderUtils.renderShape(e.getMatrix(), BlockShape.fullCube(), 1, 1, 0, 0.5f);
        RenderUtils.renderShapeSides(e.getMatrix(), BlockShape.fullCube(), 0, 1, 1, 0.5f);
        RenderUtils.resetState();
        e.getMatrix().pop();
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderWorldEvent e){
        Vec3d camera = RenderUtils.getCameraPosition();
        e.getPoseStack().push();
        e.getPoseStack().translate(-camera.x, -camera.y, -camera.z);
        RenderUtils.disableDepthTest();
        RenderUtils.renderShape(e.getPoseStack(), BlockShape.fullCube(), 1, 1, 0, 0.5f);
        RenderUtils.renderShapeSides(e.getPoseStack(), BlockShape.fullCube(), 0, 1, 1, 0.5f);
        RenderUtils.resetState();
        e.getPoseStack().pop();
    }
}
