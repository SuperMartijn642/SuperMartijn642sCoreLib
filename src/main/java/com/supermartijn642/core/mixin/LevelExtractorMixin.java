package com.supermartijn642.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Created 04/07/2026 by SuperMartijn642
 */
@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {

    @Final
    @Shadow
    private LevelRenderState levelRenderState;

    @Inject(
        method = "extract(Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/Camera;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/extract/LevelExtractor;extractBlockOutline(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V",
            shift = At.Shift.AFTER
        )
    )
    private void renderLevel(CallbackInfo ci, @Local Camera camera){
        List<BiConsumer<PoseStack,SubmitNodeCollector>> submitters = new ArrayList<>();
        RenderWorldEvent.EVENT.invoker().accept(new RenderWorldEvent(ClientUtils.getPartialTicks(), camera, this.levelRenderState, submitters::add));
        this.levelRenderState.setData(RenderWorldEvent.DATA_KEY, submitters);
    }
}
