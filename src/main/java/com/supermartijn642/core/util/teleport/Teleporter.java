package com.supermartijn642.core.util.teleport;

import com.google.common.base.Suppliers;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
        if(entity.getLevel().isClientSide())
            throw new IllegalStateException("Cannot teleport entities client-side!");

        Supplier<TeleportedEntity> entityTeleporter = Suppliers.memoize(
            () -> {
                if(this.includePassengers)
                    return this.teleportEntityWithPassengers(entity, null, null, this.includePassengerPlayers);
                for(Entity passenger : List.copyOf(entity.getPassengers()))
                    passenger.stopRiding();
                Entity newEntity = this.teleportEntity(entity);
                return newEntity == null ? null : new TeleportedEntity(entity, newEntity, List.of());
            }
        );
        if(this.includeMounts){
            Entity mount = entity.getVehicle();
            if(mount != null && (!this.requireMountController || mount.getControllingPassenger() == entity))
                this.teleportMountWithMountAndPassengers(mount, entity, entityTeleporter);
        }
        TeleportedEntity teleportedEntity = entityTeleporter.get();
        if(teleportedEntity == null)
            return new Result(false, new TeleportedEntity(entity, entity, List.of()));
        return new Result(true, teleportedEntity);
    }

    private void teleportMountWithMountAndPassengers(Entity entity, Entity specialPassenger, Supplier<TeleportedEntity> specialPassengerTeleporter){
        Entity mount = entity.getVehicle();
        if(mount == null || (this.requireMountController && mount.getControllingPassenger() != entity))
            return;
        Supplier<TeleportedEntity> entityTeleporter = Suppliers.memoize(() -> {
            if(this.includeMountPassengers)
                return this.teleportEntityWithPassengers(entity, specialPassenger, specialPassengerTeleporter, this.includeMountPlayers);
            for(Entity passenger : List.copyOf(mount.getPassengers()))
                passenger.stopRiding();
            TeleportedEntity passenger = specialPassengerTeleporter.get();
            if(passenger == null)
                return null;
            Entity newEntity = this.teleportEntity(entity);
            if(newEntity == null)
                return null;
            TeleportedEntity teleportedEntity = TeleportedEntity.of(entity, newEntity, List.of(passenger));
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
        List<Entity> passengers = List.copyOf(entity.getPassengers());
        for(Entity passenger : passengers)
            passenger.stopRiding();
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
            passenger.stopRiding();
            if(!includePlayers && passenger instanceof Player && passenger != specialPassenger)
                continue;
            TeleportedEntity teleportedPassenger = passenger == specialPassenger ?
                teleportedSpecialPassenger :
                this.teleportEntityWithPassengers(passenger, null, null, includePlayers);
            if(teleportedPassenger == null)
                continue;
            teleportedPassengers.add(teleportedPassenger);
        }
        teleportedPassengers = List.copyOf(teleportedPassengers);
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
        ServerLevel targetLevel = (ServerLevel)entity.getLevel();
        float targetYaw = this.target.yaw == null ? entity.getYRot() : this.target.yaw;
        float targetPitch = this.target.pitch == null ? entity.getXRot() : this.target.pitch;
        Vec3 targetPos;
        if(this.target instanceof TeleportTarget.Entity t){
            if(t.matchYaw)
                targetYaw = t.target.getYRot();
            if(t.matchPitch)
                targetPitch = t.target.getXRot();
            Vec3 position = t.target.position();
            targetPos = new Vec3(position.x, position.y, position.z);
        }else if(this.target instanceof TeleportTarget.Dimension t){
            targetLevel = t.dimension;
            if(t.pos != null)
                targetPos = t.pos;
            else{
                if(t.applyDimensionCoordinateScaling){
                    double scale = entity.getLevel().dimensionType().coordinateScale() / targetLevel.dimensionType().coordinateScale();
                    targetPos = new Vec3(
                        entity.getX() * scale,
                        entity.getX() * scale,
                        entity.getX() * scale
                    );
                }else
                    targetPos = new Vec3(entity.getX(), entity.getY(), entity.getZ());
            }
        }else if(this.target instanceof TeleportTarget.Position t)
            targetPos = t.pos;
        else
            throw new AssertionError("Unknown target class: " + this.target.getClass());

        // Teleport entity
        if(targetLevel == entity.getLevel()){
            if(entity instanceof ServerPlayer player){
                player.closeContainer();
                player.teleportTo(targetLevel, targetPos.x(), targetPos.y(), targetPos.z(), targetYaw, targetPitch);
            }else
                entity.teleportTo(targetPos.x(), targetPos.y(), targetPos.z());
            entity.setYHeadRot(targetYaw);
            entity.setXRot(targetPitch);
            if(!this.maintainVelocity)
                entity.setDeltaMovement(Vec3.ZERO);
            if(this.resetFallDistance){
                entity.resetFallDistance();
                entity.setOnGround(true);
            }
        }else{
            if(entity instanceof ServerPlayer player){
                player.closeContainer();
                ChunkPos targetChunkPos = new ChunkPos(SectionPos.blockToSectionCoord((int)Math.floor(targetPos.x())), SectionPos.blockToSectionCoord((int)Math.floor(targetPos.z())));
                targetLevel.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, targetChunkPos, 1, entity.getId());
                player.isChangingDimension = true;
                LevelData levelData = targetLevel.getLevelData();
                player.connection.send(new ClientboundRespawnPacket(
                    targetLevel.dimensionTypeRegistration(),
                    targetLevel.dimension(),
                    BiomeManager.obfuscateSeed(targetLevel.getSeed()),
                    player.gameMode.getGameModeForPlayer(),
                    player.gameMode.getPreviousGameModeForPlayer(),
                    targetLevel.isDebug(),
                    targetLevel.isFlat(),
                    true
                ));
                player.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
                PlayerList playerList = player.server.getPlayerList();
                playerList.sendPlayerPermissionLevel(player);
                ServerLevel oldLevel = player.getLevel();
                oldLevel.removePlayerImmediately(player, Entity.RemovalReason.CHANGED_DIMENSION);
                player.unsetRemoved();
                if(targetLevel.dimension() == Level.NETHER)
                    player.enteredNetherPosition = new Vec3(targetPos.x(), targetPos.y(), targetPos.z());
                player.setLevel(targetLevel);
                player.connection.teleport(targetPos.x(), targetPos.y(), targetPos.z(), targetYaw, targetPitch);
                player.connection.resetPosition();
                if(!this.maintainVelocity)
                    player.setDeltaMovement(Vec3.ZERO);
                targetLevel.addDuringPortalTeleport(player);
                player.triggerDimensionChangeTriggers(oldLevel);
                player.connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
                playerList.sendLevelInfo(player, targetLevel);
                playerList.sendAllPlayerInfo(player);
                for(MobEffectInstance effectInstance : player.getActiveEffects())
                    player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), effectInstance));
                player.lastSentExp = -1;
                player.lastSentHealth = -1;
                player.lastSentFood = -1;
                if(this.resetFallDistance){
                    player.resetFallDistance();
                    player.setOnGround(true);
                }
            }else{
                Entity newEntity = entity.getType().create(targetLevel);
                if(newEntity == null)
                    return null;
                newEntity.restoreFrom(entity);
                newEntity.moveTo(targetPos.x(), targetPos.y(), targetPos.z(), targetYaw, targetPitch);
                newEntity.setYHeadRot(targetYaw);
                newEntity.setXRot(targetPitch);
                newEntity.fallDistance = 0;
                newEntity.setOnGround(true);
                entity.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
                targetLevel.addDuringTeleport(newEntity);
                if(this.resetFallDistance){
                    newEntity.resetFallDistance();
                    newEntity.setOnGround(true);
                }else
                    newEntity.fallDistance = entity.fallDistance;
                entity = newEntity;
            }
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
                this.teleportedEntities = List.copyOf(teleportedEntities);
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
                this.teleportedPlayers = List.copyOf(teleportedPlayers);
            }
            return this.teleportedPlayers;
        }

        private static void collectPassengers(TeleportedEntity entity, Consumer<TeleportedEntity> consumer){
            consumer.accept(entity);
            entity.passengers().forEach(e -> collectPassengers(e, consumer));
        }
    }

    public static sealed class TeleportedEntity {

        static TeleportedEntity of(Entity oldEntity, Entity newEntity, List<TeleportedEntity> passengers){
            if(oldEntity instanceof Player)
                return new TeleportedPlayer((Player)oldEntity, (Player)newEntity, passengers);
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

        private TeleportedPlayer(Player oldEntity, Player newEntity, List<TeleportedEntity> passengers){
            super(oldEntity, newEntity, passengers);
        }

        @Override
        public boolean isPlayer(){
            return true;
        }

        @Override
        public Player before(){
            return (Player)super.before();
        }

        @Override
        public Player after(){
            return (Player)super.after();
        }
    }
}
