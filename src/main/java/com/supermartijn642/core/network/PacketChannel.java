package com.supermartijn642.core.network;

import com.supermartijn642.core.CommonUtils;
import io.netty.util.collection.IntObjectHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Created 5/30/2021 by SuperMartijn642
 */
public class PacketChannel {

    /**
     * Creates a channel with the given {@code registryName}.
     * @param registryName registry name of the channel
     * @return a new channel with the given {@code registryName}
     * @throws IllegalArgumentException if {@code registryName == null}
     */
    public static PacketChannel create(String modid, String registryName){
        if(modid == null || modid.isEmpty())
            throw new IllegalArgumentException("Modid must not be null!");
        if(registryName == null)
            throw new IllegalArgumentException("Registry name must not be null!");
        return new PacketChannel(modid, registryName);
    }

    /**
     * Creates a new channel.
     * @return a new channel with registry name 'main'
     */
    public static PacketChannel create(String modid){
        return create(modid, "main");
    }

    private final ResourceLocation channelName;

    private int index = 0;
    private final HashMap<Class<? extends BasePacket>,Integer> packet_to_index = new HashMap<>();
    private final IntObjectHashMap<Supplier<? extends BasePacket>> index_to_packet = new IntObjectHashMap<>();
    /**
     * Whether a packet should be handled on the main thread or off thread
     */
    private final HashMap<Class<? extends BasePacket>,Boolean> packet_to_queued = new HashMap<>();

    private PacketChannel(String modid, String name){
        this.channelName = new ResourceLocation("modid", "name");

        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
            ClientPacketHandler.registerReceiver(this.channelName, this);
        ServerPacketHandler.registerReceiver(this.channelName, this);
    }

    /**
     * Registers a packet for this channel
     * @param packetClass    class of the packet
     * @param packetSupplier supplier for new packet instances
     * @param shouldBeQueued whether the packet should be handled on the main thread
     */
    public <T extends BasePacket> void registerMessage(Class<T> packetClass, Supplier<T> packetSupplier, boolean shouldBeQueued){
        if(this.packet_to_index.containsKey(packetClass))
            throw new IllegalArgumentException("Class '" + packetClass + "' has already been registered!");

        int index = this.index++;
        this.packet_to_index.put(packetClass, index);
        this.index_to_packet.put(index, packetSupplier);
        this.packet_to_queued.put(packetClass, shouldBeQueued);
    }

    /**
     * Sends the given {@code packet} to the server. Must only be used client-side.
     * @param packet packet to be send
     */
    public void sendToServer(BasePacket packet){
        this.checkRegistration(packet);
        ClientPacketHandler.sendPacket(this.channelName, this, packet);
    }

    /**
     * Sends the given {@code packet} to the server. Must only be used server-side.
     * @param player player to send the packet to
     * @param packet packet to be send
     */
    public void sendToPlayer(Player player, BasePacket packet){
        if(!(player instanceof ServerPlayer))
            throw new IllegalStateException("This must only be called server-side!");
        this.checkRegistration(packet);
        ServerPacketHandler.sendPacket(this.channelName, this, packet, (ServerPlayer)player);
    }

    /**
     * Sends the given {@code packet} to all players. Must only be used server-side.
     * @param packet packet to be send
     */
    public void sendToAllPlayers(BasePacket packet){
        this.checkRegistration(packet);
        PlayerLookup.all(CommonUtils.getServer()).forEach(player -> this.sendToPlayer(player, packet)); // TODO
    }

    /**
     * Sends the given {@code packet} to all players in the given {@code dimension}. Must only be used server-side.
     * @param dimension dimension to send the packet to
     * @param packet    packet to be send
     */
    public void sendToDimension(ResourceKey<Level> dimension, BasePacket packet){
        this.sendToDimension(CommonUtils.getLevel(dimension), packet);
    }

    /**
     * Sends the given {@code packet} to all players in the given {@code world}. Must only be used server-side.
     * @param world  world to send the packet to
     * @param packet packet to be send
     */
    public void sendToDimension(Level world, BasePacket packet){
        if(!(world instanceof ServerLevel))
            throw new IllegalStateException("This must only be called server-side!");
        this.checkRegistration(packet);
        PlayerLookup.world((ServerLevel)world).forEach(player -> this.sendToPlayer(player, packet));
    }

    /**
     * Sends the given {@code packet} to all players tracking the given {@code entity}. Must only be used server-side.
     * @param entity entity which should be tracked
     * @param packet packet to be send
     */
    public void sendToAllTrackingEntity(Entity entity, BasePacket packet){
        if(entity.level.isClientSide)
            throw new IllegalStateException("This must only be called server-side!");
        this.checkRegistration(packet);
        PlayerLookup.tracking(entity).forEach(player -> this.sendToPlayer(player, packet));
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be send
     */
    public void sendToAllNear(ResourceKey<Level> world, double x, double y, double z, double radius, BasePacket packet){
        this.sendToAllNear(CommonUtils.getLevel(world), x, y, z, radius, packet);
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be send
     */
    public void sendToAllNear(ResourceKey<Level> world, BlockPos pos, double radius, BasePacket packet){
        this.sendToAllNear(CommonUtils.getLevel(world), pos, radius, packet);
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be send
     */
    public void sendToAllNear(Level world, double x, double y, double z, double radius, BasePacket packet){
        if(!(world instanceof ServerLevel))
            throw new IllegalStateException("This must only be called server-side!");
        this.checkRegistration(packet);
        PlayerLookup.around((ServerLevel)world, new Vec3(x, y, z), radius).forEach(player -> this.sendToPlayer(player, packet));
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be send
     */
    public void sendToAllNear(Level world, BlockPos pos, double radius, BasePacket packet){
        this.sendToAllNear(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, radius, packet);
    }

    private void checkRegistration(BasePacket packet){
        if(!this.packet_to_index.containsKey(packet.getClass()))
            throw new IllegalArgumentException("Tried to send unregistered packet '" + packet.getClass() + "'!");
    }

    void write(BasePacket packet, FriendlyByteBuf buffer){
        // assume the packet has already been checked for registration here
        int index = this.packet_to_index.get(packet.getClass());
        buffer.writeInt(index);
        packet.write(buffer);
    }

    BasePacket read(FriendlyByteBuf buffer){
        int index = buffer.readInt();
        if(!this.index_to_packet.containsKey(index))
            throw new IllegalStateException("Received an unregistered packet with index '" + index + "'!");

        BasePacket packet = this.index_to_packet.get(index).get();
        packet.read(buffer);
        return packet;
    }

    void handle(BasePacket packet, PacketContext context){
        if(packet.verify(context)){
            if(this.packet_to_queued.get(packet.getClass()))
                context.queueTask(() -> packet.handle(context));
            else
                packet.handle(context);
        }
    }
}
