package com.supermartijn642.core.block;

import net.minecraft.util.ITickable;

/**
 * Created 17/07/2022 by SuperMartijn642
 */
public interface TickableBlockEntity extends ITickable {

    /**
     * Called once per tick.
     */
    void update();
}
