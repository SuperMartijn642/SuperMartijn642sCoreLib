package com.supermartijn642.core;

import com.supermartijn642.core.gui.ArbitraryPictureInPictureRenderer;
import com.supermartijn642.core.registry.ClientRegistrationHandler;

/**
 * Created 18/03/2022 by SuperMartijn642
 */
public class CoreLibClient {

    public static void init(){
        // Register arbitrary picture in picture renderer
        ClientRegistrationHandler handler = ClientRegistrationHandler.get("supermartijn642corelib");
        handler.registerPictureInPictureRenderer(ArbitraryPictureInPictureRenderer.State.class, ArbitraryPictureInPictureRenderer::new);
    }
}
