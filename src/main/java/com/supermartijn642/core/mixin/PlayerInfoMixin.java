package com.supermartijn642.core.mixin;

import com.supermartijn642.core.extensions.PlayerInfoExtension;
import com.supermartijn642.core.util.playerprofile.PlayerProfileCache;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerInfo.class)
public class PlayerInfoMixin implements PlayerInfoExtension {

    @Unique
    private PlayerProfileCache.Skin skin;

    @Override
    public void supermartijn642corelib_setSkin(PlayerProfileCache.Skin skin){
        this.skin = skin;
    }

    @Override
    public PlayerProfileCache.Skin supermartijn642corelib_getSkin(){
        return this.skin;
    }
}
