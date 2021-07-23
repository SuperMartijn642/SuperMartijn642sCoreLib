package com.supermartijn642.core.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class ObjectBaseContainerScreen<T, X extends ObjectBaseContainer<T>> extends BaseContainerScreen<X> {

    public ObjectBaseContainerScreen(X screenContainer, Component title){
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
    public void containerTick(){
        T object = this.getObjectOrClose();
        if(object == null)
            return;

        this.containerTick(object);
        super.tick();
    }

    protected void containerTick(@Nonnull T object){
    }

    @Override
    protected void renderBackground(PoseStack poseStack, int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.renderBackground(poseStack, mouseX, mouseY, object);
    }

    /**
     * Renders the screen's background. This will be called first in the render chain.
     */
    protected void renderBackground(PoseStack poseStack, int mouseX, int mouseY, @Nonnull T object){
        super.renderBackground(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderForeground(PoseStack poseStack, int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.renderForeground(poseStack, mouseX, mouseY, object);
    }

    /**
     * Renders the screen's foreground.
     * Widgets are drawn after this.
     */
    protected void renderForeground(PoseStack poseStack, int mouseX, int mouseY, @Nonnull T object){
        super.renderForeground(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderTooltips(PoseStack poseStack, int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.renderTooltips(poseStack, mouseX, mouseY, object);
    }

    /**
     * Renders tooltips for the given {@code mouseX} and {@code mouseY}.
     * This will be called last in the render chain.
     */
    protected void renderTooltips(PoseStack poseStack, int mouseX, int mouseY, @Nonnull T object){
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
