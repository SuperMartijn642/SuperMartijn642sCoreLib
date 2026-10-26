package com.supermartijn642.core.util.playerprofile;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Created 27/08/2026 by SuperMartijn642
 */
final class ClientPlayerProfileCache extends PlayerProfileCache {

    @Override
    public @Nullable UUID getUUID(String username){
        ClientPacketListener connection = ClientUtils.getMinecraft().getConnection();
        if(connection != null){
            PlayerInfo playerInfo = connection.getPlayerInfo(username);
            if(playerInfo != null)
                return playerInfo.getProfile().id();
        }
        return super.getUUID(username);
    }

    @Override
    public @Nullable String getUsername(UUID player){
        ClientPacketListener connection = ClientUtils.getMinecraft().getConnection();
        if(connection != null){
            PlayerInfo playerInfo = connection.getPlayerInfo(player);
            if(playerInfo != null)
                return playerInfo.getProfile().name();
        }
        return super.getUsername(player);
    }

    @Override
    public @Nullable PlayerSkin getSkin(UUID player){
        ClientPacketListener connection = ClientUtils.getMinecraft().getConnection();
        if(connection != null){
            PlayerInfo playerInfo = connection.getPlayerInfo(player);
            if(playerInfo != null)
                return playerInfo.getSkin();
        }
        return ClientUtils.getMinecraft().playerSkinRenderCache()
            .lookup(ResolvableProfile.createUnresolved(player))
            .getNow(Optional.empty())
            .map(PlayerSkinRenderCache.RenderInfo::playerSkin)
            .orElse(null);
    }

    @Override
    public PlayerSkin getSkinOrDefault(UUID player){
        ClientPacketListener connection = ClientUtils.getMinecraft().getConnection();
        if(connection != null){
            PlayerInfo playerInfo = connection.getPlayerInfo(player);
            if(playerInfo != null)
                return playerInfo.getSkin();
        }
        return ClientUtils.getMinecraft().playerSkinRenderCache()
            .getOrDefault(ResolvableProfile.createUnresolved(player))
            .playerSkin();
    }
}
