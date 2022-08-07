package com.supermartijn642.core.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;

/**
 * Created 23/07/2022 by SuperMartijn642
 */
public class RenderConfiguration {

    public static RenderConfiguration create(String modid, String name, VertexFormat format, PrimitiveType primitive, RenderStateConfiguration renderStateConfiguration){
        return new RenderConfiguration(modid, name, format, primitive, renderStateConfiguration);
    }

    private final String modid, name;
    private final VertexFormat format;
    private final PrimitiveType primitiveType;
    private final RenderStateConfiguration configuration;

    private RenderConfiguration(String modid, String name, VertexFormat format, PrimitiveType primitive, RenderStateConfiguration configuration){
        this.modid = modid;
        this.name = name;
        this.format = format;
        this.primitiveType = primitive;
        this.configuration = configuration;
    }

    public void setupState(){
        this.configuration.setup();
    }

    public void clearState(){
        this.configuration.clear();
    }

    public BufferBuilder begin(){
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(this.primitiveType.glMode, this.format);
        return bufferBuilder;
    }

    public void end(){
        this.setupState();
        Tessellator.getInstance().draw();
        this.clearState();
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
