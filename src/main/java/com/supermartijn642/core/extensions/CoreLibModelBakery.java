package com.supermartijn642.core.extensions;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * Created 21/12/2024 by SuperMartijn642
 */
public interface CoreLibModelBakery {

    Function<BakedModel,BakedModel> supermartijn642corelibGetModelOverwrite(ResourceLocation location);
}
