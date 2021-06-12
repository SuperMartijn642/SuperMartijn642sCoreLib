package com.supermartijn642.core.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BlockShape;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

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
    public static void renderShape(MatrixStack matrixStack, BlockShape shape, double x, double y, double z, float red, float green, float blue, float alpha){
        matrixStack.push();

        Vec3d camera = getCameraPosition();
        matrixStack.translate(x - camera.x, y - camera.y, z - camera.z);

        IVertexBuilder builder = ClientUtils.getMinecraft().getRenderTypeBuffers().getBufferSource().getBuffer(RenderType.getLines());
        Matrix4f matrix4f = matrixStack.getLast().getMatrix();
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            builder.pos(matrix4f, (float)x1, (float)y1, (float)z1).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix4f, (float)x2, (float)y2, (float)z2).color(red, green, blue, alpha).endVertex();
        });

        matrixStack.pop();
    }

    /**
     * Draws an outline for the given box.
     */
    public static void renderBox(MatrixStack matrixStack, AxisAlignedBB box, double x, double y, double z, float red, float green, float blue, float alpha){
        renderShape(matrixStack, BlockShape.create(box), x, y, z, red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(MatrixStack matrixStack, BlockShape shape, double x, double y, double z, float red, float green, float blue){
        renderShape(matrixStack, shape, x, y, z, red, green, blue, 1);
    }

    /**
     * Draws an outline for the given box.
     */
    public static void renderBox(MatrixStack matrixStack, AxisAlignedBB box, double x, double y, double z, float red, float green, float blue){
        renderShape(matrixStack, BlockShape.create(box), x, y, z, red, green, blue, 1);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(MatrixStack matrixStack, BlockShape shape, double x, double y, double z){
        renderShape(matrixStack, shape, x, y, z, 1, 1, 1, 1);
    }

    /**
     * Draws an outline for the given box.
     */
    public static void renderBox(MatrixStack matrixStack, AxisAlignedBB box, double x, double y, double z){
        renderShape(matrixStack, BlockShape.create(box), x, y, z, 1, 1, 1, 1);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(MatrixStack matrixStack, BlockShape shape, float red, float green, float blue, float alpha){
        renderShape(matrixStack, shape, 0, 0, 0, red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given box.
     */
    public static void renderBox(MatrixStack matrixStack, AxisAlignedBB box, float red, float green, float blue, float alpha){
        renderShape(matrixStack, BlockShape.create(box), 0, 0, 0, red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(MatrixStack matrixStack, BlockShape shape, float red, float green, float blue){
        renderShape(matrixStack, shape, 0, 0, 0, red, green, blue, 1);
    }

    /**
     * Draws an outline for the given box.
     */
    public static void renderBox(MatrixStack matrixStack, AxisAlignedBB box, float red, float green, float blue){
        renderShape(matrixStack, BlockShape.create(box), 0, 0, 0, red, green, blue, 1);
    }

}
