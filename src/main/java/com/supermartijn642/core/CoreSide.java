package com.supermartijn642.core;

import net.fabricmc.api.EnvType;

/**
 * Created 5/30/2021 by SuperMartijn642
 */
public enum CoreSide {
    CLIENT(EnvType.CLIENT), SERVER(EnvType.SERVER);

    private final EnvType environment;

    CoreSide(EnvType environment){
        this.environment = environment;
    }

    @Deprecated
    public EnvType getUnderlyingSide(){
        return this.environment;
    }
}
