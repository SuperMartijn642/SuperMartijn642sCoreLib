package com.supermartijn642.core.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created 23/07/2022 by SuperMartijn642
 */
public class RenderStateConfiguration {

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {

        private RenderStateEntry textureState;
        private RenderStateEntry shaderState;
        private RenderStateEntry transparencyState;
        private RenderStateEntry depthTestState;
        private RenderStateEntry cullingState;
        private RenderStateEntry lightmapState;
        private RenderStateEntry overlayState;
        private RenderStateEntry layeringState;
        private RenderStateEntry depthMaskState;
        private RenderStateEntry colorMaskState;
        private RenderStateEntry lineWidthState;

        private final List<RenderStateEntry> entries = new ArrayList<>();

        private Builder(){
            this.disableTexture();
            this.disableShader();
            this.disableTransparency();
            this.useLessThanOrEqualDepthTest();
            this.enableCulling();
            this.disableLightmap();
            this.disableOverlay();
            this.disableLayering();
            this.enableDepthMask();
            this.enableColorMask();
            this.useDefaultLineWidth();
        }

        public Builder append(RenderStateEntry entry){
            this.entries.add(entry);
            return this;
        }

        public Builder disableTexture(){
            this.textureState = new RenderStateEntry(RenderSystem::disableTexture, RenderSystem::enableTexture);
            return this;
        }

        public Builder useTexture(ResourceLocation texture, boolean useBlur, boolean useMipmap){
            this.textureState = new RenderStateEntry(() -> {
                RenderSystem.enableTexture();
                ClientUtils.getTextureManager().getTexture(texture).setFilter(useBlur, useMipmap);
                RenderSystem.setShaderTexture(0, texture);
            }, null);
            return this;
        }

        public Builder disableShader(){
            return this.useShader(() -> null);
        }

        public Builder useShader(Supplier<ShaderInstance> shader){
            this.shaderState = new RenderStateEntry(() -> RenderSystem.setShader(shader), null);
            return this;
        }

        public Builder disableTransparency(){
            this.transparencyState = new RenderStateEntry(RenderSystem::disableBlend, null);
            return this;
        }

        public Builder useAdditiveTransparency(){
            this.transparencyState = new RenderStateEntry(() -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });
            return this;
        }

        public Builder useTranslucentTransparency(){
            this.transparencyState = new RenderStateEntry(() -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });
            return this;
        }

        public Builder disableDepthTest(){
            this.depthTestState = new RenderStateEntry(RenderSystem::disableDepthTest, null);
            return this;
        }

        public Builder useEqualDepthTest(){
            this.depthTestState = new RenderStateEntry(() -> {
                RenderSystem.enableDepthTest();
                RenderSystem.depthFunc(GL11.GL_EQUAL);
            }, () -> {
                RenderSystem.disableDepthTest();
                RenderSystem.depthFunc(GL11.GL_LEQUAL);
            });
            return this;
        }

        public Builder useLessThanOrEqualDepthTest(){
            this.depthTestState = new RenderStateEntry(() -> {
                RenderSystem.enableDepthTest();
                RenderSystem.depthFunc(GL11.GL_LEQUAL);
            }, RenderSystem::disableDepthTest);
            return this;
        }

        public Builder disableCulling(){
            this.cullingState = new RenderStateEntry(RenderSystem::disableCull, RenderSystem::enableCull);
            return this;
        }

        public Builder enableCulling(){
            this.cullingState = new RenderStateEntry(RenderSystem::enableCull, null);
            return this;
        }

        public Builder disableLightmap(){
            this.lightmapState = new RenderStateEntry(() -> ClientUtils.getMinecraft().gameRenderer.lightTexture().turnOffLightLayer(), null);
            return this;
        }

        public Builder enableLightmap(){
            this.lightmapState = new RenderStateEntry(() -> ClientUtils.getMinecraft().gameRenderer.lightTexture().turnOnLightLayer(), () -> ClientUtils.getMinecraft().gameRenderer.lightTexture().turnOffLightLayer());
            return this;
        }

        public Builder disableOverlay(){
            this.overlayState = new RenderStateEntry(() -> ClientUtils.getMinecraft().gameRenderer.overlayTexture().teardownOverlayColor(), null);
            return this;
        }

        public Builder enableOverlay(){
            this.overlayState = new RenderStateEntry(() -> ClientUtils.getMinecraft().gameRenderer.overlayTexture().setupOverlayColor(), () -> ClientUtils.getMinecraft().gameRenderer.overlayTexture().teardownOverlayColor());
            return this;
        }

        public Builder disableLayering(){
            this.layeringState = new RenderStateEntry(RenderSystem::disablePolygonOffset, null);
            return this;
        }

        public Builder usePolygonOffsetLayering(){
            this.layeringState = new RenderStateEntry(() -> {
                RenderSystem.polygonOffset(-1.0F, -10.0F);
                RenderSystem.enablePolygonOffset();
            }, () -> {
                RenderSystem.polygonOffset(0.0F, 0.0F);
                RenderSystem.disablePolygonOffset();
            });
            return this;
        }

        public Builder useViewOffsetZLayering(){
            this.layeringState = new RenderStateEntry(() -> {
                PoseStack posestack = RenderSystem.getModelViewStack();
                posestack.pushPose();
                posestack.scale(0.99975586F, 0.99975586F, 0.99975586F);
                RenderSystem.applyModelViewMatrix();
            }, () -> {
                PoseStack posestack = RenderSystem.getModelViewStack();
                posestack.popPose();
                RenderSystem.applyModelViewMatrix();
            });
            return this;
        }

        public Builder disableDepthMask(){
            this.depthMaskState = new RenderStateEntry(() -> RenderSystem.depthMask(false), () -> RenderSystem.depthMask(true));
            return this;
        }

        public Builder enableDepthMask(){
            this.depthMaskState = new RenderStateEntry(() -> RenderSystem.depthMask(true), null);
            return this;
        }

        public Builder disableColorMask(){
            this.colorMaskState = new RenderStateEntry(() -> RenderSystem.colorMask(false, false, false, false), () -> RenderSystem.colorMask(true, true, true, true));
            return this;
        }

        public Builder enableColorMask(){
            this.colorMaskState = new RenderStateEntry(() -> RenderSystem.colorMask(true, true, true, true), null);
            return this;
        }

        public Builder useColorMask(boolean writeRed, boolean writeGreen, boolean writeBlue, boolean writeAlpha){
            this.colorMaskState = new RenderStateEntry(() -> RenderSystem.colorMask(writeRed, writeGreen, writeBlue, writeAlpha), () -> RenderSystem.colorMask(true, true, true, true));
            return this;
        }

        public Builder useDefaultLineWidth(){
            this.lineWidthState = new RenderStateEntry(() -> RenderSystem.lineWidth(1), null);
            return this;
        }

        public Builder useLineWidth(float width){
            this.lineWidthState = new RenderStateEntry(() -> RenderSystem.lineWidth(width), () -> RenderSystem.lineWidth(1));
            return this;
        }

        public Builder useWindowRelativeLineWidth(){
            this.lineWidthState = new RenderStateEntry(() -> RenderSystem.lineWidth(Math.max(2.5F, ClientUtils.getMinecraft().getWindow().getWidth() / 1920f * 2.5f)), () -> RenderSystem.lineWidth(1));
            return this;
        }

        public RenderStateConfiguration build(){
            List<RenderStateEntry> combinedEntries = new ArrayList<>(11 + this.entries.size());
            combinedEntries.add(this.textureState);
            combinedEntries.add(this.shaderState);
            combinedEntries.add(this.transparencyState);
            combinedEntries.add(this.depthTestState);
            combinedEntries.add(this.cullingState);
            combinedEntries.add(this.lightmapState);
            combinedEntries.add(this.overlayState);
            combinedEntries.add(this.layeringState);
            combinedEntries.add(this.depthMaskState);
            combinedEntries.add(this.colorMaskState);
            combinedEntries.add(this.lineWidthState);
            combinedEntries.addAll(this.entries);
            return new RenderStateConfiguration(combinedEntries);
        }
    }

    public static final class RenderStateEntry {

        private final Runnable setup, clear;

        public RenderStateEntry(Runnable setup, Runnable clear){
            this.setup = setup == null ? () -> {
            } : setup;
            this.clear = clear == null ? () -> {
            } : clear;
        }

        /**
         * Initializes any OpenGl properties
         */
        public void setup(){
            this.setup.run();
        }

        /**
         * Resets any OpenGl properties set in {@link #setup()}
         */
        public void clear(){
            this.clear.run();
        }
    }

    private final List<RenderStateEntry> renderStateEntries;

    private RenderStateConfiguration(List<RenderStateEntry> renderStateEntries){
        this.renderStateEntries = renderStateEntries;
    }

    /**
     * Initializes any render states
     */
    public void setup(){
        this.renderStateEntries.forEach(RenderStateEntry::setup);
    }

    /**
     * Resets any render states
     */
    public void clear(){
        this.renderStateEntries.forEach(RenderStateEntry::clear);
    }
}
