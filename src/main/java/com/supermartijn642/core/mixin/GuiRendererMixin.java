package com.supermartijn642.core.mixin;

import com.supermartijn642.core.gui.ArbitraryPictureInPictureRenderer;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Created 08/07/2025 by SuperMartijn642
 */
@Mixin(GuiRenderer.class)
public class GuiRendererMixin {

    @Final
    @Shadow
    private Map<Class<? extends PictureInPictureRenderState>,PictureInPictureRenderer<?>> pictureInPictureRenderers;

    @Inject(
        method = "render",
        at = @At("TAIL")
    )
    private void render(CallbackInfo ci){
        for(PictureInPictureRenderer<?> renderer : this.pictureInPictureRenderers.values()){
            if(renderer instanceof ArbitraryPictureInPictureRenderer)
                ((ArbitraryPictureInPictureRenderer)renderer).afterFrame();
        }
    }
}
