package com.supermartijn642.core.extensions;

import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.core.block.BaseBlockEntityType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

/**
 * Created 31/07/2022 by SuperMartijn642
 */
public interface TileEntityRendererDispatcherExtension {

    <T extends BaseBlockEntity> void coreLibRegisterRenderer(BaseBlockEntityType<T> entityType, TileEntitySpecialRenderer<? super T> renderer);

}
