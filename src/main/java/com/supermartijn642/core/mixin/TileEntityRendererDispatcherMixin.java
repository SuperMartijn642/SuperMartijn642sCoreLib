package com.supermartijn642.core.mixin;

import com.google.common.collect.Maps;
import com.supermartijn642.core.extensions.TileEntityRendererDispatcherExtension;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Created 31/07/2022 by SuperMartijn642
 */
@Mixin(TileEntityRendererDispatcher.class)
public class TileEntityRendererDispatcherMixin implements TileEntityRendererDispatcherExtension {

    private final Map<TileEntityType<?>,TileEntityRenderer<?>> coreLibRenderers = Maps.newHashMap();

    @Inject(
        method = "getRenderer(Lnet/minecraft/tileentity/TileEntity;)Lnet/minecraft/client/renderer/tileentity/TileEntityRenderer;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void getRenderer(TileEntity entity, CallbackInfoReturnable<TileEntityRenderer<?>> ci){
        if(entity != null && !entity.isRemoved()){
            TileEntityRenderer<?> renderer = this.coreLibRenderers.get(entity.getType());
            if(renderer != null)
                ci.setReturnValue(renderer);
        }
    }

    @Override
    public <T extends TileEntity> void coreLibRegisterRenderer(TileEntityType<T> entityType, TileEntityRenderer<? super T> renderer){
        this.coreLibRenderers.put(entityType, renderer);
    }
}
