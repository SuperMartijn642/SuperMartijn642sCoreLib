package com.supermartijn642.core.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BlockShape;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Created 6/12/2021 by SuperMartijn642
 */
public class RenderUtils {

    private static final RenderType LINES = RenderType.create(
        "supermartijn642corelib:lines",
        RenderSetup.builder(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation("pipeline/lines_translucent")
            .withLocation(Identifier.fromNamespaceAndPath("supermartijn642corelib", "lines"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true))
            .build()
        ).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup()
    );
    private static final RenderType LINES_NO_DEPTH = RenderType.create(
        "supermartijn642corelib:lines_no_depth",
        RenderSetup.builder(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation("pipeline/lines_translucent")
            .withLocation(Identifier.fromNamespaceAndPath("supermartijn642corelib", "lines_no_depth"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
            .build()
        ).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup()
    );
    private static final RenderType QUADS = RenderType.create(
        "supermartijn642corelib:quads",
        RenderSetup.builder(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("supermartijn642corelib", "quads"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .build()
        ).createRenderSetup()
    );
    private static final RenderType QUADS_NO_DEPTH = RenderType.create(
        "supermartijn642corelib:quads_no_depth",
        RenderSetup.builder(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("supermartijn642corelib", "quads_no_depth"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .build()
        ).createRenderSetup()
    );

    /**
     * @return the current interpolated camera position
     */
    public static Vec3 getCameraPosition(){
        return ClientUtils.getMinecraft().getEntityRenderDispatcher().camera.position();
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
                vertexConsumer.addVertex(matrix, (float)x1, (float)y1, (float)z1).setColor(red, green, blue, alpha).setNormal(pose, (float)normal.x, (float)normal.y, (float)normal.z).setLineWidth(1);
                vertexConsumer.addVertex(matrix, (float)x2, (float)y2, (float)z2).setColor(red, green, blue, alpha).setNormal(pose, (float)normal.x, (float)normal.y, (float)normal.z).setLineWidth(1);
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
