package com.supermartijn642.core.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraftforge.client.model.BakedModelWrapper;

/**
 * Created 25/07/2022 by SuperMartijn642
 */
public final class CustomRendererBakedModelWrapper extends BakedModelWrapper<IBakedModel> {

    public static IBakedModel wrap(IBakedModel originalModel){
        return new CustomRendererBakedModelWrapper(originalModel);
    }

    private CustomRendererBakedModelWrapper(IBakedModel originalModel){
        super(originalModel);
    }

    @Override
    public boolean isCustomRenderer(){
        return true;
    }

    @Override
    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack poseStack){
        super.handlePerspective(cameraTransformType, poseStack);
        return this;
    }
}
