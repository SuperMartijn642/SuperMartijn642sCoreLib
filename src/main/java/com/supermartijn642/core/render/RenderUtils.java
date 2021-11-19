package com.supermartijn642.core.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BlockShape;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Created 6/12/2021 by SuperMartijn642
 */
public class RenderUtils {

    private static final RenderType LINES = RenderType.create(
        "supermartijn642corelib:lines",
        DefaultVertexFormat.POSITION_COLOR_NORMAL,
        VertexFormat.Mode.LINES,
        128,
        true,
        true,
        RenderTypeExtension.getLinesState()
    );
    private static final RenderType LINES_NO_DEPTH = RenderType.create(
        "supermartijn642corelib:lines_no_depth",
        DefaultVertexFormat.POSITION_COLOR_NORMAL,
        VertexFormat.Mode.LINES,
        128,
        true,
        true,
        RenderTypeExtension.getLinesStateNoDepth()
    );
    public static final RenderType QUADS = RenderType.create(
        "supermartijn642corelib:quads",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.QUADS,
        256,
        false,
        true,
        RenderTypeExtension.getQuadState()
    );
    public static final RenderType QUADS_NO_DEPTH = RenderType.create(
        "supermartijn642corelib:quads_no_depth",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.QUADS,
        256,
        false,
        true,
        RenderTypeExtension.getQuadStateNoDepth()
    );

    private static boolean depthTest = true;

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
    public static void renderShape(PoseStack poseStack, BlockShape shape, float red, float green, float blue, float alpha){
        VertexConsumer builder = getMainBufferSource().getBuffer(depthTest ? LINES : LINES_NO_DEPTH);
        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            Vec3 normal = new Vec3(x2 - x1, y2 - y1, z2 - z1);
            normal.normalize();
            builder.vertex(matrix4f, (float)x1, (float)y1, (float)z1).color(red, green, blue, alpha).normal(matrix3f, (float)normal.x, (float)normal.y, (float)normal.z).endVertex();
            builder.vertex(matrix4f, (float)x2, (float)y2, (float)z2).color(red, green, blue, alpha).normal(matrix3f, (float)normal.x, (float)normal.y, (float)normal.z).endVertex();
        });
        getMainBufferSource().endBatch(depthTest ? LINES : LINES_NO_DEPTH);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(PoseStack poseStack, BlockShape shape, float red, float green, float blue, float alpha){
        VertexConsumer builder = getMainBufferSource().getBuffer(depthTest ? QUADS : QUADS_NO_DEPTH);
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
        getMainBufferSource().endBatch(depthTest ? QUADS : QUADS_NO_DEPTH);
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(PoseStack poseStack, VoxelShape shape, float red, float green, float blue, float alpha){
        renderShape(poseStack, BlockShape.create(shape), red, green, blue, alpha);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(PoseStack poseStack, VoxelShape shape, float red, float green, float blue, float alpha){
        renderShapeSides(poseStack, BlockShape.create(shape), red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given box
     */
    public static void renderBox(PoseStack poseStack, AABB box, float red, float green, float blue, float alpha){
        renderShape(poseStack, BlockShape.create(box), red, green, blue, alpha);
    }

    /**
     * Draws the sides of the given box
     */
    public static void renderBoxSides(PoseStack poseStack, AABB box, float red, float green, float blue, float alpha){
        renderShapeSides(poseStack, BlockShape.create(box), red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(PoseStack poseStack, BlockShape shape, float red, float green, float blue){
        renderShape(poseStack, shape, red, green, blue, 1);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(PoseStack poseStack, BlockShape shape, float red, float green, float blue){
        renderShapeSides(poseStack, shape, red, green, blue, 1);
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(PoseStack poseStack, VoxelShape shape, float red, float green, float blue){
        renderShape(poseStack, BlockShape.create(shape), red, green, blue, 1);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(PoseStack poseStack, VoxelShape shape, float red, float green, float blue){
        renderShapeSides(poseStack, BlockShape.create(shape), red, green, blue, 1);
    }

    /**
     * Draws an outline for the given box
     */
    public static void renderBox(PoseStack poseStack, AABB box, float red, float green, float blue){
        renderShape(poseStack, BlockShape.create(box), red, green, blue, 1);
    }

    /**
     * Draws the sides of the given box
     */
    public static void renderBoxSides(PoseStack poseStack, AABB box, float red, float green, float blue){
        renderShapeSides(poseStack, BlockShape.create(box), red, green, blue, 1);
    }

    private static class RenderTypeExtension extends RenderType {

        // Because stupid protected inner classes
        public RenderTypeExtension(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_){
            super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
        }

        private static final DepthTestStateShard NO_DEPTH_TEST = new DepthTestStateShard("always", 519){
            @Override
            public void setupRenderState(){
                // Actually disable the depth test
                RenderSystem.disableDepthTest();
                super.setupRenderState();
            }
        };

        public static CompositeState getLinesState(){
            return RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_LINES_SHADER)
                .setLineState(DEFAULT_LINE)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .setCullState(NO_CULL)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        }

        public static CompositeState getLinesStateNoDepth(){
            return RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_LINES_SHADER)
                .setLineState(DEFAULT_LINE)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .setCullState(NO_CULL)
                .setDepthTestState(NO_DEPTH_TEST)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        }

        public static RenderType.CompositeState getQuadState(){
            return RenderType.CompositeState.builder()
                .setShaderState(POSITION_COLOR_SHADER)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setTextureState(NO_TEXTURE)
                .setCullState(NO_CULL)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        }

        public static RenderType.CompositeState getQuadStateNoDepth(){
            return RenderType.CompositeState.builder()
                .setShaderState(POSITION_COLOR_SHADER)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setTextureState(NO_TEXTURE)
                .setCullState(NO_CULL)
                .setDepthTestState(NO_DEPTH_TEST)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        }
    }
}
