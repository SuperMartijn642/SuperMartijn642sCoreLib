package com.supermartijn642.core.util.teleport;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Created 25/08/2026 by SuperMartijn642
 */
public sealed class TeleportTarget<T extends TeleportTarget<T>> {

    /**
     * Teleports to the given entity.
     */
    public static Entity entity(net.minecraft.world.entity.Entity entity){
        return new Entity(entity);
    }

    /**
     * Teleports to the given dimension.
     */
    public static Dimension dimension(ServerLevel level){
        return new Dimension(level);
    }

    /**
     * Teleports to the given position.
     */
    public static Position position(Vector3fc pos){
        return new Position(pos);
    }

    /**
     * Teleports to the given block position.
     */
    public static Position position(BlockPos pos){
        return position(pos.getX() + 0.5f, pos.getY() + 0.1f, pos.getZ() + 0.5f);
    }

    /**
     * Teleports to the given position.
     */
    public static Position position(Vec3 pos){
        return position(pos.x, pos.y, pos.z);
    }

    /**
     * Teleports to the given position.
     */
    public static Position position(double x, double y, double z){
        return position(new Vector3f((float)x, (float)y, (float)z));
    }

    protected Float yaw, pitch;

    /**
     * Sets the target head rotation.
     */
    public T headRotation(float yaw, float pitch){
        return this.headYaw(yaw).headPitch(pitch);
    }

    /**
     * Sets the target head yaw.
     */
    public T headYaw(float yaw){
        this.yaw = yaw;
        return this.self();
    }

    /**
     * Sets the target head pitch.
     */
    public T headPitch(float pitch){
        this.pitch = pitch;
        return this.self();
    }

    private T self(){
        //noinspection unchecked
        return (T)this;
    }

    public static final class Entity extends TeleportTarget<Entity> {

        final net.minecraft.world.entity.Entity target;
        boolean matchYaw, matchPitch;

        private Entity(net.minecraft.world.entity.Entity target){
            this.target = target;
        }

        @Override
        public Entity headYaw(float yaw){
            this.matchYaw = false;
            return super.headYaw(yaw);
        }

        @Override
        public Entity headPitch(float pitch){
            this.matchPitch = false;
            return super.headPitch(pitch);
        }

        /**
         * Sets whether to match the target entity's head rotation.
         */
        public Entity matchHeadRotation(boolean matchYaw, boolean matchPitch){
            this.matchYaw = matchYaw;
            if(matchYaw)
                this.yaw = null;
            this.matchPitch = matchPitch;
            if(matchPitch)
                this.pitch = null;
            return this;
        }

        /**
         * Matches the target entity's head rotation.
         */
        public Entity matchHeadRotation(){
            return this.matchHeadRotation(true, true);
        }
    }

    public static final class Dimension extends TeleportTarget<Dimension> {

        final ServerLevel dimension;
        Vector3f pos;
        boolean applyDimensionCoordinateScaling;

        private Dimension(ServerLevel dimension){
            this.dimension = dimension;
        }

        /**
         * Sets the target position.
         */
        public Dimension pos(Vector3fc pos){
            return this.pos(pos.x(), pos.y(), pos.z());
        }

        /**
         * Sets the target block position.
         */
        public Dimension pos(BlockPos pos){
            return this.pos(pos.getX() + 0.5f, pos.getY() + 0.1f, pos.getZ() + 0.5f);
        }

        /**
         * Sets the target position.
         */
        public Dimension pos(Vec3 pos){
            return this.pos(pos.x, pos.y, pos.z);
        }

        /**
         * Sets the target position.
         */
        public Dimension pos(double x, double y, double z){
            if(this.pos == null)
                this.pos = new Vector3f((float)x, (float)y, (float)z);
            else
                this.pos.set((float)x, (float)y, (float)z);
            this.applyDimensionCoordinateScaling = false;
            return this;
        }

        /**
         * Sets whether to scale any teleported entity's position by the relative dimension coordinate scaling.
         */
        public Dimension applyDimensionCoordinateScaling(boolean apply){
            this.applyDimensionCoordinateScaling = apply;
            this.pos = null;
            return this;
        }
    }

    public static final class Position extends TeleportTarget<Position> {

        final Vector3fc pos;

        public Position(Vector3fc pos){
            this.pos = pos;
        }
    }
}
