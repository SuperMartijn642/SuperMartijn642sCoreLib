package com.supermartijn642.core.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BlockShape;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;

/**
 * Created 6/12/2021 by SuperMartijn642
 */
public class RenderUtils {

    private static final RenderState.LayerState VIEW_OFFSET_Z_LAYERING = new RenderState.LayerState("view_offset_z_layering", () -> {
        RenderSystem.pushMatrix();
        RenderSystem.scalef(0.99975586F, 0.99975586F, 0.99975586F);
    }, RenderSystem::popMatrix);
    private static final RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY = new RenderState.TransparencyState("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    private static final RenderType LINES_NO_DEPTH = RenderType.makeType(
        "supermartijn642corelib:highlight",
        DefaultVertexFormats.POSITION_COLOR,
        GL11.GL_LINES,
        128,
        RenderType.State.getBuilder().line(new RenderState.LineState(OptionalDouble.of(1))).transparency(TRANSLUCENT_TRANSPARENCY).layer(VIEW_OFFSET_Z_LAYERING).writeMask(new RenderState.WriteMaskState(true, false)).depthTest(new RenderState.DepthTestState(GL11.GL_ALWAYS)).build(false));
    private static final IRenderTypeBuffer.Impl LINE_BUFFER = IRenderTypeBuffer.getImpl(new BufferBuilder(128));

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
        return ClientUtils.getMinecraft().getRenderManager().info.getProjectedView();
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(MatrixStack matrixStack, BlockShape shape, double x, double y, double z, float red, float green, float blue, float alpha){
        matrixStack.push();

        Vec3d camera = getCameraPosition();
        matrixStack.translate(x - camera.x, y - camera.y, z - camera.z);

        IVertexBuilder builder = LINE_BUFFER.getBuffer(LINES_NO_DEPTH);
        Matrix4f matrix4f = matrixStack.getLast().getMatrix();
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            builder.pos(matrix4f, (float)x1, (float)y1, (float)z1).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix4f, (float)x2, (float)y2, (float)z2).color(red, green, blue, alpha).endVertex();
        });
        LINE_BUFFER.finish();

        matrixStack.pop();
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(MatrixStack matrixStack, VoxelShape shape, double x, double y, double z, float red, float green, float blue, float alpha){
        renderShape(matrixStack, BlockShape.create(shape), x, y, z, red, green, blue, alpha);
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
    public static void renderShape(MatrixStack matrixStack, BlockShape shape, BlockPos pos, float red, float green, float blue, float alpha){
        renderShape(matrixStack, shape, pos.getX(), pos.getY(), pos.getZ(), red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(MatrixStack matrixStack, VoxelShape shape, BlockPos pos, float red, float green, float blue, float alpha){
        renderShape(matrixStack, BlockShape.create(shape), pos.getX(), pos.getY(), pos.getZ(), red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given box.
     */
    public static void renderBox(MatrixStack matrixStack, AxisAlignedBB box, BlockPos pos, float red, float green, float blue, float alpha){
        renderShape(matrixStack, BlockShape.create(box), pos.getX(), pos.getY(), pos.getZ(), red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(MatrixStack matrixStack, BlockShape shape, double x, double y, double z, float red, float green, float blue){
        renderShape(matrixStack, shape, x, y, z, red, green, blue, 1);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(MatrixStack matrixStack, VoxelShape shape, double x, double y, double z, float red, float green, float blue){
        renderShape(matrixStack, BlockShape.create(shape), x, y, z, red, green, blue, 1);
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
    public static void renderShape(MatrixStack matrixStack, BlockShape shape, BlockPos pos, float red, float green, float blue){
        renderShape(matrixStack, shape, pos.getX(), pos.getY(), pos.getZ(), red, green, blue, 1);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(MatrixStack matrixStack, VoxelShape shape, BlockPos pos, float red, float green, float blue){
        renderShape(matrixStack, BlockShape.create(shape), pos.getX(), pos.getY(), pos.getZ(), red, green, blue);
    }

    /**
     * Draws an outline for the given box.
     */
    public static void renderBox(MatrixStack matrixStack, AxisAlignedBB box, BlockPos pos, float red, float green, float blue){
        renderShape(matrixStack, BlockShape.create(box), pos.getX(), pos.getY(), pos.getZ(), red, green, blue);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(MatrixStack matrixStack, BlockShape shape, float red, float green, float blue, float alpha){
        renderShape(matrixStack, shape, 0, 0, 0, red, green, blue, alpha);
    }

    /**
     * Draws an outline for the given shape.
     */
    public static void renderShape(MatrixStack matrixStack, VoxelShape shape, float red, float green, float blue, float alpha){
        renderShape(matrixStack, BlockShape.create(shape), 0, 0, 0, red, green, blue, alpha);
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
     * Draws an outline for the given shape.
     */
    public static void renderShape(MatrixStack matrixStack, VoxelShape shape, float red, float green, float blue){
        renderShape(matrixStack, BlockShape.create(shape), 0, 0, 0, red, green, blue, 1);
    }

    /**
     * Draws an outline for the given box.
     */
    public static void renderBox(MatrixStack matrixStack, AxisAlignedBB box, float red, float green, float blue){
        renderShape(matrixStack, BlockShape.create(box), 0, 0, 0, red, green, blue, 1);
    }

}
