package com.supermartijn642.core.gui.widget;

import net.minecraft.inventory.Container;

/**
 * Created 17/07/2022 by SuperMartijn642
 */
public interface ContainerWidget<T extends Container> extends Widget {

    @Override
    default void initialize(){
        throw new IllegalStateException("Container widgets must be initialized with a container!");
    }

    /**
     * Called when the widget is added.
     * @param container the container this widget is in, must not be {@code null}
     */
    void initialize(T container);
}
