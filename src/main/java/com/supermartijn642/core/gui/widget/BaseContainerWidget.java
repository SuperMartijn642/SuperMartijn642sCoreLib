package com.supermartijn642.core.gui.widget;

import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public abstract class BaseContainerWidget<T extends AbstractContainerMenu> extends BaseWidget implements ContainerWidget<T> {

    protected final List<ContainerWidget<? super T>> containerWidgets = new ArrayList<>();
    protected T container;

    public BaseContainerWidget(int x, int y, int width, int height){
        super(x, y, width, height);
    }

    @Override
    public void initialize(T container){
        if(container == null)
            throw new IllegalArgumentException("Cannot initialize ContainerWidget with a null container!");
        this.container = container;

        this.initialize();
    }

    @Override
    public void initialize(){
        if(this.container == null)
            throw new IllegalStateException("Container widgets must be initialized with a container!");

        this.addWidgets();
        this.containerWidgets.forEach(w -> w.initialize(this.container));
        this.widgets.forEach(w -> {
            if(!(w instanceof ContainerWidget))
                w.initialize();
        });
    }
}
