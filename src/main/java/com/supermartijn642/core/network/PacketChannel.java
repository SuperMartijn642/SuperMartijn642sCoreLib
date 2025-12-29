package com.supermartijn642.core.network;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                CoreLib.LOGGER.warn("Mod '{}' is creating a packet channel for different modid '{}'!", ModLoadingContext.get().getActiveContainer().getModInfo().getDisplayName(), modid);
        }else if(modid.equals("minecraft") || modid.equals("forge"))
            CoreLib.LOGGER.warn("Mod is creating a packet channel for modid '{}'!", modid);

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
    private final ResourceLocation channelName;
    private final CustomPacketPayload.Type<Payload> payloadType;

    private final List<PacketProperties<?>> packetsByIndex = new ArrayList<>();
    private final Map<Class<? extends BasePacket>,PacketProperties<?>> packetsByClass = new HashMap<>();

    private PacketChannel(String modid, String name){
        this.modid = modid;
        this.name = name;
        this.channelName = ResourceLocation.fromNamespaceAndPath(modid, name);
        this.payloadType = new CustomPacketPayload.Type<>(this.channelName);

        ModLoadingContext.get().getActiveContainer().getEventBus().addListener(this::handleRegistration);
    }

    private void handleRegistration(RegisterPayloadHandlersEvent event){
        StreamCodec<FriendlyByteBuf,Payload> payloadCodec = StreamCodec.of(
            (buffer, payload) -> this.write(payload.packet, buffer),
            buffer -> new Payload(this.read(buffer))
        );
        IPayloadHandler<Payload> serverPayloadHandler = (payload, context) -> {
            PacketContext packetContext = new PacketContext(context);
            this.handle(payload.packet, packetContext, PacketDirection.CLIENT_TO_SERVER);
        };
        IPayloadHandler<Payload> clientPayloadHandler = (payload, context) -> {
            PacketContext packetContext = new PacketContext(context);
            this.handle(payload.packet, packetContext, PacketDirection.SERVER_TO_CLIENT);
        };
        event.registrar(this.modid)
            .versioned("1")
            .commonBidirectional(this.payloadType, payloadCodec, serverPayloadHandler, clientPayloadHandler);
    }

    /**
     * Registers a packet for this channel
     * @param packetClass    class of the packet
     * @param packetSupplier supplier for new packet instances
     * @param direction      direction that the packet is allowed to be sent
     * @param shouldBeQueued whether the packet should be handled on the main thread
     */
    public <T extends BasePacket> void registerMessage(Class<T> packetClass, Supplier<T> packetSupplier, PacketDirection direction, boolean shouldBeQueued){
        if(this.packetsByClass.containsKey(packetClass))
            throw new IllegalArgumentException("Class '" + packetClass + "' has already been registered!");

        int index = this.packetsByIndex.size();
        PacketProperties<T> properties = new PacketProperties<>(index, packetClass, packetSupplier, direction, shouldBeQueued);
        this.packetsByIndex.add(properties);
        this.packetsByClass.put(packetClass, properties);
    }

    /**
     * Registers a packet for this channel
     * @param packetClass    class of the packet
     * @param packetSupplier supplier for new packet instances
     * @param shouldBeQueued whether the packet should be handled on the main thread
     * @deprecated Use {@link #registerMessage(Class, Supplier, PacketDirection, boolean)}.
     */
    @Deprecated
    public <T extends BasePacket> void registerMessage(Class<T> packetClass, Supplier<T> packetSupplier, boolean shouldBeQueued){
        this.registerMessage(packetClass, packetSupplier, PacketDirection.BOTH_WAYS, shouldBeQueued);
    }

    /**
     * Sends the given {@code packet} to the server. Must only be used client-side.
     * @param packet packet to be sent
     */
    public void sendToServer(BasePacket packet){
        this.checkRegistration(packet, PacketDirection.CLIENT_TO_SERVER);
        ClientPacketDistributor.sendToServer(new Payload(packet));
    }

    /**
     * Sends the given {@code packet} to the server. Must only be used client-side.
     * @param connection connection to send the packet along
     * @param packet     packet to be sent
     */
    public void sendToClient(Connection connection, BasePacket packet){
        this.checkRegistration(packet, PacketDirection.SERVER_TO_CLIENT);
        if(connection.getReceiving() == PacketFlow.CLIENTBOUND)
            throw new IllegalArgumentException("This must only be called server-side!");
        if(connection.getPacketListener() instanceof ServerConfigurationPacketListenerImpl listener)
            listener.send(new Payload(packet));
        else if(connection.getPacketListener() instanceof ServerGamePacketListenerImpl listener)
            PacketDistributor.sendToPlayer(listener.player, new Payload(packet));
        else
            throw new IllegalArgumentException("Cannot send packet during the current network stage!");
    }

    /**
     * Sends the given {@code packet} to the server. Must only be used server-side.
     * @param player player to send the packet to
     * @param packet packet to be sent
     */
    public void sendToPlayer(Player player, BasePacket packet){
        if(!(player instanceof ServerPlayer))
            throw new IllegalStateException("This must only be called server-side!");
        this.checkRegistration(packet, PacketDirection.SERVER_TO_CLIENT);
        PacketDistributor.sendToPlayer((ServerPlayer)player, new Payload(packet));
    }

    /**
     * Sends the given {@code packet} to all players. Must only be used server-side.
     * @param packet packet to be sent
     */
    public void sendToAllPlayers(BasePacket packet){
        this.checkRegistration(packet, PacketDirection.SERVER_TO_CLIENT);
        PacketDistributor.sendToAllPlayers(new Payload(packet));
    }

    /**
     * Sends the given {@code packet} to all players in the given {@code dimension}. Must only be used server-side.
     * @param dimension dimension to send the packet to
     * @param packet    packet to be sent
     */
    public void sendToDimension(ResourceKey<Level> dimension, BasePacket packet){
        this.sendToDimension(CommonUtils.getLevel(dimension), packet);
    }

    /**
     * Sends the given {@code packet} to all players in the given {@code world}. Must only be used server-side.
     * @param world  world to send the packet to
     * @param packet packet to be sent
     */
    public void sendToDimension(Level world, BasePacket packet){
        if(!(world instanceof ServerLevel))
            throw new IllegalStateException("This must only be called server-side!");
        this.checkRegistration(packet, PacketDirection.SERVER_TO_CLIENT);
        PacketDistributor.sendToPlayersInDimension((ServerLevel)world, new Payload(packet));
    }

    /**
     * Sends the given {@code packet} to all players tracking the given {@code entity}. Must only be used server-side.
     * @param entity entity which should be tracked
     * @param packet packet to be sent
     */
    public void sendToAllTrackingEntity(Entity entity, BasePacket packet){
        if(entity.level().isClientSide)
            throw new IllegalStateException("This must only be called server-side!");
        this.checkRegistration(packet, PacketDirection.SERVER_TO_CLIENT);
        PacketDistributor.sendToPlayersTrackingEntity(entity, new Payload(packet));
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be sent
     */
    public void sendToAllNear(ResourceKey<Level> world, double x, double y, double z, double radius, BasePacket packet){
        this.sendToAllNear(CommonUtils.getLevel(world), x, y, z, radius, packet);
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be sent
     */
    public void sendToAllNear(ResourceKey<Level> world, BlockPos pos, double radius, BasePacket packet){
        //noinspection DataFlowIssue
        this.sendToAllNear(CommonUtils.getLevel(world), pos, radius, packet);
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be sent
     */
    public void sendToAllNear(Level world, double x, double y, double z, double radius, BasePacket packet){
        if(!(world instanceof ServerLevel))
            throw new IllegalStateException("This must only be called server-side!");
        this.checkRegistration(packet, PacketDirection.SERVER_TO_CLIENT);
        PacketDistributor.sendToPlayersNear((ServerLevel)world, null, x, y, z, radius, new Payload(packet));
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be sent
     */
    public void sendToAllNear(Level world, BlockPos pos, double radius, BasePacket packet){
        if(world.isClientSide)
            throw new IllegalStateException("This must only be called server-side!");
        this.sendToAllNear(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, radius, packet);
    }

    private void checkRegistration(BasePacket packet, PacketDirection direction){
        PacketProperties<?> properties = this.packetsByClass.get(packet.getClass());
        if(properties == null)
            throw new IllegalArgumentException("Tried to send unregistered packet '" + packet.getClass() + "' on channel '" + this.modid + ":" + this.name + "'!");
        if(properties.direction != PacketDirection.BOTH_WAYS && properties.direction != direction)
            throw new IllegalArgumentException("Tried to send packet '" + packet.getClass() + "' on channel '" + this.modid + ":" + this.name + "' in invalid direction '" + direction + "'!");
    }

    void write(BasePacket packet, FriendlyByteBuf buffer){
        // assume the packet has already been checked for registration here
        int index = this.packetsByClass.get(packet.getClass()).index;
        buffer.writeInt(index);
        try{
            packet.write(buffer);
        }catch(Exception e){
            throw new RuntimeException("Encountered an exception whilst writing packet of class '" + packet.getClass().getName() + "' for channel '" + this.modid + ":" + this.name + "'!", e);
        }
    }

    BasePacket read(FriendlyByteBuf buffer){
        int index = buffer.readInt();
        if(this.packetsByIndex.size() < index)
            throw new RuntimeException("Received an unregistered packet with index '" + index + "' on channel '" + this.modid + ":" + this.name + "'!");

        PacketProperties<?> properties = this.packetsByIndex.get(index);
        BasePacket packet = properties.supplier().get();
        try{
            packet.read(buffer);
        }catch(Exception e){
            throw new RuntimeException("Encountered an exception whilst reading packet of class '" + packet.getClass().getName() + "' for channel '" + this.modid + ":" + this.name + "'!", e);
        }
        return packet;
    }

    void handle(BasePacket packet, PacketContext context, PacketDirection direction){
        PacketProperties<?> properties = this.packetsByClass.get(packet.getClass());
        if(properties.direction != PacketDirection.BOTH_WAYS && properties.direction != direction)
            throw new RuntimeException("Received packet of class '" + properties.clazz + "' on channel '" + this.modid + ":" + this.name + "' for invalid direction '" + (direction == PacketDirection.CLIENT_TO_SERVER ? PacketDirection.SERVER_TO_CLIENT : PacketDirection.CLIENT_TO_SERVER) + "'!");

        // Verify packet
        try{
            boolean verify = packet.verify(context);
            if(!verify)
                return;
        }catch(Exception e){
            throw new RuntimeException("Encountered an exception whilst verifying packet of class '" + packet.getClass().getName() + "' for channel '" + this.modid + ":" + this.name + "'!", e);
        }
        // Handle packet
        Runnable handle = () -> {
            try{
                packet.handle(context);
            }catch(Exception e){
                throw new RuntimeException("Encountered an exception whilst processing packet of class '" + packet.getClass().getName() + "' for channel '" + this.modid + ":" + this.name + "'!", e);
            }
        };
        if(properties.shouldBeQueued)
            context.queueTask(handle);
        else
            handle.run();
    }

    /**
     * @param clazz          the packet's class
     * @param supplier       supplied to create new packet instances
     * @param direction      direction that the packet is allowed to be sent
     * @param shouldBeQueued whether the packet should be handled on the main thread or off thread
     */
    private record PacketProperties<T extends BasePacket>(int index, Class<T> clazz, Supplier<T> supplier, PacketDirection direction, boolean shouldBeQueued) {
    }

    class Payload implements CustomPacketPayload {
        private final BasePacket packet;

        private Payload(BasePacket packet){
            this.packet = packet;
        }

        @Override
        public Type<? extends CustomPacketPayload> type(){
            return PacketChannel.this.payloadType;
        }
    }
}
