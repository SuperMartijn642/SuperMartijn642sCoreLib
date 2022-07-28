package com.supermartijn642.core.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BlockShape;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Created 6/12/2021 by SuperMartijn642
 */
public class RenderUtils {

    private static final RenderConfiguration LINES = RenderConfiguration.create(
        "supermartijn642corelib",
        "lines",
        DefaultVertexFormats.POSITION_COLOR,
        RenderConfiguration.PrimitiveType.LINES,
        128,
        true,
        true,
        RenderStateConfiguration.builder()
            .useDefaultLineWidth()
            .useTranslucentTransparency()
            .useViewOffsetZLayering()
            .disableCulling()
            .useLessThanOrEqualDepthTest()
            .disableDepthMask()
            .build()
    );
    private static final RenderConfiguration LINES_NO_DEPTH = RenderConfiguration.create(
        "supermartijn642corelib",
        "lines_no_depth",
        DefaultVertexFormats.POSITION_COLOR,
        RenderConfiguration.PrimitiveType.LINES,
        128,
        true,
        true,
        RenderStateConfiguration.builder()
            .useDefaultLineWidth()
            .useTranslucentTransparency()
            .useViewOffsetZLayering()
            .disableCulling()
            .disableDepthTest()
            .disableDepthMask()
            .build()
    );
    private static final RenderConfiguration QUADS = RenderConfiguration.create(
        "supermartijn642corelib",
        "quads",
        DefaultVertexFormats.POSITION_COLOR,
        RenderConfiguration.PrimitiveType.QUADS,
        256,
        false,
        true,
        RenderStateConfiguration.builder()
            .useTranslucentTransparency()
            .disableTexture()
            .disableCulling()
            .useLessThanOrEqualDepthTest()
            .disableDepthMask()
            .build()
    );
    private static final RenderConfiguration QUADS_NO_DEPTH = RenderConfiguration.create(
        "supermartijn642corelib",
        "quads_no_depth",
        DefaultVertexFormats.POSITION_COLOR,
        RenderConfiguration.PrimitiveType.QUADS,
        256,
        false,
        true,
        RenderStateConfiguration.builder()
            .useTranslucentTransparency()
            .disableTexture()
            .disableCulling()
            .disableDepthTest()
            .disableDepthMask()
            .build()
    );

    /**
     * @return the current interpolated camera position
     */
    public static Vector3d getCameraPosition(){
        return ClientUtils.getMinecraft().getEntityRenderDispatcher().camera.getPosition();
    }

    /**
     * @return the current interpolated camera position
     */
    public static IRenderTypeBuffer.Impl getMainBufferSource(){
        return ClientUtils.getMinecraft().renderBuffers().bufferSource();
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(MatrixStack poseStack, BlockShape shape, float red, float green, float blue, float alpha, boolean depthTest){
        RenderConfiguration renderConfiguration = depthTest ? LINES : LINES_NO_DEPTH;
        IRenderTypeBuffer.Impl bufferSource = getMainBufferSource();
        IVertexBuilder builder = renderConfiguration.begin(bufferSource);
        Matrix4f matrix4f = poseStack.last().pose();
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            builder.vertex(matrix4f, (float)x1, (float)y1, (float)z1).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix4f, (float)x2, (float)y2, (float)z2).color(red, green, blue, alpha).endVertex();
        });
        renderConfiguration.end(bufferSource);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(MatrixStack poseStack, BlockShape shape, float red, float green, float blue, float alpha, boolean depthTest){
        RenderConfiguration renderConfiguration = depthTest ? QUADS : QUADS_NO_DEPTH;
        IRenderTypeBuffer.Impl bufferSource = getMainBufferSource();
        IVertexBuilder builder = renderConfiguration.begin(bufferSource);
        Matrix4f matrix = poseStack.last().pose();
        shape.forEachBox(box -> {
            float minX = (float)box.minX, maxX = (float)box.maxX;
            float minY = (float)box.minY, maxY = (float)box.maxY;
            float minZ = (float)box.minZ, maxZ = (float)box.maxZ;

            builder.vertex(matrix, minX, minY, minZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, minY, minZ).color(red, green, blue, alpha).endVertex();

            builder.vertex(matrix, minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();


            builder.vertex(matrix, minX, minY, minZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, minX, minY, maxZ).color(red, green, blue, alpha).endVertex();

            builder.vertex(matrix, minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();


            builder.vertex(matrix, minX, minY, minZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, minX, maxY, minZ).color(red, green, blue, alpha).endVertex();

            builder.vertex(matrix, maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        });
        renderConfiguration.end(bufferSource);
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(MatrixStack poseStack, VoxelShape shape, float red, float green, float blue, float alpha, boolean depthTest){
        renderShape(poseStack, BlockShape.create(shape), red, green, blue, alpha, depthTest);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(MatrixStack poseStack, VoxelShape shape, float red, float green, float blue, float alpha, boolean depthTest){
        renderShapeSides(poseStack, BlockShape.create(shape), red, green, blue, alpha, depthTest);
    }

    /**
     * Draws an outline for the given box
     */
    public static void renderBox(MatrixStack poseStack, AxisAlignedBB box, float red, float green, float blue, float alpha, boolean depthTest){
        renderShape(poseStack, BlockShape.create(box), red, green, blue, alpha, depthTest);
    }

    /**
     * Draws the sides of the given box
     */
    public static void renderBoxSides(MatrixStack poseStack, AxisAlignedBB box, float red, float green, float blue, float alpha, boolean depthTest){
        renderShapeSides(poseStack, BlockShape.create(box), red, green, blue, alpha, depthTest);
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(MatrixStack poseStack, BlockShape shape, float red, float green, float blue, boolean depthTest){
        renderShape(poseStack, shape, red, green, blue, 1, depthTest);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(MatrixStack poseStack, BlockShape shape, float red, float green, float blue, boolean depthTest){
        renderShapeSides(poseStack, shape, red, green, blue, 1, depthTest);
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(MatrixStack poseStack, VoxelShape shape, float red, float green, float blue, boolean depthTest){
        renderShape(poseStack, BlockShape.create(shape), red, green, blue, 1, depthTest);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(MatrixStack poseStack, VoxelShape shape, float red, float green, float blue, boolean depthTest){
        renderShapeSides(poseStack, BlockShape.create(shape), red, green, blue, 1, depthTest);
    }

    /**
     * Draws an outline for the given box
     */
    public static void renderBox(MatrixStack poseStack, AxisAlignedBB box, float red, float green, float blue, boolean depthTest){
        renderShape(poseStack, BlockShape.create(box), red, green, blue, 1, depthTest);
    }

    /**
     * Draws the sides of the given box
     */
    public static void renderBoxSides(MatrixStack poseStack, AxisAlignedBB box, float red, float green, float blue, boolean depthTest){
        renderShapeSides(poseStack, BlockShape.create(box), red, green, blue, 1, depthTest);
    }
}
