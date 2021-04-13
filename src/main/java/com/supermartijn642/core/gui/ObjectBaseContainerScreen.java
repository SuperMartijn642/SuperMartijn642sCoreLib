package com.supermartijn642.core.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
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
    public void tick(){
        T object = this.getObjectOrClose();
        if(object == null)
            return;

        this.tick(object);
        super.tick();
    }

    protected void tick(T object){
    }

    @Override
    protected void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.renderBackground(matrixStack, mouseX, mouseY, object);
    }

    protected void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, T object){
        super.renderBackground(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderForeground(MatrixStack matrixStack, int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.renderForeground(matrixStack, mouseX, mouseY, object);
    }

    protected void renderForeground(MatrixStack matrixStack, int mouseX, int mouseY, T object){
        super.renderForeground(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderTooltips(MatrixStack matrixStack, int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.renderTooltips(matrixStack, mouseX, mouseY, object);
    }

    protected abstract void renderTooltips(MatrixStack matrixStack, int mouseX, int mouseY, T object);

    protected T getObjectOrClose(){
        return this.container.getObjectOrClose();
    }
}
