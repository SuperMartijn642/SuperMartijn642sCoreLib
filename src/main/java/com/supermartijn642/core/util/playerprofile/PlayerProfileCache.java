package com.supermartijn642.core.util.playerprofile;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.CoreLib;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@ApiStatus.NonExtendable
public abstract class PlayerProfileCache {

    private static final boolean IS_DEDICATED_SERVER = CommonUtils.getEnvironmentSide().isServer();
    private static final PlayerProfileCache CLIENT = IS_DEDICATED_SERVER ? null : new ClientPlayerProfileCache(), SERVER = new ServerPlayerProfileCache();

    public static PlayerProfileCache get(boolean isClient){
        if(IS_DEDICATED_SERVER && isClient)
            throw new IllegalStateException("Cannot have logical client on dedicated server!");
        return isClient ? CLIENT : SERVER;
    }

    private static final LoadingCache<UUID,CompletableFuture<@Nullable GameProfile>> BY_UUID = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).maximumSize(256).build(
        new CacheLoader<UUID,CompletableFuture<@Nullable GameProfile>>() {
            @Override
            public CompletableFuture<@Nullable GameProfile> load(UUID key){
                return CompletableFuture.supplyAsync(
                    () -> {
                        GameProfile profile = CoreLib.sessionService.fillProfileProperties(new GameProfile(key, null), false);
                        return profile.getName() == null ? null : profile;
                    },
                    Util.backgroundExecutor()
                );
            }
        }
    );
    private static final LoadingCache<String,CompletableFuture<@Nullable GameProfile>> BY_USERNAME = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).maximumSize(256).build(
        new CacheLoader<String,CompletableFuture<@Nullable GameProfile>>() {
            @Override
            public CompletableFuture<@Nullable GameProfile> load(String key){
                return CompletableFuture.supplyAsync(
                    () -> CoreLib.gameProfileCache.get(key),
                    Util.backgroundExecutor()
                );
            }
        }
    );

    PlayerProfileCache(){
    }

    static @Nullable GameProfile getProfile(UUID player){
        net.minecraft.server.management.PlayerProfileCache.ProfileEntry profileEntry = CoreLib.gameProfileCache.getProfileInfo(player);
        if(profileEntry != null)
            return profileEntry.getProfile();
        return BY_UUID.getUnchecked(player).getNow(null);
    }

    /**
     * Gets the uuid associated with the given username.
     */
    @Nullable
    public UUID getUUID(String username){
        net.minecraft.server.management.PlayerProfileCache profileCache = CoreLib.gameProfileCache;
        net.minecraft.server.management.PlayerProfileCache.ProfileEntry gameProfileInfo = profileCache.profilesByName.get(username);
        if(gameProfileInfo != null){
            UUID id = gameProfileInfo.getProfile().getId();
            profileCache.getProfileInfo(id); // Reset last used
            return id;
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
            ServerPlayerEntity p = server.getPlayerList().getPlayerByName(username);
            if(p != null)
                return p.getGameProfile().getId();
            return super.getUUID(username);
        }

        @Override
        public @Nullable String getUsername(UUID player){
            MinecraftServer server = CommonUtils.getServer();
            if(server == null)
                return null;
            ServerPlayerEntity p = server.getPlayerList().getPlayer(player);
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
