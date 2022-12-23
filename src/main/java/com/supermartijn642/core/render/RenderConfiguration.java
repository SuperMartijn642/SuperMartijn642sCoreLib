package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;

/**
 * Created 23/07/2022 by SuperMartijn642
 */
public class RenderConfiguration extends RenderType {

    public static RenderConfiguration create(String modid, String name, VertexFormat format, PrimitiveType primitive, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, RenderStateConfiguration renderStateConfiguration){
        return new RenderConfiguration(modid + ":" + name, format, primitive.getGlMode(), bufferSize, affectsCrumbling, sortOnUpload, renderStateConfiguration::setup, renderStateConfiguration::clear);
    }

    public static RenderConfiguration wrap(RenderType renderType){
        if(renderType instanceof RenderConfiguration)
            return (RenderConfiguration)renderType;
        return new RenderConfiguration(renderType.toString(), renderType.format(), renderType.mode(), renderType.bufferSize(), renderType.affectsCrumbling(), renderType.sortOnUpload, renderType::setupRenderState, renderType::clearRenderState);
    }

    private RenderConfiguration(String name, VertexFormat format, int glMode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setup, Runnable clear){
        super(name, format, glMode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear);
    }

    public void setupState(){
        super.setupRenderState();
    }

    public void clearState(){
        super.clearRenderState();
    }

    public IVertexBuilder begin(IRenderTypeBuffer bufferSource){
        return bufferSource.getBuffer(this);
    }

    public void end(IRenderTypeBuffer.Impl bufferSource){
        bufferSource.endBatch(this);
    }

    public enum PrimitiveType {
        LINES(GL11.GL_LINES, 2, 2, false),
        LINE_STRIP(GL11.GL_LINE_STRIP, 2, 1, true),
        TRIANGLE_LINES(GL11.GL_TRIANGLES, 2, 2, false),
        TRIANGLE_LINE_STRIP(GL11.GL_TRIANGLE_STRIP, 2, 1, true),
        TRIANGLES(GL11.GL_TRIANGLES, 3, 3, false),
        TRIANGLE_STRIP(GL11.GL_TRIANGLE_STRIP, 3, 1, true),
        TRIANGLE_FAN(GL11.GL_TRIANGLE_FAN, 3, 1, true),
        QUADS(GL11.GL_QUADS, 4, 4, false);

        private final int glMode;
        private final int vertexCount;
        private final int vertexOffset;
        private final boolean isConnected;

        PrimitiveType(int glMode, int vertexCount, int vertexOffset, boolean isConnected){
            this.glMode = glMode;
            this.vertexCount = vertexCount;
            this.vertexOffset = vertexOffset;
            this.isConnected = isConnected;
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
    }
}
