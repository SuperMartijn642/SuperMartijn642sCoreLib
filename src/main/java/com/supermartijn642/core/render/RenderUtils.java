package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BlockShape;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

/**
 * Created 6/12/2021 by SuperMartijn642
 */
public class RenderUtils {

    private static final RenderConfiguration LINES = RenderConfiguration.create(
        "supermartijn642corelib",
        "lines",
        DefaultVertexFormat.POSITION_COLOR_NORMAL,
        RenderConfiguration.PrimitiveType.TRIANGLE_LINES,
        128,
        true,
        true,
        RenderStateConfiguration.builder()
            .useShader(GameRenderer::getRendertypeLinesShader)
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
        DefaultVertexFormat.POSITION_COLOR_NORMAL,
        RenderConfiguration.PrimitiveType.TRIANGLE_LINES,
        128,
        true,
        true,
        RenderStateConfiguration.builder()
            .useShader(GameRenderer::getRendertypeLinesShader)
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
        DefaultVertexFormat.POSITION_COLOR,
        RenderConfiguration.PrimitiveType.QUADS,
        256,
        false,
        true,
        RenderStateConfiguration.builder()
            .useShader(GameRenderer::getPositionColorShader)
            .useTranslucentTransparency()
            .disableCulling()
            .useLessThanOrEqualDepthTest()
            .disableDepthMask()
            .build()
    );
    private static final RenderConfiguration QUADS_NO_DEPTH = RenderConfiguration.create(
        "supermartijn642corelib",
        "quads_no_depth",
        DefaultVertexFormat.POSITION_COLOR,
        RenderConfiguration.PrimitiveType.QUADS,
        256,
        false,
        true,
        RenderStateConfiguration.builder()
            .useShader(GameRenderer::getPositionColorShader)
            .useTranslucentTransparency()
            .disableCulling()
            .disableDepthTest()
            .disableDepthMask()
            .build()
    );

    /**
     * @return the current interpolated camera position
     */
    public static Vec3 getCameraPosition(){
        return ClientUtils.getMinecraft().getEntityRenderDispatcher().camera.getPosition();
    }

    /**
     * @return the current interpolated camera position
     */
    public static MultiBufferSource.BufferSource getMainBufferSource(){
        return ClientUtils.getMinecraft().renderBuffers().bufferSource();
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(PoseStack poseStack, BlockShape shape, float red, float green, float blue, float alpha, boolean depthTest){
        RenderConfiguration renderConfiguration = depthTest ? LINES : LINES_NO_DEPTH;
        MultiBufferSource.BufferSource bufferSource = getMainBufferSource();
        VertexConsumer builder = renderConfiguration.begin(bufferSource);
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            Vec3 normal = new Vec3(x2 - x1, y2 - y1, z2 - z1);
            normal.normalize();
            builder.vertex(matrix, (float)x1, (float)y1, (float)z1).color(red, green, blue, alpha).normal(pose, (float)normal.x, (float)normal.y, (float)normal.z).endVertex();
            builder.vertex(matrix, (float)x2, (float)y2, (float)z2).color(red, green, blue, alpha).normal(pose, (float)normal.x, (float)normal.y, (float)normal.z).endVertex();
        });
        renderConfiguration.end(bufferSource);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(PoseStack poseStack, BlockShape shape, float red, float green, float blue, float alpha, boolean depthTest){
        RenderConfiguration renderConfiguration = depthTest ? QUADS : QUADS_NO_DEPTH;
        MultiBufferSource.BufferSource bufferSource = getMainBufferSource();
        VertexConsumer builder = renderConfiguration.begin(bufferSource);
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
    public static void renderShape(PoseStack poseStack, VoxelShape shape, float red, float green, float blue, float alpha, boolean depthTest){
        renderShape(poseStack, BlockShape.create(shape), red, green, blue, alpha, depthTest);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(PoseStack poseStack, VoxelShape shape, float red, float green, float blue, float alpha, boolean depthTest){
        renderShapeSides(poseStack, BlockShape.create(shape), red, green, blue, alpha, depthTest);
    }

    /**
     * Draws an outline for the given box
     */
    public static void renderBox(PoseStack poseStack, AABB box, float red, float green, float blue, float alpha, boolean depthTest){
        renderShape(poseStack, BlockShape.create(box), red, green, blue, alpha, depthTest);
    }

    /**
     * Draws the sides of the given box
     */
    public static void renderBoxSides(PoseStack poseStack, AABB box, float red, float green, float blue, float alpha, boolean depthTest){
        renderShapeSides(poseStack, BlockShape.create(box), red, green, blue, alpha, depthTest);
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(PoseStack poseStack, BlockShape shape, float red, float green, float blue, boolean depthTest){
        renderShape(poseStack, shape, red, green, blue, 1, depthTest);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(PoseStack poseStack, BlockShape shape, float red, float green, float blue, boolean depthTest){
        renderShapeSides(poseStack, shape, red, green, blue, 1, depthTest);
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(PoseStack poseStack, VoxelShape shape, float red, float green, float blue, boolean depthTest){
        renderShape(poseStack, BlockShape.create(shape), red, green, blue, 1, depthTest);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(PoseStack poseStack, VoxelShape shape, float red, float green, float blue, boolean depthTest){
        renderShapeSides(poseStack, BlockShape.create(shape), red, green, blue, 1, depthTest);
    }

    /**
     * Draws an outline for the given box
     */
    public static void renderBox(PoseStack poseStack, AABB box, float red, float green, float blue, boolean depthTest){
        renderShape(poseStack, BlockShape.create(box), red, green, blue, 1, depthTest);
    }

    /**
     * Draws the sides of the given box
     */
    public static void renderBoxSides(PoseStack poseStack, AABB box, float red, float green, float blue, boolean depthTest){
        renderShapeSides(poseStack, BlockShape.create(box), red, green, blue, 1, depthTest);
    }
}
