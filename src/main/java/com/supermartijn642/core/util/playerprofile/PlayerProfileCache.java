package com.supermartijn642.core.util.playerprofile;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.supermartijn642.core.CommonUtils;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract sealed class PlayerProfileCache permits ClientPlayerProfileCache, PlayerProfileCache.ServerPlayerProfileCache {

    private static final boolean IS_DEDICATED_SERVER = CommonUtils.getEnvironmentSide().isServer();
    private static final PlayerProfileCache CLIENT = IS_DEDICATED_SERVER ? null : new ClientPlayerProfileCache(), SERVER = new ServerPlayerProfileCache();

    public static PlayerProfileCache get(boolean isClient){
        if(IS_DEDICATED_SERVER && isClient)
            throw new IllegalStateException("Cannot have logical client on dedicated server!");
        return isClient ? CLIENT : SERVER;
    }

    private static final LoadingCache<UUID,CompletableFuture<@Nullable GameProfile>> BY_UUID = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(10)).maximumSize(256).build(
        new CacheLoader<>() {
            @Override
            public CompletableFuture<@Nullable GameProfile> load(UUID key){
                return CompletableFuture.supplyAsync(
                    () -> {
                        GameProfile profile = CommonUtils.getServices().sessionService().fillProfileProperties(new GameProfile(key, null), false);
                        return profile.getName() == null ? null : profile;
                    },
                    Util.backgroundExecutor()
                );
            }
        }
    );
    private static final LoadingCache<String,CompletableFuture<@Nullable GameProfile>> BY_USERNAME = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(10)).maximumSize(256).build(
        new CacheLoader<>() {
            @Override
            public CompletableFuture<@Nullable GameProfile> load(String key){
                return CompletableFuture.supplyAsync(
                    () -> CommonUtils.getServices().profileCache().get(key).orElse(null),
                    Util.backgroundExecutor()
                );
            }
        }
    );

    PlayerProfileCache(){
    }

    static @Nullable GameProfile getProfile(UUID player){
        GameProfile profile = CommonUtils.getServices().profileCache().get(player).orElse(null);
        if(profile != null)
            return profile;
        return BY_UUID.getUnchecked(player).getNow(null);
    }

    /**
     * Gets the uuid associated with the given username.
     */
    @Nullable
    public UUID getUUID(String username){
        GameProfileCache profileCache = CommonUtils.getServices().profileCache();
        GameProfileCache.GameProfileInfo gameProfileInfo = profileCache.profilesByName.get(username);
        if(gameProfileInfo != null){
            gameProfileInfo.setLastAccess(profileCache.getNextOperation());
            return gameProfileInfo.getProfile().getId();
        }
        GameProfile profile = BY_USERNAME.getUnchecked(username).getNow(null);
        return profile == null ? null : profile.getId();
    }

    /**
     * Gets the username associated with the given uuid.
     */
    @Nullable
    public String getUsername(UUID player){
        GameProfile profile = getProfile(player);
        return profile == null ? null : profile.getName();
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

    static final class ServerPlayerProfileCache extends PlayerProfileCache {
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
