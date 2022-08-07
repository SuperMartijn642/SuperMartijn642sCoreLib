package com.supermartijn642.core.mixin;

import com.google.common.collect.Maps;
import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.extensions.TileEntityRendererDispatcherExtension;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
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

    private final Map<BaseBlockEntityType<?>,TileEntitySpecialRenderer<?>> coreLibRenderers = Maps.newHashMap();

    @Inject(
        method = "getRenderer(Lnet/minecraft/tileentity/TileEntity;)Lnet/minecraft/client/renderer/tileentity/TileEntitySpecialRenderer;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void getRenderer(TileEntity entity, CallbackInfoReturnable<TileEntitySpecialRenderer<?>> ci){
        if(entity instanceof BaseBlockEntity && !entity.isInvalid()){
            TileEntitySpecialRenderer<?> renderer = this.coreLibRenderers.get(((BaseBlockEntity)entity).getType());
            if(renderer != null)
                ci.setReturnValue(renderer);
        }
    }

    @Override
    public <T extends BaseBlockEntity> void coreLibRegisterRenderer(BaseBlockEntityType<T> entityType, TileEntitySpecialRenderer<? super T> renderer){
        this.coreLibRenderers.put(entityType, renderer);
    }
}
