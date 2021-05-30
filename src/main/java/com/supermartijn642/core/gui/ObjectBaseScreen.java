package com.supermartijn642.core.gui;

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
    protected void render(int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.render(mouseX, mouseY, object);
    }

    protected abstract void render(int mouseX, int mouseY, T object);

    @Override
    protected void renderTooltips(int mouseX, int mouseY){
        T object = this.getObjectOrClose();
        if(object != null)
            this.renderTooltips(mouseX, mouseY, object);
    }

    protected void renderTooltips(int mouseX, int mouseY, T object){
    }

    protected T getObjectOrClose(){
        T object = this.getObject();
        if(object == null)
            ClientUtils.closeScreen();
        return object;
    }

    protected abstract T getObject();
}
