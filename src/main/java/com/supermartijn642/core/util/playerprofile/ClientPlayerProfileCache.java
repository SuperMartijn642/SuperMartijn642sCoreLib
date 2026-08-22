package com.supermartijn642.core.util.playerprofile;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.extensions.NetworkPlayerInfoExtension;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created 27/08/2026 by SuperMartijn642
 */
final class ClientPlayerProfileCache extends PlayerProfileCache {

    private static final LoadingCache<GameProfile,CompletableFuture<Skin>> SKINS = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).maximumSize(256).build( // This expires after 10 minutes, but the session service itself has a cache with longer expiration
        new CacheLoader<GameProfile,CompletableFuture<Skin>>() {
            @Override
            public CompletableFuture<Skin> load(GameProfile key){
                return CompletableFuture.supplyAsync(
                    () -> {
                        GameProfile profile = CoreLib.sessionService.fillProfileProperties(key, false);
                        SkinManager skinManager = ClientUtils.getMinecraft().getSkinManager();
                        Map<MinecraftProfileTexture.Type,MinecraftProfileTexture> textures = skinManager.loadSkinFromCache(profile);
                        ResourceLocation body = textures.containsKey(MinecraftProfileTexture.Type.SKIN) ?
                            skinManager.loadSkin(textures.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN) :
                            null;
                        ResourceLocation cape = textures.containsKey(MinecraftProfileTexture.Type.CAPE) ?
                            skinManager.loadSkin(textures.get(MinecraftProfileTexture.Type.CAPE), MinecraftProfileTexture.Type.CAPE) :
                            null;
                        ResourceLocation elytra = textures.containsKey(MinecraftProfileTexture.Type.ELYTRA) ?
                            skinManager.loadSkin(textures.get(MinecraftProfileTexture.Type.ELYTRA), MinecraftProfileTexture.Type.ELYTRA) :
                            null;
                        Skin.ModelType modelType = textures.containsKey(MinecraftProfileTexture.Type.SKIN)
                            && "slim".equals(textures.get(MinecraftProfileTexture.Type.SKIN).getMetadata("model")) ?
                            Skin.ModelType.SLIM : Skin.ModelType.WIDE;
                        return new Skin() {
                            @Override
                            public ResourceLocation body(){
                                return body;
                            }

                            @Override
                            public @Nullable ResourceLocation cape(){
                                return cape;
                            }

                            @Override
                            public @Nullable ResourceLocation elytra(){
                                return elytra;
                            }

                            @Override
                            public ModelType modelType(){
                                return modelType;
                            }
                        };
                    },
                    PlayerProfileCache.EXECUTOR
                );
            }
        }
    );
    private static final Skin STEVE_SKIN = new Skin() {
        @Override
        public ResourceLocation body(){
            return DefaultPlayerSkin.TEXTURE_STEVE;
        }

        @Override
        public @Nullable ResourceLocation cape(){
            return null;
        }

        @Override
        public @Nullable ResourceLocation elytra(){
            return null;
        }

        @Override
        public ModelType modelType(){
            return ModelType.WIDE;
        }
    };
    private static final Skin ALEX_SKIN = new Skin() {
        @Override
        public ResourceLocation body(){
            return DefaultPlayerSkin.TEXTURE_ALEX;
        }

        @Override
        public @Nullable ResourceLocation cape(){
            return null;
        }

        @Override
        public @Nullable ResourceLocation elytra(){
            return null;
        }

        @Override
        public ModelType modelType(){
            return ModelType.SLIM;
        }
    };

    @Override
    public @Nullable UUID getUUID(String username){
        NetHandlerPlayClient connection = ClientUtils.getMinecraft().getConnection();
        if(connection != null){
            NetworkPlayerInfo playerInfo = connection.getPlayerInfo(username);
            if(playerInfo != null)
                return playerInfo.getGameProfile().getId();
        }
        return super.getUUID(username);
    }

    @Override
    public @Nullable String getUsername(UUID player){
        NetHandlerPlayClient connection = ClientUtils.getMinecraft().getConnection();
        if(connection != null){
            NetworkPlayerInfo playerInfo = connection.getPlayerInfo(player);
            if(playerInfo != null)
                return playerInfo.getGameProfile().getName();
        }
        return super.getUsername(player);
    }

    @Override
    public @Nullable Skin getSkin(UUID player){
        Skin skin = getSkinFromConnection(player);
        if(skin != null)
            return skin;
        GameProfile profile = getProfile(player);
        if(profile == null)
            return null;
        return SKINS.getUnchecked(profile).getNow(null);
    }

    @Override
    public Skin getSkinOrDefault(UUID player){
        Skin skin = this.getSkin(player);
        if(skin != null)
            return skin;
        return DefaultPlayerSkin.isSlimSkin(player) ? ALEX_SKIN : STEVE_SKIN;
    }

    private static @Nullable Skin getSkinFromConnection(UUID player){
        NetHandlerPlayClient connection = ClientUtils.getMinecraft().getConnection();
        if(connection == null)
            return null;
        NetworkPlayerInfo playerInfo = connection.getPlayerInfo(player);
        if(playerInfo == null)
            return null;
        return getSkinForPlayerSkin(playerInfo);
    }

    private static Skin getSkinForPlayerSkin(NetworkPlayerInfo playerInfo){
        Skin skin = ((NetworkPlayerInfoExtension)playerInfo).supermartijn642corelib_getSkin();
        if(skin == null){
            skin = new Skin() {
                @Override
                public ResourceLocation body(){
                    return playerInfo.getLocationSkin();
                }

                @Override
                public @Nullable ResourceLocation cape(){
                    return playerInfo.getLocationCape();
                }

                @Override
                public @Nullable ResourceLocation elytra(){
                    return playerInfo.getLocationElytra();
                }

                @Override
                public ModelType modelType(){
                    return "slim".equals(playerInfo.getSkinType()) ? Skin.ModelType.SLIM : Skin.ModelType.WIDE;
                }
            };
            ((NetworkPlayerInfoExtension)playerInfo).supermartijn642corelib_setSkin(skin);
        }
        return skin;
    }
}
