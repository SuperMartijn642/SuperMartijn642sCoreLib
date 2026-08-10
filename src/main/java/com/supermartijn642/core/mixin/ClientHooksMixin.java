package com.supermartijn642.core.mixin;

import com.supermartijn642.core.gui.ScreenUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 10/08/2026 by SuperMartijn642
 */
@Mixin(ClientHooks.class)
public class ClientHooksMixin {

    @Inject(
        method = "drawScreen",
        at = @At("HEAD")
    )
    private static void drawScreenHead(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ScreenUtils.bufferSourceOverwrite = guiGraphics.bufferSource;
    }

    @Inject(
        method = "drawScreen",
        at = @At("TAIL")
    )
    private static void drawScreenTail(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ScreenUtils.bufferSourceOverwrite = null;
    }
}
