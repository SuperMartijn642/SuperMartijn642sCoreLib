package com.supermartijn642.core;

import com.supermartijn642.core.gui.ArbitraryPictureInPictureRenderer;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import net.fabricmc.api.ClientModInitializer;

/**
 * Created 18/03/2022 by SuperMartijn642
 */
public class CoreLibClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(){
        // Register arbitrary picture in picture renderer
        ClientRegistrationHandler handler = ClientRegistrationHandler.get("supermartijn642corelib");
        handler.registerPictureInPictureRenderer(ArbitraryPictureInPictureRenderer::new);
    }
}
