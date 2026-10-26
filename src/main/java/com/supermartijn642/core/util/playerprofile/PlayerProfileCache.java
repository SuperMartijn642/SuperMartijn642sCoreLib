package com.supermartijn642.core.util.playerprofile;

import com.supermartijn642.core.CommonUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract sealed class PlayerProfileCache permits ClientPlayerProfileCache, PlayerProfileCache.ServerPlayerProfileCache {

    private static final boolean IS_DEDICATED_SERVER = CommonUtils.getEnvironmentSide().isServer();
    private static final PlayerProfileCache CLIENT = IS_DEDICATED_SERVER ? null : new ClientPlayerProfileCache(), SERVER = new ServerPlayerProfileCache();

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
    public UUID getUUID(String username){
        return CommonUtils.getServices().nameToIdCache().get(username).map(NameAndId::id).orElse(null);
    }

    /**
     * Gets the username associated with the given uuid.
     */
    @Nullable
    public String getUsername(UUID player){
        return CommonUtils.getServices().nameToIdCache().get(player).map(NameAndId::name).orElse(null);
    }

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

    static final class ServerPlayerProfileCache extends PlayerProfileCache {
        @Override
        public @Nullable UUID getUUID(String username){
            MinecraftServer server = CommonUtils.getServer();
            if(server == null)
                return null;
            ServerPlayer p = server.getPlayerList().getPlayer(username);
            if(p != null)
                return p.getGameProfile().id();
            return super.getUUID(username);
        }

        @Override
        public @Nullable String getUsername(UUID player){
            MinecraftServer server = CommonUtils.getServer();
            if(server == null)
                return null;
            ServerPlayer p = server.getPlayerList().getPlayer(player);
            if(p != null)
                return p.getGameProfile().name();
            return super.getUsername(player);
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
