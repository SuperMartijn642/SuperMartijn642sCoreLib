package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Created 03/10/2025 by SuperMartijn642
 */
public class MutableCustomBlockEntityRendererContext<S> extends BlockEntityRenderState implements CustomBlockEntityRenderer.UpdateContext, CustomBlockEntityRenderer.RenderContext {

    private S state;
    private PoseStack poseStack;
    private CameraRenderState cameraRenderState;
    private float partialTicks;
    private RandomSource randomSource;

    MutableCustomBlockEntityRendererContext(){
    }

    public void setForUpdate(BlockEntity entity, float partialTicks, Vec3 camera, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay){
        extractBase(entity, this, crumblingOverlay);
        this.partialTicks = partialTicks;
        if(this.cameraRenderState == null)
            this.cameraRenderState = new CameraRenderState();
        this.cameraRenderState.pos = camera;
        this.breakProgress = crumblingOverlay;
    }

    public void setForSubmit(PoseStack poseStack, CameraRenderState cameraRenderState){
        this.poseStack = poseStack;
        this.cameraRenderState = cameraRenderState;
    }

    public void setState(S state){
        this.state = state;
    }

    public S getState(){
        return this.state;
    }

    @Override
    public PoseStack poseStack(){
        return this.poseStack;
    }

    @Override
    public CameraRenderState cameraRenderState(){
        return this.cameraRenderState;
    }

    @Override
    public BlockPos pos(){
        return this.blockPos;
    }

    @Override
    public BlockState blockState(){
        return this.blockState;
    }

    @Override
    public BlockEntityType<?> blockEntityType(){
        return this.blockEntityType;
    }

    @Override
    public int packedLight(){
        return this.lightCoords;
    }

    @Override
    public BlockEntityRenderState blockEntityRenderState(){
        return this;
    }

    @Override
    public float partialTicks(){
        return this.partialTicks;
    }

    @Override
    public Vec3 cameraPos(){
        return this.cameraRenderState.pos;
    }

    @Override
    public @Nullable ModelFeatureRenderer.CrumblingOverlay breakingOverlay(){
        return this.breakProgress;
    }

    @Override
    public RandomSource randomSource(long seed){
        if(this.randomSource == null)
            this.randomSource = RandomSource.create(seed);
        else
            this.randomSource.setSeed(seed);
        return this.randomSource;
    }
}
