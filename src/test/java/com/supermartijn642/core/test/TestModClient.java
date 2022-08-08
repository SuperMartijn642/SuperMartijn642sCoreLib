package com.supermartijn642.core.test;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Created 1/22/2021 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TestModClient {

    @SubscribeEvent
    public static void onBlockBreak(LivingEvent.LivingJumpEvent e){
        if(e.getEntity().level.isClientSide)
            ClientUtils.displayScreen(WidgetScreen.of(new TestScreen()));
    }

    @SubscribeEvent
    public static void onDrawSelection(DrawBlockHighlightEvent e){
        Vec3d camera = RenderUtils.getCameraPosition();
        GlStateManager.pushMatrix();
        GlStateManager.translated(-camera.x, -camera.y, -camera.z);
        RenderUtils.renderShape(BlockShape.fullCube(), 1, 1, 0, 0.5f, false);
        RenderUtils.renderShapeSides(BlockShape.fullCube(), 0, 1, 1, 0.5f, false);
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderWorldEvent e){
        Vec3d camera = RenderUtils.getCameraPosition();
        GlStateManager.pushMatrix();
        GlStateManager.translated(-camera.x, -camera.y, -camera.z);
        RenderUtils.renderShape(BlockShape.fullCube(), 1, 1, 0, 0.5f, false);
        RenderUtils.renderShapeSides(BlockShape.fullCube(), 0, 1, 1, 0.5f, false);
        GlStateManager.popMatrix();
    }
}
