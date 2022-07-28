package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.opengl.GL11;

/**
 * Created 23/07/2022 by SuperMartijn642
 */
public class RenderConfiguration extends RenderType {

    public static RenderConfiguration create(String modid, String name, VertexFormat format, PrimitiveType primitive, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, RenderStateConfiguration renderStateConfiguration){
        return new RenderConfiguration(modid + ":" + name, format, primitive, bufferSize, affectsCrumbling, sortOnUpload, renderStateConfiguration);
    }

    private RenderConfiguration(String name, VertexFormat format, PrimitiveType primitive, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, RenderStateConfiguration renderStateConfiguration){
        super(name, format, primitive.getUnderlying(), bufferSize, affectsCrumbling, sortOnUpload, renderStateConfiguration::setup, renderStateConfiguration::clear);
    }

    public void setupState(){
        super.setupRenderState();
    }

    public void clearState(){
        super.clearRenderState();
    }

    public VertexConsumer begin(MultiBufferSource bufferSource){
        return bufferSource.getBuffer(this);
    }

    public void end(MultiBufferSource.BufferSource bufferSource){
        bufferSource.endBatch(this);
    }

    public enum PrimitiveType {
        LINES(GL11.GL_LINES, 2, 2, false, VertexFormat.Mode.DEBUG_LINES),
        LINE_STRIP(GL11.GL_LINE_STRIP, 2, 1, true, VertexFormat.Mode.DEBUG_LINE_STRIP),
        TRIANGLE_LINES(GL11.GL_TRIANGLES, 2, 2, false, VertexFormat.Mode.LINES),
        TRIANGLE_LINE_STRIP(GL11.GL_TRIANGLE_STRIP, 2, 1, true, VertexFormat.Mode.LINE_STRIP),
        TRIANGLES(GL11.GL_TRIANGLES, 3, 3, false, VertexFormat.Mode.TRIANGLES),
        TRIANGLE_STRIP(GL11.GL_TRIANGLE_STRIP, 3, 1, true, VertexFormat.Mode.DEBUG_LINES),
        TRIANGLE_FAN(GL11.GL_TRIANGLE_FAN, 3, 1, true, VertexFormat.Mode.TRIANGLE_FAN),
        QUADS(GL11.GL_QUADS, 4, 4, false, VertexFormat.Mode.QUADS);

        private final int glMode;
        private final int vertexCount;
        private final int vertexOffset;
        private final boolean isConnected;
        private final VertexFormat.Mode underlying;

        PrimitiveType(int glMode, int vertexCount, int vertexOffset, boolean isConnected, VertexFormat.Mode underlying){
            this.glMode = glMode;
            this.vertexCount = vertexCount;
            this.vertexOffset = vertexOffset;
            this.isConnected = isConnected;
            this.underlying = underlying;
        }

        public int getGlMode(){
            return this.glMode;
        }

        public int getVertexCount(){
            return this.vertexCount;
        }

        public int getVertexOffset(){
            return this.vertexOffset;
        }

        public boolean isConnected(){
            return this.isConnected;
        }

        @Deprecated
        public VertexFormat.Mode getUnderlying(){
            return this.underlying;
        }
    }
}
