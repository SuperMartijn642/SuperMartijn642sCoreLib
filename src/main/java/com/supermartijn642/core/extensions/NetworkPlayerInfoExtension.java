package com.supermartijn642.core.extensions;

import com.supermartijn642.core.util.playerprofile.PlayerProfileCache;

public interface NetworkPlayerInfoExtension {

    void supermartijn642corelib_setSkin(PlayerProfileCache.Skin skin);

    PlayerProfileCache.Skin supermartijn642corelib_getSkin();
}
