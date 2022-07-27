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

    public boolean isClient(){
        return this == CLIENT;
    }

    public boolean isServer(){
        return this == SERVER;
    }

    @Deprecated
    public EnvType getUnderlyingSide(){
        return this.environment;
    }

    @Deprecated
    public static CoreSide fromUnderlying(EnvType environment){
        return environment == EnvType.CLIENT ? CLIENT : SERVER;
    }
}
