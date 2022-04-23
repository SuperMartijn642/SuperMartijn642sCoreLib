package com.supermartijn642.core.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BlockShape;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import org.lwjgl.opengl.GL11;

/**
 * Created 6/12/2021 by SuperMartijn642
 */
public class RenderUtils {

    private static final RenderType LINES = RenderType.create(
        "supermartijn642corelib:lines",
        DefaultVertexFormats.POSITION_COLOR,
        GL11.GL_LINES,
        128,
        RenderTypeExtension.getLinesState()
    );
    private static final RenderType LINES_NO_DEPTH = RenderType.create(
        "supermartijn642corelib:lines_no_depth",
        DefaultVertexFormats.POSITION_COLOR,
        GL11.GL_LINES,
        128,
        RenderTypeExtension.getLinesStateNoDepth()
    );
    private static final RenderType QUADS = RenderType.create(
        "supermartijn642corelib:quads",
        DefaultVertexFormats.POSITION_COLOR,
        GL11.GL_QUADS,
        128,
        RenderTypeExtension.getQuadState()
    );
    private static final RenderType QUADS_NO_DEPTH = RenderType.create(
        "supermartijn642corelib:quads_no_depth",
        DefaultVertexFormats.POSITION_COLOR,
        GL11.GL_QUADS,
        128,
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
    public static Vec3d getCameraPosition(){
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
    public static void renderShape(MatrixStack matrixStack, BlockShape shape, float red, float green, float blue, float alpha){
        IVertexBuilder builder = getMainBufferSource().getBuffer(depthTest ? LINES : LINES_NO_DEPTH);
        Matrix4f matrix4f = matrixStack.last().pose();
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            builder.vertex(matrix4f, (float)x1, (float)y1, (float)z1).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix4f, (float)x2, (float)y2, (float)z2).color(red, green, blue, alpha).endVertex();
        });
        getMainBufferSource().endBatch();
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(MatrixStack matrixStack, BlockShape shape, float red, float green, float blue, float alpha){
        IVertexBuilder builder = getMainBufferSource().getBuffer(depthTest ? QUADS : QUADS_NO_DEPTH);
        Matrix4f matrix = matrixStack.last().pose();
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
        getMainBufferSource().endBatch();
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(MatrixStack poseStack, VoxelShape shape, float red, float green, float blue, float alpha){
        renderShape(poseStack, BlockShape.create(shape), red, green, blue, alpha);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(MatrixStack poseStack, VoxelShape shape, float red, float green, float blue, float alpha){
        renderShapeSides(poseStack, BlockShape.create(shape), red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given box
     */
    public static void renderBox(MatrixStack poseStack, AxisAlignedBB box, float red, float green, float blue, float alpha){
        renderShape(poseStack, BlockShape.create(box), red, green, blue, alpha);
    }

    /**
     * Draws the sides of the given box
     */
    public static void renderBoxSides(MatrixStack poseStack, AxisAlignedBB box, float red, float green, float blue, float alpha){
        renderShapeSides(poseStack, BlockShape.create(box), red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(MatrixStack poseStack, BlockShape shape, float red, float green, float blue){
        renderShape(poseStack, shape, red, green, blue, 1);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(MatrixStack poseStack, BlockShape shape, float red, float green, float blue){
        renderShapeSides(poseStack, shape, red, green, blue, 1);
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(MatrixStack poseStack, VoxelShape shape, float red, float green, float blue){
        renderShape(poseStack, BlockShape.create(shape), red, green, blue, 1);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(MatrixStack poseStack, VoxelShape shape, float red, float green, float blue){
        renderShapeSides(poseStack, BlockShape.create(shape), red, green, blue, 1);
    }

    /**
     * Draws an outline for the given box
     */
    public static void renderBox(MatrixStack poseStack, AxisAlignedBB box, float red, float green, float blue){
        renderShape(poseStack, BlockShape.create(box), red, green, blue, 1);
    }

    /**
     * Draws the sides of the given box
     */
    public static void renderBoxSides(MatrixStack poseStack, AxisAlignedBB box, float red, float green, float blue){
        renderShapeSides(poseStack, BlockShape.create(box), red, green, blue, 1);
    }

    private static class RenderTypeExtension extends RenderType {

        // Because stupid protected inner classes
        public RenderTypeExtension(String p_i225992_1_, VertexFormat p_i225992_2_, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable p_i225992_7_, Runnable p_i225992_8_){
            super(p_i225992_1_, p_i225992_2_, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, p_i225992_7_, p_i225992_8_);
        }

        private static final DepthTestState NO_DEPTH_TEST = new DepthTestState(519){
            @Override
            public void setupRenderState(){
                // Actually disable the depth test
                RenderSystem.disableDepthTest();
                super.setupRenderState();
            }
        };

        public static State getLinesState(){
            return State.builder()
                .setAlphaState(DEFAULT_ALPHA)
                .setLineState(DEFAULT_LINE)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLayeringState(PROJECTION_LAYERING)
                .setCullState(NO_CULL)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        }

        public static State getLinesStateNoDepth(){
            return State.builder()
                .setAlphaState(DEFAULT_ALPHA)
                .setLineState(DEFAULT_LINE)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLayeringState(PROJECTION_LAYERING)
                .setCullState(NO_CULL)
                .setDepthTestState(NO_DEPTH_TEST)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        }

        public static State getQuadState(){
            return State.builder()
                .setAlphaState(DEFAULT_ALPHA)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setTextureState(NO_TEXTURE)
                .setCullState(NO_CULL)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        }

        public static State getQuadStateNoDepth(){
            return State.builder()
                .setAlphaState(DEFAULT_ALPHA)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setTextureState(NO_TEXTURE)
                .setCullState(NO_CULL)
                .setDepthTestState(NO_DEPTH_TEST)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        }
    }
}
