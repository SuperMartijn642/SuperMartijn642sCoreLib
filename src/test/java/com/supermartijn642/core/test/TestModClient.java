package com.supermartijn642.core.test;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawSelectionEvent;
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
        if(e.getEntity().getLevel().isClientSide)
            ClientUtils.displayScreen(WidgetScreen.of(new TestScreen()));
    }

    @SubscribeEvent
    public static void onDrawSelection(DrawSelectionEvent.HighlightBlock e){
        Vec3 camera = RenderUtils.getCameraPosition();
        e.getPoseStack().pushPose();
        e.getPoseStack().translate(-camera.x, -camera.y, -camera.z);
        RenderUtils.renderShape(e.getPoseStack(), BlockShape.fullCube(), 1, 1, 0, 0.5f, false);
        RenderUtils.renderShapeSides(e.getPoseStack(), BlockShape.fullCube(), 0, 1, 1, 0.5f, false);
        e.getPoseStack().popPose();
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderWorldEvent e){
        Vec3 camera = RenderUtils.getCameraPosition();
        e.getPoseStack().pushPose();
        e.getPoseStack().translate(-camera.x, -camera.y, -camera.z);
        RenderUtils.renderShape(e.getPoseStack(), BlockShape.fullCube(), 1, 1, 0, 0.5f, false);
        RenderUtils.renderShapeSides(e.getPoseStack(), BlockShape.fullCube(), 0, 1, 1, 0.5f, false);
        e.getPoseStack().popPose();
    }
}
