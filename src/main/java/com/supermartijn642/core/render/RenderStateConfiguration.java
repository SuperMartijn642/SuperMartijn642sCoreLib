package com.supermartijn642.core.render;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 23/07/2022 by SuperMartijn642
 */
public class RenderStateConfiguration {

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {

        private RenderStateEntry textureState;
        private RenderStateEntry transparencyState;
        private RenderStateEntry depthTestState;
        private RenderStateEntry cullingState;
        private RenderStateEntry lightmapState;
        //        private RenderStateEntry overlayState; // TODO
        private RenderStateEntry layeringState;
        private RenderStateEntry depthMaskState;
        private RenderStateEntry colorMaskState;
        private RenderStateEntry lineWidthState;
        private RenderStateEntry lightingState;

        private final List<RenderStateEntry> entries = new ArrayList<>();

        private Builder(){
            this.disableTexture();
            this.disableTransparency();
            this.useLessThanOrEqualDepthTest();
            this.enableCulling();
            this.disableLightmap();
            this.disableOverlay();
            this.disableLayering();
            this.enableDepthMask();
            this.enableColorMask();
            this.useDefaultLineWidth();
            this.enableLighting();
        }

        public Builder append(RenderStateEntry entry){
            this.entries.add(entry);
            return this;
        }

        public Builder disableTexture(){
            this.textureState = new RenderStateEntry(null, null) {
                private boolean enableTextures;

                @Override
                public void setup(){
                    this.enableTextures = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);

                    GlStateManager.disableTexture();
                }

                @Override
                public void clear(){
                    if(this.enableTextures)
                        GlStateManager.enableTexture();
                }
            };
            return this;
        }

        public Builder useTexture(ResourceLocation texture, boolean useBlur, boolean useMipmap){
            this.textureState = new RenderStateEntry(null, null) {
                private boolean enableTextures;

                @Override
                public void setup(){
                    this.enableTextures = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);

                    GlStateManager.enableTexture();
                    ClientUtils.getTextureManager().bind(texture);
                    ClientUtils.getTextureManager().getTexture(texture).pushFilter(useBlur, useMipmap);
                }

                @Override
                public void clear(){
                    if(!this.enableTextures)
                        GlStateManager.disableTexture();
                    ClientUtils.getTextureManager().getTexture(texture).popFilter();
                }
            };
            return this;
        }

        public Builder disableTransparency(){
            this.transparencyState = new RenderStateEntry(null, null) {
                private boolean enableBlend;

                @Override
                public void setup(){
                    this.enableBlend = GL11.glIsEnabled(GL11.GL_BLEND);

                    GlStateManager.disableBlend();
                }

                @Override
                public void clear(){
                    if(this.enableBlend)
                        GlStateManager.enableBlend();
                }
            };
            return this;
        }

        public Builder useAdditiveTransparency(){
            this.transparencyState = new RenderStateEntry(null, null) {
                private boolean enableBlend;

                @Override
                public void setup(){
                    this.enableBlend = GL11.glIsEnabled(GL11.GL_BLEND);

                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                }

                @Override
                public void clear(){
                    if(!this.enableBlend){
                        GlStateManager.disableBlend();
                        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    }
                }
            };
            return this;
        }

        public Builder useTranslucentTransparency(){
            this.transparencyState = new RenderStateEntry(null, null) {
                private boolean enableBlend;

                @Override
                public void setup(){
                    this.enableBlend = GL11.glIsEnabled(GL11.GL_BLEND);

                    GlStateManager.enableBlend();
                    GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                }

                @Override
                public void clear(){
                    if(!this.enableBlend){
                        GlStateManager.disableBlend();
                        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    }
                }
            };
            return this;
        }

        public Builder disableDepthTest(){
            this.depthTestState = new RenderStateEntry(null, null) {
                private boolean enableDepthTest;

                @Override
                public void setup(){
                    this.enableDepthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);

                    GlStateManager.disableDepthTest();
                }

                @Override
                public void clear(){
                    if(this.enableDepthTest)
                        GlStateManager.enableDepthTest();
                }
            };
            return this;
        }

        public Builder useEqualDepthTest(){
            this.depthTestState = new RenderStateEntry(null, null) {
                private boolean enableDepthTest;

                @Override
                public void setup(){
                    this.enableDepthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);

                    GlStateManager.enableDepthTest();
                    GlStateManager.depthFunc(GL11.GL_EQUAL);
                }

                @Override
                public void clear(){
                    if(!this.enableDepthTest)
                        GlStateManager.disableDepthTest();
                    GlStateManager.depthFunc(GL11.GL_LEQUAL);
                }
            };
            return this;
        }

        public Builder useLessThanOrEqualDepthTest(){
            this.depthTestState = new RenderStateEntry(null, null) {
                private boolean enableDepthTest;

                @Override
                public void setup(){
                    this.enableDepthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);

                    GlStateManager.enableDepthTest();
                    GlStateManager.depthFunc(GL11.GL_LEQUAL);
                }

                @Override
                public void clear(){
                    if(!this.enableDepthTest)
                        GlStateManager.disableDepthTest();
                }
            };
            return this;
        }

        public Builder disableCulling(){
            this.cullingState = new RenderStateEntry(null, null) {
                private boolean enableCulling;

                @Override
                public void setup(){
                    this.enableCulling = GL11.glIsEnabled(GL11.GL_CULL_FACE);

                    GlStateManager.disableCull();
                }

                @Override
                public void clear(){
                    if(this.enableCulling)
                        GlStateManager.enableCull();
                }
            };
            return this;
        }

        public Builder enableCulling(){
            this.cullingState = new RenderStateEntry(null, null) {
                private boolean enableCulling;

                @Override
                public void setup(){
                    this.enableCulling = GL11.glIsEnabled(GL11.GL_CULL_FACE);

                    GlStateManager.enableCull();
                }

                @Override
                public void clear(){
                    if(!this.enableCulling)
                        GlStateManager.disableCull();
                }
            };
            return this;
        }

        public Builder disableLightmap(){
            this.lightmapState = new RenderStateEntry(null, null) {
                private boolean enableLightmap;

                @Override
                public void setup(){
                    GlStateManager.activeTexture(GLX.GL_TEXTURE1);
                    this.enableLightmap = GL11.glIsEnabled(GL11.GL_TEXTURE);
                    GlStateManager.activeTexture(GLX.GL_TEXTURE0);

                    ClientUtils.getMinecraft().gameRenderer.turnOffLightLayer();
                }

                @Override
                public void clear(){
                    if(this.enableLightmap)
                        ClientUtils.getMinecraft().gameRenderer.turnOnLightLayer();
                }
            };
            return this;
        }

        public Builder enableLightmap(){
            this.lightmapState = new RenderStateEntry(null, null) {
                private boolean enableLightmap;

                @Override
                public void setup(){
                    GlStateManager.activeTexture(GLX.GL_TEXTURE1);
                    this.enableLightmap = GL11.glIsEnabled(GL11.GL_TEXTURE);
                    GlStateManager.activeTexture(GLX.GL_TEXTURE0);

                    ClientUtils.getMinecraft().gameRenderer.turnOnLightLayer();
                }

                @Override
                public void clear(){
                    if(!this.enableLightmap)
                        ClientUtils.getMinecraft().gameRenderer.turnOffLightLayer();
                }
            };
            return this;
        }

        public Builder disableOverlay(){ // TODO
//            this.overlayState = new RenderStateEntry(() -> ClientUtils.getMinecraft().gameRenderer.overlayTexture().teardownOverlayColor(), null);
            return this;
        }

        public Builder enableOverlay(){
//            this.overlayState = new RenderStateEntry(() -> ClientUtils.getMinecraft().gameRenderer.overlayTexture().setupOverlayColor(), () -> ClientUtils.getMinecraft().gameRenderer.overlayTexture().teardownOverlayColor());
            return this;
        }

        public Builder disableLayering(){
            this.layeringState = new RenderStateEntry(null, null) {
                private boolean enablePolygonOffset;

                @Override
                public void setup(){
                    this.enablePolygonOffset = GL11.glIsEnabled(GL11.GL_POLYGON_OFFSET_FILL);

                    GlStateManager.disablePolygonOffset();
                }

                @Override
                public void clear(){
                    if(this.enablePolygonOffset)
                        GlStateManager.enablePolygonOffset();
                }
            };
            return this;
        }

        public Builder usePolygonOffsetLayering(){
            this.layeringState = new RenderStateEntry(null, null) {
                private boolean enablePolygonOffset;

                @Override
                public void setup(){
                    this.enablePolygonOffset = GL11.glIsEnabled(GL11.GL_POLYGON_OFFSET_FILL);

                    GlStateManager.polygonOffset(-1.0F, -10.0F);
                    GlStateManager.enablePolygonOffset();
                }

                @Override
                public void clear(){
                    if(!this.enablePolygonOffset)
                        GlStateManager.disablePolygonOffset();
                    GlStateManager.polygonOffset(0.0F, 0.0F);
                }
            };
            return this;
        }

        public Builder useViewOffsetZLayering(){
            this.layeringState = new RenderStateEntry(() -> {
                GlStateManager.pushMatrix();
                GlStateManager.scalef(0.99975586F, 0.99975586F, 0.99975586F);
            }, GlStateManager::popMatrix);
            return this;
        }

        public Builder disableDepthMask(){
            this.depthMaskState = new RenderStateEntry(() -> GlStateManager.depthMask(false), () -> GlStateManager.depthMask(true));
            return this;
        }

        public Builder enableDepthMask(){
            this.depthMaskState = new RenderStateEntry(() -> GlStateManager.depthMask(true), null);
            return this;
        }

        public Builder disableColorMask(){
            this.colorMaskState = new RenderStateEntry(null, null) {
                @Override
                public void setup(){
                    GlStateManager.colorMask(false, false, false, false);
                }

                @Override
                public void clear(){
                    GlStateManager.colorMask(true, true, true, true);
                }
            };
            return this;
        }

        public Builder enableColorMask(){
            this.colorMaskState = new RenderStateEntry(() -> GlStateManager.colorMask(true, true, true, true), null);
            return this;
        }

        public Builder useColorMask(boolean writeRed, boolean writeGreen, boolean writeBlue, boolean writeAlpha){
            this.colorMaskState = new RenderStateEntry(() -> GlStateManager.colorMask(writeRed, writeGreen, writeBlue, writeAlpha), () -> GlStateManager.colorMask(true, true, true, true));
            return this;
        }

        public Builder useDefaultLineWidth(){
            this.lineWidthState = new RenderStateEntry(() -> GlStateManager.lineWidth(1), null);
            return this;
        }

        public Builder useLineWidth(float width){
            this.lineWidthState = new RenderStateEntry(() -> GlStateManager.lineWidth(width), () -> GlStateManager.lineWidth(1));
            return this;
        }

        public Builder useWindowRelativeLineWidth(){
            this.lineWidthState = new RenderStateEntry(() -> GlStateManager.lineWidth(Math.max(2.5F, ClientUtils.getMinecraft().window.getWidth() / 1920f * 2.5f)), () -> GlStateManager.lineWidth(1));
            return this;
        }

        public Builder disableLighting(){
            this.lightingState = new RenderStateEntry(null, null) {
                private boolean enableLighting;

                @Override
                public void setup(){
                    this.enableLighting = GL11.glIsEnabled(GL11.GL_LIGHTING);

                    GlStateManager.disableLighting();
                }

                @Override
                public void clear(){
                    if(this.enableLighting)
                        GlStateManager.enableLighting();
                }
            };
            return this;
        }

        public Builder enableLighting(){
            this.lightingState = new RenderStateEntry(null, null) {
                private boolean enableLighting;

                @Override
                public void setup(){
                    this.enableLighting = GL11.glIsEnabled(GL11.GL_LIGHTING);

                    GlStateManager.enableLighting();
                }

                @Override
                public void clear(){
                    if(!this.enableLighting)
                        GlStateManager.disableLighting();
                }
            };
            return this;
        }

        public RenderStateConfiguration build(){
            List<RenderStateEntry> combinedEntries = new ArrayList<>(11 + this.entries.size());
            combinedEntries.add(this.textureState);
            combinedEntries.add(this.transparencyState);
            combinedEntries.add(this.depthTestState);
            combinedEntries.add(this.cullingState);
            combinedEntries.add(this.lightmapState);
//            combinedEntries.add(this.overlayState); // TODO
            combinedEntries.add(this.layeringState);
            combinedEntries.add(this.depthMaskState);
            combinedEntries.add(this.colorMaskState);
            combinedEntries.add(this.lineWidthState);
            combinedEntries.add(this.lightingState);
            combinedEntries.addAll(this.entries);
            return new RenderStateConfiguration(combinedEntries);
        }
    }

    public static class RenderStateEntry {

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
