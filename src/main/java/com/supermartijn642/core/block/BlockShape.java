package com.supermartijn642.core.block;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created 6/11/2021 by SuperMartijn642
 */
public class BlockShape {

    public static BlockShape create(AxisAlignedBB box){
        return new BlockShape(box);
    }

    public static BlockShape create(VoxelShape box){
        return new BlockShape(box);
    }

    public static BlockShape create(double x1, double y1, double z1, double x2, double y2, double z2){
        return create(VoxelShapes.box(x1, y1, z1, x2, y2, z2));
    }

    /**
     * Creates a shape with coordinates {@code x1 / 16, y1 / 16, z1 / 16, x2 / 16, y2 / 16, z2 / 16}.
     */
    public static BlockShape createBlockShape(double x1, double y1, double z1, double x2, double y2, double z2){
        return create(VoxelShapes.box(x1 / 16, y1 / 16, z1 / 16, x2 / 16, y2 / 16, z2 / 16));
    }

    /**
     * Combines two shapes.
     */
    public static BlockShape or(BlockShape shape, BlockShape... shapes){
        return new BlockShape(VoxelShapes.or(shape.shape, Arrays.stream(shapes).map(s -> s.shape).toArray(VoxelShape[]::new)));
    }

    public static BlockShape fullCube(){
        return new BlockShape(VoxelShapes.block());
    }

    public static BlockShape empty(){
        return new BlockShape(VoxelShapes.empty());
    }

    /**
     * Checks whether the given shapes intersect.
     */
    public static boolean intersects(BlockShape shape1, BlockShape shape2){
        return shape1.intersects(shape2);
    }

    private final VoxelShape shape;

    public BlockShape(VoxelShape shape){
        this.shape = shape;
    }

    public BlockShape(AxisAlignedBB shape){
        this(VoxelShapes.create(shape));
    }

    public BlockShape(List<AxisAlignedBB> shapes){
        this(VoxelShapes.or(VoxelShapes.empty(), shapes.stream().map(VoxelShapes::create).toArray(VoxelShape[]::new)));
    }

    public List<AxisAlignedBB> toBoxes(){
        return this.shape.toAabbs();
    }

    public void forEachBox(Consumer<AxisAlignedBB> action){
        this.toBoxes().forEach(action);
    }

    public void forEachEdge(LineConsumer action){
        this.shape.forAllEdges(action::apply);
    }

    public void forEachCorner(PointConsumer action){
        this.shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            action.apply(x1, y1, z1);
            action.apply(x2, y1, z1);
            action.apply(x1, y1, z2);
            action.apply(x2, y1, z2);
            action.apply(x1, y2, z1);
            action.apply(x2, y2, z1);
            action.apply(x1, y2, z2);
            action.apply(x2, y2, z2);
        });
    }

    /**
     * Creates the smallest box that encapsulate the entire shape.
     */
    public AxisAlignedBB simplify(){
        return this.shape.bounds();
    }

    /**
     * @return the minimum coordinate for the given axis.
     */
    public double getStart(Direction.Axis axis){
        return this.shape.min(axis);
    }

    /**
     * @return the maximum coordinate for the given axis.
     */
    public double getEnd(Direction.Axis axis){
        return this.shape.max(axis);
    }

    public double minX(){
        return this.getStart(Direction.Axis.X);
    }

    public double minY(){
        return this.getStart(Direction.Axis.Y);
    }

    public double minZ(){
        return this.getStart(Direction.Axis.Z);
    }

    public double maxX(){
        return this.getEnd(Direction.Axis.X);
    }

    public double maxY(){
        return this.getEnd(Direction.Axis.Y);
    }

    public double maxZ(){
        return this.getEnd(Direction.Axis.Z);
    }

    /**
     * @return whether the shape has a volume greater than 0.
     */
    public boolean isEmpty(){
        return this.shape.isEmpty();
    }

    public BlockShape offset(double x, double y, double z){
        return new BlockShape(this.shape.move(x, y, z));
    }

    public BlockShape offset(BlockPos pos){
        return new BlockShape(this.shape.move(pos.getX(), pos.getY(), pos.getZ()));
    }

    /**
     * Offsets the shape by 1 in the given direction.
     */
    public BlockShape offset(Direction direction){
        return this.offset(direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    /**
     * Checks whether the shape intersects with the given shape.
     */
    public boolean intersects(BlockShape shape){
        if(this.isEmpty() || shape.isEmpty())
            return false;

        return this.maxX() > shape.minX() && this.minX() < shape.maxX() &&
            this.maxY() > shape.minY() && this.minY() < shape.maxY() &&
            this.maxZ() > shape.minZ() && this.minZ() < shape.maxZ();
    }

    public BlockShape grow(double amount){
        return this.transformBoxes(box -> box.inflate(amount));
    }

    public BlockShape shrink(double amount){
        return this.transformBoxes(box -> box.deflate(amount));
    }

    /**
     * Flips the shape on the given axis.
     */
    public BlockShape flip(Direction.Axis axis){
        return this.transformBoxes(box -> new AxisAlignedBB(
            axis == Direction.Axis.X ? 1 - box.minX : box.minX,
            axis == Direction.Axis.Y ? 1 - box.minY : box.minY,
            axis == Direction.Axis.Z ? 1 - box.minZ : box.minZ,
            axis == Direction.Axis.X ? 1 - box.maxX : box.maxX,
            axis == Direction.Axis.Y ? 1 - box.maxY : box.maxY,
            axis == Direction.Axis.Z ? 1 - box.maxZ : box.maxZ
        ));
    }

    /**
     * Rotates the shape by 90° around the given axis.
     */
    public BlockShape rotate(Direction.Axis axis){
        if(axis == null)
            throw new IllegalArgumentException("axis must not be null!");
        if(axis == Direction.Axis.X)
            return this.transformBoxes(box -> new AxisAlignedBB(box.minX, box.minZ, -box.minY + 1, box.maxX, box.maxZ, -box.maxY + 1));
        if(axis == Direction.Axis.Y)
            return this.transformBoxes(box -> new AxisAlignedBB(-box.minZ + 1, box.minY, box.minX, -box.maxZ + 1, box.maxY, box.maxX));
        if(axis == Direction.Axis.Z)
            return this.transformBoxes(box -> new AxisAlignedBB(box.minY, -box.minX + 1, box.minZ, box.maxY, -box.maxX + 1, box.maxZ));
        return null;
    }

    private BlockShape transformBoxes(Function<AxisAlignedBB,AxisAlignedBB> transformer){
        return new BlockShape(this.toBoxes().stream().map(transformer::apply).collect(Collectors.toList()));
    }

    @Deprecated
    public VoxelShape getUnderlying(){
        return this.shape;
    }

    public interface LineConsumer {

        void apply(double x1, double y1, double z1, double x2, double y2, double z2);
    }

    public interface PointConsumer {

        void apply(double x, double y, double z);
    }
}
