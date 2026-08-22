package com.supermartijn642.core.util.playerprofile;

import com.mojang.authlib.GameProfile;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.extensions.PlayerSkinExtension;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

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
        PlayerSkin playerSkin = ClientUtils.getMinecraft().getSkinManager().getInsecureSkin(profile);
        return player == null ? null : getSkinForPlayerSkin(playerSkin);
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
