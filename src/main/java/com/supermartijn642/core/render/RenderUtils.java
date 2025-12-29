package com.supermartijn642.core.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BlockShape;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

/**
 * Created 6/12/2021 by SuperMartijn642
 */
public class RenderUtils {

    private static final RenderType LINES = RenderType.create(
        "supermartijn642corelib:lines",
        RenderSetup.builder(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("supermartijn642corelib", "lines"))
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .build()
        ).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup()
    );
    private static final RenderType LINES_NO_DEPTH = RenderType.create(
        "supermartijn642corelib:lines_no_depth",
        RenderSetup.builder(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("supermartijn642corelib", "lines_no_depth"))
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build()
        ).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup()
    );
    private static final RenderType QUADS = RenderType.create(
        "supermartijn642corelib:quads",
        RenderSetup.builder(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("supermartijn642corelib", "quads"))
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withDepthWrite(false)
            .build()
        ).createRenderSetup()
    );
    private static final RenderType QUADS_NO_DEPTH = RenderType.create(
        "supermartijn642corelib:quads_no_depth",
        RenderSetup.builder(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("supermartijn642corelib", "quads_no_depth"))
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .build()
        ).createRenderSetup()
    );

    public static final ThreadLocal<MultiBufferSource.BufferSource> GUI_BUFFER_SOURCE_OVERWRITE = new ThreadLocal<>();

    /**
     * @return the current interpolated camera position
     */
    public static Vec3 getCameraPosition(){
        return ClientUtils.getMinecraft().getEntityRenderDispatcher().camera.position();
    }

    /**
     * @return the current interpolated camera position
     */
    public static MultiBufferSource.BufferSource getMainBufferSource(){
        MultiBufferSource.BufferSource bufferSource = GUI_BUFFER_SOURCE_OVERWRITE.get();
        if(bufferSource != null)
            return bufferSource;
        return ClientUtils.getMinecraft().renderBuffers().bufferSource();
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(PoseStack poseStack, BlockShape shape, float red, float green, float blue, float alpha, boolean depthTest){
        RenderType renderType = depthTest ? LINES : LINES_NO_DEPTH;
        MultiBufferSource.BufferSource bufferSource = getMainBufferSource();
        VertexConsumer builder = bufferSource.getBuffer(renderType);
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            Vec3 normal = new Vec3(x2 - x1, y2 - y1, z2 - z1);
            normal.normalize();
            builder.addVertex(matrix, (float)x1, (float)y1, (float)z1).setColor(red, green, blue, alpha).setNormal(pose, (float)normal.x, (float)normal.y, (float)normal.z);
            builder.addVertex(matrix, (float)x2, (float)y2, (float)z2).setColor(red, green, blue, alpha).setNormal(pose, (float)normal.x, (float)normal.y, (float)normal.z);
        });
        bufferSource.endBatch(renderType);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(PoseStack poseStack, BlockShape shape, float red, float green, float blue, float alpha, boolean depthTest){
        RenderType renderType = depthTest ? QUADS : QUADS_NO_DEPTH;
        MultiBufferSource.BufferSource bufferSource = getMainBufferSource();
        VertexConsumer builder = bufferSource.getBuffer(renderType);
        Matrix4f matrix = poseStack.last().pose();
        shape.forEachBox(box -> {
            float minX = (float)box.minX, maxX = (float)box.maxX;
            float minY = (float)box.minY, maxY = (float)box.maxY;
            float minZ = (float)box.minZ, maxZ = (float)box.maxZ;

            builder.addVertex(matrix, minX, minY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, minY, minZ).setColor(red, green, blue, alpha);

            builder.addVertex(matrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);


            builder.addVertex(matrix, minX, minY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, minY, maxZ).setColor(red, green, blue, alpha);

            builder.addVertex(matrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);


            builder.addVertex(matrix, minX, minY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, maxY, minZ).setColor(red, green, blue, alpha);

            builder.addVertex(matrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        });
        bufferSource.endBatch(renderType);
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

    /**
     * Draws an outline for the given shape
     */
    public static void submitShape(OrderedSubmitNodeCollector output, PoseStack poseStack, BlockShape shape, float red, float green, float blue, float alpha, boolean depthTest){
        RenderType renderType = depthTest ? LINES : LINES_NO_DEPTH;
        output.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
            Matrix4f matrix = pose.pose();
            shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
                Vec3 normal = new Vec3(x2 - x1, y2 - y1, z2 - z1);
                normal.normalize();
                vertexConsumer.addVertex(matrix, (float)x1, (float)y1, (float)z1).setColor(red, green, blue, alpha).setNormal(pose, (float)normal.x, (float)normal.y, (float)normal.z);
                vertexConsumer.addVertex(matrix, (float)x2, (float)y2, (float)z2).setColor(red, green, blue, alpha).setNormal(pose, (float)normal.x, (float)normal.y, (float)normal.z);
            });
        });
    }

    /**
     * Draws an outline for the given shape
     */
    public static void submitShapeSides(OrderedSubmitNodeCollector output, PoseStack poseStack, BlockShape shape, float red, float green, float blue, float alpha, boolean depthTest){
        RenderType renderType = depthTest ? QUADS : QUADS_NO_DEPTH;
        output.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
            Matrix4f matrix = pose.pose();
            shape.forEachBox(box -> {
                float minX = (float)box.minX, maxX = (float)box.maxX;
                float minY = (float)box.minY, maxY = (float)box.maxY;
                float minZ = (float)box.minZ, maxZ = (float)box.maxZ;

                vertexConsumer.addVertex(matrix, minX, minY, minZ).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, maxX, minY, minZ).setColor(red, green, blue, alpha);

                vertexConsumer.addVertex(matrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);


                vertexConsumer.addVertex(matrix, minX, minY, minZ).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, minX, minY, maxZ).setColor(red, green, blue, alpha);

                vertexConsumer.addVertex(matrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);


                vertexConsumer.addVertex(matrix, minX, minY, minZ).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, minX, maxY, minZ).setColor(red, green, blue, alpha);

                vertexConsumer.addVertex(matrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
            });
        });
    }
}
