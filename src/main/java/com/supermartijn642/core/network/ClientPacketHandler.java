package com.supermartijn642.core.network;

import com.supermartijn642.core.CoreSide;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Created 19/03/2022 by SuperMartijn642
 */
class ClientPacketHandler implements ClientPlayNetworking.PlayChannelHandler {

    public static void registerReceiver(ResourceLocation channelName, PacketChannel channel){
        ClientPlayNetworking.registerGlobalReceiver(channelName, new ClientPacketHandler(channel));
    }

    public static void sendPacket(ResourceLocation channelName, PacketChannel channel, BasePacket packet){
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        channel.write(packet, buffer);
        ClientPlayNetworking.send(channelName, buffer);
    }

    private final PacketChannel channel;

    public ClientPacketHandler(PacketChannel channel){
        this.channel = channel;
    }

    @Override
    public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender responseSender){
        BasePacket packet = this.channel.read(buffer);
        PacketContext context = new PacketContext(CoreSide.CLIENT, null, null);
        this.channel.handle(packet, context);
    }
}
