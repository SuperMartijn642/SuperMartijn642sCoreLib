package com.supermartijn642.core.mixin;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.CursorTypes;
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
        at = @At("TAIL")
    )
    private void changeCursor(float f, long l, boolean bl, CallbackInfo ci){
        if(!ClientUtils.getMinecraft().noRender)
            CursorTypes.applyPending();
    }
}
