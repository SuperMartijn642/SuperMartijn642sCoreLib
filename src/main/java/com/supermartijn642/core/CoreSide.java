package com.supermartijn642.core;

import net.minecraftforge.fml.relauncher.Side;

/**
 * Created 5/30/2021 by SuperMartijn642
 */
public enum CoreSide {
    CLIENT(Side.CLIENT), SERVER(Side.SERVER);

    private final Side side;

    CoreSide(Side side){
        this.side = side;
    }

    public boolean isClient(){
        return this == CLIENT;
    }

    public boolean isServer(){
        return this == SERVER;
    }

    @Deprecated
    public Side getUnderlyingSide(){
        return side;
    }

    @Deprecated
    public static CoreSide fromUnderlying(Side environment){
        return environment == Side.CLIENT ? CLIENT : SERVER;
    }
}
