package com.supermartijn642.core.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.util.text.ITextComponent;

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

    protected abstract float sizeX(T object);

    @Override
    protected float sizeY(){
        T object = this.getObjectOrClose();
        return object == null ? 0 : this.sizeY(object);
    }

    protected abstract float sizeY(T object);

    @Override
    protected void addWidgets(){
        T object = this.getObjectOrClose();
        if(object != null)
            this.addWidgets(object);
    }

    protected abstract void addWidgets(T object);

    @Override
    protected void render(MatrixStack matrixStack, int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.render(matrixStack, mouseX, mouseY, object);
    }

    protected abstract void render(MatrixStack matrixStack, int mouseX, int mouseY, T object);

    @Override
    protected void renderTooltips(MatrixStack matrixStack, int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.renderTooltips(matrixStack, mouseX, mouseY, object);
    }

    protected abstract void renderTooltips(MatrixStack matrixStack, int mouseX, int mouseY, T object);

    protected T getObjectOrClose(){
        T object = this.getObject();
        if(object == null)
            ClientUtils.closeScreen();
        return object;
    }

    protected abstract T getObject();
}
