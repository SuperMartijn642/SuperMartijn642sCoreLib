package com.supermartijn642.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.supermartijn642.core.gui.ArbitraryPictureInPictureRenderer;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
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
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/google/common/collect/ImmutableMap;builder()Lcom/google/common/collect/ImmutableMap$Builder;",
            shift = At.Shift.BEFORE,
            remap = false
        )
    )
    private void init(GuiRenderState renderState, FeatureRenderDispatcher featureRenderDispatcher, List<?> ignore, CallbackInfo ci, @Local LocalRef<List<PictureInPictureRenderer<?>>> pictureInPictureRenderers){
        List<PictureInPictureRenderer<?>> mutableRenderers = new ArrayList<>(pictureInPictureRenderers.get());
        ClientRegistrationHandler.registerPictureInPictureRenderersInternal(mutableRenderers::add);
        pictureInPictureRenderers.set(mutableRenderers);
    }

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
