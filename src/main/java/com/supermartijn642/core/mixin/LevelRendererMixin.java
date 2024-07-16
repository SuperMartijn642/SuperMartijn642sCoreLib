package com.supermartijn642.core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 17/11/2021 by SuperMartijn642
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Unique
    private static final PoseStack POSE_STACK = new PoseStack();

    @Inject(method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Options;getCloudsType()Lnet/minecraft/client/CloudStatus;"
        )
    )
    public void renderLevel(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci){
        NeoForge.EVENT_BUS.post(new RenderWorldEvent(POSE_STACK, deltaTracker.getGameTimeDeltaPartialTick(false)));
        if(!POSE_STACK.clear())
            throw new IllegalStateException("Pose stack was not cleared properly during RenderWorldEvent!");
    }
}
