package com.supermartijn642.core.network;

import com.supermartijn642.core.CoreSide;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

/**
 * Created 19/03/2022 by SuperMartijn642
 */
class ServerPacketHandler implements ServerPlayNetworking.PlayChannelHandler {

    public static void registerReceiver(ResourceLocation channelName, PacketChannel channel){
        ClientPlayNetworking.registerGlobalReceiver(channelName, new ClientPacketHandler(channel));
    }

    public static void sendPacket(ResourceLocation channelName, PacketChannel channel, BasePacket packet, ServerPlayer target){
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        channel.write(packet, buffer);
        ServerPlayNetworking.send(target, channelName, buffer);
    }

    private final PacketChannel channel;

    public ServerPacketHandler(PacketChannel channel){
        this.channel = channel;
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buffer, PacketSender responseSender){
        BasePacket packet = this.channel.read(buffer);
        PacketContext context = new PacketContext(CoreSide.SERVER, player, server);
        this.channel.handle(packet, context);
    }
}
