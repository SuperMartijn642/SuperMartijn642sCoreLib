package com.supermartijn642.core;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.LogicalSide;

/**
 * Created 5/30/2021 by SuperMartijn642
 */
public enum CoreSide {
    CLIENT(Dist.CLIENT), SERVER(Dist.DEDICATED_SERVER);

    private final Dist environment;

    CoreSide(Dist environment){
        this.environment = environment;
    }

    public boolean isClient(){
        return this == CLIENT;
    }

    public boolean isServer(){
        return this == SERVER;
    }

    @Deprecated
    public Dist getUnderlyingSide(){
        return this.environment;
    }

    @Deprecated
    public static CoreSide fromUnderlying(Dist environment){
        return environment == Dist.CLIENT ? CLIENT : SERVER;
    }

    @Deprecated
    public static CoreSide fromUnderlying(LogicalSide environment){
        return environment == LogicalSide.CLIENT ? CLIENT : SERVER;
    }
}
