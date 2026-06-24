package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Created 27/07/2022 by SuperMartijn642
 */
public interface CustomBlockEntityRenderer<T extends BlockEntity, S> {

    static <T extends BlockEntity, S> BlockEntityRenderer<T,?> of(CustomBlockEntityRenderer<T,S> customRenderer){
        return new BlockEntityRenderer<T,MutableCustomBlockEntityRendererContext<S>>() {
            @Override
            public MutableCustomBlockEntityRendererContext<S> createRenderState(){
                MutableCustomBlockEntityRendererContext<S> context = new MutableCustomBlockEntityRendererContext<>();
                context.setState(customRenderer.createStateHolder());
                return context;
            }

            @Override
            public void extractRenderState(T entity, MutableCustomBlockEntityRendererContext<S> state, float partialTicks, Vec3 camera, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay){
                state.setForUpdate(entity, partialTicks, camera, crumblingOverlay);
                customRenderer.updateState(state.getState(), entity, state);
            }

            @Override
            public void submit(MutableCustomBlockEntityRendererContext<S> state, PoseStack poseStack, SubmitNodeCollector output, CameraRenderState cameraRenderState){
                state.setForSubmit(poseStack, cameraRenderState);
                customRenderer.submit(output, state.getState(), state);
            }
        };
    }

    S createStateHolder();

    void updateState(S state, T entity, UpdateContext context);

    void submit(SubmitNodeCollector output, S state, RenderContext context);

    interface UpdateContext {
        float partialTicks();

        Vec3 cameraPos();

        @Nullable
        ModelFeatureRenderer.CrumblingOverlay breakingOverlay();
    }

    interface RenderContext {
        float partialTicks();

        PoseStack poseStack();

        Vec3 cameraPos();

        CameraRenderState cameraRenderState();

        BlockPos pos();

        BlockState blockState();

        BlockEntityType<?> blockEntityType();

        int packedLight();

        @Nullable
        ModelFeatureRenderer.CrumblingOverlay breakingOverlay();

        BlockEntityRenderState blockEntityRenderState();
    }
}
