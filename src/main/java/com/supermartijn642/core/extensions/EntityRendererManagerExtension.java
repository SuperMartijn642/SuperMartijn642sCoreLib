package com.supermartijn642.core.extensions;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

/**
 * Created 31/07/2022 by SuperMartijn642
 */
public interface EntityRendererManagerExtension {

    <T extends Entity> void coreLibRegisterRenderer(EntityType<T> entityType, EntityRenderer<? super T> renderer);

}
