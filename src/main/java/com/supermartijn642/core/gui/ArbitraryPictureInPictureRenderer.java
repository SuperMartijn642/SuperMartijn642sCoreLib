package com.supermartijn642.core.gui;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.CoreLib;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2f;

import java.util.function.Consumer;

/**
 * Created 07/07/2025 by SuperMartijn642
 */
public class ArbitraryPictureInPictureRenderer extends PictureInPictureRenderer<ArbitraryPictureInPictureRenderer.State> {

    private final PoseStack poseStack = new PoseStack();
    private int lastGuiScale;
    private int textureWidth = -1, textureHeight = -1;

    public ArbitraryPictureInPictureRenderer(MultiBufferSource.BufferSource bufferSource){
        super(bufferSource);
    }

    @Override
    public Class<State> getRenderStateClass(){
        return State.class;
    }

    @Override
    public void prepare(State state, GuiRenderState guiRenderState, int guiScale){
        // Create texture if needed
        int width = state.width * guiScale;
        int height = state.height * guiScale;
        if(this.texture == null || guiScale < this.lastGuiScale || width > this.textureWidth || height > this.textureHeight){
            this.prepareTexturesAndProjection(true, width, height);
            this.textureWidth = width;
            this.textureHeight = height;
            this.lastGuiScale = guiScale;
            CoreLib.LOGGER.info("Increasing size to {}x{}", this.textureWidth, this.textureHeight);
        }else{
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(this.texture, 0, this.depthTexture, 1);
            RenderSystem.setProjectionMatrix(this.projectionMatrixBuffer.getBuffer(this.textureWidth, this.textureHeight), ProjectionType.ORTHOGRAPHIC);
        }

        // Render to the texture
        RenderSystem.outputColorTextureOverride = this.textureView;
        RenderSystem.outputDepthTextureOverride = this.depthTextureView;
        this.poseStack.pushPose();
        this.poseStack.scale(guiScale, guiScale, -guiScale);
        this.renderToTexture(state, this.poseStack);
        this.poseStack.popPose();
        this.bufferSource.endBatch();
        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;

        // Blit texture
        guiRenderState.submitBlitToCurrentLayer(
            new BlitRenderState(
                RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
                TextureSetup.singleTexture(this.textureView),
                state.pose(),
                state.x0(),
                state.y0(),
                state.x1(),
                state.y1(),
                0,
                (float)width / this.textureWidth,
                1,
                1 - (float)height / this.textureHeight,
                -1,
                state.scissorArea(),
                state.bounds()
            )
        );
    }

    @Override
    protected void renderToTexture(State state, PoseStack poseStack){
        try{
            state.rendering.accept(poseStack);
        }catch(Exception e){
            throw new RuntimeException("Encountered an exception whilst rendering picture in picture element!", e);
        }
    }

    @Override
    protected String getTextureLabel(){
        return "SuperMartijn642's Core Library custom picture in picture rendering";
    }

    @Override
    public boolean canBeReusedFor(State state, int textureWidth, int textureHeight){
        return true;
    }

    public record State(int x, int y, int width, int height,
                        Matrix3x2f pose,
                        Consumer<PoseStack> rendering) implements PictureInPictureRenderState {

        @Override
        public int x0(){
            return this.x;
        }

        @Override
        public int x1(){
            return this.x + this.width;
        }

        @Override
        public int y0(){
            return this.y;
        }

        @Override
        public int y1(){
            return this.y + this.height;
        }

        @Override
        public float scale(){
            return 1;
        }

        @Override
        public ScreenRectangle scissorArea(){
            return null;
        }

        @Override
        public ScreenRectangle bounds(){
            return new ScreenRectangle(this.x, this.y, this.width, this.height);
        }
    }
}
