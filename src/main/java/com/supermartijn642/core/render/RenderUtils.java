package com.supermartijn642.core.render;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BlockShape;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;

/**
 * Created 6/12/2021 by SuperMartijn642
 */
public class RenderUtils {

    private static final RenderConfiguration LINES = RenderConfiguration.create(
        "supermartijn642corelib",
        "lines",
        DefaultVertexFormats.POSITION_COLOR,
        RenderConfiguration.PrimitiveType.LINES,
        RenderStateConfiguration.builder()
            .useDefaultLineWidth()
            .useTranslucentTransparency()
            .useViewOffsetZLayering()
            .disableCulling()
            .useLessThanOrEqualDepthTest()
            .disableDepthMask()
            .disableLighting()
            .build()
    );
    private static final RenderConfiguration LINES_NO_DEPTH = RenderConfiguration.create(
        "supermartijn642corelib",
        "lines_no_depth",
        DefaultVertexFormats.POSITION_COLOR,
        RenderConfiguration.PrimitiveType.LINES,
        RenderStateConfiguration.builder()
            .useDefaultLineWidth()
            .useTranslucentTransparency()
            .useViewOffsetZLayering()
            .disableCulling()
            .disableDepthTest()
            .disableDepthMask()
            .disableLighting()
            .build()
    );
    private static final RenderConfiguration QUADS = RenderConfiguration.create(
        "supermartijn642corelib",
        "quads",
        DefaultVertexFormats.POSITION_COLOR,
        RenderConfiguration.PrimitiveType.QUADS,
        RenderStateConfiguration.builder()
            .useTranslucentTransparency()
            .disableTexture()
            .disableCulling()
            .useLessThanOrEqualDepthTest()
            .disableDepthMask()
            .disableLighting()
            .build()
    );
    private static final RenderConfiguration QUADS_NO_DEPTH = RenderConfiguration.create(
        "supermartijn642corelib",
        "quads_no_depth",
        DefaultVertexFormats.POSITION_COLOR,
        RenderConfiguration.PrimitiveType.QUADS,
        RenderStateConfiguration.builder()
            .useTranslucentTransparency()
            .disableTexture()
            .disableCulling()
            .disableDepthTest()
            .disableDepthMask()
            .disableLighting()
            .build()
    );

    /**
     * @return the current interpolated camera position
     */
    public static Vec3d getCameraPosition(){
        return ClientUtils.getMinecraft().gameRenderer.getMainCamera().getPosition();
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(BlockShape shape, float red, float green, float blue, float alpha, boolean depthTest){
        RenderConfiguration renderConfiguration = depthTest ? LINES : LINES_NO_DEPTH;
        BufferBuilder builder = renderConfiguration.begin();
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            builder.vertex((float)x1, (float)y1, (float)z1).color(red, green, blue, alpha).endVertex();
            builder.vertex((float)x2, (float)y2, (float)z2).color(red, green, blue, alpha).endVertex();
        });
        renderConfiguration.end();
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(BlockShape shape, float red, float green, float blue, float alpha, boolean depthTest){
        RenderConfiguration renderConfiguration = depthTest ? QUADS : QUADS_NO_DEPTH;
        BufferBuilder builder = renderConfiguration.begin();
        shape.forEachBox(box -> {
            float minX = (float)box.minX, maxX = (float)box.maxX;
            float minY = (float)box.minY, maxY = (float)box.maxY;
            float minZ = (float)box.minZ, maxZ = (float)box.maxZ;

            builder.vertex(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();

            builder.vertex(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();


            builder.vertex(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();

            builder.vertex(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();


            builder.vertex(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();

            builder.vertex(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        });
        renderConfiguration.end();
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(VoxelShape shape, float red, float green, float blue, float alpha, boolean depthTest){
        renderShape(BlockShape.create(shape), red, green, blue, alpha, depthTest);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(VoxelShape shape, float red, float green, float blue, float alpha, boolean depthTest){
        renderShapeSides(BlockShape.create(shape), red, green, blue, alpha, depthTest);
    }

    /**
     * Draws an outline for the given box
     */
    public static void renderBox(AxisAlignedBB box, float red, float green, float blue, float alpha, boolean depthTest){
        renderShape(BlockShape.create(box), red, green, blue, alpha, depthTest);
    }

    /**
     * Draws the sides of the given box
     */
    public static void renderBoxSides(AxisAlignedBB box, float red, float green, float blue, float alpha, boolean depthTest){
        renderShapeSides(BlockShape.create(box), red, green, blue, alpha, depthTest);
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(BlockShape shape, float red, float green, float blue, boolean depthTest){
        renderShape(shape, red, green, blue, 1, depthTest);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(BlockShape shape, float red, float green, float blue, boolean depthTest){
        renderShapeSides(shape, red, green, blue, 1, depthTest);
    }

    /**
     * Draws an outline for the given shape
     */
    public static void renderShape(VoxelShape shape, float red, float green, float blue, boolean depthTest){
        renderShape(BlockShape.create(shape), red, green, blue, 1, depthTest);
    }

    /**
     * Draws the sides of the given shape
     */
    public static void renderShapeSides(VoxelShape shape, float red, float green, float blue, boolean depthTest){
        renderShapeSides(BlockShape.create(shape), red, green, blue, 1, depthTest);
    }

    /**
     * Draws an outline for the given box
     */
    public static void renderBox(AxisAlignedBB box, float red, float green, float blue, boolean depthTest){
        renderShape(BlockShape.create(box), red, green, blue, 1, depthTest);
    }

    /**
     * Draws the sides of the given box
     */
    public static void renderBoxSides(AxisAlignedBB box, float red, float green, float blue, boolean depthTest){
        renderShapeSides(BlockShape.create(box), red, green, blue, 1, depthTest);
    }
}
