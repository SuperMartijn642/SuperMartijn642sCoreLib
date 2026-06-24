package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Created 05/10/2025 by SuperMartijn642
 */
public class MutableCustomItemRendererContext implements CustomItemRenderer.RenderContext{

    private PoseStack poseStack;
    private int packedLight, packedOverlay;

    MutableCustomItemRendererContext(){
    }

    public void set(PoseStack poseStack, int packedLight, int packedOverlay){
        this.poseStack = poseStack;
        this.packedLight = packedLight;
        this.packedOverlay = packedOverlay;
    }

    @Override
    public ItemDisplayContext displayContext(){
        return null;
    }

    @Override
    public PoseStack poseStack(){
        return null;
    }

    @Override
    public int packedLight(){
        return 0;
    }

    @Override
    public int packedBreakingOverlay(){
        return 0;
    }
}
