package com.supermartijn642.core.network;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.registry.RegistryUtil;
import io.netty.buffer.ByteBuf;
import io.netty.util.collection.IntObjectHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Created 5/30/2021 by SuperMartijn642
 */
public class PacketChannel {

    private static final HashMap<String,PacketChannel> NAME_TO_CHANNEL = new HashMap<>();

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
        String activeMod = Loader.instance().activeModContainer() == null ? null : Loader.instance().activeModContainer().getModId();
        boolean validActiveMod = activeMod != null && !activeMod.equals("minecraft") && !activeMod.equals("forge");
        if(validActiveMod){
            if(!activeMod.equals(modid))
                CoreLib.LOGGER.warn("Mod '" + Loader.instance().activeModContainer().getName() + "' is creating a packet channel for different modid '" + modid + "'!");
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
        ModContainer modContainer = Loader.instance().activeModContainer();
        return create(modContainer == null ? "unknown" : modContainer.getModId(), "main");
    }

    private final String modid, name;
    private final SimpleNetworkWrapper channel;

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
        this.channel = NetworkRegistry.INSTANCE.newSimpleChannel(modid + ":" + name);
        this.channel.registerMessage(new InternalPacket(this), InternalPacket.class, 0, Side.SERVER);
        this.channel.registerMessage(new InternalPacket(this), InternalPacket.class, 1, Side.CLIENT);

        NAME_TO_CHANNEL.put(modid + ":" + name, this);
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
        this.channel.sendToServer(new InternalPacket(this).setPacket(packet));
    }

    /**
     * Sends the given {@code packet} to the server. Must only be used server-side.
     * @param player player to send the packet to
     * @param packet packet to be send
     */
    public void sendToPlayer(EntityPlayer player, BasePacket packet){
        if(!(player instanceof EntityPlayerMP))
            throw new IllegalStateException("This must only be called server-side!");
        this.checkRegistration(packet);
        this.channel.sendTo(new InternalPacket(this).setPacket(packet), (EntityPlayerMP)player);
    }

    /**
     * Sends the given {@code packet} to all players. Must only be used server-side.
     * @param packet packet to be send
     */
    public void sendToAllPlayers(BasePacket packet){
        this.checkRegistration(packet);
        this.channel.sendToAll(new InternalPacket(this).setPacket(packet));
    }

    /**
     * Sends the given {@code packet} to all players in the given {@code dimension}. Must only be used server-side.
     * @param dimension dimension id to send the packet to
     * @param packet    packet to be send
     */
    public void sendToDimension(int dimension, BasePacket packet){
        this.checkRegistration(packet);
        this.channel.sendToDimension(new InternalPacket(this).setPacket(packet), dimension);
    }

    /**
     * Sends the given {@code packet} to all players in the given {@code dimension}. Must only be used server-side.
     * @param dimension dimension to send the packet to
     * @param packet    packet to be send
     * @deprecated Use {@link #sendToDimension(World, BasePacket)} or {@link #sendToDimension(int, BasePacket)} instead
     */
    @Deprecated
    public void sendToDimension(DimensionType dimension, BasePacket packet){
        this.sendToDimension(dimension.getId(), packet);
    }

    /**
     * Sends the given {@code packet} to all players in the given {@code world}. Must only be used server-side.
     * @param world  world to send the packet to
     * @param packet packet to be send
     */
    public void sendToDimension(World world, BasePacket packet){
        if(world.isRemote)
            throw new IllegalStateException("This must only be called server-side!");
        this.sendToDimension(world.provider.getDimension(), packet);
    }

    /**
     * Sends the given {@code packet} to all players tracking the given {@code entity}. Must only be used server-side.
     * @param entity entity which should be tracked
     * @param packet packet to be send
     */
    public void sendToAllTrackingEntity(Entity entity, BasePacket packet){
        if(entity.world.isRemote)
            throw new IllegalStateException("This must only be called server-side!");
        this.checkRegistration(packet);
        this.channel.sendToAllTracking(new InternalPacket(this).setPacket(packet), entity);
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be send
     */
    public void sendToAllNear(DimensionType world, double x, double y, double z, double radius, BasePacket packet){
        this.checkRegistration(packet);
        this.channel.sendToAllAround(new InternalPacket(this).setPacket(packet), new NetworkRegistry.TargetPoint(world.getId(), x, y, z, radius));
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be send
     */
    public void sendToAllNear(DimensionType world, BlockPos pos, double radius, BasePacket packet){
        this.sendToAllNear(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, radius, packet);
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be send
     */
    public void sendToAllNear(World world, double x, double y, double z, double radius, BasePacket packet){
        if(world.isRemote)
            throw new IllegalStateException("This must only be called server-side!");
        this.sendToAllNear(world.provider.getDimensionType(), x, y, z, radius, packet);
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be send
     */
    public void sendToAllNear(World world, BlockPos pos, double radius, BasePacket packet){
        if(world.isRemote)
            throw new IllegalStateException("This must only be called server-side!");
        this.sendToAllNear(world.provider.getDimensionType(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, radius, packet);
    }

    private void checkRegistration(BasePacket packet){
        if(!this.packet_to_index.containsKey(packet.getClass()))
            throw new IllegalArgumentException("Tried to send unregistered packet '" + packet.getClass() + "' on channel '" + this.modid + ":" + this.name + "'!");
    }

    private void write(BasePacket packet, PacketBuffer buffer){
        // assume the packet has already been checked for registration here
        int index = this.packet_to_index.get(packet.getClass());
        buffer.writeInt(index);
        packet.write(buffer);
    }

    private BasePacket read(PacketBuffer buffer){
        int index = buffer.readInt();
        if(!this.index_to_packet.containsKey(index))
            throw new RuntimeException("Received an unregistered packet with index '" + index + "' on channel '" + this.modid + ":" + this.name + "'!");

        BasePacket packet = this.index_to_packet.get(index).get();
        packet.read(buffer);
        return packet;
    }

    private void handle(BasePacket packet, MessageContext messageContext){
        PacketContext context = new PacketContext(messageContext);
        if(packet.verify(context)){
            if(this.packet_to_queued.get(packet.getClass()))
                context.queueTask(() -> packet.handle(context));
            else
                packet.handle(context);
        }
    }

    /**
     * Don't access this, this may change between versions and is only public because the {@link SimpleNetworkWrapper} requires it to be
     */
    @Deprecated
    public static class InternalPacket implements IMessage, IMessageHandler<InternalPacket,IMessage> {

        private PacketChannel channel;
        private BasePacket packet;

        public InternalPacket(){
        }

        public InternalPacket(PacketChannel channel){
            this.channel = channel;
        }

        private InternalPacket setPacket(BasePacket packet){
            this.packet = packet;
            return this;
        }

        @Override
        public void fromBytes(ByteBuf buffer){
            PacketBuffer packetBuffer = new PacketBuffer(buffer);
            this.channel = NAME_TO_CHANNEL.get(packetBuffer.readString(32767));
            if(this.channel == null)
                throw new IllegalStateException("Couldn't find received channel name!");

            this.packet = this.channel.read(packetBuffer);
        }

        @Override
        public void toBytes(ByteBuf buffer){
            PacketBuffer packetBuffer = new PacketBuffer(buffer);
            packetBuffer.writeString(this.channel.modid + ":" + this.channel.name);

            this.channel.write(this.packet, packetBuffer);
        }

        @Override
        public IMessage onMessage(InternalPacket message, MessageContext context){
            this.channel.handle(message.packet, context);
            return null;
        }
    }

}
