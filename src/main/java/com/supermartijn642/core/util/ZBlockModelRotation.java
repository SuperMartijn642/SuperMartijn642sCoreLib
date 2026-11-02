package com.supermartijn642.core.util;

import com.mojang.math.OctahedralGroup;
import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockMath;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.EnumMap;
import java.util.Map;

@ApiStatus.Internal
public class ZBlockModelRotation implements ModelState {

    private final Transformation transformation;
    private final Map<Direction,Matrix4fc> faceMapping, inverseFaceMapping;
    private final boolean uvLock;

    public ZBlockModelRotation(OctahedralGroup rotation, boolean uvLock){
        this.transformation = rotation == OctahedralGroup.IDENTITY ? Transformation.identity() : new Transformation(new Matrix4f(rotation.transformation()));
        this.uvLock = uvLock;
        if(uvLock){
            this.faceMapping = new EnumMap<>(Direction.class);
            this.inverseFaceMapping = new EnumMap<>(Direction.class);
            for(Direction direction : Direction.values()){
                Matrix4fc faceRotation = BlockMath.getFaceTransformation(this.transformation, direction).getMatrix();
                this.faceMapping.put(direction, faceRotation);
                this.inverseFaceMapping.put(direction, faceRotation.invertAffine(new Matrix4f()));
            }
        }else
            this.faceMapping = this.inverseFaceMapping = null;
    }

    @Override
    public Transformation transformation(){
        return this.transformation;
    }

    @Override
    public Matrix4fc faceTransformation(Direction direction){
        return this.uvLock ? this.faceMapping.get(direction) : NO_TRANSFORM;
    }

    @Override
    public Matrix4fc inverseFaceTransformation(Direction direction){
        return this.uvLock ? this.inverseFaceMapping.get(direction) : NO_TRANSFORM;
    }
}
