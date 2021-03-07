package com.supermartijn642.core.gui;

import net.minecraft.util.text.ITextComponent;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class ObjectBaseContainerScreen<T, X extends ObjectBaseContainer<T>> extends BaseContainerScreen<X> {
    public ObjectBaseContainerScreen(X screenContainer, ITextComponent title){
        super(screenContainer, title);
    }

    @Override
    protected int sizeX(){
        T object = this.getObjectOrClose();
        return object == null ? 0 : this.sizeX(object);
    }

    protected abstract int sizeX(T object);

    @Override
    protected int sizeY(){
        T object = this.getObjectOrClose();
        return object == null ? 0 : this.sizeY(object);
    }

    protected abstract int sizeY(T object);

    @Override
    protected void addWidgets(){
        T object = this.getObjectOrClose();
        if(object != null)
            this.addWidgets(object);
    }

    protected abstract void addWidgets(T object);

    @Override
    protected void renderBackground(int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.renderBackground(mouseX, mouseY, object);
    }

    protected void renderBackground(int mouseX, int mouseY, T object){
        super.renderBackground(mouseX, mouseY);
    }

    @Override
    protected void renderForeground(int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.renderForeground(mouseX, mouseY, object);
    }

    protected void renderForeground(int mouseX, int mouseY, T object){
        super.renderForeground(mouseX, mouseY);
    }

    @Override
    protected void renderTooltips(int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.renderTooltips(mouseX, mouseY, object);
    }

    protected abstract void renderTooltips(int mouseX, int mouseY, T object);

    protected T getObjectOrClose(){
        return this.container.getObjectOrClose();
    }
}
