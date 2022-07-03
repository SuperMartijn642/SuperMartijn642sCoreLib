package com.supermartijn642.core.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class ObjectBaseScreen<T> extends BaseScreen {

    protected ObjectBaseScreen(ITextComponent title){
        super(title);
    }

    @Override
    protected float sizeX(){
        T object = this.getObjectOrClose();
        return object == null ? 0 : this.sizeX(object);
    }

    /**
     * @return the width of the screen
     */
    protected abstract float sizeX(@Nonnull T object);

    @Override
    protected float sizeY(){
        T object = this.getObjectOrClose();
        return object == null ? 0 : this.sizeY(object);
    }

    /**
     * @return the height of the screen
     */
    protected abstract float sizeY(@Nonnull T object);

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
    protected void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.renderBackground(matrixStack, mouseX, mouseY, object);
    }

    /**
     * Renders the screen's background. This will be called first in the render chain.
     */
    protected void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, @Nonnull T object){
        super.renderBackground(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void render(MatrixStack matrixStack, int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.render(matrixStack, mouseX, mouseY, object);
    }

    /**
     * Renders the screen's main features.
     * Called after the background and slots are drawn, but before widgets, items, and tooltips are drawn.
     */
    protected void render(MatrixStack matrixStack, int mouseX, int mouseY, @Nonnull T object){
    }

    @Override
    protected void renderForeground(MatrixStack matrixStack, int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.renderForeground(matrixStack, mouseX, mouseY, object);
    }

    /**
     * Renders the screen's foreground.
     * Called after widgets are drawn, but before tooltips are drawn.
     */
    protected void renderForeground(MatrixStack matrixStack, int mouseX, int mouseY, @Nonnull T object){
        super.renderForeground(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderTooltips(MatrixStack matrixStack, int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.renderTooltips(matrixStack, mouseX, mouseY, object);
    }

    /**
     * Renders tooltips for the given {@code mouseX} and {@code mouseY}.
     * This will be called last in the render chain.
     */
    protected void renderTooltips(MatrixStack matrixStack, int mouseX, int mouseY, @Nonnull T object){
    }

    /**
     * Gets the object from {@link #getObject()}, if {@code null} the screen
     * will be closed, the object from {@link #getObject()} will be returned.
     * @return the object from {@link #getObject()} or {@code null}
     */
    protected T getObjectOrClose(){
        T object = this.getObject();
        if(object == null)
            ClientUtils.closeScreen();
        return object;
    }

    /**
     * @return the object required for the container to remain open
     */
    protected abstract T getObject();
}
