package com.supermartijn642.core.test;

import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Created 1/22/2021 by SuperMartijn642
 */
public class ClientProxy {

    static{
        WorldRenderEvents.BLOCK_OUTLINE.register(ClientProxy::onDrawSelection);
        AttackBlockCallback.EVENT.register(ClientProxy::onBlockBreak);
    }

    public static InteractionResult onBlockBreak(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction){
        if(world.isClientSide)
            Minecraft.getInstance().setScreen(new TestScreen());
        return InteractionResult.PASS;
    }

    public static boolean onDrawSelection(WorldRenderContext renderContext, WorldRenderContext.BlockOutlineContext outlineContext){
        Vec3 camera = RenderUtils.getCameraPosition();
        renderContext.matrixStack().pushPose();
        renderContext.matrixStack().translate(-camera.x, -camera.y, -camera.z);
        RenderUtils.disableDepthTest();
        RenderUtils.renderShape(renderContext.matrixStack(), BlockShape.fullCube(), 1, 1, 0, 0.5f);
        RenderUtils.renderShapeSides(renderContext.matrixStack(), BlockShape.fullCube(), 0, 1, 1, 0.5f);
        RenderUtils.resetState();
        renderContext.matrixStack().popPose();
        return true;
    }

    public static void onRenderWorld(RenderWorldEvent e){
        Vec3 camera = RenderUtils.getCameraPosition();
        e.getPoseStack().pushPose();
        e.getPoseStack().translate(-camera.x, -camera.y, -camera.z);
        RenderUtils.disableDepthTest();
        RenderUtils.renderShape(e.getPoseStack(), BlockShape.fullCube(), 1, 1, 0, 0.5f);
        RenderUtils.renderShapeSides(e.getPoseStack(), BlockShape.fullCube(), 0, 1, 1, 0.5f);
        RenderUtils.resetState();
        e.getPoseStack().popPose();
    }
}
