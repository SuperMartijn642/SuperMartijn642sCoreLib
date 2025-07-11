package com.supermartijn642.core.mixin;

import com.supermartijn642.core.gui.ArbitraryPictureInPictureRenderer;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.neoforged.neoforge.client.gui.PictureInPictureRendererPool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 11/07/2025 by SuperMartijn642
 */
@SuppressWarnings("UnstableApiUsage")
@Mixin(PictureInPictureRendererPool.class)
public class PictureInPictureRendererPoolMixin {

    @SuppressWarnings({"FieldMayBeFinal", "MismatchedQueryAndUpdateOfCollection"})
    @Shadow
    private Object2ObjectMap<?,PictureInPictureRenderer<?>> renderersLastFrame = new Object2ObjectOpenHashMap<>();

    @Unique
    private boolean checkedType, isCoreLibRenderer;

    @Inject(
        method = "clearUnusedRenderers",
        at = @At("TAIL")
    )
    public void clearUnusedRenderers(CallbackInfo ci){
        if(!this.checkedType){
            if(this.renderersLastFrame.isEmpty())
                return;
            //noinspection resource
            this.isCoreLibRenderer = this.renderersLastFrame.values().iterator().next() instanceof ArbitraryPictureInPictureRenderer;
            this.checkedType = true;
        }
        if(this.isCoreLibRenderer){
            for(PictureInPictureRenderer<?> renderer : this.renderersLastFrame.values())
                ((ArbitraryPictureInPictureRenderer)renderer).afterFrame();
        }
    }
}
