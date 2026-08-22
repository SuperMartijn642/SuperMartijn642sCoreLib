package com.supermartijn642.core.util.playerprofile;

import com.mojang.authlib.GameProfile;
import com.supermartijn642.core.CommonUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
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
    public UUID getUUID(String username){
        return SkullBlockEntity.fetchGameProfile(username).getNow(Optional.empty()).map(GameProfile::getId).orElse(null);
    }

    /**
     * Gets the username associated with the given uuid.
     */
    @Nullable
    public String getUsername(UUID player){
        return SkullBlockEntity.fetchGameProfile(player).getNow(Optional.empty()).map(GameProfile::getName).orElse(null);
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
    public abstract Skin getSkin(UUID player);

    /**
     * Gets the skin associated with the given uuid.
     */
    public abstract Skin getSkinOrDefault(UUID player);

    /**
     * Dedicated server, logical server
     */
    static final class DedicatedServerPlayerProfileCache extends PlayerProfileCache {
        @Override
        public @Nullable UUID getUUID(String username){
            MinecraftServer server = CommonUtils.getServer();
            if(server == null)
                return null;
            ServerPlayer p = server.getPlayerList().getPlayerByName(username);
            if(p != null)
                return p.getGameProfile().getId();
            return super.getUUID(username);
        }

        @Override
        public @Nullable String getUsername(UUID player){
            MinecraftServer server = CommonUtils.getServer();
            if(server == null)
                return null;
            ServerPlayer p = server.getPlayerList().getPlayer(player);
            if(p != null)
                return p.getGameProfile().getName();
            return super.getUsername(player);
        }

        @Override
        public @Nullable Skin getSkin(UUID player){
            throw new IllegalStateException("Cannot get skin on server!");
        }

        @Override
        public Skin getSkinOrDefault(UUID player){
            throw new IllegalStateException("Cannot get skin on server!");
        }
    }

    @ApiStatus.NonExtendable
    public interface Skin {
        ResourceLocation body();

        @Nullable ResourceLocation cape();

        @Nullable ResourceLocation elytra();

        ModelType modelType();

        enum ModelType {
            SLIM, WIDE
        }
    }
}
