package com.supermartijn642.core.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BlockShape;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

/**
 * Created 6/12/2021 by SuperMartijn642
 */
public class RenderUtils {

    public static void enableDepthTest(){
        GlStateManager.enableDepthTest();
    }

    public static void disableDepthTest(){
        GlStateManager.disableDepthTest();
    }

    /**
     * @return the current interpolated camera position.
     */
    public static Vec3d getCameraPosition(){
        return ClientUtils.getMinecraft().gameRenderer.getActiveRenderInfo().getProjectedView();
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(BlockShape shape, double x, double y, double z, float red, float green, float blue, float alpha){
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        Vec3d camera = getCameraPosition();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buffer.setTranslation(x - camera.x, y - camera.y, z - camera.z);
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            buffer.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
            buffer.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
        });
        buffer.setTranslation(0, 0, 0);
        tessellator.draw();

        GlStateManager.popMatrix();
        GlStateManager.enableTexture();
    }

    /**
     * Draws an outline for the given box.
     */
    public static void renderBox(AxisAlignedBB box, double x, double y, double z, float red, float green, float blue, float alpha){
        renderShape(BlockShape.create(box), x, y, z, red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(BlockShape shape, double x, double y, double z, float red, float green, float blue){
        renderShape(shape, x, y, z, red, green, blue, 1);
    }

    /**
     * Draws an outline for the given box.
     */
    public static void renderBox(AxisAlignedBB box, double x, double y, double z, float red, float green, float blue){
        renderShape(BlockShape.create(box), x, y, z, red, green, blue, 1);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(BlockShape shape, double x, double y, double z){
        renderShape(shape, x, y, z, 1, 1, 1, 1);
    }

    /**
     * Draws an outline for the given box.
     */
    public static void renderBox(AxisAlignedBB box, double x, double y, double z){
        renderShape(BlockShape.create(box), x, y, z, 1, 1, 1, 1);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(BlockShape shape, float red, float green, float blue, float alpha){
        renderShape(shape, 0, 0, 0, red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given box.
     */
    public static void renderBox(AxisAlignedBB box, float red, float green, float blue, float alpha){
        renderShape(BlockShape.create(box), 0, 0, 0, red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(BlockShape shape, float red, float green, float blue){
        renderShape(shape, 0, 0, 0, red, green, blue, 1);
    }

    /**
     * Draws an outline for the given box.
     */
    public static void renderBox(AxisAlignedBB box, float red, float green, float blue){
        renderShape(BlockShape.create(box), 0, 0, 0, red, green, blue, 1);
    }

}
