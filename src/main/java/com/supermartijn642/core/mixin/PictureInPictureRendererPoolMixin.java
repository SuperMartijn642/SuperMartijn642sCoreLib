package com.supermartijn642.core.mixin;

import com.supermartijn642.core.gui.ArbitraryPictureInPictureRenderer;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.neoforged.neoforge.client.gui.PictureInPictureRendererPool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 11/07/2025 by SuperMartijn642
 */
@SuppressWarnings("UnstableApiUsage")
@Mixin(PictureInPictureRendererPool.class)
public class PictureInPictureRendererPoolMixin {

    @SuppressWarnings({"FieldMayBeFinal", "MismatchedQueryAndUpdateOfCollection"})
    @Shadow
    private Object2ObjectMap<Object,PictureInPictureRenderer<?>> renderersLastFrame = new Object2ObjectOpenHashMap<>();
    @SuppressWarnings({"FieldMayBeFinal", "MismatchedQueryAndUpdateOfCollection"})
    private Object2ObjectMap<Object,PictureInPictureRenderer<?>> renderersThisFrame = new Object2ObjectOpenHashMap<>();

    @Unique
    private boolean checkedType, isCoreLibRenderer;

    @Inject(
        method = "get",
        at = @At("HEAD"),
        cancellable = true
    )
    private void get(PictureInPictureRenderState state, int guiScale, boolean firstPass, CallbackInfoReturnable<PictureInPictureRenderer<?>> ci){
        if(!this.checkType())
            return;
        if(!this.renderersThisFrame.isEmpty()){
            ci.setReturnValue(this.renderersThisFrame.values().iterator().next());
            return;
        }
        if(!this.renderersLastFrame.isEmpty()){
            ObjectIterator<PictureInPictureRenderer<?>> iterator = this.renderersLastFrame.values().iterator();
            PictureInPictureRenderer<?> renderer = iterator.next();
            iterator.remove();
            this.renderersThisFrame.put(state, renderer);
            ci.setReturnValue(renderer);
        }
    }

    @Inject(
        method = "clearUnusedRenderers",
        at = @At("TAIL")
    )
    private void clearUnusedRenderers(CallbackInfo ci){
        if(this.checkType()){
            for(PictureInPictureRenderer<?> renderer : this.renderersLastFrame.values())
                ((ArbitraryPictureInPictureRenderer)renderer).afterFrame();
        }
    }

    @Unique
    private boolean checkType(){
        if(!this.checkedType){
            if(this.renderersLastFrame.isEmpty())
                return false;
            //noinspection resource
            this.isCoreLibRenderer = this.renderersLastFrame.values().iterator().next() instanceof ArbitraryPictureInPictureRenderer;
            this.checkedType = true;
        }
        return this.isCoreLibRenderer;
    }
}
