package com.supermartijn642.core.util.playerprofile;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.extensions.PlayerSkinExtension;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Created 27/08/2026 by SuperMartijn642
 */
sealed abstract class ClientPlayerProfileCache extends PlayerProfileCache {

    /**
     * Client, logical client
     */
    public static final class Client extends ClientPlayerProfileCache {
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
            return SkullBlockEntity.fetchGameProfile(player).getNow(Optional.empty())
                .map(p -> ClientUtils.getMinecraft().getSkinManager().getInsecureSkin(p))
                .map(Client::getSkinForPlayerSkin)
                .orElse(null);
        }

        @Override
        public Skin getSkinOrDefault(UUID player){
            Skin skin = this.getSkin(player);
            if(skin != null)
                return skin;
            return getSkinForPlayerSkin(DefaultPlayerSkin.get(player));
        }

        private static @Nullable Skin getSkinFromConnection(UUID player){
            ClientPacketListener connection = ClientUtils.getMinecraft().getConnection();
            if(connection == null)
                return null;
            PlayerInfo playerInfo = connection.getPlayerInfo(player);
            if(playerInfo == null)
                return null;
            return getSkinForPlayerSkin(playerInfo.getSkin());
        }

        private static Skin getSkinForPlayerSkin(PlayerSkin playerSkin){
            //noinspection DataFlowIssue
            Skin skin = ((PlayerSkinExtension)(Object)playerSkin).supermartijn642corelib_getSkin();
            if(skin == null){
                Skin.ModelType modelType = playerSkin.model() == PlayerSkin.Model.SLIM ? Skin.ModelType.SLIM : Skin.ModelType.WIDE;
                skin = new Skin() {
                    @Override
                    public ResourceLocation body(){
                        return playerSkin.texture();
                    }

                    @Override
                    public @Nullable ResourceLocation cape(){
                        return playerSkin.capeTexture();
                    }

                    @Override
                    public @Nullable ResourceLocation elytra(){
                        return playerSkin.elytraTexture();
                    }

                    @Override
                    public ModelType modelType(){
                        return modelType;
                    }
                };
                //noinspection DataFlowIssue
                ((PlayerSkinExtension)(Object)playerSkin).supermartijn642corelib_setSkin(skin);
            }
            return skin;
        }
    }

    /**
     * Client, logical server
     */
    public static final class Server extends ClientPlayerProfileCache {
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
}
