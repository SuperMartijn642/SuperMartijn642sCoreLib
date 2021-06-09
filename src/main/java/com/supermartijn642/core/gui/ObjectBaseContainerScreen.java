package com.supermartijn642.core.gui;

import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    /**
     * @return the width of the screen
     */
    protected abstract int sizeX(@Nonnull T object);

    @Override
    protected int sizeY(){
        T object = this.getObjectOrClose();
        return object == null ? 0 : this.sizeY(object);
    }

    /**
     * @return the height of the screen
     */
    protected abstract int sizeY(@Nonnull T object);

    @Override
    protected void addWidgets(){
        T object = this.getObjectOrClose();
        if(object != null)
            this.addWidgets(object);
    }

    /**
     * Adds widgets to the screen via {@link #addWidget(Widget)}.
     */
    protected abstract void addWidgets(@Nonnull T object);

    @Override
    public void tick(){
        T object = this.getObjectOrClose();
        if(object == null)
            return;

        this.tick(object);
        super.tick();
    }

    protected void tick(@Nonnull T object){
    }

    @Override
    protected void renderBackground(int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.renderBackground(mouseX, mouseY, object);
    }

    /**
     * Renders the screen's background. This will be called first in the render chain.
     */
    protected void renderBackground(int mouseX, int mouseY, @Nonnull T object){
        super.renderBackground(mouseX, mouseY);
    }

    @Override
    protected void renderForeground(int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.renderForeground(mouseX, mouseY, object);
    }

    /**
     * Renders the screen's foreground.
     * Widgets are drawn after this.
     */
    protected void renderForeground(int mouseX, int mouseY, @Nonnull T object){
        super.renderForeground(mouseX, mouseY);
    }

    @Override
    protected void renderTooltips(int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.renderTooltips(mouseX, mouseY, object);
    }

    /**
     * Renders tooltips for the given {@code mouseX} and {@code mouseY}.
     * This will be called last in the render chain.
     */
    protected void renderTooltips(int mouseX, int mouseY, @Nonnull T object){
    }

    /**
     * Gets the object from {@link ObjectBaseContainer#getObject()}, if {@code null} the screen
     * will be closed, the object from {@link ObjectBaseContainer#getObject()} will be returned.
     * @return the object from {@link ObjectBaseContainer#getObject()} or {@code null}
     */
    @Nullable
    protected T getObjectOrClose(){
        return this.container.getObjectOrClose();
    }
}
