package com.supermartijn642.core.block;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created 6/11/2021 by SuperMartijn642
 */
public class BlockShape {

    public static BlockShape create(double x1, double y1, double z1, double x2, double y2, double z2){
        return new BlockShape(new AxisAlignedBB(x1, y1, z1, x2, y2, z2));
    }

    /**
     * Creates a shape with coordinates {@code x1 / 16, y1 / 16, z1 / 16, x2 / 16, y2 / 16, z2 / 16}.
     */
    public static BlockShape createBlockShape(double x1, double y1, double z1, double x2, double y2, double z2){
        return new BlockShape(new AxisAlignedBB(x1 / 16, y1 / 16, z1 / 16, x2 / 16, y2 / 16, z2 / 16));
    }

    /**
     * Combines two shapes.
     */
    public static BlockShape or(BlockShape shape, BlockShape... shapes){
        LinkedList<AxisAlignedBB> boxes = new LinkedList<>(shape.boxes);
        Arrays.stream(shapes).forEach(box -> boxes.addAll(box.boxes));
        return new BlockShape(boxes);
    }

    public static BlockShape fullCube(){
        return new BlockShape(new AxisAlignedBB(0, 0, 0, 1, 1, 1));
    }

    public static BlockShape empty(){
        return new BlockShape();
    }

    /**
     * Checks whether the given shapes intersect.
     */
    public static boolean intersects(BlockShape shape1, BlockShape shape2){
        return shape1.intersects(shape2);
    }

    private final List<AxisAlignedBB> boxes;
    private final AxisAlignedBB simplified;

    public BlockShape(List<AxisAlignedBB> shapes){
        if(shapes == null)
            shapes = Collections.emptyList();

        List<AxisAlignedBB> boxes = new ArrayList<>(shapes.size());
        loop:
        for(int i = 0; i < shapes.size(); i++){
            AxisAlignedBB shape1 = shapes.get(i);
            if(shape1.maxX - shape1.minX == 0 || shape1.maxY - shape1.minY == 0 || shape1.maxZ - shape1.minZ == 0)
                continue;
            for(AxisAlignedBB shape2 : boxes){
                if(shape1.minX >= shape2.minX && shape1.minY >= shape2.minY && shape1.minZ >= shape2.minZ &&
                    shape1.maxX <= shape2.maxX && shape1.maxY <= shape2.maxY && shape1.maxZ <= shape2.maxZ)
                    continue loop;
            }
            for(int j = i + 1; j < shapes.size(); j++){
                AxisAlignedBB shape2 = shapes.get(j);
                if(shape1.minX >= shape2.minX && shape1.minY >= shape2.minY && shape1.minZ >= shape2.minZ &&
                    shape1.maxX <= shape2.maxX && shape1.maxY <= shape2.maxY && shape1.maxZ <= shape2.maxZ)
                    continue loop;
            }
            boxes.add(shape1);
        }
        this.boxes = Collections.unmodifiableList(boxes);

        if(this.boxes.isEmpty())
            this.simplified = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
        else{
            double minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            double maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
            for(AxisAlignedBB box : this.boxes){
                if(box.minX < minX)
                    minX = box.minX;
                if(box.minY < minY)
                    minY = box.minY;
                if(box.minZ < minZ)
                    minZ = box.minZ;
                if(box.maxX > maxX)
                    maxX = box.maxX;
                if(box.maxY > maxY)
                    maxY = box.maxY;
                if(box.maxZ > maxZ)
                    maxZ = box.maxZ;
            }
            this.simplified = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    public BlockShape(AxisAlignedBB shape){
        this(Collections.singletonList(shape));
    }

    public BlockShape(){
        this((List<AxisAlignedBB>)null);
    }

    public List<AxisAlignedBB> toBoxes(){
        return this.boxes;
    }

    public void forEachBox(Consumer<AxisAlignedBB> action){
        this.boxes.forEach(action);
    }

    public void forEachEdge(LineConsumer action){
        this.forEachBox(box -> {
            // bottom
            action.apply(box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ);
            action.apply(box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ);
            action.apply(box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ);
            action.apply(box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ);
            // top
            action.apply(box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ);
            action.apply(box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ);
            action.apply(box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ);
            action.apply(box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ);
            // vertical
            action.apply(box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ);
            action.apply(box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ);
            action.apply(box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ);
            action.apply(box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ);
        });
    }

    public void forEachCorner(PointConsumer action){
        this.forEachBox(box -> {
            action.apply(box.minX, box.minY, box.minZ);
            action.apply(box.maxX, box.minY, box.minZ);
            action.apply(box.minX, box.minY, box.maxZ);
            action.apply(box.maxX, box.minY, box.maxZ);
            action.apply(box.minX, box.maxY, box.minZ);
            action.apply(box.maxX, box.maxY, box.minZ);
            action.apply(box.minX, box.maxY, box.maxZ);
            action.apply(box.maxX, box.maxY, box.maxZ);
        });
    }

    /**
     * Creates the smallest box that encapsulate the entire shape.
     */
    public AxisAlignedBB simplify(){
        return this.simplified;
    }

    /**
     * @return the minimum coordinate for the given axis.
     */
    public double getStart(EnumFacing.Axis axis){
        return axis == EnumFacing.Axis.X ? this.minX() :
            axis == EnumFacing.Axis.Y ? this.minY() :
                axis == EnumFacing.Axis.Z ? this.minZ() :
                    0;
    }

    /**
     * @return the maximum coordinate for the given axis.
     */
    public double getEnd(EnumFacing.Axis axis){
        return axis == EnumFacing.Axis.X ? this.minX() :
            axis == EnumFacing.Axis.Y ? this.minY() :
                axis == EnumFacing.Axis.Z ? this.minZ() :
                    0;
    }

    public double minX(){
        return this.simplified.minX;
    }

    public double minY(){
        return this.simplified.minY;
    }

    public double minZ(){
        return this.simplified.minZ;
    }

    public double maxX(){
        return this.simplified.maxX;
    }

    public double maxY(){
        return this.simplified.maxY;
    }

    public double maxZ(){
        return this.simplified.maxZ;
    }

    /**
     * @return whether the shape has a volume greater than 0.
     */
    public boolean isEmpty(){
        return this.boxes.isEmpty();
    }

    public BlockShape offset(double x, double y, double z){
        return this.transformBoxes(box -> box.offset(x, y, z));
    }

    /**
     * Offsets the shape by 1 in the given direction.
     */
    public BlockShape offset(EnumFacing facing){
        return this.offset(facing.getFrontOffsetX(), facing.getFrontOffsetY(), facing.getFrontOffsetZ());
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
        return this.transformBoxes(box -> box.grow(amount));
    }

    public BlockShape shrink(double amount){
        return this.transformBoxes(box -> box.shrink(amount));
    }

    /**
     * Flips the shape on the given axis.
     */
    public BlockShape flip(EnumFacing.Axis axis){
        return this.transformBoxes(box -> new AxisAlignedBB(
            axis == EnumFacing.Axis.X ? 1 - box.minX : box.minX,
            axis == EnumFacing.Axis.Y ? 1 - box.minY : box.minY,
            axis == EnumFacing.Axis.Z ? 1 - box.minZ : box.minZ,
            axis == EnumFacing.Axis.X ? 1 - box.maxX : box.maxX,
            axis == EnumFacing.Axis.Y ? 1 - box.maxY : box.maxY,
            axis == EnumFacing.Axis.Z ? 1 - box.maxZ : box.maxZ
        ));
    }

    /**
     * Rotates the shape by 90° around the given axis.
     */
    public BlockShape rotate(EnumFacing.Axis axis){
        if(axis == null)
            throw new IllegalArgumentException("axis must not be null!");
        if(axis == EnumFacing.Axis.X)
            return this.transformBoxes(box -> new AxisAlignedBB(box.minX, box.minZ, -box.minY + 1, box.maxX, box.maxZ, -box.maxY + 1));
        if(axis == EnumFacing.Axis.Y)
            return this.transformBoxes(box -> new AxisAlignedBB(-box.minZ + 1, box.minY, box.minX, -box.maxZ + 1, box.maxY, box.maxX));
        if(axis == EnumFacing.Axis.Z)
            return this.transformBoxes(box -> new AxisAlignedBB(box.minY, -box.minX + 1, box.minZ, box.maxY, -box.maxX + 1, box.maxZ));
        return null;
    }

    private BlockShape transformBoxes(Function<AxisAlignedBB,AxisAlignedBB> transformer){
        return new BlockShape(this.toBoxes().stream().map(transformer::apply).collect(Collectors.toList()));
    }

    public interface LineConsumer {

        void apply(double x1, double y1, double z1, double x2, double y2, double z2);
    }

    public interface PointConsumer {

        void apply(double x, double y, double z);
    }
}
