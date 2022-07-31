package com.supermartijn642.core.mixin;

import com.google.common.collect.Maps;
import com.supermartijn642.core.extensions.EntityRendererManagerExtension;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Created 31/07/2022 by SuperMartijn642
 */
@Mixin(EntityRendererManager.class)
public class EntityRendererManagerMixin implements EntityRendererManagerExtension {

    private final Map<EntityType<?>,EntityRenderer<?>> coreLibRenderers = Maps.newHashMap();

    @Inject(
        method = "getRenderer(Lnet/minecraft/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void getRenderer(Entity entity, CallbackInfoReturnable<EntityRenderer<?>> ci){
        EntityRenderer<?> renderer = this.coreLibRenderers.get(entity.getType());
        if(renderer != null)
            ci.setReturnValue(renderer);
    }

    @Override
    public <T extends Entity> void coreLibRegisterRenderer(EntityType<T> entityType, EntityRenderer<? super T> renderer){
        this.coreLibRenderers.put(entityType, renderer);
    }
}
