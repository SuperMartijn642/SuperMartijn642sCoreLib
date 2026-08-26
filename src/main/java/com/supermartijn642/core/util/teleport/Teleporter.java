package com.supermartijn642.core.util.teleport;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility for teleporting entities.
 * <p>
 * Created 25/08/2026 by SuperMartijn642
 */
public final class Teleporter {

    /**
     * Creates a teleporter for the given target.
     * @see TeleportTarget
     */
    public static Teleporter create(TeleportTarget<?> target){
        return new Teleporter(target);
    }

    private final TeleportTarget<?> target;
    private boolean includeMounts, requireMountController, includeMountPassengers, includeMountPlayers;
    private boolean includePassengers, includePassengerPlayers;
    private boolean maintainVelocity;
    private boolean resetFallDistance;

    private Teleporter(TeleportTarget<?> target){
        this.target = target;
    }

    /**
     * Sets whether to include the teleported entity's mounts.
     * @param include                whether to include the teleported entity's mounts
     * @param onlyIfController       if true, the teleported entity's mounts will only be teleported if the entity is the mounts controller
     * @param includeMountPassengers whether to include other passengers of the mounts
     * @param includePlayers         whether to include other player passengers of the mounts
     */
    public Teleporter includeMount(boolean include, boolean onlyIfController, boolean includeMountPassengers, boolean includePlayers){
        this.includeMounts = include;
        this.requireMountController = onlyIfController;
        this.includeMountPassengers = includeMountPassengers;
        this.includeMountPlayers = includePlayers;
        return this;
    }

    /**
     * Sets whether to include the teleported entity's passengers.
     * @param include        whether to include the teleported entity's passengers
     * @param includePlayers whether to include player passengers
     */
    public Teleporter includePassengers(boolean include, boolean includePlayers){
        this.includePassengers = include;
        this.includePassengerPlayers = includePlayers;
        return this;
    }

    /**
     * The teleported entity will maintain its velocity.
     */
    public Teleporter maintainVelocity(){
        this.maintainVelocity = true;
        return this;
    }

    /**
     * The teleported entity's (and its mounts and passengers') fall distance will be reset.
     */
    public Teleporter resetFallDistance(){
        this.resetFallDistance = true;
        return this;
    }

    /**
     * Teleports the given entity.
     * @return a result with {@link Result#succeeded()} true if and only if the given entity was teleported
     */
    public Result teleport(Entity entity){
        if(entity.world.isRemote)
            throw new IllegalStateException("Cannot teleport entities client-side!");

        Supplier<TeleportedEntity> entityTeleporter = Suppliers.memoize(
            () -> {
                if(this.includePassengers)
                    return this.teleportEntityWithPassengers(entity, null, null, this.includePassengerPlayers);
                for(Entity passenger : entity.getPassengers().toArray(new Entity[0]))
                    passenger.dismountRidingEntity();
                Entity newEntity = this.teleportEntity(entity);
                return newEntity == null ? null : new TeleportedEntity(entity, newEntity, Collections.emptyList());
            }
        );
        if(this.includeMounts){
            Entity mount = entity.getRidingEntity();
            if(mount != null && (!this.requireMountController || mount.getControllingPassenger() == entity))
                this.teleportMountWithMountAndPassengers(mount, entity, entityTeleporter);
        }
        TeleportedEntity teleportedEntity = entityTeleporter.get();
        if(teleportedEntity == null)
            return new Result(false, new TeleportedEntity(entity, entity, Collections.emptyList()));
        return new Result(true, teleportedEntity);
    }

    private void teleportMountWithMountAndPassengers(Entity entity, Entity specialPassenger, Supplier<TeleportedEntity> specialPassengerTeleporter){
        Entity mount = entity.getRidingEntity();
        if(mount == null || (this.requireMountController && mount.getControllingPassenger() != entity))
            return;
        Supplier<TeleportedEntity> entityTeleporter = Suppliers.memoize(() -> {
            if(this.includeMountPassengers)
                return this.teleportEntityWithPassengers(entity, specialPassenger, specialPassengerTeleporter, this.includeMountPlayers);
            for(Entity passenger : mount.getPassengers().toArray(new Entity[0]))
                passenger.dismountRidingEntity();
            TeleportedEntity passenger = specialPassengerTeleporter.get();
            if(passenger == null)
                return null;
            Entity newEntity = this.teleportEntity(entity);
            if(newEntity == null)
                return null;
            TeleportedEntity teleportedEntity = TeleportedEntity.of(entity, newEntity, Collections.singletonList(passenger));
            passenger.after().startRiding(newEntity, true);
            passenger.setMount(teleportedEntity);
            return teleportedEntity;
        });
        this.teleportMountWithMountAndPassengers(
            mount,
            entity,
            entityTeleporter
        );
        entityTeleporter.get();
    }

    @Nullable
    private TeleportedEntity teleportEntityWithPassengers(Entity entity, @Nullable Entity specialPassenger, Supplier<TeleportedEntity> specialPassengerTeleporter, boolean includePlayers){
        // Dismount passengers
        List<Entity> passengers = ImmutableList.copyOf(entity.getPassengers());
        for(Entity passenger : passengers)
            passenger.dismountRidingEntity();
        // Teleport special passenger first
        TeleportedEntity teleportedSpecialPassenger = null;
        if(specialPassenger != null){
            if(!passengers.contains(specialPassenger))
                return null;
            teleportedSpecialPassenger = specialPassengerTeleporter.get();
            if(teleportedSpecialPassenger == null)
                return null;
        }
        // Teleport entity
        Entity newEntity = this.teleportEntity(entity);
        if(newEntity == null)
            return null;
        // Teleport passengers
        List<TeleportedEntity> teleportedPassengers = new ArrayList<>(passengers.size());
        for(Entity passenger : passengers){
            passenger.dismountRidingEntity();
            if(!includePlayers && passenger instanceof EntityPlayer && passenger != specialPassenger)
                continue;
            TeleportedEntity teleportedPassenger = passenger == specialPassenger ?
                teleportedSpecialPassenger :
                this.teleportEntityWithPassengers(passenger, null, null, includePlayers);
            if(teleportedPassenger == null)
                continue;
            teleportedPassengers.add(teleportedPassenger);
        }
        teleportedPassengers = ImmutableList.copyOf(teleportedPassengers);
        TeleportedEntity teleportedEntity = TeleportedEntity.of(entity, newEntity, teleportedPassengers);
        // Mount passengers
        for(TeleportedEntity teleportedPassenger : teleportedPassengers){
            teleportedPassenger.after().startRiding(newEntity, true);
            teleportedPassenger.setMount(teleportedEntity);
        }
        return teleportedEntity;
    }

    @Nullable
    private Entity teleportEntity(Entity entity){
        // Get destination properties
        WorldServer targetLevel = (WorldServer)entity.world;
        float targetYaw = this.target.yaw == null ? entity.getRotationYawHead() : this.target.yaw;
        float targetPitch = this.target.pitch == null ? entity.rotationPitch : this.target.pitch;
        Vec3d targetPos;
        if(this.target instanceof TeleportTarget.Entity){
            TeleportTarget.Entity t = (TeleportTarget.Entity)this.target;
            if(t.matchYaw)
                targetYaw = t.target.getRotationYawHead();
            if(t.matchPitch)
                targetPitch = t.target.rotationPitch;
            Vec3d position = t.target.getPositionVector();
            targetPos = new Vec3d(position.x, position.y, position.z);
        }else if(this.target instanceof TeleportTarget.Dimension){
            TeleportTarget.Dimension t = (TeleportTarget.Dimension)this.target;
            targetLevel = t.dimension;
            if(t.pos != null)
                targetPos = t.pos;
            else{
                if(t.applyDimensionCoordinateScaling){
                    double scale = entity.world.provider.getMovementFactor() / targetLevel.provider.getMovementFactor();
                    targetPos = new Vec3d(
                        entity.posX * scale,
                        entity.posY * scale,
                        entity.posZ * scale
                    );
                }else
                    targetPos = new Vec3d(entity.posX, entity.posY, entity.posZ);
            }
        }else if(this.target instanceof TeleportTarget.Position)
            targetPos = ((TeleportTarget.Position)this.target).pos;
        else
            throw new AssertionError("Unknown target class: " + this.target.getClass());

        // Teleport entity
        if(targetLevel == entity.world){
            if(entity instanceof EntityPlayerMP){
                EntityPlayerMP player = (EntityPlayerMP)entity;
                player.closeContainer();
                player.connection.setPlayerLocation(targetPos.x, targetPos.y, targetPos.z, targetYaw, targetPitch);
            }else
                entity.setPositionAndUpdate(targetPos.x, targetPos.y, targetPos.z);
            entity.setRotationYawHead(targetYaw);
            entity.rotationPitch = entity.prevRotationPitch = targetPitch;
            if(!this.maintainVelocity)
                entity.setVelocity(0, 0, 0);
            if(this.resetFallDistance){
                entity.fallDistance = 0;
                entity.onGround = true;
            }
        }else{
            float finalTargetYaw = targetYaw;
            float finalTargetPitch = targetPitch;
            Entity newEntity = entity.changeDimension(
                targetLevel.provider.getDimension(),
                (world, e, yaw) -> e.setLocationAndAngles(targetPos.x, targetPos.y, targetPos.z, finalTargetYaw, finalTargetPitch)
            );
            if(newEntity == null)
                return null;
            if(this.maintainVelocity)
                newEntity.setVelocity(entity.motionX, entity.motionY, entity.motionZ);
            else
                newEntity.setVelocity(0, 0, 0);
            if(Teleporter.this.resetFallDistance){
                newEntity.fallDistance = 0;
                newEntity.onGround = true;
            }else
                newEntity.fallDistance = entity.fallDistance;
            entity = newEntity;
        }
        return entity;
    }

    public static final class Result {
        private final boolean success;
        private final TeleportedEntity entity;
        private List<TeleportedEntity> teleportedEntities;
        private List<TeleportedPlayer> teleportedPlayers;

        public Result(boolean success, TeleportedEntity entity){
            this.success = success;
            this.entity = entity;
        }

        public boolean succeeded(){
            return this.success;
        }

        public TeleportedEntity entity(){
            return this.entity;
        }

        public List<TeleportedEntity> allEntities(){
            if(this.teleportedEntities == null){
                TeleportedEntity root = this.entity;
                while(root.mount() != null)
                    root = root.mount();
                List<TeleportedEntity> teleportedEntities = new ArrayList<>();
                collectPassengers(root, teleportedEntities::add);
                this.teleportedEntities = ImmutableList.copyOf(teleportedEntities);
            }
            return this.teleportedEntities;
        }

        public List<TeleportedPlayer> allPlayers(){
            if(this.teleportedPlayers == null){
                List<TeleportedPlayer> teleportedPlayers = new ArrayList<>();
                for(TeleportedEntity entity : this.allEntities()){
                    if(entity.isPlayer())
                        teleportedPlayers.add((TeleportedPlayer)entity);
                }
                this.teleportedPlayers = ImmutableList.copyOf(teleportedPlayers);
            }
            return this.teleportedPlayers;
        }

        private static void collectPassengers(TeleportedEntity entity, Consumer<TeleportedEntity> consumer){
            consumer.accept(entity);
            entity.passengers().forEach(e -> collectPassengers(e, consumer));
        }
    }

    @ApiStatus.NonExtendable
    public static class TeleportedEntity {

        static TeleportedEntity of(Entity oldEntity, Entity newEntity, List<TeleportedEntity> passengers){
            if(oldEntity instanceof EntityPlayer)
                return new TeleportedPlayer((EntityPlayer)oldEntity, (EntityPlayer)newEntity, passengers);
            return new TeleportedEntity(oldEntity, newEntity, passengers);
        }

        private final Entity oldEntity, newEntity;
        private final List<TeleportedEntity> passengers;
        private @Nullable TeleportedEntity mount;

        private TeleportedEntity(Entity oldEntity, Entity newEntity, List<TeleportedEntity> passengers){
            this.oldEntity = oldEntity;
            this.newEntity = newEntity;
            this.passengers = passengers;
        }

        private void setMount(TeleportedEntity mount){
            this.mount = mount;
        }

        public boolean isPlayer(){
            return false;
        }

        public Entity before(){
            return this.oldEntity;
        }

        public Entity after(){
            return this.newEntity;
        }

        public List<TeleportedEntity> passengers(){
            return this.passengers;
        }

        @Nullable
        @Contract(pure = true)
        public TeleportedEntity mount(){
            return this.mount;
        }
    }

    public static final class TeleportedPlayer extends TeleportedEntity {

        private TeleportedPlayer(EntityPlayer oldEntity, EntityPlayer newEntity, List<TeleportedEntity> passengers){
            super(oldEntity, newEntity, passengers);
        }

        @Override
        public boolean isPlayer(){
            return true;
        }

        @Override
        public EntityPlayer before(){
            return (EntityPlayer)super.before();
        }

        @Override
        public EntityPlayer after(){
            return (EntityPlayer)super.after();
        }
    }
}
