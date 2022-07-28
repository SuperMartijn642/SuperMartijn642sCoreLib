package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.model.BakedModelWrapper;

/**
 * Created 25/07/2022 by SuperMartijn642
 */
public final class CustomRendererBakedModelWrapper extends BakedModelWrapper<BakedModel> {

    public static BakedModel wrap(BakedModel originalModel){
        return new CustomRendererBakedModelWrapper(originalModel);
    }

    private CustomRendererBakedModelWrapper(BakedModel originalModel){
        super(originalModel);
    }

    @Override
    public boolean isCustomRenderer(){
        return true;
    }

    @Override
    public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack poseStack){
        super.handlePerspective(cameraTransformType, poseStack);
        return this;
    }
}
