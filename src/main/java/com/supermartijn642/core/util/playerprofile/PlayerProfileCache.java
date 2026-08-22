package com.supermartijn642.core.util.playerprofile;

import com.supermartijn642.core.CommonUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract sealed class PlayerProfileCache permits ClientPlayerProfileCache, PlayerProfileCache.DedicatedServerPlayerProfileCache {

    private static final boolean IS_DEDICATED_SERVER = CommonUtils.getEnvironmentSide().isServer();
    private static final PlayerProfileCache CLIENT, SERVER;

    static{
        if(IS_DEDICATED_SERVER){
            CLIENT = null;
            SERVER = new DedicatedServerPlayerProfileCache();
        }else{
            CLIENT = new ClientPlayerProfileCache.Client();
            SERVER = new ClientPlayerProfileCache.Server();
        }
    }

    public static PlayerProfileCache get(boolean isClient){
        if(IS_DEDICATED_SERVER && isClient)
            throw new IllegalStateException("Cannot have logical client on dedicated server!");
        return isClient ? CLIENT : SERVER;
    }

    PlayerProfileCache(){
    }

    /**
     * Gets the uuid associated with the given username.
     */
    @Nullable
    public abstract UUID getUUID(String username);

    /**
     * Gets the username associated with the given uuid.
     */
    @Nullable
    public abstract String getUsername(UUID player);

    /**
     * Gets the username associated with the given uuid.
     */
    @Nullable
    @Contract("_,!null -> !null")
    public String getUsernameOr(UUID player, @Nullable String fallback){
        String username = this.getUsername(player);
        return username == null ? fallback : username;
    }

    /**
     * Gets the skin associated with the given uuid.
     */
    @Nullable
    public abstract PlayerSkin getSkin(UUID player);

    /**
     * Gets the skin associated with the given uuid.
     */
    public abstract PlayerSkin getSkinOrDefault(UUID player);

    /**
     * Dedicated server, logical server
     */
    private static final class DedicatedServerPlayerProfileCache extends PlayerProfileCache {
        @Override
        public @Nullable UUID getUUID(String username){
            MinecraftServer server = CommonUtils.getServer();
            if(server == null)
                return null;
            ServerPlayer p = server.getPlayerList().getPlayer(username);
            if(p != null)
                return p.getGameProfile().id();
            return server.services().nameToIdCache().get(username).map(NameAndId::id).orElse(null);
        }

        @Override
        public @Nullable String getUsername(UUID player){
            MinecraftServer server = CommonUtils.getServer();
            if(server == null)
                return null;
            ServerPlayer p = server.getPlayerList().getPlayer(player);
            if(p != null)
                return p.getGameProfile().name();
            return server.services().nameToIdCache().get(player).map(NameAndId::name).orElse(null);
        }

        @Override
        public @Nullable PlayerSkin getSkin(UUID player){
            throw new IllegalStateException("Cannot get skin on server!");
        }

        @Override
        public PlayerSkin getSkinOrDefault(UUID player){
            throw new IllegalStateException("Cannot get skin on server!");
        }
    }
}
