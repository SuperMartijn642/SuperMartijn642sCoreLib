package com.supermartijn642.core.util.playerprofile;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.extensions.PlayerInfoExtension;
import net.minecraft.Util;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Created 27/08/2026 by SuperMartijn642
 */
final class ClientPlayerProfileCache extends PlayerProfileCache {

    private static final LoadingCache<GameProfile,CompletableFuture<Skin>> SKINS = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(30)).maximumSize(256).build( // This expires after 10 minutes, but the session service itself has a cache with longer expiration
        new CacheLoader<>() {
            @Override
            public CompletableFuture<Skin> load(GameProfile key){
                return CompletableFuture.supplyAsync(
                    () -> {
                        GameProfile profile = CommonUtils.getServices().sessionService().fillProfileProperties(key, false);
                        SkinManager skinManager = ClientUtils.getMinecraft().getSkinManager();
                        Map<MinecraftProfileTexture.Type,MinecraftProfileTexture> textures = skinManager.getInsecureSkinInformation(profile);
                        ResourceLocation body = textures.containsKey(MinecraftProfileTexture.Type.SKIN) ?
                            skinManager.registerTexture(textures.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN) :
                            null;
                        ResourceLocation cape = textures.containsKey(MinecraftProfileTexture.Type.CAPE) ?
                            skinManager.registerTexture(textures.get(MinecraftProfileTexture.Type.CAPE), MinecraftProfileTexture.Type.CAPE) :
                            null;
                        ResourceLocation elytra = textures.containsKey(MinecraftProfileTexture.Type.ELYTRA) ?
                            skinManager.registerTexture(textures.get(MinecraftProfileTexture.Type.ELYTRA), MinecraftProfileTexture.Type.ELYTRA) :
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
                    Util.backgroundExecutor()
                );
            }
        }
    );
    private static final Skin STEVE_SKIN = new Skin() {
        @Override
        public ResourceLocation body(){
            return DefaultPlayerSkin.STEVE_SKIN_LOCATION;
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
            return DefaultPlayerSkin.ALEX_SKIN_LOCATION;
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
        ClientPacketListener connection = ClientUtils.getMinecraft().getConnection();
        if(connection != null){
            PlayerInfo playerInfo = connection.getPlayerInfo(username);
            if(playerInfo != null)
                return playerInfo.getProfile().getId();
        }
        return super.getUUID(username);
    }

    @Override
    public @Nullable String getUsername(UUID player){
        ClientPacketListener connection = ClientUtils.getMinecraft().getConnection();
        if(connection != null){
            PlayerInfo playerInfo = connection.getPlayerInfo(player);
            if(playerInfo != null)
                return playerInfo.getProfile().getName();
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
        return DefaultPlayerSkin.isAlexDefault(player) ? ALEX_SKIN : STEVE_SKIN;
    }

    private static @Nullable Skin getSkinFromConnection(UUID player){
        ClientPacketListener connection = ClientUtils.getMinecraft().getConnection();
        if(connection == null)
            return null;
        PlayerInfo playerInfo = connection.getPlayerInfo(player);
        if(playerInfo == null)
            return null;
        return getSkinForPlayerSkin(playerInfo);
    }

    private static Skin getSkinForPlayerSkin(PlayerInfo playerInfo){
        Skin skin = ((PlayerInfoExtension)playerInfo).supermartijn642corelib_getSkin();
        if(skin == null){
            skin = new Skin() {
                @Override
                public ResourceLocation body(){
                    return playerInfo.getSkinLocation();
                }

                @Override
                public @Nullable ResourceLocation cape(){
                    return playerInfo.getCapeLocation();
                }

                @Override
                public @Nullable ResourceLocation elytra(){
                    return playerInfo.getElytraLocation();
                }

                @Override
                public ModelType modelType(){
                    return "slim".equals(playerInfo.getModelName()) ? Skin.ModelType.SLIM : Skin.ModelType.WIDE;
                }
            };
            //noinspection DataFlowIssue
            ((PlayerInfoExtension)(Object)playerInfo).supermartijn642corelib_setSkin(skin);
        }
        return skin;
    }
}
