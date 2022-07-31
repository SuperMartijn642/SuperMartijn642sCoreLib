package com.supermartijn642.core.extensions;

import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

/**
 * Created 31/07/2022 by SuperMartijn642
 */
public interface TileEntityRendererDispatcherExtension {

    <T extends TileEntity> void coreLibRegisterRenderer(TileEntityType<T> entityType, TileEntityRenderer<? super T> renderer);

}
