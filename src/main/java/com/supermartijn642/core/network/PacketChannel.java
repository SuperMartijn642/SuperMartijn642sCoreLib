package com.supermartijn642.core.network;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.registry.RegistryUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                CoreLib.LOGGER.warn("Mod '{}' is creating a packet channel for different modid '{}'!", Loader.instance().activeModContainer().getName(), modid);
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
        ModContainer modContainer = Loader.instance().activeModContainer();
        return create(modContainer == null ? "unknown" : modContainer.getModId(), "main");
    }

    private final String modid, name;
    private final ResourceLocation channelName;
    private final SimpleNetworkWrapper channel;

    private final List<PacketProperties<?>> packetsByIndex = new ArrayList<>();
    private final Map<Class<? extends BasePacket>,PacketProperties<?>> packetsByClass = new HashMap<>();

    private PacketChannel(String modid, String name){
        this.modid = modid;
        this.name = name;
        this.channelName = new ResourceLocation(modid, name);
        this.channel = NetworkRegistry.INSTANCE.newSimpleChannel(this.channelName.toString());
        this.channel.registerMessage((message, context) -> {
            this.handle(message.packet, new PacketContext(context), PacketDirection.CLIENT_TO_SERVER);
            return null;
        }, InternalPacket.class, 0, Side.SERVER);
        this.channel.registerMessage((message, context) -> {
            this.handle(message.packet, new PacketContext(context), PacketDirection.SERVER_TO_CLIENT);
            return null;
        }, InternalPacket.class, 1, Side.CLIENT);

        NAME_TO_CHANNEL.put(this.channelName.toString(), this);
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
        this.channel.sendToServer(new InternalPacket(this, packet));
    }

    /**
     * Sends the given {@code packet} to the server. Must only be used server-side.
     * @param player player to send the packet to
     * @param packet packet to be sent
     */
    public void sendToPlayer(EntityPlayer player, BasePacket packet){
        if(!(player instanceof EntityPlayerMP))
            throw new IllegalStateException("This must only be called server-side!");
        this.checkRegistration(packet, PacketDirection.SERVER_TO_CLIENT);
        this.channel.sendTo(new InternalPacket(this, packet), (EntityPlayerMP)player);
    }

    /**
     * Sends the given {@code packet} to all players. Must only be used server-side.
     * @param packet packet to be sent
     */
    public void sendToAllPlayers(BasePacket packet){
        this.checkRegistration(packet, PacketDirection.SERVER_TO_CLIENT);
        this.channel.sendToAll(new InternalPacket(this, packet));
    }

    /**
     * Sends the given {@code packet} to all players in the given {@code dimension}. Must only be used server-side.
     * @param dimension dimension id to send the packet to
     * @param packet    packet to be sent
     */
    public void sendToDimension(int dimension, BasePacket packet){
        this.checkRegistration(packet, PacketDirection.SERVER_TO_CLIENT);
        this.channel.sendToDimension(new InternalPacket(this, packet), dimension);
    }

    /**
     * Sends the given {@code packet} to all players in the given {@code dimension}. Must only be used server-side.
     * @param dimension dimension to send the packet to
     * @param packet    packet to be sent
     * @deprecated Use {@link #sendToDimension(World, BasePacket)} or {@link #sendToDimension(int, BasePacket)} instead
     */
    @Deprecated
    public void sendToDimension(DimensionType dimension, BasePacket packet){
        this.sendToDimension(dimension.getId(), packet);
    }

    /**
     * Sends the given {@code packet} to all players in the given {@code world}. Must only be used server-side.
     * @param world  world to send the packet to
     * @param packet packet to be sent
     */
    public void sendToDimension(World world, BasePacket packet){
        if(world.isRemote)
            throw new IllegalStateException("This must only be called server-side!");
        this.sendToDimension(world.provider.getDimension(), packet);
    }

    /**
     * Sends the given {@code packet} to all players tracking the given {@code entity}. Must only be used server-side.
     * @param entity entity which should be tracked
     * @param packet packet to be sent
     */
    public void sendToAllTrackingEntity(Entity entity, BasePacket packet){
        if(entity.world.isRemote)
            throw new IllegalStateException("This must only be called server-side!");
        this.checkRegistration(packet, PacketDirection.SERVER_TO_CLIENT);
        this.channel.sendToAllTracking(new InternalPacket(this, packet), entity);
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be sent
     */
    public void sendToAllNear(int dimension, double x, double y, double z, double radius, BasePacket packet){
        this.checkRegistration(packet, PacketDirection.SERVER_TO_CLIENT);
        this.channel.sendToAllAround(new InternalPacket(this, packet), new NetworkRegistry.TargetPoint(dimension, x, y, z, radius));
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be sent
     */
    public void sendToAllNear(int dimension, BlockPos pos, double radius, BasePacket packet){
        this.sendToAllNear(dimension, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, radius, packet);
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be sent
     * @deprecated Use {@link #sendToAllNear(World, double, double, double, double, BasePacket)} or {@link #sendToAllNear(int, double, double, double, double, BasePacket)} instead
     */
    @Deprecated
    public void sendToAllNear(DimensionType world, double x, double y, double z, double radius, BasePacket packet){
        this.sendToAllNear(world.getId(), x, y, z, radius, packet);
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be sent
     * @deprecated Use {@link #sendToAllNear(World, BlockPos, double, BasePacket)} or {@link #sendToAllNear(int, BlockPos, double, BasePacket)} instead
     */
    @Deprecated
    public void sendToAllNear(DimensionType world, BlockPos pos, double radius, BasePacket packet){
        this.sendToAllNear(world.getId(), pos, radius, packet);
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be sent
     */
    public void sendToAllNear(World world, double x, double y, double z, double radius, BasePacket packet){
        if(world.isRemote)
            throw new IllegalStateException("This must only be called server-side!");
        this.sendToAllNear(world.provider.getDimension(), x, y, z, radius, packet);
    }

    /**
     * Sends the given {@code packet} to all players tracking the given position in the given {@code world}. Must only be used server-side.
     * @param packet packet to be sent
     */
    public void sendToAllNear(World world, BlockPos pos, double radius, BasePacket packet){
        this.sendToAllNear(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, radius, packet);
    }

    private void checkRegistration(BasePacket packet, PacketDirection direction){
        PacketProperties<?> properties = this.packetsByClass.get(packet.getClass());
        if(properties == null)
            throw new IllegalArgumentException("Tried to send unregistered packet '" + packet.getClass() + "' on channel '" + this.modid + ":" + this.name + "'!");
        if(properties.direction != PacketDirection.BOTH_WAYS && properties.direction != direction)
            throw new IllegalArgumentException("Tried to send packet '" + packet.getClass() + "' on channel '" + this.modid + ":" + this.name + "' in invalid direction '" + direction + "'!");
    }

    private void write(BasePacket packet, PacketBuffer buffer){
        // assume the packet has already been checked for registration here
        int index = this.packetsByClass.get(packet.getClass()).index;
        buffer.writeInt(index);
        try{
            packet.write(buffer);
        }catch(Exception e){
            throw new RuntimeException("Encountered an exception whilst writing packet of class '" + packet.getClass().getName() + "' for channel '" + this.modid + ":" + this.name + "'!", e);
        }
    }

    private BasePacket read(PacketBuffer buffer){
        int index = buffer.readInt();
        if(this.packetsByIndex.size() < index)
            throw new RuntimeException("Received an unregistered packet with index '" + index + "' on channel '" + this.modid + ":" + this.name + "'!");

        PacketProperties<?> properties = this.packetsByIndex.get(index);
        BasePacket packet = properties.supplier.get();
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

    private static class PacketProperties<T extends BasePacket> {
        private final int index;
        /**
         * The packet's class
         */
        private final Class<T> clazz;
        /**
         * Supplier to create new packet instances
         */
        private final Supplier<T> supplier;
        /**
         * Direction that the packet is allowed to be sent
         */
        private final PacketDirection direction;
        /**
         * Whether the packet should be handled on the main thread or off thread
         */
        private final boolean shouldBeQueued;

        private PacketProperties(int index, Class<T> clazz, Supplier<T> supplier, PacketDirection direction, boolean shouldBeQueued){
            this.index = index;
            this.clazz = clazz;
            this.supplier = supplier;
            this.direction = direction;
            this.shouldBeQueued = shouldBeQueued;
        }
    }

    /**
     * Don't access this, this may change between versions and is only public because the {@link SimpleNetworkWrapper} requires it to be
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public static class InternalPacket implements IMessage {

        private PacketChannel channel;
        private BasePacket packet;

        public InternalPacket(PacketChannel channel, BasePacket packet){
            this.channel = channel;
            this.packet = packet;
        }

        @SuppressWarnings("unused")
        public InternalPacket(){
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
    }
}
