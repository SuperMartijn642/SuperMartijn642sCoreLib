package com.supermartijn642.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.supermartijn642.core.gui.CursorTypes;
import com.supermartijn642.core.gui.ScreenUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 15/01/2026 by SuperMartijn642
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;flush()V",
            shift = At.Shift.AFTER
        )
    )
    private void changeCursor(float f, long l, boolean bl, CallbackInfo ci){
        CursorTypes.applyPending();
    }

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/Screen;renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            shift = At.Shift.BEFORE
        )
    )
    private void beforeDrawScreen(float f, long l, boolean bl, CallbackInfo ci, @Local GuiGraphics guiGraphics) {
        ScreenUtils.bufferSourceOverwrite = guiGraphics.bufferSource;
    }

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/Screen;renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            shift = At.Shift.AFTER
        )
    )
    private void afterDrawScreen(float f, long l, boolean bl, CallbackInfo ci, @Local GuiGraphics guiGraphics) {
        ScreenUtils.bufferSourceOverwrite = null;
    }
}
