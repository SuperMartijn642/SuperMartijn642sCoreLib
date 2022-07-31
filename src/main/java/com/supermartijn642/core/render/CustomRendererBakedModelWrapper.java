package com.supermartijn642.core.render;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraftforge.client.model.BakedModelWrapper;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;

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
    public Pair<? extends IBakedModel,Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType){
        return Pair.of(this, super.handlePerspective(cameraTransformType).getRight());
    }
}
