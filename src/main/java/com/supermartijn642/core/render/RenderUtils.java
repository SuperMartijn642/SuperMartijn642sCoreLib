package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BlockShape;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Created 6/12/2021 by SuperMartijn642
 */
public class RenderUtils {

    private static final RenderType LINES_NO_DEPTH = RenderType.create(
        "supermartijn642corelib:highlight",
        DefaultVertexFormat.POSITION_COLOR_NORMAL,
        VertexFormat.Mode.LINES,
        128,
        true,
        true,
        RenderTypeExtension.getLinesNoDepthCompositeState()
    );

    private static final MultiBufferSource.BufferSource LINE_BUFFER = MultiBufferSource.immediate(new BufferBuilder(128));

    private static boolean depthTest = true;

    public static void enableDepthTest(){
        depthTest = true;
    }

    public static void disableDepthTest(){
        depthTest = false;
    }

    /**
     * @return the current interpolated camera position.
     */
    public static Vec3 getCameraPosition(){
        return ClientUtils.getMinecraft().getEntityRenderDispatcher().camera.getPosition();
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(PoseStack poseStack, BlockShape shape, double x, double y, double z, float red, float green, float blue, float alpha){
        poseStack.pushPose();

        Vec3 camera = getCameraPosition();
        poseStack.translate(x - camera.x, y - camera.y, z - camera.z);

        VertexConsumer builder = LINE_BUFFER.getBuffer(LINES_NO_DEPTH);
        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            Vec3 normal = new Vec3(x2 - x1, y2 - y1, z2 - z1);
            normal.normalize();
            builder.vertex(matrix4f, (float)x1, (float)y1, (float)z1).color(red, green, blue, alpha).normal(matrix3f, (float)normal.x, (float)normal.y, (float)normal.z).endVertex();
            builder.vertex(matrix4f, (float)x2, (float)y2, (float)z2).color(red, green, blue, alpha).normal(matrix3f, (float)normal.x, (float)normal.y, (float)normal.z).endVertex();
        });
        LINE_BUFFER.endBatch();

        poseStack.popPose();

        // TODO this is stupid vv find a better solution
        depthTest = true;
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(PoseStack poseStack, VoxelShape shape, double x, double y, double z, float red, float green, float blue, float alpha){
        renderShape(poseStack, BlockShape.create(shape), x, y, z, red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given box.
     */
    public static void renderBox(PoseStack poseStack, AABB box, double x, double y, double z, float red, float green, float blue, float alpha){
        renderShape(poseStack, BlockShape.create(box), x, y, z, red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(PoseStack poseStack, BlockShape shape, BlockPos pos, float red, float green, float blue, float alpha){
        renderShape(poseStack, shape, pos.getX(), pos.getY(), pos.getZ(), red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(PoseStack poseStack, VoxelShape shape, BlockPos pos, float red, float green, float blue, float alpha){
        renderShape(poseStack, BlockShape.create(shape), pos.getX(), pos.getY(), pos.getZ(), red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given box.
     */
    public static void renderBox(PoseStack poseStack, AABB box, BlockPos pos, float red, float green, float blue, float alpha){
        renderShape(poseStack, BlockShape.create(box), pos.getX(), pos.getY(), pos.getZ(), red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(PoseStack poseStack, BlockShape shape, double x, double y, double z, float red, float green, float blue){
        renderShape(poseStack, shape, x, y, z, red, green, blue, 1);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(PoseStack poseStack, VoxelShape shape, double x, double y, double z, float red, float green, float blue){
        renderShape(poseStack, BlockShape.create(shape), x, y, z, red, green, blue, 1);
    }

    /**
     * Draws an outline for the given box.
     */
    public static void renderBox(PoseStack poseStack, AABB box, double x, double y, double z, float red, float green, float blue){
        renderShape(poseStack, BlockShape.create(box), x, y, z, red, green, blue, 1);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(PoseStack poseStack, BlockShape shape, BlockPos pos, float red, float green, float blue){
        renderShape(poseStack, shape, pos.getX(), pos.getY(), pos.getZ(), red, green, blue, 1);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(PoseStack poseStack, VoxelShape shape, BlockPos pos, float red, float green, float blue){
        renderShape(poseStack, BlockShape.create(shape), pos.getX(), pos.getY(), pos.getZ(), red, green, blue);
    }

    /**
     * Draws an outline for the given box.
     */
    public static void renderBox(PoseStack poseStack, AABB box, BlockPos pos, float red, float green, float blue){
        renderShape(poseStack, BlockShape.create(box), pos.getX(), pos.getY(), pos.getZ(), red, green, blue);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(PoseStack poseStack, BlockShape shape, float red, float green, float blue, float alpha){
        renderShape(poseStack, shape, 0, 0, 0, red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(PoseStack poseStack, VoxelShape shape, float red, float green, float blue, float alpha){
        renderShape(poseStack, BlockShape.create(shape), 0, 0, 0, red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given box.
     */
    public static void renderBox(PoseStack poseStack, AABB box, float red, float green, float blue, float alpha){
        renderShape(poseStack, BlockShape.create(box), 0, 0, 0, red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(PoseStack poseStack, BlockShape shape, float red, float green, float blue){
        renderShape(poseStack, shape, 0, 0, 0, red, green, blue, 1);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(PoseStack poseStack, VoxelShape shape, float red, float green, float blue){
        renderShape(poseStack, BlockShape.create(shape), 0, 0, 0, red, green, blue, 1);
    }

    /**
     * Draws an outline for the given box.
     */
    public static void renderBox(PoseStack poseStack, AABB box, float red, float green, float blue){
        renderShape(poseStack, BlockShape.create(box), 0, 0, 0, red, green, blue, 1);
    }

    private static class RenderTypeExtension extends RenderType {

        // Because stupid protected inner classes
        public RenderTypeExtension(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_){
            super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
        }

        public static CompositeState getLinesNoDepthCompositeState(){
            return RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LINES_SHADER).setLineState(DEFAULT_LINE).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLayeringState(VIEW_OFFSET_Z_LAYERING).setWriteMaskState(COLOR_WRITE).setDepthTestState(getDepthState()).setCullState(NO_CULL).createCompositeState(false);
        }

        private static DepthTestStateShard getDepthState(){
            return new DepthTestStateShard("dependent", 515) {
                @Override
                public void setupRenderState(){
                    if(depthTest)
                        super.setupRenderState();
                }

                @Override
                public void clearRenderState(){
                    if(depthTest)
                        super.clearRenderState();
                }
            };
        }
    }
}
