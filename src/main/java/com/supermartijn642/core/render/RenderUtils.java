package com.supermartijn642.core.render;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BlockShape;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

/**
 * Created 6/12/2021 by SuperMartijn642
 */
public class RenderUtils {

    public static boolean depthTest = true;

    public static void enableDepthTest(){
        depthTest = true;
    }

    public static void disableDepthTest(){
        depthTest = false;
    }

    /**
     * Resets all settings to their defaults
     */
    public static void resetState(){
        depthTest = true;
    }

    /**
     * @return the current interpolated camera position
     */
    public static Vec3d getCameraPosition(){
        RenderManager renderManager = ClientUtils.getMinecraft().getRenderManager();
        return new Vec3d(renderManager.viewerPosX, renderManager.viewerPosY, renderManager.viewerPosZ);
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(BlockShape shape, float red, float green, float blue, float alpha){
        boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean texture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);

        if(RenderUtils.depthTest){
            GlStateManager.enableDepth();
//            GlStateManager.depthFunc(515);
//            GlStateManager.depthMask(false);
        }else GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.alphaFunc(516, 0);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableCull();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            buffer.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
            buffer.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
        });
        tessellator.draw();

        if(depthTest) GlStateManager.enableDepth();
        else GlStateManager.disableDepth();
        if(texture) GlStateManager.enableTexture2D();
        if(lighting) GlStateManager.enableLighting();
        if(!blend) GlStateManager.disableBlend();
        if(cull) GlStateManager.enableCull();
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(BlockShape shape, float red, float green, float blue, float alpha){
        boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean texture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);

        if(RenderUtils.depthTest){
            GlStateManager.enableDepth();
            GlStateManager.depthFunc(515);
            GlStateManager.depthMask(false);
        }else GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.alphaFunc(516, 0);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableCull();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        shape.forEachBox(box -> {
            float minX = (float)box.minX, maxX = (float)box.maxX;
            float minY = (float)box.minY, maxY = (float)box.maxY;
            float minZ = (float)box.minZ, maxZ = (float)box.maxZ;

            builder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
            builder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            builder.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            builder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();

            builder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();


            builder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
            builder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
            builder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();

            builder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            builder.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();


            builder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
            builder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();

            builder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
            builder.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            builder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        });
        tessellator.draw();

        if(depthTest){
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
        }
        else GlStateManager.disableDepth();
        if(texture) GlStateManager.enableTexture2D();
        if(lighting) GlStateManager.enableLighting();
        if(!blend) GlStateManager.disableBlend();
        if(cull) GlStateManager.enableCull();
    }

    /**
     * Draws an outline for the given box
     */
    public static void renderBox(AxisAlignedBB box, float red, float green, float blue, float alpha){
        renderShape(BlockShape.create(box), red, green, blue, alpha);
    }

    /**
     * Draws the sides of the given box
     */
    public static void renderBoxSides(AxisAlignedBB box, float red, float green, float blue, float alpha){
        renderShapeSides(BlockShape.create(box), red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(BlockShape shape, float red, float green, float blue){
        renderShape(shape, red, green, blue, 1);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(BlockShape shape, float red, float green, float blue){
        renderShapeSides(shape, red, green, blue, 1);
    }

    /**
     * Draws an outline for the given box
     */
    public static void renderBox(AxisAlignedBB box, float red, float green, float blue){
        renderShape(BlockShape.create(box), red, green, blue, 1);
    }

    /**
     * Draws the sides of the given box
     */
    public static void renderBoxSides(AxisAlignedBB box, float red, float green, float blue){
        renderShapeSides(BlockShape.create(box), red, green, blue, 1);
    }

}
