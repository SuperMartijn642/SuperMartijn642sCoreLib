package com.supermartijn642.core.gui;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created 07/07/2025 by SuperMartijn642
 */
public class ArbitraryPictureInPictureRenderer extends PictureInPictureRenderer<ArbitraryPictureInPictureRenderer.State> {

    private final PoseStack poseStack = new PoseStack();
    private final List<TextureEntry> textures = new ArrayList<>();
    private final BitSet inUse = new BitSet();

    public ArbitraryPictureInPictureRenderer(MultiBufferSource.BufferSource bufferSource){
        super(bufferSource);
    }

    @Override
    public Class<State> getRenderStateClass(){
        return State.class;
    }

    @Override
    public void prepare(State state, GuiRenderState guiRenderState, int guiScale){
        // Calculate required size
        int width = state.width * guiScale;
        int height = state.height * guiScale;

        // Find the smallest texture which fits the required size
        TextureEntry texture = null;
        int textureIndex = 0;
        while((textureIndex = this.inUse.nextClearBit(textureIndex)) < this.textures.size()){
            TextureEntry entry = this.textures.get(textureIndex);
            if(entry.width >= width && entry.height >= height){
                texture = entry;
                break;
            }
            textureIndex++;
        }

        // If no texture was found, create a new texture
        if(texture == null){
            texture = this.createTexture(width, height);
            this.textures.add(texture);
        }
        this.inUse.set(textureIndex);

        // Clear texture
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(texture.texture, 0, texture.depthTexture, 1);
        RenderSystem.setProjectionMatrix(this.projectionMatrixBuffer.getBuffer(texture.width, texture.height), ProjectionType.ORTHOGRAPHIC);

        // Render to the texture
        RenderSystem.outputColorTextureOverride = texture.textureView;
        RenderSystem.outputDepthTextureOverride = texture.depthTextureView;
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
                TextureSetup.singleTexture(texture.textureView),
                state.pose(),
                state.x0(),
                state.y0(),
                state.x1(),
                state.y1(),
                0,
                (float)width / texture.width,
                1,
                1 - (float)height / texture.height,
                -1,
                state.scissorArea(),
                state.bounds()
            )
        );
    }

    private TextureEntry createTexture(int width, int height){
        GpuDevice device = RenderSystem.getDevice();
        GpuTexture texture = device.createTexture(this::getTextureLabel, 12, TextureFormat.RGBA8, width, height, 1, 1);
        texture.setTextureFilter(FilterMode.NEAREST, false);
        GpuTextureView textureView = device.createTextureView(texture);
        GpuTexture depthTexture = device.createTexture(() -> this.getTextureLabel() + " depth texture", 8, TextureFormat.DEPTH32, width, height, 1, 1);
        GpuTextureView depthTextureView = device.createTextureView(depthTexture);
        return new TextureEntry(width, height, texture, textureView, depthTexture, depthTextureView);
    }

    public void afterFrame(){
        if(this.textures.isEmpty())
            return;

        // Discard unused textures
        int index = this.textures.size() - 1;
        while((index = this.inUse.previousClearBit(index)) != -1){
            this.textures.remove(index).close();
            index--;
        }
        this.inUse.clear();

        // Sort the remaining textures by size
        this.textures.sort(TextureEntry::compareTo);
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
    public void close(){
        super.close();
        this.textures.forEach(TextureEntry::close);
        this.textures.clear();
        this.inUse.clear();
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

    private record TextureEntry(int width, int height, GpuTexture texture, GpuTextureView textureView,
                                GpuTexture depthTexture,
                                GpuTextureView depthTextureView) implements Comparable<TextureEntry> {

        public void close(){
            this.texture.close();
            this.textureView.close();
            this.depthTexture.close();
            this.depthTextureView.close();
        }

        @Override
        public int compareTo(@NotNull ArbitraryPictureInPictureRenderer.TextureEntry o){
            return Integer.compare(this.width * this.height, o.width * o.height);
        }
    }
}
