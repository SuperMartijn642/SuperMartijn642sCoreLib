package com.supermartijn642.core;

import net.minecraftforge.fml.LogicalSide;

/**
 * Created 5/30/2021 by SuperMartijn642
 */
public enum CoreSide {
    CLIENT(LogicalSide.CLIENT), SERVER(LogicalSide.SERVER);

    private final LogicalSide side;

    CoreSide(LogicalSide side){
        this.side = side;
    }

    @Deprecated
    public LogicalSide getUnderlyingSide(){
        return side;
    }
}
