package com.supermartijn642.core.network;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.registry.RegistryUtil;
import io.netty.util.collection.IntObjectHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Created 5/30/2021 by SuperMartijn642
 */
public class PacketChannel {

    /**
     * Creates a channel with the given {@code channelName}.
     * @param channelName registry channelName of the channel
     * @return a new channel with the given {@code channelName}
     * @throws IllegalArgumentException if {@code channelName == null}
     */
    public static PacketChannel create(String modid, String channelName){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");
        if(!RegistryUtil.isValidNamespace(channelName))
            throw new IllegalArgumentException("Channel name '" + channelName + "' must only contain characters [a-z0-9_.-]!");
        String activeMod = ModLoadingContext.get().getActiveNamespace();
        if(activeMod != null && !activeMod.equals("minecraft") && !activeMod.equals("forge")){
            if(!activeMod.equals(modid))
                CoreLib.LOGGER.warn("Mod '" + ModLoadingContext.get().getActiveContainer().getModInfo().getDisplayName() + "' is creating a packet channel for different modid '" + modid + "'!");
        }else if(modid.equals("minecraft") || modid.equals("forge"))
            CoreLib.LOGGER.warn("Mod is creating a packet channel for modid '" + modid + "'!");

        return new PacketChannel(modid, channelName);
    }

    /**
     * Creates a new channel.
     * @return a new channel with channel name 'main'
     */
    public static PacketChannel create(String modid){
        return create(modid, "main");
    }

    @Deprecated
    public static PacketChannel create(){
        return create(ModLoadingContext.get().getActiveNamespace(), "main");
    }

    private final String modid, name;
    private final SimpleChannel channel;

    private int index = 0;
    private final HashMap<Class<? extends BasePacket>,Integer> packet_to_index = new HashMap<>();
    private final IntObjectHashMap<Supplier<? extends BasePacket>> index_to_packet = new IntObjectHashMap<>();
    /**
     * Whether a packet should be handled on the main thread or off thread
     */
    private final HashMap<Class<? extends BasePacket>,Boolean> packet_to_queued = new HashMap<>();

    private PacketChannel(String modid, String name){
        this.modid = modid;
        this.name = name;
        this.channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(modid, name), () -> "1", "1"::equals, "1"::equals);
        this.channel.registerMessage(0, InternalPacket.class,
            (message, buffer) -> InternalPacket.write(this, message, buffer),
            buffer -> InternalPacket.read(this, buffer),
            (message, context) -> InternalPacket.handle(this, message, context));
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
        this.channel.sendToServer(new InternalPacket().setPacket(packet));
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
        this.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), new InternalPacket().setPacket(packet));
    }

    /**
     * Sends the given {@code packet} to all players. Must only be used server-side.
     * @param packet packet to be send
     */
    public void sendToAllPlayers(BasePacket packet){
        this.checkRegistration(packet);
        this.channel.send(PacketDistributor.ALL.noArg(), new InternalPacket().setPacket(packet));
    }

    /**
     * Sends the given {@code packet} to all players in the given {@code dimension}. Must only be used server-side.
     * @param dimension dimension to send the packet to
     * @param packet    packet to be send
     */
    public void sendToDimension(ResourceKey<Level> dimension, BasePacket packet){
        this.checkRegistration(packet);
        this.channel.send(PacketDistributor.DIMENSION.with(() -> dimension), new InternalPacket().setPacket(packet));
    }

    /**
     * Sends the given {@code packet} to all players in the given {@code world}. Must only be used server-side.
     * @param world  world to send the packet to
     * @param packet packet to be send
     */
    public void sendToDimension(Level world, BasePacket packet){
        if(world.isClientSide)
            throw new IllegalStateException("This must only be called server-side!");
        this.sendToDimension(world.dimension(), packet);
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
        this.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new InternalPacket().setPacket(packet));
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be send
     */
    public void sendToAllNear(ResourceKey<Level> world, double x, double y, double z, double radius, BasePacket packet){
        this.checkRegistration(packet);
        this.channel.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, radius, world)), new InternalPacket().setPacket(packet));
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be send
     */
    public void sendToAllNear(ResourceKey<Level> world, BlockPos pos, double radius, BasePacket packet){
        this.sendToAllNear(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, radius, packet);
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be send
     */
    public void sendToAllNear(Level world, double x, double y, double z, double radius, BasePacket packet){
        if(world.isClientSide)
            throw new IllegalStateException("This must only be called server-side!");
        this.sendToAllNear(world.dimension(), x, y, z, radius, packet);
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be send
     */
    public void sendToAllNear(Level world, BlockPos pos, double radius, BasePacket packet){
        if(world.isClientSide)
            throw new IllegalStateException("This must only be called server-side!");
        this.sendToAllNear(world.dimension(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, radius, packet);
    }

    private void checkRegistration(BasePacket packet){
        if(!this.packet_to_index.containsKey(packet.getClass()))
            throw new IllegalArgumentException("Tried to send unregistered packet '" + packet.getClass() + "' on channel '" + this.modid + ":" + this.name + "'!");
    }

    private void write(BasePacket packet, FriendlyByteBuf buffer){
        // assume the packet has already been checked for registration here
        int index = this.packet_to_index.get(packet.getClass());
        buffer.writeInt(index);
        packet.write(buffer);
    }

    private BasePacket read(FriendlyByteBuf buffer){
        int index = buffer.readInt();
        if(!this.index_to_packet.containsKey(index))
            throw new RuntimeException("Received an unregistered packet with index '" + index + "' on channel '" + this.modid + ":" + this.name + "'!");

        BasePacket packet = this.index_to_packet.get(index).get();
        packet.read(buffer);
        return packet;
    }

    private void handle(BasePacket packet, Supplier<NetworkEvent.Context> contextSupplier){
        contextSupplier.get().setPacketHandled(true);
        PacketContext context = new PacketContext(contextSupplier.get());
        if(packet.verify(context)){
            if(this.packet_to_queued.get(packet.getClass()))
                context.queueTask(() -> packet.handle(context));
            else
                packet.handle(context);
        }
    }

    private static class InternalPacket {

        public static InternalPacket read(PacketChannel channel, FriendlyByteBuf buffer){
            return new InternalPacket().setPacket(channel.read(buffer));
        }

        public static void write(PacketChannel channel, InternalPacket packet, FriendlyByteBuf buffer){
            channel.write(packet.packet, buffer);
        }

        public static void handle(PacketChannel channel, InternalPacket packet, Supplier<NetworkEvent.Context> context){
            channel.handle(packet.packet, context);
        }

        private BasePacket packet;

        public InternalPacket setPacket(BasePacket packet){
            this.packet = packet;
            return this;
        }
    }

}
